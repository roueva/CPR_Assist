    @file:Suppress("DEPRECATION")

    package com.example.cpr_assist

    import android.Manifest
    import android.annotation.SuppressLint
    import android.bluetooth.BluetoothAdapter
    import android.bluetooth.BluetoothDevice
    import android.bluetooth.BluetoothGatt
    import android.bluetooth.BluetoothGattCallback
    import android.bluetooth.BluetoothGattCharacteristic
    import android.bluetooth.BluetoothGattDescriptor
    import android.bluetooth.BluetoothManager
    import android.content.Context
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.FrameLayout
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import java.nio.ByteBuffer
    import java.nio.ByteOrder
    import java.util.UUID
    import javax.crypto.Cipher
    import javax.crypto.SecretKey
    import javax.crypto.spec.SecretKeySpec



    class MainActivity : AppCompatActivity() {

        companion object {
            private const val TAG = "MainActivity"
            private const val EXPECTED_DATA_SIZE = 32
            private const val BUFFER_SIZE = EXPECTED_DATA_SIZE // Adjust if needed
        }

        private val AES_KEY = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
        )

        private lateinit var bluetoothAdapter: BluetoothAdapter
        private lateinit var connectButton: Button
        private lateinit var disconnectButton: Button
        private lateinit var totalCompressions: TextView
        private lateinit var correctWeightCompressions: TextView
        private lateinit var correctFrequencyCompressions: TextView
        private lateinit var weightGrade: TextView
        private lateinit var frequencyGrade: TextView
        private lateinit var angleGrade: TextView
        private lateinit var totalGrade: TextView
        private lateinit var totalGradeLayout: FrameLayout
        private lateinit var circularProgressBar: CircularProgressBar // Add this line



        private val bluetoothDeviceName = "Arduino Nano 33 BLE"
        private val serviceUUID: UUID = UUID.fromString("19b10000-e8f2-537e-4f6c-d104768a1214")
        private val characteristicUUID: UUID = UUID.fromString("19b10001-e8f2-537e-4f6c-d104768a1214")

        private var bluetoothGatt: BluetoothGatt? = null
        private var dataBuffer = ByteArray(BUFFER_SIZE)
        private var receivedDataSize = 0

        private val bluetoothPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allGranted = permissions.all { it.value }
                if (allGranted) {
                    enableBluetooth()
                } else {
                    showToast("Bluetooth permissions denied.")
                }
            }

        private val enableBluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Bluetooth enabled, proceed to search paired devices
                } else {
                    showToast("Bluetooth activation cancelled.")
                }
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            initializeViews()

            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            connectButton.setOnClickListener {searchAndConnectToDevice()}

            updateProgress(0f) // Example percentage value (50%)

            disconnectButton.setOnClickListener {
                circularProgressBar.resetProgress() // Reset circular progress bar
                disconnectFromArduino() // Disconnect from Arduino and clear UI
            }


            checkBluetoothPermissions()
        }

        private fun initializeViews() {
            connectButton = findViewById(R.id.connectButton)
            disconnectButton = findViewById(R.id.disconnectButton)
            totalCompressions = findViewById(R.id.totalCompressions)
            correctWeightCompressions = findViewById(R.id.correctWeightCompressions)
            correctFrequencyCompressions = findViewById(R.id.correctFrequencyCompressions)
            weightGrade = findViewById(R.id.weightGrade)
            frequencyGrade = findViewById(R.id.frequencyGrade)
            angleGrade = findViewById(R.id.angleGrade)
            totalGradeLayout = findViewById(R.id.totalGradeLayout)
            circularProgressBar= findViewById(R.id.circularProgressBar) // Add this line
            totalGrade = findViewById(R.id.totalGradeText) // Initialize your TextView here


        }


        // Add this method to update total grade and animate the circular progress
        @SuppressLint("SetTextI18n")
        // Method to update the circular progress and related UI elements
        private fun updateProgress(percentage: Float) {
            val progressPercentage = percentage.coerceIn(0f, 100f) // Ensure percentage is within 0-100 range
            circularProgressBar.setProgressPercentage(progressPercentage)
        }


        private fun checkBluetoothPermissions() {
            val requiredPermissions = listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            val permissionsToRequest = requiredPermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                enableBluetooth()
            }
        }

        private fun enableBluetooth() {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                // Bluetooth is already enabled, do nothing here
            }
        }

        @SuppressLint("SetTextI18n")
        private fun searchAndConnectToDevice() {
            try {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                if (!pairedDevices.isNullOrEmpty()) {
                    val arduinoDevice = pairedDevices.find { it.name == bluetoothDeviceName }
                    if (arduinoDevice != null) {
                        // Connect to the Arduino device
                        connectToDevice(arduinoDevice)
                    } else {
                        showToast("$bluetoothDeviceName not found among paired devices.")
                    }
                } else {
                    showToast("No paired devices found.")
                }
            } catch (e: SecurityException) {
                showToast("Failed to retrieve paired devices: ${e.message}")
            }
        }

        private fun connectToDevice(device: BluetoothDevice) {
            try {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Connect to the device
                    bluetoothGatt = device.connectGatt(this, false, gattCallback)
                } else {
                    requestBluetoothConnectPermission()
                    showToast("Bluetooth permissions not granted.")
                }
            } catch (e: SecurityException) {
                showToast("Failed to connect due to security reasons: ${e.message}")
            }
        }

        private fun requestBluetoothConnectPermission() {
            if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)) {
                // Explain to the user why the permission is needed
                showToast("App requires Bluetooth connection permission to connect to devices.")
            }
            // Request the permission
            bluetoothPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        }



        private val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                Log.d(TAG, "Connection State Changed: $newState")
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    runOnUiThread {
                        showToast("Arduino Nano 33 BLE connected.")
                    }
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Discover services
                        gatt?.discoverServices()
                    } else {
                        showToast("Bluetooth permissions not granted.")
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    runOnUiThread {
                        showToast("Disconnected")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                Log.d(TAG, "Services Discovered: $status")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val characteristic =
                        gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
                    if (characteristic != null) {
                        setCharacteristicNotification(characteristic)
                    }
                } else {
                    showToast("Failed to discover services, status: $status")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                Log.d(TAG, "Characteristic Changed")
                if (characteristic != null) {
                    val data = characteristic.value
                    if (data != null) {
                        processData(data)
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                Log.d(TAG, "Characteristic Read: $status")
            }
        }

        private fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
            try {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    bluetoothGatt?.writeDescriptor(descriptor)
                } else {
                    showToast("Bluetooth permissions not granted.")
                }
            } catch (e: SecurityException) {
                showToast("Failed to set notification: ${e.message}")
            }
        }

        private fun processData(data: ByteArray) {
            if (receivedDataSize + data.size <= EXPECTED_DATA_SIZE) {
                // Append new data to the buffer
                System.arraycopy(data, 0, dataBuffer, receivedDataSize, data.size)
                receivedDataSize += data.size

                if (receivedDataSize >= EXPECTED_DATA_SIZE) {
                    // Process the complete buffer
                    runOnUiThread {
                        updateUI(dataBuffer)
                    }
                    // Reset buffer and size for the next set of data
                    receivedDataSize = 0
                }
            } else {
                // Buffer overflow, reset
                receivedDataSize = 0
            }
        }

        @SuppressLint("SetTextI18n")
        private fun updateUI(data: ByteArray) {
            val decryptedData = decryptData(data)
            if (decryptedData != null) {

                val totalComp = decryptedData.getInt(0)
                val correctWeightComp = decryptedData.getInt(4)
                val correctFreqComp = decryptedData.getInt(8)
                val weightGradePercent = decryptedData.getInt(12)
                val frequencyGradePercent = decryptedData.getInt(16)
                val angleGradePercent = decryptedData.getInt(20)
                val totalGradePercent = decryptedData.getInt(24)

                Log.d(TAG, "Parsed Values: totalComp=$totalComp, correctWeightComp=$correctWeightComp, " +
                        "correctFreqComp=$correctFreqComp, weightGradePercent=$weightGradePercent, " +
                        "frequencyGradePercent=$frequencyGradePercent, angleGradePercent=$angleGradePercent, " +
                        "totalGradePercent=$totalGradePercent")


                runOnUiThread {
                    totalCompressions.text = totalComp.toString()
                    correctWeightCompressions.text = correctWeightComp.toString()
                    correctFrequencyCompressions.text = correctFreqComp.toString()
                    weightGrade.text = "%.2f%%".format(weightGradePercent / 100.0f)
                    frequencyGrade.text = "%.2f%%".format(frequencyGradePercent / 100.0f)
                    angleGrade.text = "%.2f%%".format(angleGradePercent / 100.0f)

                    updateProgress(totalGradePercent / 100.0f) // Convert to percentage

                }
            }
        }

        @SuppressLint("GetInstance")
        private fun decryptData(data: ByteArray): ByteBuffer? {
            return try {
                val secretKey: SecretKey = SecretKeySpec(AES_KEY, "AES")
                val cipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                val decryptedData = cipher.doFinal(data)
                ByteBuffer.wrap(decryptedData).order(ByteOrder.LITTLE_ENDIAN)
            } catch (e: Exception) {
                showToast("Decryption error: ${e.message}")
                null
            }
        }

        private fun disconnectFromArduino() {
            try {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Disconnect from the Arduino device
                    bluetoothGatt?.close() // Close the GATT properly
                    clearUI()
                    receivedDataSize = 0 // Reset received data size
                } else {
                    // Request the permission
                    requestBluetoothConnectPermissionForDisconnect()
                }
            } catch (e: SecurityException) {
                showToast("Failed to disconnect due to security reasons: ${e.message}")
            }
        }

        @SuppressLint("SetTextI18n")
        private fun clearUI() {
            runOnUiThread {
                totalCompressions.text = "--"
                correctWeightCompressions.text = "--"
                correctFrequencyCompressions.text = "--"
                weightGrade.text = "--%"
                frequencyGrade.text = "--%"
                angleGrade.text = "--%"
            }

        }



        private fun requestBluetoothConnectPermissionForDisconnect() {
            if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)) {
                // Explain to the user why the permission is needed
                showToast("App requires Bluetooth connection permission to disconnect from devices.")
            }
            // Request the permission
            bluetoothPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        }

        private fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
