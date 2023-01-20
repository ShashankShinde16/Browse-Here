package com.shashankshinde.browsehere.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.shashankshinde.browsehere.R
import com.shashankshinde.browsehere.activity.MainActivity.Companion.myView
import com.shashankshinde.browsehere.activity.MainActivity.Companion.tabsBtn
import com.shashankshinde.browsehere.adapter.TabAdapter
import com.shashankshinde.browsehere.databinding.ActivityMainBinding
import com.shashankshinde.browsehere.databinding.BookmarkDialogBinding
import com.shashankshinde.browsehere.databinding.MoreToolsBinding
import com.shashankshinde.browsehere.databinding.TabViewBinding
import com.shashankshinde.browsehere.fragment.BrowseFragment
import com.shashankshinde.browsehere.fragment.HomeFragment
import com.shashankshinde.browsehere.model.Bookmark
import com.shashankshinde.browsehere.model.tabs
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var printJob: PrintJob? = null


    companion object{
        var fragsList: ArrayList<tabs> = ArrayList()
        private var isFullscreen: Boolean = true
        var isDesktopView : Boolean = false
        var bookmarkArray: ArrayList<Bookmark> = ArrayList()
        var bookmarkIndex: Int = -1
        lateinit var myView: ViewPager2
        lateinit var tabsBtn: MaterialTextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //splash screen
        setTheme(R.style.Theme_BrowseHere)

        // to cover the curve area of front camera for fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //calling get Bookmark function
        getAllBookmark()

        fragsList.add(tabs(name = "Home", HomeFragment()))
        binding.myView.adapter = FragAdapter(supportFragmentManager,lifecycle)
        binding.myView.isUserInputEnabled = false
        myView = binding.myView
        tabsBtn = binding.tabsBtn
        initializeView()
    }

    // back button working for load previous activity/view
    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {

        var frag: BrowseFragment? = null
        try {
            frag = fragsList[binding.myView.currentItem].fragment as BrowseFragment
        }catch (_:Exception){}

        when{
            frag?.binding?.webView?.canGoBack() == true -> frag.binding.webView.goBack()
            binding.myView.currentItem != 0 -> {
                fragsList.removeAt(binding.myView.currentItem)
                binding.myView.adapter?.notifyDataSetChanged()
                binding.myView.currentItem = fragsList.size - 1
            }
            else -> super.onBackPressed()
        }
    }

    // for fragment management
    private inner class FragAdapter(fa: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fa, lc) {
        override fun getItemCount(): Int = fragsList.size

        override fun createFragment(position: Int): Fragment = fragsList[position].fragment
    }


    private fun initializeView() {

        //alert dialog for tab button
        binding.tabsBtn.setOnClickListener {
            val tabView = layoutInflater.inflate(R.layout.tab_view, binding.root, false)
            val tabDialogBinding = TabViewBinding.bind(tabView)
            val tabDialog = MaterialAlertDialogBuilder(this, R.style.roundCornerDialog).setView(tabView)
                .setTitle("Select Tab")
                .setPositiveButton("Home"){self, _ ->
                    changeTabs("Home",HomeFragment())
                    self.dismiss()}
                .setNeutralButton("Google"){self, _ ->
                    changeTabs("Google",BrowseFragment(Url = "https://www.google.com/"))
                    self.dismiss()}
                .create()

            tabDialogBinding.rVForTab.setHasFixedSize(true)
            tabDialogBinding.rVForTab.layoutManager = LinearLayoutManager(this)
            tabDialogBinding.rVForTab.adapter = TabAdapter(this, tabDialog)

            tabDialog.show()

            val positiveButton = tabDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val neutralButton = tabDialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            positiveButton.isAllCaps = false
            neutralButton.isAllCaps = false

            positiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources,R.drawable.add_ic, theme),
                null,null,null)
            neutralButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources,R.drawable.home_ic, theme),
                null,null,null)
        }

        // more tools alert dialog
        binding.moreTools.setOnClickListener {
            var frag: BrowseFragment? = null
            try {
                frag = fragsList[binding.myView.currentItem].fragment as BrowseFragment
            } catch (_: Exception) {
            }

            val view = layoutInflater.inflate(R.layout.more_tools, binding.root, false)
            val dialogBinding = MoreToolsBinding.bind(view)
            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            // dialog attributes
            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
                setBackgroundDrawable(ColorDrawable(0xFFFFFFFF.toInt()))
            }
            dialog.show()

//            if (isFullscreen){
//                dialogBinding.fullscreenBtn.setIconTintResource(R.color.bright_blue)
//                dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this, R.color.bright_blue))
//            }

            frag?.let {
                bookmarkIndex = isBookmark(it.binding.webView.url!!)
                if (bookmarkIndex != -1){
                    dialogBinding.fullscreenBtn.setIconTintResource(R.color.bright_blue)
                    dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this, R.color.bright_blue))
                }
            }

            if (isDesktopView){
                dialogBinding.fullscreenBtn.setIconTintResource(R.color.bright_blue)
                dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this, R.color.bright_blue))
            }

            // back button working
            dialogBinding.backBtn.setOnClickListener{
                onBackPressed()
            }
            // forward button working
            dialogBinding.nextBtn.setOnClickListener {
                frag?.apply {
                    if(binding.webView.canGoForward()){
                        binding.webView.goForward()
                    }
                }
            }
            // save button working
            dialogBinding.saveAsPdf.setOnClickListener {
                dialog.dismiss()
                if (frag != null){
                    saveAsPdf(webView = frag.binding.webView)
                }else{
                    Snackbar.make(binding.root,"This is not webpage", 2000).show()
                }
            }
            // fullscreen button working
            dialogBinding.fullscreenBtn.setOnClickListener {
                isFullscreen = if (isFullscreen){
                    changeFullscreen(enable = false)
                    dialogBinding.fullscreenBtn.setIconTintResource(R.color.black)
                    dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this,
                        R.color.black
                    ))
                    false
                }else{
                    changeFullscreen(enable = true)
                    dialogBinding.fullscreenBtn.setIconTintResource(R.color.bright_blue)
                    dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this,
                        R.color.bright_blue
                    ))
                    true
                }
            }

            // desktop view button working
            dialogBinding.desktopBtn.setOnClickListener {

                frag?.binding?.webView?.apply {
                    isDesktopView = if (isDesktopView){
                        settings.userAgentString = null
                        dialogBinding.fullscreenBtn.setIconTintResource(R.color.black)
                        dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.black
                        ))
                        false
                    }else{
                        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
                        settings.useWideViewPort = true
                        evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null)
                        dialogBinding.fullscreenBtn.setIconTintResource(R.color.bright_blue)
                        dialogBinding.fullscreenBtn.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.bright_blue
                        ))
                        true
                    }
                    reload()
                    dialog.dismiss()
                }
            }

            // Bookmark button working
            dialogBinding.bookmarkBtn.setOnClickListener {
                frag?.let {
                    if (bookmarkIndex == -1){
                        val viewBookmark = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false)
                        val dialogBindingBookmark = BookmarkDialogBinding.bind(viewBookmark)
                        val dialogBookmark = MaterialAlertDialogBuilder(this)
                            .setTitle("Add Bookmark")
                            .setMessage("Url:${it.binding.webView.url}")
                            .setPositiveButton("Add"){self, _ ->
                                try {
                                    val array = ByteArrayOutputStream()
                                    it.favicon?.compress(Bitmap.CompressFormat.PNG, 100, array)
                                    bookmarkArray.add(Bookmark(name = dialogBindingBookmark.bookmarkValue.text.toString(), url = it.binding.webView.url!!, array.toByteArray()))
                                }catch (e: Exception){
                                    bookmarkArray.add(Bookmark(name = dialogBindingBookmark.bookmarkValue.text.toString(), url = it.binding.webView.url!!))
                                }
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self, _ -> self.dismiss()}
                            .setView(viewBookmark).create()
                        dialogBookmark.show()
                        dialogBindingBookmark.bookmarkValue.setText(it.binding.webView.url!!)
                    }else{
                        val dialogBookmark = MaterialAlertDialogBuilder(this)
                            .setTitle("Remove Bookmark")
                            .setMessage("Url: ${it.binding.webView.url}")
                            .setPositiveButton("Remove"){self, _ ->
                                bookmarkArray.removeAt(bookmarkIndex)
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self, _ -> self.dismiss()}
                            .create()
                        dialogBookmark.show()
                    }
                }
                dialog.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // let is use for not null condition
        printJob?.let {
            when{
                it.isCompleted -> Snackbar.make(binding.root, "Successful -> ${it.info.label}", 2000).show()
                it.isFailed -> Snackbar.make(binding.root, "Unsuccessful -> ${it.info.label}", 2000).show()
            }
        }
    }

    // function store pdf of web with format
    private fun saveAsPdf(webView: WebView){
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${URL(webView.url).host}_${
            SimpleDateFormat("HH:mm, d_MMM_yy", Locale.ENGLISH)
            .format(Calendar.getInstance().time)}"
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        val printAttributes = PrintAttributes.Builder()
        printJob = printManager.print(jobName, printAdapter, printAttributes.build())
    }

    // function for changing the window in fullscreen/exit from fullscreen
    private fun changeFullscreen(enable: Boolean){
        if (enable){
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { Controller ->
                Controller.hide(WindowInsetsCompat.Type.systemBars())
                Controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
            }
        }else{
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // check for bookmark is present or not
    fun isBookmark(url: String): Int{
        bookmarkArray.forEachIndexed{index, bookmark ->
            if (bookmark.url == url) return index
        }
        return -1
    }

    // for storing bookmarks using shared preference
    fun saveBookmark(){
        val add = getSharedPreferences("BOOKMARK", MODE_PRIVATE).edit()
        val data = GsonBuilder().create().toJson(bookmarkArray)
        add.putString("bookmarkArray", data)
        add.apply()
    }

    // for getting bookmarks using shared preference from storage
    private fun getAllBookmark(){
        bookmarkArray = ArrayList()
        val add = getSharedPreferences("BOOKMARK", MODE_PRIVATE)
        val data = add.getString("bookmarkArray", null)

        if (data != null){
            val list: ArrayList<Bookmark> = GsonBuilder().create().fromJson(data, object : TypeToken<kotlin.collections.ArrayList<Bookmark>>(){}.type)
            bookmarkArray.addAll(list)
        }
    }
}

// function to change tab
@SuppressLint("NotifyDataSetChanged")
fun changeTabs(url: String, fragment: Fragment){
    MainActivity.fragsList.add(tabs(name = url,fragment = fragment))
    myView.adapter?.notifyDataSetChanged()
    myView.currentItem = MainActivity.fragsList.size - 1
    //tabs count work
    tabsBtn.text = MainActivity.fragsList.size.toString()
}

// to check internet connection
fun checkForInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}