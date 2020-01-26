package com.fardhani.smarthome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "About Us"
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    //set action of back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
