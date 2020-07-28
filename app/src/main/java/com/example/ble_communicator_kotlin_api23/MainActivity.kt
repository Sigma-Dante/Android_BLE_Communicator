package com.example.ble_communicator_kotlin_api23

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), RecyclerViewAdapter.ItemClickListener {
    // Log TAG
    companion object { private const val TAG = "MainActivity" }
    // Bluetooth
    private var mbluetoothAdapter: BluetoothAdapter? = null
    private var mbluetoothManager: BluetoothManager? = null
    private var gattClient: BluetoothGatt? = null

    var adapter: RecyclerViewAdapter? = null

    // Instantiate the Bluetooth Fragment
    private var bluetoothFragment = BluetoothFragment()

    // Checks if the device has Bluetooth and Bluetooth Low Energy Modules
    private fun initialCheckBluetooth(): Boolean {
        var bluetoothCheck: Boolean = true
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Log.d(TAG, Constants().bluetoothUnavailableText)
            Toast.makeText(this, Constants().bluetoothUnavailableText, Toast.LENGTH_SHORT).show()
            bluetoothCheck = false
            return bluetoothCheck
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, Constants().bluetoothLEUnavailableText)
            Toast.makeText(this, Constants().bluetoothLEUnavailableText, Toast.LENGTH_SHORT).show()
            bluetoothCheck = false
            return bluetoothCheck
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
        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.rv_device_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        adapter = RecyclerViewAdapter(this, deviceItemList)
        adapter!!.setClickListener(this)
        recyclerView.adapter = adapter
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.action_disconnect) {
            Log.d(TAG, "Attempting GATT disconnect...")
            gattClient?.close()
            return true
        }

        if (id == R.id.action_scan){
            scanMenu()
            return true
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.scan_toolbar)
        setSupportActionBar(toolbar)
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
        super.onStart()
        runtimeCheckLocation()
        runtimeCheckBluetooth()
        }

    /** Called when user taps Scan button */
    private fun scanMenu(){
        Handler().postDelayed({bluetoothFragment.bleScan(false, false)}, Constants().SCAN_PERIOD)
        bluetoothFragment.bleScan(true, false)
        Toast.makeText(this, "Scanning", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({displayResults()}, 2000)
    }

    /** Called when user taps Send button */
    fun buttonSendGATT(view:View){
        val editText = findViewById<EditText>(R.id.msgGATT)
        val textToSend = editText.text.toString()
        Log.d(TAG, "Sending Text: $textToSend")
        bluetoothFragment.writeGATT(gattClient, textToSend)
        editText.text.clear()
    }

    fun buttonReadGatt(view:View){
        Log.d(TAG, "buttonReadGatt()")
        var msgToDisplay = bluetoothFragment.readGATT(gattClient)
        val readText = findViewById<EditText>(R.id.readGATT)
        readText.text.clear()
        readText.setText(msgToDisplay)
    }

    private fun setupFragments() {
        // val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        bluetoothFragment.setBluetoothAdapter(mbluetoothAdapter)
    }

    override fun onItemClick(view: View?, position: Int) {
        val deviceClicked = adapter?.getItem(position)
        val index = deviceClicked?.indexOf("\n")
        val deviceClickedMAC = deviceClicked?.substring(index!!)?.trim()
        gattClient = bluetoothFragment.connectGATT(this, deviceClickedMAC)
        //Toast.makeText(this, "You clicked $deviceClickedMAC on row number $position", Toast.LENGTH_SHORT).show();
    }
}