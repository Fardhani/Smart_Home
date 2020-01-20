package com.fardhani.smarthome.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fardhani.smarthome.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.change_pin_1.view.*

class ChangePIN1Fragment : Fragment() {
    private lateinit var databaseReference: DatabaseReference
    private var PIN: String = ""
    private lateinit var onButtonClickedListener: OnButtonClickedListener
    private lateinit var onButtonCancelClickedListener: OnButtonClickedListener

    interface OnButtonClickedListener {
        fun onButtonClicked(view: View)
        fun onButtonCancelClicked(view: View)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onButtonClickedListener = context as OnButtonClickedListener
        onButtonCancelClickedListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.change_pin_1, container, false)
        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("PIN").addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        activity?.applicationContext!!,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    PIN = p0.value.toString()
                }
            }
        )
        view.btNext.setOnClickListener {
            if (view.etOldPIN.text.toString() == PIN && PIN != "") {
                onButtonClickedListener.onButtonClicked(it)
            } else {
                view.etOldPIN.setText("")
                view.etOldPIN.hint = "Wrong PIN, try again"
            }
        }
        view.btCancel.setOnClickListener {
            onButtonCancelClickedListener.onButtonCancelClicked(it)
        }
        return view
    }
}