package com.fardhani.smarthome

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.fardhani.smarthome.Adapter.ViewPagerAdapter
import com.fardhani.smarthome.Fragment.ChangePIN1Fragment
import com.fardhani.smarthome.Fragment.ChangePIN2Fragment
import com.fardhani.smarthome.Fragment.ChangePIN3Fragment
import kotlinx.android.synthetic.main.activity_change_pin.*

class ChangePINActivity : AppCompatActivity(), ChangePIN1Fragment.OnButtonClickedListener,
    ChangePIN2Fragment.OnButtonClickedListener1, ChangePIN3Fragment.OnButtonClickedListener2 {
    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "Change PIN"

        //declaration of viewpager
        viewPager = viewPagerChangePIN as ViewPager
        setupViewPager(viewPager!!)

        viewPager!!.setCurrentItem(0, true) //first page
    }

    //function to initiate viewpager
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ChangePIN1Fragment())
        adapter.addFragment(ChangePIN2Fragment())
        adapter.addFragment(ChangePIN3Fragment())
        viewPager.adapter = adapter
    }

    //on button clicked
    override fun onButtonClicked(view: View) {
        viewPager!!.setCurrentItem(1, true)
    }

    //cancel clicked on page 1
    override fun onButtonCancelClicked(view: View) {
        finish()
    }

    //on button page 2 clicked
    override fun onButtonClicked1(view: View, PIN: String) {
        var tag = "android:switcher:" + R.id.viewPagerChangePIN + ":" + 2
        var f: ChangePIN3Fragment =
            supportFragmentManager.findFragmentByTag(tag) as ChangePIN3Fragment
        f.setPIN(PIN)
        viewPager!!.setCurrentItem(2, true)
    }

    //on back button page 2 clicked
    override fun onButtonBackClicked1(view: View) {
        viewPager!!.setCurrentItem(0, true)
    }

    //on button page 3
    override fun onButtonClicked2(view: View) {
        finish()
    }

    //on back button page 3
    override fun onButtonBackClicked2(view: View) {
        viewPager!!.setCurrentItem(1, true)
    }
}
