package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mbluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_ENABLE_LOCATION = 1
    private val mBluetoothLeScanner: BluetoothLeScanner? = null
    private val deviceaddress = "CE:01:23:D9:13:BE"

    // RxAndroidBLE Library Specific
    // private val rxBleClient = RxBleClient.create(this)
    // private val scanDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        Log.d("ScanDeviceActivity", "onStart()")
        super.onStart()

        // Check and ask for fine location needed for BLE scanning
        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> mBluetoothLeScanner?.startScan(blescancallback)
            else -> requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ENABLE_LOCATION)
        }

        // Check if phone has Bluetooth
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(
                this,
                "Bluetooth is not supported on this device",
                Toast.LENGTH_SHORT
            ).show();
        }

        // Check if phone has Bluetooth LE
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(
                this,
                "Bluetooth Low Energy is not supported on this device",
                Toast.LENGTH_SHORT
            ).show();
        }

        // Checks if bluetooth adapter is disabled or not and if disabled ask user to enable bluetooth
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mbluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show()
        }
        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults) {
                intArrayOf(PackageManager.PERMISSION_GRANTED) -> {
                    Log.d("ScanDevices", "onRequestPermissionsResult(PERMISSION_GRANTED)")
                    mBluetoothLeScanner?.startScan(blescancallback)
                }
                else -> {
                    Log.d("ScanDevices", "onRequestPermissionsResult(not PERMISSION_GRANTED)")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startBLEScan() {
        var resultStart = mBluetoothLeScanner?.startScan(blescancallback)
        Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show()
        Log.i("Log", String.format("BLE Scan Start\" = %d", resultStart))
    }

    private fun stopBLEScan() {
        var resultStop = mBluetoothLeScanner?.stopScan(blescancallback)
        Toast.makeText(this, "", Toast.LENGTH_LONG).show()
        Log.i("Log", String.format("BLE Scan End\" = %d", resultStop))
    }

    /** Called when user taps Scan button */
    fun startScanBLE(view: View){
        startBLEScan()
        Handler().postDelayed({
            stopBLEScan()
        }, 5000)
    }
}

private val blescancallback = object :ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        val device = result!!.device
        Log.i("Log", "******************************************")
        Log.i("Log", "The scan result: $result");
        Log.i("Log", "------------------------------------------");
        //Log.i("Log","onScanResult: ${result.device?.address} - ${result.device?.name}")
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
