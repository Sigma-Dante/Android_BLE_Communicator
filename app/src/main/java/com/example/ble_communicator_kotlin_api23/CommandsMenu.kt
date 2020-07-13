package com.example.ble_communicator_kotlin_api23

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_commands_menu.*
import org.w3c.dom.Text




class CommandsMenu : AppCompatActivity() {
    companion object { private const val TAG = "CommandsMenu" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commands_menu)
        MainActivity().connectGATT()
        Log.d(TAG, "Attempting to call connectGATT from CommandsMenu")
    }

    fun getBLEMessage(view: View){
        val bleMessage = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
    }



}