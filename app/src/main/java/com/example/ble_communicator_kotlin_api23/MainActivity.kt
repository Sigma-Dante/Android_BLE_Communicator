package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import java.util.*


const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"
var deviceItemList = ArrayList<String>()

class MainActivity : AppCompatActivity() {
    private var mbluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_ENABLE_LOCATION = 1
    private val deviceMACAddress = "CE:01:23:D9:13:BE"
    companion object { private const val TAG = "MainActivity" }

    private val mBluetoothLeScanner: BluetoothLeScanner
        get() { val mbluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val mbluetoothAdapter = mbluetoothManager.adapter
            return mbluetoothAdapter.bluetoothLeScanner }

    // Checks if the device has Bluetooth and Bluetooth Low Energy Modules
    private fun initialCheckBluetooth() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothUnavailableText = "Bluetooth is not supported on this device"
            Log.d(TAG, bluetoothUnavailableText)
            Toast.makeText(this, bluetoothUnavailableText, Toast.LENGTH_SHORT).show()}
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothLEUnavailableText = "Bluetooth LE is not supported on this device"
            Log.d(TAG, bluetoothLEUnavailableText)
            Toast.makeText(this, bluetoothLEUnavailableText, Toast.LENGTH_SHORT).show()}
    }

    // runtime function to check if user has bluetooth enabled
    private fun runtimeCheckBluetooth() {
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mbluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter unable to resolve") }
        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            Log.d(TAG, "Requesting from user to enable Bluetooth...") }
        else { Log.d(TAG, "Bluetooth already enabled: $mbluetoothAdapter") }
    }

    // Checks for ACCESS_FINE_LOCATION permissions and LOCATION_SERVICE
    private fun runtimeCheckLocation() {
        // Checks ACCESS_FINE_LOCATION
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted: ACCESS_FINE_LOCATION") }
        else { requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ENABLE_LOCATION)
            Log.d(TAG, "Requesting from user to grant permission ACCESS_FINE_LOCATION....") }
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Checks LOCATION_SERVICE
        if (!LocationManagerCompat.isLocationEnabled(lm)){
            Log.d(TAG, "Location Source settings were disabled, requesting from user to approve...")
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        else{ Log.d(TAG, "Location Source settings are already enabled") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialCheckBluetooth()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
        runtimeCheckBluetooth()
        runtimeCheckLocation()
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults) {
                intArrayOf(PackageManager.PERMISSION_GRANTED) -> {
                    Log.d(TAG, "Permission request Granted: Access Fine Location") }
                else -> { Log.d(TAG, "Permission request Denied: Access Fine Location") }
            }
        }
    }

    private fun startBLEScan() {
        // Scan Filter requirements
        val scanFilter = ScanFilter.Builder().setDeviceAddress(deviceMACAddress).build()
        val scanFilterList = listOf(scanFilter)
        val scanSettings = ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()

        Log.d(TAG, "Beginning BLE Scan...")
        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show()

        // var deviceItemList = ArrayList<String>()
        var pairedDevices: Set<BluetoothDevice> = mbluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val newDevice = "${device.name} :: ${device.address}"
                deviceItemList.add(newDevice) } }

        // Start Scan
        // mBluetoothLeScanner?.startScan(blescancallback) // No filter
        mBluetoothLeScanner.startScan(scanFilterList,scanSettings,blescancallback) // filtered

        val arrayAdapter: ArrayAdapter<*>
        var mListView = findViewById<ListView>(R.id.userlist)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, deviceItemList)
        mListView.adapter = arrayAdapter

        // Connect GATT (removes from DeviceListActivity if connected here)
        //connectGATT()

        // Send to Command Menu
        //val intent = Intent(this, CommandsMenu::class.java)
        //startActivity(intent)
    }

    fun connectGATT(){
        Log.d(TAG, "Function: connectGATT running from MainActivity")
        val device = mbluetoothAdapter?.getRemoteDevice(deviceMACAddress)
        val gattClient = device?.connectGatt(this, true, gattCallback)
    }

    private fun stopBLEScan() {
        mBluetoothLeScanner.stopScan(blescancallback)
        mBluetoothLeScanner.flushPendingScanResults(blescancallback)
        Log.d(TAG, "Ending BLE Scan...")
        deviceItemList.clear()
    }

    /** Called when user taps Scan button */
    fun buttonScanBLE(view: View){
        startBLEScan()
        // Scan for 10s then stop
        Handler().postDelayed({stopBLEScan()}, 10000)
    }

    fun buttonConnectGATT(view:View){
        connectGATT()
    }
}

// blescancallback is needed to pass the results of the scan back to the main function
val blescancallback = object :ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        val deviceToAdd = "${result?.device?.name} :: ${result?.device?.address}"
        if (!deviceItemList.contains(deviceToAdd)) {
            deviceItemList.add(deviceToAdd)
        }
        Log.d("DeviceListActivity",
            "onScanResult: ${result?.device?.address} - ${result?.device?.name}")
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

    var writeCharacteristic: BluetoothGattCharacteristic? = null
    //var characteristic2: BluetoothGattCharacteristic? = null
    //var descriptor: BluetoothGattDescriptor? = null
    private val UART_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val TXC: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    //private val RXC: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    //private val TXD: UUID = UUID.fromString("00002901-0000-1000-8000-00805F9B34FB")
    //private var writeInProgress: Boolean = true
    val stringToSend = "Hello from Android!"
    //var statusWrite: Int = 0

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
            writeCharacteristic = gatt?.getService(UART_SERVICE)?.getCharacteristic(TXC)
            //characteristic2 = gatt?.getService(UART_SERVICE)?.getCharacteristic(RXC)
            gatt?.setCharacteristicNotification(writeCharacteristic, true)
            //gatt?.setCharacteristicNotification(characteristic2, true)
            writeCharacteristic?.setValue(stringToSend)
            gatt?.writeCharacteristic(writeCharacteristic)
            //gatt?.readCharacteristic(characteristic2)
        }
        else {
            Log.d("Gatt", "Failure.... cannot communicate with device")
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Log.d("Gatt", "Wrote to characteristic: $stringToSend")
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        Log.d("Gatt", "Read Characteristic: $characteristic")

    }
}