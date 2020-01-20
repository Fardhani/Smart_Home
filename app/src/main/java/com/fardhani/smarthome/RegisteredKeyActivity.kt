package com.fardhani.smarthome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fardhani.smarthome.Adapter.KeyAdapter
import com.fardhani.smarthome.Adapter.RecentActivityAdapter
import com.fardhani.smarthome.Model.KeyModel
import com.fardhani.smarthome.Model.RecentActivityModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_registered_key.*

class RegisteredKeyActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registered_key)

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "Registered Key"
        actionBar.setDisplayHomeAsUpEnabled(true)

        setRegisteredKey(rvRegisteredKey)
    }

    //get recent activity
    private fun setRegisteredKey(recyclerView: RecyclerView) {
        databaseReference.child("key")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        this@RegisteredKeyActivity,
                        "Error : $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //prepare the data from firebase
                    var listKey = ArrayList<KeyModel>()
                    p0.children.forEach {
                        listKey.add(
                            KeyModel(
                                it.key.toString(),
                                it.child("name").value.toString()
                            )
                        )
                    }
                    //recyclerview initiation
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(this@RegisteredKeyActivity)
                    recyclerView.adapter = KeyAdapter(listKey)
                }
            })
    }

    //set action of back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
