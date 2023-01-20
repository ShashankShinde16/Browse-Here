package com.shashankshinde.browsehere.fragment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.shashankshinde.browsehere.R
import com.shashankshinde.browsehere.activity.BookmarkActivity
import com.shashankshinde.browsehere.activity.MainActivity
import com.shashankshinde.browsehere.activity.changeTabs
import com.shashankshinde.browsehere.activity.checkForInternet
import com.shashankshinde.browsehere.adapter.BookmarkAdapter
import com.shashankshinde.browsehere.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()

        val mainActivityRefer = requireActivity() as MainActivity

        MainActivity.tabsBtn.text = MainActivity.fragsList.size.toString()
        //To change the name of tab
        MainActivity.fragsList[MainActivity.myView.currentItem].name = "Home"

        // to set search bar and top search bar null
        mainActivityRefer.binding.topSearchField.text = SpannableStringBuilder("")
        binding.searchView.setQuery("", false)
        mainActivityRefer.binding.webIC.setImageBitmap(null)

        mainActivityRefer.binding.refreshBtn.visibility = View.GONE
        mainActivityRefer.binding.homeBtn.visibility = View.GONE

        // search bar
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (checkForInternet(requireContext())){
                    // query!! for not nullable string
                    changeTabs(query!!, BrowseFragment(query))
                }else{
                    Snackbar.make(binding.root, "Internet is not available", 3000).show()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        // top search bar initialization
        mainActivityRefer.binding.topSearchButton.setOnClickListener{
            if (checkForInternet(requireContext())){
                changeTabs(mainActivityRefer.binding.topSearchField.text.toString(),
                    BrowseFragment(mainActivityRefer.binding.topSearchField.text.toString())
                )
            }else{
                Snackbar.make(binding.root, "Internet is not available", 3000).show()
            }

        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerView.adapter = BookmarkAdapter(requireContext())

        if (MainActivity.bookmarkArray.size < 1){
            binding.viewAllBtn.visibility = View.GONE
        }
        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(), BookmarkActivity::class.java))
        }
    }

}