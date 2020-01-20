package com.fardhani.smarthome.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fardhani.smarthome.R
import kotlinx.android.synthetic.main.change_pin_1.view.btNext
import kotlinx.android.synthetic.main.change_pin_2.*
import kotlinx.android.synthetic.main.change_pin_2.view.*

class ChangePIN2Fragment : Fragment() {
    private lateinit var onButtonClickedListener: OnButtonClickedListener1
    private lateinit var onButtonBackClickedListener: OnButtonClickedListener1

    interface OnButtonClickedListener1 {
        fun onButtonClicked1(view: View, PIN: String)
        fun onButtonBackClicked1(view: View)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onButtonClickedListener = context as OnButtonClickedListener1
        onButtonBackClickedListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.change_pin_2, container, false)
        view.btNext.setOnClickListener {
            if (newPIN.text.toString().length == 6) {
                onButtonClickedListener.onButtonClicked1(it, newPIN.text.toString())
            } else {
                newPIN.setText("")
                newPIN.hint = "PIN must in 6 character"
            }
        }
        view.btBack.setOnClickListener {
            onButtonBackClickedListener.onButtonBackClicked1(it)
        }
        return view
    }
}