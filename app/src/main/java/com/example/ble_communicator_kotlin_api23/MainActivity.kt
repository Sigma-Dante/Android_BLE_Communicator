package com.example.ble_communicator_kotlin_api23

import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast

private const val SCAN_PERIOD: Long = 1000

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun checkBluetooth() {
        val REQUEST_ENABLE_BT = 1
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)
        }
    }

}

class DeviceScanActivity(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler,
    private val 
) : ListActivity() {
    private var mScanning: Boolean = false

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                handler.postDelayed({
                    mScanning = false

                }, SCAN_PERIOD)
                mScanning = true

                
            }
            else -> {
                mScanning = false
            }
        }
    }
}