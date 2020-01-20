package com.fardhani.smarthome.Fragment.Dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.fardhani.smarthome.Fragment.DashboardFragment
import com.fardhani.smarthome.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.dialog_input_pin.view.*

class DialogPINFragment : DialogFragment() {
    private lateinit var databaseReference: DatabaseReference
    private var PIN: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
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
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater.inflate(R.layout.dialog_input_pin, null)
            inflater.btConfirm.setOnClickListener {
                if (inflater.etInputPIN.text.toString() == PIN && inflater.etInputPIN.text.toString() != "") {
                    //add PIN auth before do this
                    DashboardFragment().setDoorUnlock()
                    dialog.dismiss()
                } else {
                    inflater.etInputPIN.setText("")
                    inflater.etInputPIN.hint = "Wrong PIN, try again"
                }
            }
            inflater.btCancel.setOnClickListener {
                dialog.cancel()
            }

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}