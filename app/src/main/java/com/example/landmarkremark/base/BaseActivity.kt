package com.example.landmarkremark.base

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import java.security.AccessController.getContext

open class BaseActivity : AppCompatActivity() {
    fun showToast(message : String) {
        if (message.isNotBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


}