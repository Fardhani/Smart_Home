package com.fardhani.smarthome.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fardhani.smarthome.*
import kotlinx.android.synthetic.main.setting.view.*

class SettingFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.setting, container, false)
        //edit home profile
        view.editHomeProfile.setOnClickListener {
            startActivity(Intent(activity?.applicationContext!!, EditHomeProfileActivity::class.java))
        }
        //change home location
        view.changeHomeLocation.setOnClickListener {
            startActivity(Intent(activity?.applicationContext!!, ChangeHomeLocationActivity::class.java))
        }
        //registered key
        view.registeredKey.setOnClickListener {
            startActivity(Intent(activity?.applicationContext!!, RegisteredKeyActivity::class.java))
        }
        //change pin
        view.changePIN.setOnClickListener {
            startActivity(Intent(activity?.applicationContext!!, ChangePINActivity::class.java))
        }
        //about us
        view.layAboutUs.setOnClickListener {
            startActivity(Intent(activity?.applicationContext!!, AboutUsActivity::class.java))
        }
        return view
    }
}