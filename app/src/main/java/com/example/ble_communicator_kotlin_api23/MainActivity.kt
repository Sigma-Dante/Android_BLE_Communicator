package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    // Log TAG
    companion object { private const val TAG = "MainActivity" }
    // Bluetooth
    private var mbluetoothAdapter: BluetoothAdapter? = null
    private var mbluetoothManager: BluetoothManager? = null
    private var gattClient: BluetoothGatt? = null

    // Instantiate the Bluetooth Fragment
    private var bluetoothFragment = BluetoothFragment()

    // Checks if the device has Bluetooth and Bluetooth Low Energy Modules
    private fun initialCheckBluetooth(): Boolean {
        var bluetoothCheck: Boolean = true
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothUnavailableText = "Bluetooth is not supported on this device"
            Log.d(TAG, bluetoothUnavailableText)
            Toast.makeText(this, bluetoothUnavailableText, Toast.LENGTH_SHORT).show()
            bluetoothCheck = false
            return bluetoothCheck
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            val bluetoothLEUnavailableText = "Bluetooth LE is not supported on this device"
            Log.d(TAG, bluetoothLEUnavailableText)
            Toast.makeText(this, bluetoothLEUnavailableText, Toast.LENGTH_SHORT).show()
            bluetoothCheck = false
            return false
        }
        return bluetoothCheck
    }

    // runtime function to check if user has bluetooth enabled
    private fun runtimeCheckBluetooth() {
        if (mbluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter unable to resolve")
        }
        if (!mbluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, Constants().REQUEST_ENABLE_BLUETOOTH)
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
                Constants().REQUEST_ENABLE_LOCATION)
            Log.d(TAG, "Requesting from user to grant permission ACCESS_FINE_LOCATION....") }
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Checks LOCATION_SERVICE
        if (!LocationManagerCompat.isLocationEnabled(lm)){
            Log.d(TAG, "Location Source settings were disabled, requesting from user to approve...")
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        else{ Log.d(TAG, "Location Source settings are already enabled") }
    }

    private fun setBluetoothAdapter(){
        mbluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mbluetoothAdapter = mbluetoothManager!!.adapter
    }

    private fun displayResults(){
        val deviceItemList = bluetoothFragment.sendResults()
        val arrayAdapter: ArrayAdapter<*>
        var mListView = findViewById<ListView>(R.id.device_list)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceItemList)
        mListView.adapter = arrayAdapter
        // arrayAdapter.notifyDataSetChanged()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothFragment = BluetoothFragment()
        var isBluetoothAvailable = initialCheckBluetooth()
        if (isBluetoothAvailable) {
            setBluetoothAdapter()
            setupFragments()
        }
    }

    override fun onStop() {
        super.onStop()
        // Closes the GATT client connection to preserve resources and open Bluetooth connection slot
        gattClient?.close()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
        runtimeCheckLocation()
        runtimeCheckBluetooth()
        }

    /** Called when user taps Scan button */
    fun buttonScanBLE(view: View){
        Handler().postDelayed({bluetoothFragment.bleScan(false, false)}, Constants().SCAN_PERIOD)
        bluetoothFragment.bleScan(true, false)
        Toast.makeText(this, "Scanning", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({displayResults()}, 2000)
    }

    fun buttonConnectGATT(view:View){
        gattClient =  bluetoothFragment.connectGATT(this)
    }

    fun buttonSendGATT(view:View){
        val editText = findViewById<EditText>(R.id.msgGATT)
        val textToSend = editText.text.toString()
        Log.d(TAG, "Sending Text: $textToSend")
        bluetoothFragment.writeGATT(gattClient, textToSend)
        // TODO: create a new function button for reading and GET IT TO WORK
        //bluetoothFragment.readGATT(gattClient)
        editText.text.clear()
    }

    private fun setupFragments() {
        // val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        Log.d(TAG, "setupFragments()")
        Log.d(TAG, "$mbluetoothAdapter")
        bluetoothFragment.setBluetoothAdapter(mbluetoothAdapter)
    }
}