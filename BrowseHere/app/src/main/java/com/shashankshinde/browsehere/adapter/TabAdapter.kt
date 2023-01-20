package com.shashankshinde.browsehere.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.shashankshinde.browsehere.activity.MainActivity
import com.shashankshinde.browsehere.databinding.TabFeatureBinding

class TabAdapter(private val context: Context, private val dialog: AlertDialog): RecyclerView.Adapter<TabAdapter.MyHolder>() {

    class MyHolder(binding: TabFeatureBinding) : RecyclerView.ViewHolder(binding.root) {
        val cancelBtn = binding.cancelBtn
        val name = binding.tabName
        val root = binding.root
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(TabFeatureBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = MainActivity.fragsList[position].name
        holder.cancelBtn.setOnClickListener {
            MainActivity.fragsList.removeAt(position)
            notifyDataSetChanged()
            MainActivity.myView.adapter?.notifyItemRemoved(position)
        }
        holder.root.setOnClickListener {
            MainActivity.myView.currentItem = position
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return MainActivity.fragsList.size
    }

}