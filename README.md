# CPR Training Glove

## Overview - Project Description

The CPR Training Glove is a wearable device designed to assist in performing and training cardiopulmonary resuscitation (CPR) with accurate real-time feedback. This project integrates hardware components with custom software to provide guidance and performance evaluation during CPR.

## Table of Contents

- Overview - Project Description
- Features
- Components
- Hardware Connections
- Software Setup
- Usage Instructions
- Code Repositories

## Features

- **Real-time Feedback on CPR compressions:** Provides instant visual and haptic feedback on compression depth, frequency, and angle.
- **Training and Evaluation:** Grades the user's performance to help improve CPR technique.
- **Wireless Communication:** Connects to a mobile application via Bluetooth for additional feedback and data visualization.
- **User-Friendly Operation:** Simple start and stop functionality using a momentary push button.
- **Mobile application for additional feedback.**

## Components

### Microcontroller
- **Arduino Nano 33 BLE:** Central microcontroller for processing sensor data and managing communication.

### Sensors
- **Accelerometer (LSM9DS1):** Measures pitch angle for hand positioning during compressions.
- **Velostat Material:** DIY pressure sensor for measuring compression depth.

### Actuators
- **LEDs:**
  - Green LED: Provides feedback on correct compression frequency (connected to digital pin 5).
  - White LED: Provides feedback on correct compression depth (connected to digital pin 10).
  - Red LED: Indicates incorrect compression angle (connected to digital pin 6).
- **Vibrator Motor:** Offers haptic feedback, pulsing at 110 BPM to guide compression rhythm (connected to digital pin 3).
- **Momentary Push Button:** Controls start and stop of CPR session (connected to digital pin 9 with internal pull-up resistor).

### Power Supply
- **Battery:** Powers the glove, connected via a breadboard for distribution to components.

## Hardware Connections

- **Battery:** Connected to the breadboard; power distributed to Arduino VIN and vibrator motor.
- **LEDs:**
  - Green LED: Digital pin 5
  - White LED: Digital pin 10
  - Red LED: Digital pin 6
- **Vibrator Motor:** Digital pin 3
- **Velostat Pressure Sensor:** Analog pin A0
- **Momentary Push Button:** Digital pin 9 with internal pull-up resistor

## Software Setup

### Arduino IDE

1. Install the necessary libraries:
   - Arduino_LSM9DS1
   - ArduinoBLE
   - Crypto
2. Upload the `CPR_Glove.ino` code to the Arduino Nano 33 BLE.

### Android Studio

1. Clone the Android application code from the repository.
2. Open the project in Android Studio.
3. Build and run the application on an Android device.

### MATLAB

1. Ensure MATLAB is installed on your computer.
2. Clone the MATLAB scripts from the repository.
3. Use the provided scripts to analyze the relationship between weight and the analog signal from the Velostat sensor.

## Usage Instructions

### Real CPR Mode

1. **Start:** Press and hold the button on the glove for 1.5 seconds. The LEDs will blink, and the vibrator motor will activate.
2. **Perform CPR:** Follow the guidance from the LEDs and vibration motor.
   - Green LED: Correct compression frequency
   - White LED: Correct compression depth
   - Red LED: Incorrect compression angle
3. **Stop:** Press and hold the button again for 1.5 seconds to end the session.

### Training Mode

1. **Connect App:** Open the mobile app and press "Connect."
2. **Start Session:** Press and hold the button on the glove for 1.5 seconds.
3. **Perform CPR:** Follow the guidance as described above.
4. **Stop Session:** Press and hold the button for 1.5 seconds. The app will display your performance grades.
5. **Disconnect App:** Press "Disconnect" in the app to end the session and clear the data.

## Code Repositories

- Arduino Code: `CPR_Glove.ino`
- Android App: Android Studio Project
- MATLAB Scripts: MATLAB Scripts
