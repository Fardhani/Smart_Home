package com.fardhani.smarthome.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fardhani.smarthome.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.change_pin_3.*
import kotlinx.android.synthetic.main.change_pin_3.view.*

class ChangePIN3Fragment : Fragment() {
    private lateinit var databaseReference: DatabaseReference
    private var PIN: String = ""
    private lateinit var onButtonClickedListener: OnButtonClickedListener2
    private lateinit var onButtonBackClickedListener: OnButtonClickedListener2

    interface OnButtonClickedListener2 {
        fun onButtonClicked2(view: View)
        fun onButtonBackClicked2(view: View)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onButtonClickedListener = context as OnButtonClickedListener2
        onButtonBackClickedListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.change_pin_3, container, false)
        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference
        view.btFinish.setOnClickListener {
            if (etConfirmPIN.text.toString() == PIN && PIN != "") {
                databaseReference.child("PIN").setValue(PIN)
                onButtonClickedListener.onButtonClicked2(it)
            } else {
                etConfirmPIN.setText("")
                etConfirmPIN.hint = "PIN must match with previous"
            }
        }
        view.btBack.setOnClickListener {
            onButtonBackClickedListener.onButtonBackClicked2(it)
        }
        return view
    }

    fun setPIN(PIN: String) {
        this.PIN = PIN
    }
}