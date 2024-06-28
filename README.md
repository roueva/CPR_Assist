_**CPR Training Glove**_

_Overview - Project Description_

The CPR Training Glove is a wearable device designed to assist in performing and training cardiopulmonary resuscitation (CPR) with accurate feedback.
This project integrates hardware components with custom software to provide real-time guidance and performance evaluation during CPR.


**Table of Contents**
Overview/
Features
Components/
Hardware Connections
Software Setup
Usage Instructions
Code Repositories

_Features:_

  Real-time Feedback on CPR compressions: The glove provides instant visual and haptic feedback on compression depth, frequency, and angle.
  Training and Evaluation: The system grades the user's performance, helping them improve their CPR technique.
  Wireless Communication: The glove connects to a mobile application via Bluetooth for additional feedback and data visualization.
  User-Friendly Operation: Simple start and stop functionality using a momentary push button.
  Mobile application for additional feedback.
  
_Components_

  Microcontroller:
    Arduino Nano 33 BLE: The central microcontroller for processing sensor data and managing communication.
  Sensors:
    Accelerometer (LSM9DS1): Measures the pitch angle to ensure correct hand positioning during compressions.
    Velostat Material: Used to create a DIY pressure sensor to measure compression depth.
  Actuators:
    LEDs: Provide visual feedback (green for correct frequency, white for correct depth, red for incorrect angle).
    Vibrator Motor: Offers haptic feedback, pulsing at a rate of 110 BPM to guide compression rhythm.
  Momentary Push Button: Starts and stops the CPR session.
  Battery: Powers the glove, connected via a breadboard for distribution to components.



_Hardware Connections_

Battery: Connected to the breadboard; power distributed to the Arduino VIN and the vibrator motor.
LEDs:
Green LED: Digital pin 5
White LED: Digital pin 10
Red LED: Digital pin 6
Vibrator Motor: Digital pin 3
Velostat Pressure Sensor: Analog pin A0
Momentary Push Button: Digital pin 9 with internal pull-up resistor


_Hardware Connections:_
 
 Battery: Connected to the breadboard; power distributed to the Arduino VIN and the vibrator motor.
 LEDs:
    Green LED: Digital pin 5
    White LED: Digital pin 10
    Red LED: Digital pin 6.
Vibrator Motor: Digital pin 3.
Velostat Pressure Sensor: Analog signal connected to pin A0.
Momentary Push Button: Connected to digital pin 9 with an internal pull-up resistor.

Software Overview:
Arduino IDE: Used to develop and upload the code to the Arduino Nano 33 BLE.
Android Studio: Used to develop the mobile application for displaying feedback and grades.


_Software Setup_

Arduino IDE
1.Install the necessary libraries:
    Arduino_LSM9DS1
    ArduinoBLE
    Crypto
2.Upload the CPR_Glove.ino code to the Arduino Nano 33 BLE.

Android Studio
1. Clone the Android application code from the repository.
2. Open the project in Android Studio.
3. Build and run the application on an Android device.

MATLAB
1. Ensure MATLAB is installed on your computer.
2. Clone the MATLAB script from the repository.
3. Use the provided scripts to analyze the relationship between weight and the analog signal from the Velostat sensor.


_Usage Instructions_

**Real CPR Mode**

- Start: Press and hold the button on the glove for 1.5 seconds. The LEDs will blink, and the vibrator motor will activate, indicating the glove is ready.
- Perform CPR: Follow the guidance from the LEDs and vibration motor.
    Green LED: Correct frequency
    White LED: Correct depth
    Red LED: Incorrect angle
- Stop: Press and hold the button again for 1.5 seconds to end the session.

**Training Mode**

- Connect App: Open the mobile app and press "Connect".
- Start Session: Press and hold the button on the glove for 1.5 seconds.
- Perform CPR: Follow the guidance as described above.
- Stop Session: Press and hold the button for 1.5 seconds. The app will display your performance grades.
- Disconnect App: Press "Disconnect" in the app to end the session and clear the data.


_Code Repositories_

Arduino Code: CPR_Glove.ino
Android App: Android Studio Project
MATLAB Scripts: MATLAB Scripts

