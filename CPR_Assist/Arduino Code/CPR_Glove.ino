#include <Arduino_LSM9DS1.h>
#include <Crypto.h>
#include <AES.h>
#include <ArduinoBLE.h>

#define FORCE_SENSOR_PIN A0
#define LED_PIN_GREEN 5
#define LED_PIN_WHITE 10
#define THRESHOLD_WEIGHT 10.0

// Define BLE characteristics
BLEService pressureService("19b10000-e8f2-537e-4f6c-d104768a1214");
BLECharacteristic pressureCharacteristic("19b10001-e8f2-537e-4f6c-d104768a1214", BLERead | BLENotify, 16);

// Define AES key (16 bytes)
byte aesKey[] = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
AES128 aes128;

const int buttonPin = 9;       // Button connected to digital pin 9
const int redLEDPin = 6;       // Red LED connected to digital pin 6
const int vibratorPin = 3;     // Vibration motor connected to digital pin 3

unsigned long buttonPressStartTime = 0;
bool buttonPressed = false;
bool ledBlinking = false;      // Flag to control LED blinking
bool runningMainCode = false;  // Flag to control the execution of the main code

unsigned long previousTime = 0;
unsigned long previousCompressionTime = 0;
unsigned long previousVibrationTime = 0; // Variable to track previous vibration time

unsigned long totalCompressions = 0;
unsigned long correctWeightCompressions = 0;
unsigned long correctFrequencyCompressions = 0;
unsigned long totalCorrectAngleTime = 0; // Correct angle time
unsigned long sessionStartTime = 0;
unsigned long angleStartTime = 0;
unsigned long correctAngleStartTime = 0;
bool inCorrectAngleState = false;

int compressionCount = 0;
bool countingStarted = false;
bool correctFrequencyDetected = false;
bool pressureExceededThreshold = false;
float maxWeight = 0;

void setup() {
    Serial.begin(9600);
    Serial.println("Started");

    if (!IMU.begin()) {
        Serial.println("Failed to initialize IMU!");
        while (1);
    }

    if (!BLE.begin()) {
      Serial.println("Starting BLE failed!");
      while (1);
    }

  BLE.setLocalName("Arduino Nano 33 BLE");
  pressureService.addCharacteristic(pressureCharacteristic);
  BLE.addService(pressureService);
  BLE.advertise();

  Serial.println("BLE Peripheral device started, waiting for connections...");
    pinMode(FORCE_SENSOR_PIN, INPUT);
    pinMode(LED_PIN_GREEN, OUTPUT);
    pinMode(LED_PIN_WHITE, OUTPUT);
    pinMode(buttonPin, INPUT_PULLUP); // Set the button pin as input with internal pull-up resistor
    pinMode(redLEDPin, OUTPUT);       // Set red LED pin as output
    pinMode(vibratorPin, OUTPUT);     // Set vibrator motor pin as output

    previousTime = millis(); // Initialize previousTime to the current time
}

void loop() {
    BLEDevice central = BLE.central();

    if (central) {
        Serial.print("Connected to central: ");
        Serial.println(central.address());

        // Wait until central disconnects
        while (central.connected()) {
            waitForButtonPress(); // Continuously check for button press
            // Perform main code operations if started
        if (runningMainCode) {
            mainCodeOperations();
        }
            delay(100); // Adjust delay as needed
        }

        Serial.print("Disconnected from central: ");
        Serial.println(central.address());
    }
    else {
        // If BLE is not connected, check for button press
        waitForButtonPress();

        // Perform main code operations if started
        if (runningMainCode) {
            mainCodeOperations();
        }

        delay(100); // Adjust delay as needed
    }
}


void mainCodeOperations() {
        if (sessionStartTime == 0) {
            sessionStartTime = millis(); // Start session timer
        }

        // Continue with LED behavior based on frequency and weight threshold
        // Read analog value from force sensor
        int sensorValue = analogRead(FORCE_SENSOR_PIN);
        float weight = -10.52 * log(sensorValue) + 72.074; // Calculate weight using the given equation

        // Ensure sensorValue is non-zero to avoid log(0)
        if (sensorValue > 0) {
            // Adjust weight to ensure it's non-negative
            if (weight < 0) {
                weight = 0;
            }

            // Check if the weight exceeds 6 to start counting
            if (weight > 6 && !countingStarted) {
                countingStarted = true; // Start counting
                maxWeight = 0; // Reset maxWeight for new compression

            }

            // If counting is started, update maxWeight
            if (countingStarted) {
                if (weight > maxWeight) {
                    maxWeight = weight;
                }
            }

            // If weight drops below 7 and counting is started, consider it as one compression
            if (countingStarted && weight <= 7) {                
                // Increment compression count
                compressionCount++;
                totalCompressions++; // Increment the total compressions count


                // Calculate and print the frequency every time a compression is detected
                unsigned long currentTime = millis();
                float frequency = 0.0; // Initialize frequency to 0 
                if (compressionCount > 0) {
                    frequency = 60000.0 / (currentTime - previousCompressionTime); // Calculate frequency in compressions per minute
                }

                previousCompressionTime = currentTime; // Update time of the last compression

                bool weightCorrect = maxWeight >= THRESHOLD_WEIGHT;
                bool frequencyCorrect = frequency >= 100 && frequency <= 120;

                // Check if frequency is within the correct range
                if (frequencyCorrect) {
                    correctFrequencyDetected = true;
                    digitalWrite(LED_PIN_GREEN, HIGH); // Turn on green LED
                    correctFrequencyCompressions++;

                } else {
                    correctFrequencyDetected = false;
                    digitalWrite(LED_PIN_GREEN, LOW); // Turn off green LED
                }

                // Check if the calculated weight exceeds the threshold
                if (weightCorrect) {
                    pressureExceededThreshold = true;
                    digitalWrite(LED_PIN_WHITE, HIGH); // Turn on white LED
                    correctWeightCompressions++;

                } else {
                    pressureExceededThreshold = false;
                    digitalWrite(LED_PIN_WHITE, LOW); // Turn off white LED
                }

                 countingStarted = false; // Reset countingStarted flag

            }

            // Check if the LEDs should be turned off after 3 seconds of inactivity
            if ((millis() - previousCompressionTime > 3000)) {
                digitalWrite(LED_PIN_GREEN, LOW);
                digitalWrite(LED_PIN_WHITE, LOW);
            }

            // Adjust the vibration motor to beat at 110 BPM with on-off duration
            unsigned long vibrationDuration = 272; // Duration for half the beat (545 ms / 2)

            if (millis() - previousVibrationTime >= vibrationDuration) {
                digitalWrite(vibratorPin, !digitalRead(vibratorPin)); // Toggle the vibrator pin state
                previousVibrationTime = millis(); // Update the previous vibration time
            }

            // IMU code to check the pitch angle
            float x, y, z;

            if (IMU.accelerationAvailable()) {
                IMU.readAcceleration(x, y, z);

                // Calculate pitch angle from accelerometer data
                float pitch = atan2(x, sqrt(y * y + z * z)) * 180 / PI;

                // Adjust pitch angle to match desired orientation
                float adjustedPitch = -pitch; // Offset by -90 degrees
                if (adjustedPitch < -180) adjustedPitch += 360; // Wrap to range -180 to 180

                // Check if the adjusted pitch angle exceeds 75 degrees
                if (adjustedPitch > 75) {
                      if (!inCorrectAngleState) {
                        inCorrectAngleState = true;
                        correctAngleStartTime = millis(); // Record the start time
                       }
                    digitalWrite(redLEDPin, HIGH); // Turn on red LED
                    
                    
                } else {
                   if (inCorrectAngleState) {
                       totalCorrectAngleTime += millis() - correctAngleStartTime;
                      inCorrectAngleState = false;
                     }
                digitalWrite(redLEDPin, LOW); // Turn off red LED
                }

            }

            previousTime = millis();
        } else {
            Serial.println("Analog reading too low, cannot calculate weight.");
            digitalWrite(LED_PIN_GREEN, LOW);
            digitalWrite(LED_PIN_WHITE, LOW);
        }
    } 

void waitForButtonPress() {
    // Read the state of the button
    int buttonState = digitalRead(buttonPin);

    // Check if the button is pressed (buttonState is LOW)
    if (buttonState == LOW) {
        if (!buttonPressed) {
            // Record the start time of button press
            buttonPressStartTime = millis();
            buttonPressed = true;
        } else {
            // Check if the button has been continuously pressed for 1.5 seconds
            if (millis() - buttonPressStartTime >= 1500) {
                //Serial.println("Button pressed for 1.5 seconds!");

                // Blink the LEDs and vibrate the motor twice in a second
                for (int i = 0; i < 2; i++) {
                    digitalWrite(LED_PIN_GREEN, HIGH);
                    digitalWrite(redLEDPin, HIGH);
                    digitalWrite(vibratorPin, HIGH);
                    delay(150);
                    digitalWrite(LED_PIN_GREEN, LOW);
                    digitalWrite(redLEDPin, LOW);
                    digitalWrite(vibratorPin, LOW);
                    delay(150);
                }

                // Print grades
                printGrades();

                // Reset session data
                sessionStartTime = 0;
                totalCompressions = 0;
                correctWeightCompressions = 0;
                correctFrequencyCompressions = 0;
                totalCorrectAngleTime = 0;
                inCorrectAngleState = false;


                // Check if LEDs are still on from previous action
                if (ledBlinking) {
                    digitalWrite(LED_PIN_GREEN, LOW);
                    digitalWrite(redLEDPin, LOW);
                    digitalWrite(LED_PIN_WHITE, LOW);

                    ledBlinking = false; // Clear the flag indicating LED blinking
                }

                // Reset button state to avoid repeated action
                buttonPressed = false;
                ledBlinking = true; // Set flag to control LED blinking
                previousVibrationTime = millis(); // Reset previous vibration time

                // Toggle the execution of the main code
                runningMainCode = !runningMainCode;
            }
        }
    } else {
        // Reset button state if button is released before 1.5 seconds
        buttonPressed = false;
    }
}

void printGrades() {
    unsigned long currentTime = millis();

    if (inCorrectAngleState) {
        totalCorrectAngleTime += currentTime - correctAngleStartTime;
        inCorrectAngleState = false;
    }
    unsigned long sessionDuration = currentTime - sessionStartTime;
   
    if (totalCompressions == 0) {
        Serial.println("No compressions detected.");
        return;
    }

    // Calculate the grades as floats
    float weightGradef = (correctWeightCompressions / (float)totalCompressions) * 100;
    float frequencyGradef = (correctFrequencyCompressions / (float)totalCompressions) * 100;
    float angleGradef = (totalCorrectAngleTime / (float)sessionDuration) * 100;
    float totalGradef = ((weightGradef * 2) + (frequencyGradef * 2) + angleGradef) / 5;

    // Convert the float grades to integers, preserving two decimal places
    int weightGrade = (int)(weightGradef * 100);
    int frequencyGrade = (int)(frequencyGradef * 100);
    int angleGrade = (int)(angleGradef * 100);
    int totalGrade = (int)(totalGradef * 100);

    // Print the calculated statistics and grades
    Serial.println("---- Compression Statistics ----");
    Serial.print("Total Compressions: ");
    Serial.println(totalCompressions);
    Serial.print("Correct Weight Compressions: ");
    Serial.println(correctWeightCompressions);
    Serial.print("Correct Frequency Compressions: ");
    Serial.println(correctFrequencyCompressions);

    Serial.println("---- Grades ----");
    Serial.print("Grade for Correct Compression Weight: ");
    Serial.print(weightGrade / 100.0); // Convert back to float for display
    Serial.println("%");
    Serial.print("Grade for Correct Frequency: ");
    Serial.print(frequencyGrade / 100.0); // Convert back to float for display
    Serial.println("%");
    Serial.print("Grade for Correct Angle: ");
    Serial.print(angleGrade / 100.0); // Convert back to float for display
    Serial.println("%");

    Serial.println("---- Total Grade ----");
    Serial.print("Total Grade: ");
    Serial.print(totalGrade / 100.0); // Convert back to float for display
    Serial.println("%");

    // Create byte arrays to store the data
    byte dataBuffer[28];

    // Pack data into byte arrays
    int32ToBytes(dataBuffer, 0, totalCompressions);
    int32ToBytes(dataBuffer, 4, correctWeightCompressions);
    int32ToBytes(dataBuffer, 8, correctFrequencyCompressions);
    int32ToBytes(dataBuffer, 12, weightGrade);
    int32ToBytes(dataBuffer, 16, frequencyGrade);
    int32ToBytes(dataBuffer, 20, angleGrade);
    int32ToBytes(dataBuffer, 24, totalGrade);

    int32ToBytes(dataBuffer, 28, 1); // Index 28 for the number 1 to make easier the decryption



     // Encrypt the data
    byte encryptedData[32];
    aes128.setKey(aesKey, 16);

    // Encrypt data in 16-byte blocks
    aes128.encryptBlock(encryptedData, dataBuffer);
    aes128.encryptBlock(encryptedData + 16, dataBuffer + 16);
    
   // Serial.println("Encrypted Data:");
   for (int i = 0; i < 32; i++) {
        //Serial.print(encryptedData[i], HEX);
        //Serial.print(" ");
    }
    //Serial.println();
    //Serial.println();
    delay(1000);  // Delay to prevent flooding the Serial Monitor

    // Send the encrypted data in chunks
    pressureCharacteristic.writeValue(encryptedData, 16);
    delay(50);
    pressureCharacteristic.writeValue(encryptedData + 16, 16); // Last 12 bytes + number

}

// Function to convert int32_t to bytes and store in byte array starting from given index
void int32ToBytes(byte* bytes, int startIndex, int value) {
    bytes[startIndex] = value & 0xFF;
    bytes[startIndex + 1] = (value >> 8) & 0xFF;
    bytes[startIndex + 2] = (value >> 16) & 0xFF;
    bytes[startIndex + 3] = (value >> 24) & 0xFF;
}
