package com.fardhani.smarthome.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fardhani.smarthome.Fragment.Dialog.DialogChangeNameFragment
import com.fardhani.smarthome.Model.KeyModel
import com.fardhani.smarthome.R
import com.fardhani.smarthome.RegisteredKeyActivity
import kotlinx.android.synthetic.main.item_registered_key.view.*

//adapter for registered key
class KeyAdapter(private val keys: ArrayList<KeyModel>) :
    RecyclerView.Adapter<KeyAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_registered_key, parent, false)
        )
    }

    override fun getItemCount(): Int = keys.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(keys[position])
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(keyModel: KeyModel) {
            with(itemView) {
                txtUID.text = keyModel.uid
                txtName.text = keyModel.name
                btEdit.setOnClickListener {
                    val activity = it.context as? RegisteredKeyActivity
                    DialogChangeNameFragment(keyModel.uid.toString()).show(activity?.supportFragmentManager, "Change Name")
                }
            }
        }
    }
}