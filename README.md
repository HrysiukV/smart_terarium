# 🌿 Terrarium Control System – ESP8266 Firmware

Українська | [English](#english)

---

# Українська

## Про проєкт

Цей репозиторій містить прошивку для **ESP8266 (NodeMCU)**, яка є центральним контролером програмно-апаратного комплексу моніторингу та автоматизації мікроклімату тераріуму.

Контролер отримує дані з датчиків, передає їх до **Firebase Realtime Database**, керує виконавчими пристроями та забезпечує роботу системи як у ручному, так і в автоматичному режимах.

---

## Основні можливості

* Підключення до Wi-Fi з автоматичним переходом у режим налаштування через **WiFiManager** у разі втрати мережі.
* Передача показників датчиків до Firebase у режимі реального часу.
* Збереження історії вимірювань із часовими мітками.
* Ведення журналу подій (увімкнення/вимкнення обладнання, запуск системи, аварійні ситуації).
* Ручне керування обладнанням через Firebase.
* Автоматичне керування температурою та освітленням.
* Автоматичний полив залежно від вологості ґрунту.
* Захист від перегріву нагрівального елемента.
* Синхронізація часу через NTP-сервери.
* Асинхронне зчитування температурного датчика для підвищення швидкодії.

---

## Підключене обладнання

| Пристрій                           | Призначення                             |
| ---------------------------------- | --------------------------------------- |
| ESP8266 NodeMCU                    | Центральний контролер                   |
| BMP280                             | Температура повітря та атмосферний тиск |
| DS18B20                            | Температура ґрунту                      |
| Аналоговий датчик вологості ґрунту | Контроль вологості субстрату            |
| Релейний модуль                    | Керування обладнанням                   |

---

## Керовані пристрої

💡 Освітлення
🔥 Нагрівальний килимок
🚿 Насос поливу

---

## Режими роботи

### Manual

Користувач безпосередньо керує всіма реле через мобільний застосунок. Стан реле синхронізується з Firebase.

### Day

Автоматичний денний режим.

* освітлення увімкнене;
* підтримується денна температура;
* автоматично працює система поливу.

### Night

Автоматичний нічний режим.

* освітлення вимкнене;
* підтримується нічна температура;
* автоматично працює система поливу.

---

## Автоматичний полив

Полив виконується лише якщо:

* вологість ґрунту нижче встановленого порога;
* нагрівальний килимок вимкнений.

Після поливу система очікує кілька хвилин, щоб вода встигла рівномірно розподілитися в субстраті, після чого повторно оцінює рівень вологості.

---

## Захист системи

Для запобігання перегріву реалізовано аварійне вимкнення нагрівального килимка.

Якщо температура ґрунту перевищує безпечне значення, нагрівання автоматично вимикається та записується відповідний запис у журнал подій.

---

## Firebase

У Firebase зберігаються:

* поточні показники датчиків;
* стан реле;
* активний режим роботи;
* історія вимірювань;
* журнал роботи системи.

---

## Використані бібліотеки

* ESP8266WiFi
* ESP8266WiFiMulti
* WiFiManager
* Firebase ESP Client
* Adafruit BMP280
* DallasTemperature
* OneWire

---

# English

## About

This repository contains the firmware for an **ESP8266 (NodeMCU)** used as the main controller of a smart terrarium monitoring and automation system.

The controller collects sensor data, synchronizes it with **Firebase Realtime Database**, controls connected devices, and supports both manual and automatic operating modes.

---

## Features

* Automatic Wi-Fi connection with **WiFiManager** fallback.
* Real-time synchronization with Firebase.
* Sensor history logging with timestamps.
* Event logging for all device actions.
* Manual device control.
* Automatic temperature regulation.
* Automatic irrigation based on soil moisture.
* Emergency overheating protection.
* NTP time synchronization.
* Non-blocking DS18B20 temperature reading.

---

## Hardware

* ESP8266 NodeMCU
* BMP280
* DS18B20
* Soil Moisture Sensor
* Relay Module

---

## Controlled Devices

* Lighting
* Heating Mat
* Water Pump

---

## Operating Modes

### Manual

The user controls all relays remotely through the mobile application.

### Day

Automatic daytime operation with lighting enabled and temperature regulation.

### Night

Automatic nighttime operation with lighting disabled and lower temperature thresholds.

---

## Automatic Irrigation

The irrigation system starts only when soil moisture drops below the configured threshold and the heating system is inactive.

After watering, the controller waits for the substrate to absorb water before checking the moisture level again.

---

## Safety

An emergency overheating protection mechanism disables the heating mat whenever the soil temperature exceeds the safe limit.

All emergency events are stored in the system log.

---

## Firebase Structure

The firmware synchronizes:

* sensor values;
* relay states;
* current operating mode;
* measurement history;
* event logs.

---

## Libraries

* ESP8266WiFi
* ESP8266WiFiMulti
* WiFiManager
* Firebase ESP Client
* Adafruit BMP280
* DallasTemperature
* OneWire

---

## Author

**Victoria Hrysiuk**

Bachelor's Qualification Project

**Software and Hardware Complex for Monitoring and Automation of Terrarium Ecosystems**
