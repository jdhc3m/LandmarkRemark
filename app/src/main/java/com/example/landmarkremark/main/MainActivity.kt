package com.example.landmarkremark.main

import android.content.Intent
import android.os.Bundle
import com.example.landmarkremark.R
import com.example.landmarkremark.base.BaseActivity
import com.example.landmarkremark.map.MapActivity
import com.example.landmarkremark.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLayout()

    }

    override fun onStart() {
        super.onStart()
        userNameEt.setText("")
    }

    private fun setupLayout() {
        nextButton.setOnClickListener {
            if (!userNameEt.text.isEmpty()) {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(Constants.IntentExtras.USER_NAME, userNameEt.text.toString())
                startActivity(intent)
            } else {
                showToast(getString(R.string.can_not_be_blank_message))
            }
        }
    }

}