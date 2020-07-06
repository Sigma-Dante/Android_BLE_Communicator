package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import java.util.*


class MainActivity : AppCompatActivity() {

/*    private val mBluetoothLeScanner: BluetoothLeScanner
    get(){
        val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE)
        val bluetoothAdapter: BluetoothAdapter? = null
        return bluetoothAdapter.bluetoothLeScanner
    }*/

    private var mbluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_ENABLE_LOCATION = 1



    private val deviceMACAddress = "CE:01:23:D9:13:BE"

    //private val UART_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    //private val TX: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9­E50E24DCCA9E")
    //private val RX: UUID = UUID.fromString("6E400003-B5A3-F393­E0A9­E50E24DCCA9E")


    private val TAG = MainActivity::class.java.simpleName


    // THe below call did NOT work for scanning BLE.
    // It would constantly return NULL only
    // Why? TODO
    //private val mBluetoothLeScanner: BluetoothLeScanner? = null

    // THe below call was needed to fix BLE scanning. Why? TODO
    private val mBluetoothLeScanner: BluetoothLeScanner
        get() {
            val mbluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val mbluetoothAdapter = mbluetoothManager.adapter
            return mbluetoothAdapter.bluetoothLeScanner
        }

    private fun hasBluetoothLE(){
        // Check if phone has Bluetooth LE
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "Bluetooth LE not supported on device")
        }

        // Checks if bluetooth adapter is disabled or not and if disabled ask user to enable bluetooth
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mbluetoothAdapter == null) {
            Log.d(TAG, "No Bluetooth module in device")
        }
        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            Log.d(TAG, "Requesting to enable Bluetooth...")
        }
        else {
            Log.d(TAG, "Bluetooth already enabled: $mbluetoothAdapter")
        }
    }

    // This function checks for ACCESS_FINE_LOCATION permissions and LOCATION_SERVICE
    private fun checkLocation() {

        // Checks ACCESS_FINE_LOCATION
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted: ACCESS_FINE_LOCATION")
        }
        else {
            // If ACCESS_FINE_LOCATION is not permitted, then request user to allow
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ENABLE_LOCATION)
            Log.d(TAG, "Requesting from user ACCESS_FINE_LOCATION....")
        }
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Checks LOCATION_SERVICE
        if (!LocationManagerCompat.isLocationEnabled(lm)){
            Log.d(TAG, "Location Source settings was disabled, requesting user to approve...")
            // Start Location Settings Activity
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        else{ Log.d(TAG, "Location Source settings are enabled") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
        hasBluetoothLE()
        checkLocation()
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults) {
                intArrayOf(PackageManager.PERMISSION_GRANTED) -> {
                    Log.d(TAG, "Permission request Granted: Access Fine Location")
                }
                else -> {
                    Log.d(TAG, "Permission request Denied: Access Fine Location")
                }
            }
        }
    }

    private fun startBLEScan() {
        if (mBluetoothLeScanner == null) {
            Log.d(TAG, "mBluetoothLeScanner is null")
        } else {
            mBluetoothLeScanner?.startScan(blescancallback)
            //Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show()
            Log.d(TAG, "************************************************************************")
            Log.d(TAG, "Beginning BLE Scan...")
            val device = mbluetoothAdapter?.getRemoteDevice(deviceMACAddress)
            val gattClient = device?.connectGatt(this, true, gattCallback)
            //val service = bluetoothGatt.getService(TX)
            //val characteristic = service.getCharacteristic(TX)
        }
    }


    private fun stopBLEScan() {
        if (mBluetoothLeScanner == null) {
            Log.d(TAG, "mBluetoothLeScanner is null")
        } else {
            mBluetoothLeScanner?.stopScan(blescancallback)
            mBluetoothLeScanner?.flushPendingScanResults(blescancallback)
            //Toast.makeText(this, "Scan end", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Ending BLE Scan...")
            Log.d(TAG, "------------------------------------------------------------------------")
        }
    }

    /** Called when user taps Scan button */
    fun startScanBLE(view: View){
        startBLEScan()
        Handler().postDelayed({
            stopBLEScan()
        }, 30000)
    }
}



// blescancallback is needed to pass the results of the scan back to the main function
val blescancallback = object :ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        println(result?.device?.name)
        Log.d(
            "DeviceListActivity",
            "onScanResult: ${result?.device?.address} - ${result?.device?.name}"
        )
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Log.d("DeviceListActivity","onBatchScanResults:${results.toString()}")
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.d("DeviceListActivity", "onScanFailed: $errorCode")
    }
}

// gattCallback is needed to pass results of Gatt server to the main function
val gattCallback = object :BluetoothGattCallback(){

    var characteristic: BluetoothGattCharacteristic? = null
    var descriptor: BluetoothGattDescriptor? = null
    private val UART_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val TXC: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    //private val RXC: UUID = UUID.fromString("6E400003-B5A3-F393­E0A9­E50E24DCCA9E")
    //private val TXD: UUID = UUID.fromString("00002901-0000-1000-8000-00805F9B34FB")
    private var writeInProgress: Boolean = true

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (newState == STATE_CONNECTED){
            gatt?.discoverServices()
            Log.d("Gatt", "Connected to GATT server, attempting to connect to device")
        }
        if (newState == STATE_DISCONNECTED) {
            Log.d("Gatt", "Disconnected from GATT server")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("Gatt", "Communicating with device")
            characteristic = gatt?.getService(UART_SERVICE)?.getCharacteristic(TXC)
            gatt?.setCharacteristicNotification(characteristic, true)
            //descriptor = characteristic.getDescriptor(TXD)
            characteristic?.setValue("Hello! From Android Studio")
            writeInProgress = true
            gatt?.writeCharacteristic(characteristic)
            while (writeInProgress) { }
        }
        else {
            Log.d("Gatt", "Failure.... cannot communicate with device")
        }
    }
}