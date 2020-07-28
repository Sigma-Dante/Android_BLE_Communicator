package com.example.ble_communicator_kotlin_api23

import java.util.*

class Constants {
    // Permission Requests
    val REQUEST_ENABLE_LOCATION = 1
    val REQUEST_ENABLE_BLUETOOTH = 1
    val bluetoothUnavailableText = "Bluetooth is not supported on this device"
    val bluetoothLEUnavailableText = "Bluetooth LE is not supported on this device"

    // UUID's
    val UART_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    val TXC: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val RXC: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    val TXD: UUID = UUID.fromString("00002901-0000-1000-8000-00805F9B34FB")

    // MAC
    val deviceMACAddress = "CE:01:23:D9:13:BE"

    // Scanning
    val SCAN_PERIOD: Long = 5000
}