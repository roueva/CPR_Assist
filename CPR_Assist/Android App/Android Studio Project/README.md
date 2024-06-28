# Android Application for CPR Training Glove

## Overview
This folder contains the Android Studio project for the CPR Training Glove mobile application. The app provides real-time feedback and performance evaluation during CPR training sessions.

## Features
- Connect to the CPR Training Glove via Bluetooth
- Display real-time data on compression depth, frequency, and angle
- Provide performance grades and feedback

## Setup
1. Clone the repository
2. Open the project in Android Studio.
3. Build and run the application on an Android device.

## Usage
1. Open the app on your Android device.
2. Press "Connect" to connect to the CPR Training Glove.
3. Follow the instructions to start a training session.
4. View real-time feedback and performance grades on the app.
5. Press "Disconnect" to end the session and clear the data.

## Code Description
- **MainActivity**: Handles Bluetooth connection and user interface.
- **BLEService**: Manages Bluetooth communication with the glove.
```
