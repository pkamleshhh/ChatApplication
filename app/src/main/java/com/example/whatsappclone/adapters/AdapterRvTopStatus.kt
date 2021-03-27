package com.example.whatsappclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.models.UserStatus
import kotlin.collections.ArrayList

class AdapterRvTopStatus(
    private val context: Context,
    private val usersStatusData: ArrayList<UserStatus> = ArrayList(),
) :
    RecyclerView.Adapter<AdapterRvTopStatus.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_status, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return usersStatusData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }


}