package com.fardhani.smarthome

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fardhani.smarthome.Adapter.RecentActivityAdapter
import com.fardhani.smarthome.Model.RecentActivityModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_show_all.*

class ShowAllActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all)

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "All Recent Activity"
        actionBar.setDisplayHomeAsUpEnabled(true)

        //recyclerview
        setRecentActivity(rvShowAll)
    }

    //get recent activity
    private fun setRecentActivity(recyclerView: RecyclerView) {
        databaseReference.child("activity")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(
                        this@ShowAllActivity,
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
                    //recyclerview initiation
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(this@ShowAllActivity)
                    recyclerView.adapter = RecentActivityAdapter(listRecentActivity)
                }
            })
    }

    //set action of back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
