package com.shashankshinde.browsehere.adapter

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.shashankshinde.browsehere.R
import com.shashankshinde.browsehere.activity.MainActivity
import com.shashankshinde.browsehere.activity.changeTabs
import com.shashankshinde.browsehere.activity.checkForInternet
import com.shashankshinde.browsehere.databinding.BookmarkActivityFeatureBinding
import com.shashankshinde.browsehere.databinding.BookmarkFeatureBinding
import com.shashankshinde.browsehere.fragment.BrowseFragment

class BookmarkAdapter(private val context: Context, private val isActivity: Boolean = false): RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {

    private val color = context.resources.getIntArray(R.array.icColour)

    class MyHolder(binding: BookmarkFeatureBinding? = null, binding1: BookmarkActivityFeatureBinding? = null)
        : RecyclerView.ViewHolder((binding?.root ?: binding1?.root)!!) {
        val icon = (binding?.bookmarkIc ?: binding1?.bookmarkIc)!!
        val name = (binding?.bookmarkName ?: binding1?.bookmarkName)!!
        val root = (binding?.root ?: binding1?.root)!!
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        if (isActivity){
            return MyHolder(binding1 = BookmarkActivityFeatureBinding.inflate(LayoutInflater.from(context), parent, false))
        }
        return MyHolder(binding = BookmarkFeatureBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        try {
            val ic = BitmapFactory.decodeByteArray(MainActivity.bookmarkArray[position].image, 0,
            MainActivity.bookmarkArray[position].image!!.size)
            holder.icon.background = ic.toDrawable(context.resources)
        }catch (e: Exception){
            holder.icon.setBackgroundColor(color[(color.indices).random()])
            holder.icon.text = MainActivity.bookmarkArray[position].name[0].toString()
        }

        holder.name.text = MainActivity.bookmarkArray[position].name

        holder.root.setOnClickListener{
            when{
                checkForInternet(context) -> {
                    changeTabs(MainActivity.bookmarkArray[position].name,
                        BrowseFragment(Url = MainActivity.bookmarkArray[position].url))
                    if (isActivity) (context as Activity).finish()
                }

                else ->  Snackbar.make(holder.root, "Internet is not available", 3000).show()

            }
        }
    }

    override fun getItemCount(): Int {
        return MainActivity.bookmarkArray.size
    }

}