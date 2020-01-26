package com.fardhani.smarthome.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fardhani.smarthome.Adapter.RecentActivityAdapter
import com.fardhani.smarthome.Fragment.Dialog.DialogPINFragment
import com.fardhani.smarthome.Model.RecentActivityModel
import com.fardhani.smarthome.Model.SecurityStatusModel
import com.fardhani.smarthome.R
import com.fardhani.smarthome.ShowAllActivity
import com.fardhani.smarthome.ViewModel.LocationViewModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.dashboard.view.*

class DashboardFragment : Fragment() {
    private lateinit var databaseReference: DatabaseReference
    private var isLocked: Boolean = true
    private var isClosed: Boolean = true
    private var isSecurityMode: Boolean = true
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.dashboard, container, false)

        //get distance from viewmodel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationData().observe(this, androidx.lifecycle.Observer {
            view.txtDistance.text = it.distance.toInt().toString() + " meters"
            if (it.distance < 99.0)
                view.txtDistance.setTextColor(resources.getColor(R.color.green))
            else
                view.txtDistance.setTextColor(resources.getColor(R.color.red))
        })

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference
        //check child "security_status" exist or not yet
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(
                    activity?.applicationContext!!,
                    "Error : $p0",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.hasChild("security_status")) {
                    setDB()
                }
            }
        })
        databaseReference.child("security_status")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        activity?.applicationContext!!,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    setStatus(
                        p0.child("locked").value as Boolean?,
                        p0.child("closed").value as Boolean?,
                        p0.child("securityMode").value as Boolean?
                    )
                    //check switch condition
                    switchMode(isSecurityMode, view.switchSecurityMode, view.txtSecurityModeStatus)
                    //check door status condition
                    checkDoorStatus(view.btLockDoor, view.txtDoorStatus)
                    //check door condition (closed or opened)
                    checkDoorCondition(view.txtDoorCondition)
                }
            })
        databaseReference.child("home_profile")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        activity?.applicationContext!!,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.txtAddress.text = p0.child("address").value.toString()
                    view.txtPostCode.text = p0.child("postal_code").value.toString()
                    view.txtLatitude.text = p0.child("lat").value.toString()
                    view.txtLongitude.text = p0.child("lng").value.toString()
                }
            })
        //initiate recent activity
        setRecentActivity(view.rvRecentActivity)
        //check switch condition
        switchMode(isSecurityMode, view.switchSecurityMode, view.txtSecurityModeStatus)
        //check door status condition
        checkDoorStatus(view.btLockDoor, view.txtDoorStatus)
        //check door condition (closed or opened)
        checkDoorCondition(view.txtDoorCondition)
        //listener for switch condition
        view.switchSecurityMode.setOnCheckedChangeListener { _, isChecked ->
            switchMode(isSecurityMode, view.switchSecurityMode, view.txtSecurityModeStatus)
            isSecurityMode = isChecked
            setDB()
        }
        //listener of clicked button lock door
        view.btLockDoor.setOnClickListener {
            //it can lock when door closed
            if (isClosed) {
                //when unlock, input PIN
                if (isLocked) {
                    //show dialog input PIN
                    DialogPINFragment().show(fragmentManager, "Input PIN")
                } else {
                    doorStatus(view.btLockDoor, view.txtDoorStatus)
                    isLocked = !isLocked
                    setDB()
                }
            } else {
                Toast.makeText(
                    activity?.applicationContext!!,
                    "The door still open, close it first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //show all recent activity
        view.txtShowAll.setOnClickListener {
            //start show all activity
            startActivity(Intent(activity?.applicationContext!!, ShowAllActivity::class.java))
        }

        //add recent activity
        setRecentActivity(view.rvRecentActivity)

        return view
    }

    //function to check door condition
    private fun checkDoorCondition(txtDoorCondition: TextView?) {
        if (isClosed) {
            txtDoorCondition!!.text = "CLOSED"
            txtDoorCondition!!.setTextColor(resources.getColor(R.color.green))
        } else {
            txtDoorCondition!!.text = "OPENED"
            txtDoorCondition!!.setTextColor(resources.getColor(R.color.red))
        }
    }

    //function to lock the door
    public fun setDoorUnlock() {
        isLocked = false
        FirebaseDatabase.getInstance().getReference("security_status").child("locked")
            .setValue(isLocked)
    }

    //get recent activity
    private fun setRecentActivity(recyclerView: RecyclerView) {
        databaseReference.child("activity").limitToLast(5)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        activity?.applicationContext!!,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //prepare the data from firebase
                    var listRecentActivity = ArrayList<RecentActivityModel>()
                    p0.children.forEach {
                        listRecentActivity.add(
                            RecentActivityModel(
                                it.child("activity").value.toString(),
                                it.child("time").value.toString(),
                                it.child("uid_name").value.toString()
                            )
                        )
                    }
                    //to get recent activity
                    listRecentActivity.reverse()
                    if (activity?.applicationContext != null) {
                        //recyclerview initiation
                        recyclerView.setHasFixedSize(true)
                        recyclerView.layoutManager =
                            LinearLayoutManager(activity?.applicationContext!!)
                        recyclerView.adapter = RecentActivityAdapter(listRecentActivity)
                    }
                }
            })
    }

    //function to set security mode on firebase
    private fun setDB() {
        val securityStatus = SecurityStatusModel(isLocked, isClosed, isSecurityMode)
        databaseReference.child("security_status").setValue(securityStatus)
    }

    //function set value
    private fun setStatus(isLocked: Boolean?, isClosed: Boolean?, isSecurityMode: Boolean?) {
        this.isLocked = isLocked!!
        this.isClosed = isClosed!!
        this.isSecurityMode = isSecurityMode!!
    }

    //function to check door status and synchronous button with textview
    private fun checkDoorStatus(button: Button, text: TextView) {
        //set text
        if (isLocked) {
            text.text = "LOCKED"
            text.setTextColor(resources.getColor(R.color.green))
        } else {
            text.text = "UNLOCKED"
            text.setTextColor(resources.getColor(R.color.red))
        }
        //check condition of door
        if (text.text == "LOCKED") { //locked door
            button.background = resources.getDrawable(R.drawable.icon_locked)
        } else if (text.text == "UNLOCKED") { //unlocked door
            button.background = resources.getDrawable(R.drawable.icon_unlock)
        }
    }

    //function to set listener button door status
    private fun doorStatus(button: Button, text: TextView) {
        //check condition of button
        if (text.text == "LOCKED") { //locked door
            text.text = "UNLOCKED"
            text.setTextColor(resources.getColor(R.color.red))
        } else if (text.text == "UNLOCKED") { //unlocked door
            text.text = "LOCKED"
            text.setTextColor(resources.getColor(R.color.green))
        }
        checkDoorStatus(button, text)
    }

    //function to set switch condition
    private fun switchMode(isChecked: Boolean, switch: Switch, switchText: TextView) {
        if (isChecked) {
            switchText.text = "ON"
            switchText.setTextColor(resources.getColor(R.color.green))
            switch.isChecked = isChecked
        } else {
            switchText.text = "OFF"
            switchText.setTextColor(resources.getColor(R.color.red))
            switch.isChecked = isChecked
        }
    }
}