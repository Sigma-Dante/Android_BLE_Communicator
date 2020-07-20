package com.example.ble_communicator_kotlin_api23

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.ListFragment

class BluetoothFragment : ListFragment() {
    // Log TAG
    companion object { private const val TAG = "BluetoothFragment" }

    // Variables
    var deviceListArray: MutableList<BluetoothDevice> = arrayListOf()
    var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mHandler: Handler? = null
    private var mScanning: Boolean = false

    // Functions
    fun setBluetoothAdapter(btAdapter: BluetoothAdapter?): Unit {
        mBluetoothAdapter = btAdapter
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
    }

    fun bleScan (enable: Boolean, filter: Boolean) {
        //mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        when (enable){
            true -> {
                Log.d(TAG, "Starting BLE Scan")
                mScanning = true
                if (filter) {
                    val scanFilter = ScanFilter.Builder().setDeviceAddress(Constants().deviceMACAddress).build()
                    val scanFilterList = listOf(scanFilter)
                    val scanSettings = ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()
                    mBluetoothLeScanner?.startScan(scanFilterList, scanSettings, blescancallback)
                }
                else{ mBluetoothLeScanner?.startScan(blescancallback)}
            }
            else -> {
                Log.d(TAG, "Stopping BLE Scan")
                mScanning = false
                mBluetoothLeScanner?.stopScan(blescancallback)
                mBluetoothLeScanner?.flushPendingScanResults(blescancallback)
            }
        }
    }

    fun connectGATT(context: Context): BluetoothGatt? {
        Log.d(TAG, "Attempting to connect to GATT client")
        val device = mBluetoothAdapter?.getRemoteDevice(Constants().deviceMACAddress)
        val gattClient = device?.connectGatt(context, true, gattCallback)
        return gattClient
    }

    fun addScanResult(scanResult: ScanResult?) {
        val bleDevice = scanResult?.device
        if (bleDevice != null){
            if (!deviceListArray?.contains(bleDevice)!!) {
                val deviceAddress = bleDevice.address
                val deviceName = bleDevice.name
                deviceListArray?.add(bleDevice)
        }
            else{}
        }
    }

    fun sendResults(): MutableList<BluetoothDevice> {
        return deviceListArray
    }

    private val blescancallback = object :ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult: ${result?.device?.address} - ${result?.device?.name}")
            addScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG,"onBatchScanResults:${results.toString()}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanFailed: $errorCode")
        }
    }

    fun writeGATT(gattClient:BluetoothGatt?, msg: String){
        Log.d("GATT", "Attempting to write to characteristic")
        var writeCharacteristic = gattClient?.getService(Constants().UART_SERVICE)?.getCharacteristic(Constants().TXC)
        gattClient?.setCharacteristicNotification(writeCharacteristic, true)
        writeCharacteristic?.setValue(msg)
        gattClient?.writeCharacteristic(writeCharacteristic)
    }

    // gattCallback is needed to pass results of Gatt server to the main function
    private val gattCallback = object : BluetoothGattCallback(){

        var writeCharacteristic: BluetoothGattCharacteristic? = null
        //var characteristic2: BluetoothGattCharacteristic? = null
        //var descriptor: BluetoothGattDescriptor? = null
        val stringToSend = "Hello from Android!"

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothAdapter.STATE_CONNECTED){
                gatt?.discoverServices()
                Log.d("Gatt", "Connected to GATT server, attempting to connect to device")
            }
            if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                Log.d("Gatt", "Disconnected from GATT server")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Gatt", "Successfully connected with GATT Profile")
                //writeCharacteristic = gatt?.getService(Constants().UART_SERVICE)?.getCharacteristic(Constants().TXC)
                //characteristic2 = gatt?.getService(UART_SERVICE)?.getCharacteristic(RXC)
                //gatt?.setCharacteristicNotification(writeCharacteristic, true)
                //gatt?.setCharacteristicNotification(characteristic2, true)
                //writeCharacteristic?.setValue(stringToSend)
                //gatt?.writeCharacteristic(writeCharacteristic)
                //gatt?.readCharacteristic(characteristic2)
            }
            else {
                Log.d("Gatt", "Cannot connect with GATT Profile")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("Gatt", "Wrote to characteristic.")
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
}