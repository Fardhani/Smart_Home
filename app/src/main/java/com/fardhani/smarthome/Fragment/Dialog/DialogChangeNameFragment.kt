package com.fardhani.smarthome.Fragment.Dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.fardhani.smarthome.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_edit_name.view.*
import kotlinx.android.synthetic.main.dialog_input_pin.view.*
import kotlinx.android.synthetic.main.dialog_input_pin.view.btCancel
import kotlinx.android.synthetic.main.dialog_input_pin.view.btConfirm

class DialogChangeNameFragment(private val uid: String) : DialogFragment() {
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_name, null)
            //initiate database reference
            databaseReference = FirebaseDatabase.getInstance().reference
            inflater.txtUID.text = "UID : " + uid
            inflater.btConfirm.setOnClickListener {
                if (inflater.etName.text.toString() != "") {
                    //save name
                    databaseReference.child("key").child(uid).child("name")
                        .setValue(inflater.etName.text.toString())
                    inflater.etName.setText("")
                    dialog.dismiss()
                } else {
                    inflater.etInputPIN.setText("")
                    inflater.etInputPIN.hint = "Name cannot null"
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