package com.fardhani.smarthome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.fardhani.smarthome.Adapter.ViewPagerAdapter
import com.fardhani.smarthome.Fragment.DashboardFragment
import com.fardhani.smarthome.Fragment.LocationFragment
import com.fardhani.smarthome.Fragment.SettingFragment
import com.fardhani.smarthome.Service.LocationService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var doubleBackButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //declaration of viewpager
        viewPager = containerFragment as ViewPager
        setupViewPager(viewPager!!)
        //listener for bottom nav
        bottomNavigation.setNavigationChangeListener { _, position ->
            //set content of viewpager
            when (position) {
                0 -> viewPager!!.setCurrentItem(0, true) //dashboard
                1 -> viewPager!!.setCurrentItem(1, true) //location
                2 -> viewPager!!.setCurrentItem(2, true) //setting
                else -> viewPager!!.setCurrentItem(0, true) //dashboard as default
            }
        }
    }

    //function to initiate viewpager
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(DashboardFragment())
        adapter.addFragment(LocationFragment())
        adapter.addFragment(SettingFragment())
        viewPager.adapter = adapter
    }

    //double back to close application
    override fun onBackPressed() {
        if (doubleBackButton) {
            this.finish()
        }

        doubleBackButton = true
        Toast.makeText(this.applicationContext, "Press again to close", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackButton = false }, 2000)
    }

    //start service
    override fun onStop() {
        startService(Intent(this, LocationService::class.java))
        super.onStop()
    }

    override fun onPause() {
        startService(Intent(this, LocationService::class.java))
        super.onPause()
    }

    //stop service
    override fun onResume() {
        stopService(Intent(this, LocationService::class.java))
        super.onResume()
    }
}
