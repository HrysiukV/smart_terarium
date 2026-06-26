# 🌿 Smart Terrarium Control System

**Software and Hardware Complex for Monitoring and Automation of Terrarium Ecosystems**

An IoT-based system for monitoring and automatic control of a terrarium environment using **ESP8266**, **Firebase Realtime Database**, and an **Android application**.

---

# 🇺🇦 Українська

## 📖 Про проєкт

**Smart Terrarium Control System** — це програмно-апаратний комплекс, призначений для автоматичного моніторингу та керування мікрокліматом тераріуму.

Система поєднує мікроконтролер ESP8266, датчики навколишнього середовища, Firebase Realtime Database та Android-застосунок для забезпечення віддаленого контролю, автоматизації процесів і збереження історії вимірювань.

---

## 🚀 Основні можливості

### ESP8266 Firmware

- автоматичне підключення до Wi-Fi;
- резервне налаштування Wi-Fi через WiFiManager;
- передача даних до Firebase Realtime Database;
- синхронізація часу через NTP;
- асинхронне зчитування температури з DS18B20;
- керування освітленням;
- керування нагрівальним килимком;
- автоматичне керування насосом поливу;
- ручний режим роботи;
- автоматичний денний режим;
- автоматичний нічний режим;
- журналювання всіх подій;
- збереження історії показників;
- захист від перегріву нагрівального елемента.

### Android Application

- перегляд показників датчиків у режимі реального часу;
- перегляд історії вимірювань;
- графіки температури та вологості;
- журнал роботи системи;
- дистанційне керування обладнанням;
- перемикання режимів роботи;
- автоматична синхронізація з Firebase.
### 📱 Android Application

## 🏠 Головне меню
![Main Menu](screenshots/main_menu.jpg)

## 📊 Аналітика
![Analytics](screenshots/analytics.jpg)

## 📋 Список подій
![Event List](screenshots/list_event.jpg)

## ⚙️ Дії керування системою
![Actions](screenshots/2action.jpg)

---

## 🌿 Візуалізація тераріуму

## 📦 Зовнішній вигляд тераріуму
![Terrarium View](screenshots/terrarium_view.jpg)

## 🪴 Внутрішній вигляд тераріуму
![Terrarium Inside](screenshots/terrarium_inside.jpg)

---

## 🏗 Архітектура системи

```text
                 Android Application
                         │
                         │
          Firebase Realtime Database
                         │
                   ESP8266 NodeMCU
                         │
      ┌───────────┬───────────┬───────────┐
      │           │           │           │
   BMP280      DS18B20   Soil Sensor   Relay Module
                                         │
                      ┌──────────────────┼──────────────────┐
                      │                  │                  │
                  Lighting          Heating Mat        Water Pump
```

---

## 📂 Структура репозиторію

```text
smart_terrarium/

├── ESP8266/
│   └── ESP.ino
│
├── Android/
│   ├── app/
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── ...
│
└── README.md
```

---

## 🔧 Використане обладнання

- ESP8266 NodeMCU
- BMP280
- DS18B20
- Аналоговий датчик вологості ґрунту
- Релейний модуль
- Нагрівальний килимок
- Світлодіодне освітлення
- Водяний насос

---

## 💻 Використані технології

### Embedded

- Arduino IDE
- ESP8266
- Firebase ESP Client
- WiFiManager
- DallasTemperature
- Adafruit BMP280

### Mobile

- Kotlin
- Android Studio
- Firebase Realtime Database
- MPAndroidChart
- Material Design

---

## 📋 Режими роботи

### Manual Mode

Користувач самостійно керує:

- освітленням;
- нагрівальним килимком;
- насосом.

### Day Mode

Автоматично підтримуються денні параметри мікроклімату.

### Night Mode

Автоматично підтримуються нічні параметри мікроклімату зі зниженими температурними порогами.

---

# 🇬🇧 English

## 📖 About

**Smart Terrarium Control System** is a software and hardware platform designed for automatic monitoring and environmental control inside a terrarium.

The project combines an ESP8266 microcontroller, environmental sensors, Firebase Realtime Database, and an Android application to provide real-time monitoring, remote control, and automation.

---

## 🚀 Features

### ESP8266 Firmware

- Automatic Wi-Fi connection
- WiFiManager configuration portal
- Firebase Realtime Database integration
- NTP time synchronization
- Asynchronous DS18B20 temperature reading
- Lighting control
- Heating mat control
- Automatic irrigation
- Manual control mode
- Automatic Day Mode
- Automatic Night Mode
- Event logging
- Historical sensor data storage
- Emergency overheating protection

### Android Application

- Real-time sensor monitoring
- Historical data visualization
- Temperature and moisture charts
- Event log viewer
- Remote device control
- Operating mode selection
- Firebase synchronization

---

## 📂 Repository Structure

```text
ESP8266/
    ESP8266 firmware

Android/
    Android application

README.md
```

---

## 🔧 Hardware

- ESP8266 NodeMCU
- BMP280
- DS18B20
- Soil moisture sensor
- Relay module
- Heating mat
- LED lighting
- Water pump

---

## 💻 Technologies

### Embedded

- Arduino IDE
- ESP8266
- Firebase ESP Client
- WiFiManager

### Mobile

- Kotlin
- Android Studio
- Firebase Realtime Database
- MPAndroidChart
- Material Design

---

## 📄 License

This project was developed as a Bachelor's Qualification Project.

---

## 👩‍💻 Author

**Victoria Hrysiuk**

Bachelor's Qualification Project

National University of Water and Environmental Engineering
