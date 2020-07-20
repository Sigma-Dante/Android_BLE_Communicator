package com.example.ble_communicator_kotlin_api23

import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*
import java.util.concurrent.TimeUnit


class ScanResultAdapter internal constructor(
    private val mContext: Context,
    private val mInflater: LayoutInflater
) :
    BaseAdapter() {
    private val mArrayList: ArrayList<ScanResult> = ArrayList()
    override fun getCount(): Int {
        return mArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return mArrayList[position].device.address.hashCode().toLong()
    }

    override fun getView(
        position: Int,
        view: View,
        parent: ViewGroup
    ): View {

        // Reuse an old view if we can, otherwise create a new one.
        var view = view
        if (view == null) {
            view = mInflater.inflate(R.layout.listitem_scanresult, null)
        }
        val deviceNameView =
            view.findViewById<View>(R.id.device_name) as TextView
        val deviceAddressView =
            view.findViewById<View>(R.id.device_address) as TextView
        val lastSeenView =
            view.findViewById<View>(R.id.last_seen) as TextView
        val scanResult = mArrayList[position]
        var name = scanResult.device.name
        if (name == null) {
            name = mContext.resources.getString(R.string.no_name)
        }
        deviceNameView.text = name
        deviceAddressView.text = scanResult.device.address
        lastSeenView.text = getTimeSinceString(
            mContext,
            scanResult.timestampNanos
        )
        return view
    }

    /**
     * Search the adapter for an existing device address and return it, otherwise return -1.
     */
    private fun getPosition(address: String): Int {
        var position = -1
        for (i in mArrayList.indices) {
            if (mArrayList[i].device.address == address) {
                position = i
                break
            }
        }
        return position
    }

    /**
     * Add a ScanResult item to the adapter if a result from that device isn't already present.
     * Otherwise updates the existing position with the new ScanResult.
     */
    fun add(scanResult: ScanResult) {
        val existingPosition = getPosition(scanResult.device.address)
        if (existingPosition >= 0) {
            // Device is already in list, update its record.
            mArrayList[existingPosition] = scanResult
        } else {
            // Add new Device's ScanResult to list.
            mArrayList.add(scanResult)
        }
    }

    /**
     * Clear out the adapter.
     */
    fun clear() {
        mArrayList.clear()
    }

    companion object {
        /**
         * Takes in a number of nanoseconds and returns a human-readable string giving a vague
         * description of how long ago that was.
         */
        fun getTimeSinceString(
            context: Context,
            timeNanoseconds: Long
        ): String {
            var lastSeenText =
                context.resources.getString(R.string.last_seen) + " "
            val timeSince =
                SystemClock.elapsedRealtimeNanos() - timeNanoseconds
            val secondsSince = TimeUnit.SECONDS.convert(
                timeSince,
                TimeUnit.NANOSECONDS
            )
            lastSeenText += if (secondsSince < 5) {
                context.resources.getString(R.string.just_now)
            } else if (secondsSince < 60) {
                "$secondsSince " + context.resources
                    .getString(R.string.seconds_ago)
            } else {
                val minutesSince = TimeUnit.MINUTES.convert(
                    secondsSince,
                    TimeUnit.SECONDS
                )
                if (minutesSince < 60) {
                    if (minutesSince == 1L) {
                        "$minutesSince " + context.resources
                            .getString(R.string.minute_ago)
                    } else {
                        "$minutesSince " + context.resources
                            .getString(R.string.minutes_ago)
                    }
                } else {
                    val hoursSince = TimeUnit.HOURS.convert(
                        minutesSince,
                        TimeUnit.MINUTES
                    )
                    if (hoursSince == 1L) {
                        "$hoursSince " + context.resources
                            .getString(R.string.hour_ago)
                    } else {
                        "$hoursSince " + context.resources
                            .getString(R.string.hours_ago)
                    }
                }
            }
            return lastSeenText
        }
    }

}
