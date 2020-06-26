package com.example.ble_communicator_kotlin_api23

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
    private val bluetoothLeScanner: BluetoothLeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(
                this,
                "Bluetooth Low Energy is not supported on this device",
                Toast.LENGTH_SHORT
            ).show();
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(
                this,
                "Bluetooth is not supported on this device",
                Toast.LENGTH_SHORT
            ).show();
        }

        // Checks if bluetooth adapter is disabled or not, and if so then ask user to enable bluetooth
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mbluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show()
        }

        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }


    private fun startBLEScan() {
        var result = bluetoothLeScanner?.startScan(blescancallback)
        Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show()
        Log.i("Log", String.format("BLE Scan Start\" = %d", result))
    }

    private fun stopBLEScan() {
        var result = bluetoothLeScanner?.stopScan(blescancallback)
        Toast.makeText(this, "Stop scanning", Toast.LENGTH_LONG).show()
        Log.i("Log", String.format("BLE Scan End\" = %d", result))
    }

    /** Called when user taps Scan button */
    fun startScanBLE(view: View) {
        startBLEScan()
        val handler: Handler? = null

        handler?.postDelayed({
            stopBLEScan()
        }, 10000)
    }
}

private val blescancallback = object :ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        val device = result!!.device
        Log.i("Log", "onScanResult")
        Log.i("Log","onScanResult: ${result.device?.address} - ${result.device?.name}")
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Log.i("Log", "onBatchScanResults")
        Log.i("Log","onBatchScanResults:${results.toString()}")
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.i("Log", "onScanFailed")
        Log.i("Log", "onScanFailed: $errorCode")
    }
}
