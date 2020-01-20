package com.fardhani.smarthome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.fardhani.smarthome.Model.HomeProfileModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_edit_home_profile.*

class EditHomeProfileActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var homeProfileModel: HomeProfileModel
    private lateinit var lat: String
    private lateinit var lng: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_home_profile)

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("home_profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        this@EditHomeProfileActivity,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    etAddress.setText(p0.child("address").value.toString())
                    etPostalCode.setText(p0.child("postal_code").value.toString())
                    lat = p0.child("lat").value.toString()
                    lng = p0.child("lng").value.toString()
                }
            })

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "Edit Home Proile"
        actionBar.setDisplayHomeAsUpEnabled(true)

        //save button
        btSave.setOnClickListener {
            if (etAddress.text.toString() != "" && etPostalCode.text.toString() != "" && lat != "" && lng != "") {
                homeProfileModel =
                    HomeProfileModel(
                        etAddress.text.toString(),
                        etPostalCode.text.toString(),
                        lat,
                        lng
                    )
                databaseReference.child("home_profile").setValue(homeProfileModel)
                onBackPressed()
            } else {
                Toast.makeText(
                    this,
                    "Fill your form correctly",
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    //set action of back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
