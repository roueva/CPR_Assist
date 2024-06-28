# Arduino Code for CPR Training Glove

## Overview
This folder contains the Arduino code for the CPR Training Glove. The code is responsible for reading sensor data, providing feedback through LEDs and a vibration motor, and communicating with the mobile application via Bluetooth.

## Components
- Arduino Nano 33 BLE
- LSM9DS1 Accelerometer
- Velostat Pressure Sensor
- LEDs (Green, White, Red)
- Vibrator Motor
- Momentary Push Button
- Battery

## Hardware Connections
- **Battery**: Connected to the breadboard; power distributed to the Arduino VIN and the vibrator motor.
- **LEDs**:
  - Green LED: Digital pin 5
  - White LED: Digital pin 10
  - Red LED: Digital pin 6
- **Vibrator Motor**: Digital pin 3
- **Velostat Pressure Sensor**: Analog pin A0
- **Momentary Push Button**: Digital pin 9 with internal pull-up resistor

## Setup
1. Install the necessary libraries:
   - `Arduino_LSM9DS1`
   - `ArduinoBLE`
   - `Crypto`
2. Open `CPR_Glove.ino` in the Arduino IDE.
3. Connect the Arduino Nano 33 BLE to your computer.
4. Upload the code to the Arduino.

## Code Description
- **Initialization**: Sets up the IMU, BLE, and pin modes.
- **Button Handling**: Detects long press to start/stop the session.
- **Main Loop**: Reads sensor data, provides feedback, and sends performance data to the mobile app.

## Usage
1. Connect the components as described in the hardware connections section.
2. Power the Arduino.
3. The glove is ready for use in either Real CPR Mode or Training Mode.


Link in Tinkercad: https://www.tinkercad.com/things/a57BqYuMAhY-copy-of-arduino-nano/editel?tenant=circuits
