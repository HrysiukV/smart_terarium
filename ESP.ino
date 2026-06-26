#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WiFiManager.h>
#include <Firebase_ESP_Client.h>
#include <Adafruit_BMP280.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <time.h>

// ================= FIREBASE =================
#define FIREBASE_HOST "terrariumcontrol-3318a-default-rtdb.europe-west1.firebasedatabase.app"

FirebaseData fbdo;
FirebaseData fbdo_relay;
FirebaseData fbdo_mode;

FirebaseAuth auth;
FirebaseConfig config;

// ================= WIFI =================
ESP8266WiFiMulti wifiMulti;

// ================= BMP280 =================
Adafruit_BMP280 bmp;

// ================= DS18B20 =================
#define ONE_WIRE_BUS D4

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

// ================= RELAYS =================
#define RELAY1 D6 // LIGHT
#define RELAY2 D5 // HEAT MAT
#define RELAY3 D7 // PUMP

bool r1 = false;
bool r2 = false;
bool r3 = false;

// ================= SOIL =================
#define SOIL_PIN A0

// ================= MODE =================
String currentMode = "manual";

// ================= TIMERS =================
unsigned long lastSend  = 0;
unsigned long lastModeRead = 0;
unsigned long lastAuto  = 0;

String lastMode = "manual";

// DS18B20 асинхронне читання
unsigned long lastTempRequest = 0;
bool tempRequested = false;
const unsigned long TEMP_REQUEST_INTERVAL = 2000;

// ================= SAFETY =================
bool emergencyStop = false;
bool timeSynced    = false;

// ================= AUTO PUMP =================
bool pumpRunning    = false;
bool waitingForSoak = false;

unsigned long pumpStartTime = 0;
unsigned long soakStartTime = 0;

const unsigned long PUMP_DURATION = 1200;
const unsigned long SOAK_TIME     = 180000;

// ================= КЕШ СЕНСОРІВ =================
float cachedTempAir  = 25.0;
float cachedTempSoil = 25.0;
int   cachedSoil     = 50;

// ================= FORWARD DECLARATIONS =================
void addLog(String device, String action, String reason);
void applyRelay(int pin, bool state);

// ================= HELPER: ВСТАНОВИТИ РЕЛЕ =================
void setRelay(int pin, bool &stateVar, bool newState,
              const char* fbPath, const char* device, const char* reason) {

  if (stateVar == newState) return;  // 🚀 важливо

  stateVar = newState;
  applyRelay(pin, stateVar);

  Firebase.RTDB.setBool(&fbdo_relay, fbPath, stateVar);

  addLog(device, stateVar ? "ON" : "OFF", reason);
}

// ================= EVENT LOG =================
void addLog(String device, String action, String reason) {

  FirebaseJson json;
  json.set("device", device);
  json.set("action", action);
  json.set("reason", reason);

  if (timeSynced) {
    unsigned long now = (unsigned long)time(nullptr);
    json.set("timestamp", String(now));
  } else {
    json.set("timestamp", 0);
  }

  Firebase.RTDB.pushJSON(&fbdo, "/terrarium/logs", &json);
  Serial.println("LOG -> " + device + " " + action);
}

// ================= APPLY RELAY =================
void applyRelay(int pin, bool state) {
  digitalWrite(pin, state ? LOW : HIGH);
}

// ================= WIFI =================
void connectWiFi() {

  wifiMulti.addAP("TP-LINK_65F6", "06725228");
  wifiMulti.addAP("iPhone", "06725228");

  Serial.print("Connecting WiFi");

  int tries = 0;

  while (wifiMulti.run() != WL_CONNECTED && tries < 20) {
    delay(500);
    Serial.print(".");
    tries++;
  }

  if (WiFi.status() != WL_CONNECTED) {
    WiFiManager wm;
    wm.autoConnect("Terrarium-Setup");
  }

  Serial.println("");
  Serial.println("WiFi connected");
}

// ================= RELAY SYNC =================
void syncRelays() {

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay1")) {
    r1 = fbdo_relay.boolData();
    applyRelay(RELAY1, r1);
  }

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay2")) {
    r2 = fbdo_relay.boolData();
    applyRelay(RELAY2, r2);
  }

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay3")) {
    r3 = fbdo_relay.boolData();
    applyRelay(RELAY3, r3);
  }
}

// ================= MANUAL MODE =================
void handleManualMode() {

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay1")) {
    bool state = fbdo_relay.boolData();
    setRelay(RELAY1, r1, state, "/terrarium/relay1", "light", "Manual control");
  }

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay2")) {
    bool state = fbdo_relay.boolData();
    setRelay(RELAY2, r2, state, "/terrarium/relay2", "heat", "Manual control");
  }

  if (Firebase.RTDB.getBool(&fbdo_relay, "/terrarium/relay3")) {
    bool state = fbdo_relay.boolData();
    setRelay(RELAY3, r3, state, "/terrarium/relay3", "pump", "Manual control");
  }
}

// ================= AUTO PUMP =================
void handleAutoPump(int soil) {

  if (r2) {
    if (r3) {
      setRelay(RELAY3, r3, false, "/terrarium/relay3", "pump", "Heating active");
    }
    return;
  }

  if (pumpRunning) {
    if (millis() - pumpStartTime >= PUMP_DURATION) {
      pumpRunning    = false;
      waitingForSoak = true;
      soakStartTime  = millis();
      setRelay(RELAY3, r3, false, "/terrarium/relay3", "pump", "Soaking");
      Serial.println("Pump OFF -> soak");
    }
    return;
  }

  if (waitingForSoak) {
    if (millis() - soakStartTime >= SOAK_TIME) {
      waitingForSoak = false;
      Serial.println("Soak finished");
    }
    return;
  }

  if (soil < 30) {
    pumpRunning   = true;
    pumpStartTime = millis();
    setRelay(RELAY3, r3, true, "/terrarium/relay3", "pump", "Low moisture");
    Serial.println("Pump ON");
  }
}

// ================= AUTO MODE =================
void runAutoLogic(float tempAir, float tempSoil) {

  bool dayMode = (currentMode == "day");

  float airMin   = dayMode ? 24.0 : 18.0;
  float airMax   = dayMode ? 27.0 : 22.0;
  float soilMax  = dayMode ? 28.0 : 24.0;
  float soilSafe = soilMax - 2.0;

  setRelay(RELAY1, r1, dayMode,
           "/terrarium/relay1", "light",
           dayMode ? "Day mode" : "Night mode");

  if (tempSoil > 30.0) {
    emergencyStop = true;
    if (r2) {
      setRelay(RELAY2, r2, false, "/terrarium/relay2", "heat", "Emergency stop");
      Serial.println("EMERGENCY STOP");
    }
    return;
  }

  if (tempSoil < 28.0) {
    emergencyStop = false;
  }

  bool heatOn  = (tempAir < airMin) && (tempSoil < soilSafe);
  bool heatOff = (tempAir > airMax) || (tempSoil > soilMax);

  if (heatOff && r2) {
    setRelay(RELAY2, r2, false, "/terrarium/relay2", "heat", "Target reached");
  }
  else if (heatOn && !r2) {
    setRelay(RELAY2, r2, true, "/terrarium/relay2", "heat", "Temperature low");
  }
}

// ================= SETUP =================
void setup() {

  Serial.begin(115200);

  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);
  pinMode(RELAY3, OUTPUT);

  digitalWrite(RELAY1, HIGH);
  digitalWrite(RELAY2, HIGH);
  digitalWrite(RELAY3, HIGH);

  connectWiFi();

  configTime(2 * 3600, 0, "pool.ntp.org", "time.nist.gov");

  Serial.print("Syncing time");

  int retry = 0;

  while (!timeSynced && retry < 30) {
    time_t now = time(nullptr);
    struct tm *tm_info = localtime(&now);

    if (tm_info->tm_year > 120) {
      timeSynced = true;
      break;
    }

    delay(1000);
    Serial.print(".");
    retry++;
  }

  Serial.println("");
  Serial.println(timeSynced ? "TIME OK" : "TIME FAILED");

  if (!bmp.begin(0x76)) {
    Serial.println("BMP280 ERROR");
  }

  sensors.begin();
  sensors.setWaitForConversion(false);

  config.database_url = FIREBASE_HOST;
  config.signer.test_mode = true;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  delay(500);

  if (Firebase.RTDB.getString(&fbdo_mode, "/terrarium/currentMode")) {
    currentMode = fbdo_mode.stringData();
    currentMode.toLowerCase();
  } else {
    currentMode = "manual";
  }

  Serial.println("MODE = " + currentMode);

  syncRelays();

  addLog("system", "START", "ESP8266 boot");
}

// ================= LOOP =================
void loop() {

  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
    return;
  }

  unsigned long now = millis();

  // MODE
  if (now - lastModeRead > 5000) {
  lastModeRead = now;

  if (Firebase.RTDB.getString(&fbdo_mode, "/terrarium/currentMode")) {

    String newMode = fbdo_mode.stringData();
    newMode.toLowerCase();

    if (newMode == "manual" || newMode == "day" || newMode == "night") {

      if (newMode != lastMode) {
        lastMode = newMode;
        currentMode = newMode;

        Serial.println("MODE CHANGED -> " + currentMode);
      }
    }
  }
}

  // DS18B20 АСИНХРОННЕ ЧИТАННЯ
  if (!tempRequested) {
    sensors.requestTemperatures();
    lastTempRequest = now;
    tempRequested = true;
  }

  if (tempRequested && (now - lastTempRequest >= 800)) {
    tempRequested = false;

    float newTempSoil = sensors.getTempCByIndex(0);
    if (newTempSoil > -100.0) {
      cachedTempSoil = newTempSoil;
    }

    float newTempAir = bmp.readTemperature();
    if (!isnan(newTempAir)) {
      cachedTempAir = newTempAir;
    }

    int rawSoil = constrain(analogRead(SOIL_PIN), 300, 1023);
    cachedSoil  = map(rawSoil, 1023, 300, 0, 100);
  }

  float tempAir  = cachedTempAir;
  float tempSoil = cachedTempSoil;
  int   soil     = cachedSoil;

  // ================= SEND DATA =================
  if (now - lastSend > 20000) {   // було 15000 → менше spam
  lastSend = now;

  Firebase.RTDB.setFloat(&fbdo, "/terrarium/tempAir", tempAir);
  Firebase.RTDB.setFloat(&fbdo, "/terrarium/tempSoil", tempSoil);
  Firebase.RTDB.setInt(&fbdo, "/terrarium/soilMoisture", soil);

  if (timeSynced) {

    FirebaseJson historyEntry;
    historyEntry.set("tempAir", tempAir);
    historyEntry.set("tempSoil", tempSoil);
    historyEntry.set("soilMoisture", soil);
    historyEntry.set("timestamp", (unsigned long)time(nullptr));

    Firebase.RTDB.pushJSON(&fbdo, "/terrarium/history", &historyEntry);
  }
}

  // MANUAL
  if (currentMode == "manual") {
    handleManualMode();
  }

  // AUTO
  if (currentMode == "day" || currentMode == "night") {

  if (now - lastAuto > 12000) { // трохи рідше → стабільніше
    lastAuto = now;
    runAutoLogic(tempAir, tempSoil);
  }

  handleAutoPump(soil);
}
}