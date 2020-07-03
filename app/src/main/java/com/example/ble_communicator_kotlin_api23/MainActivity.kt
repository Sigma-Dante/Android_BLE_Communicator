package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var mbluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_ENABLE_LOCATION = 1
    private val mBluetoothLeScanner: BluetoothLeScanner? = null
    private val deviceMACAddress = "CE:01:23:D9:13:BE"
    private val TAG = MainActivity::class.java.simpleName

    // RxAndroidBLE Library Specific
    // private val rxBleClient = RxBleClient.create(this)
    // private val scanDisposable: Disposable? = null


    private fun hasBluetoothLE(){
        // Check if phone has Bluetooth LE
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(
                this,
                "Bluetooth Low Energy is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "Bluetooth LE not supported on device")
        }

        // Checks if bluetooth adapter is disabled or not and if disabled ask user to enable bluetooth
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mbluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show()
            Log.d(TAG, "No Bluetooth module in device")
        }
        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            Log.d(TAG, "Requesting to enable Bluetooth...")
        }
        else {
            Log.d(TAG, "Bluetooth already enabled")
        }
    }

    private fun checkLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted: ACCESS_FINE_LOCATION")
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ENABLE_LOCATION)
            Log.d(TAG, "Had to request access to fine location....")
        }
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
        mBluetoothLeScanner?.startScan(blescancallback)
        Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show()
        Log.d(TAG, "************************************************************************")
        Log.d(TAG, "Beginning BLE Scan...")
    }

    private fun stopBLEScan() {
        mBluetoothLeScanner?.stopScan(blescancallback)
        mBluetoothLeScanner?.flushPendingScanResults(blescancallback)
        Toast.makeText(this, "Scan end", Toast.LENGTH_LONG).show()
        Log.d(TAG, "Ending BLE Scan...")
        Log.d(TAG, "------------------------------------------------------------------------")
    }

    /** Called when user taps Scan button */
    fun startScanBLE(view: View){
        startBLEScan()
        Handler().postDelayed({
            stopBLEScan()
        }, 20000)
    }
}

private val blescancallback = object :ScanCallback() {

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
