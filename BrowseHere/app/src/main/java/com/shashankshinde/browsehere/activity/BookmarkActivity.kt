package com.shashankshinde.browsehere.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashankshinde.browsehere.adapter.BookmarkAdapter
import com.shashankshinde.browsehere.databinding.ActivityBookmarkBinding

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rVForBookmarkActivity.setHasFixedSize(true)
        binding.rVForBookmarkActivity.setItemViewCacheSize(5)
        binding.rVForBookmarkActivity.layoutManager = LinearLayoutManager(this)
        binding.rVForBookmarkActivity.adapter = BookmarkAdapter(this, isActivity = true)
    }
}