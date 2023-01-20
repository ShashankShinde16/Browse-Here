package com.shashankshinde.browsehere.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.shashankshinde.browsehere.R
import com.shashankshinde.browsehere.activity.MainActivity
import com.shashankshinde.browsehere.activity.changeTabs
import com.shashankshinde.browsehere.databinding.FragmentBrowseBinding
import java.io.ByteArrayOutputStream

class BrowseFragment(private var Url : String) : Fragment() {

    lateinit var binding: FragmentBrowseBinding
    var favicon: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        binding = FragmentBrowseBinding.bind(view)

        return view

    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        // to call existing download manager to download file
        binding.webView.setDownloadListener { url, _ , _ , _ , _ ->
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Url)))}

        // reference for MainActivity
        val mainActivityRefer = requireActivity() as MainActivity

        //To change the name of tab
        MainActivity.fragsList[MainActivity.myView.currentItem].name = binding.webView.url.toString()
        MainActivity.tabsBtn.text = MainActivity.fragsList.size.toString()

        mainActivityRefer.binding.homeBtn.visibility = View.VISIBLE
        mainActivityRefer.binding.refreshBtn.visibility = View.VISIBLE

        // refresh button working
        mainActivityRefer.binding.refreshBtn.setOnClickListener {
            binding.webView.reload()
        }

        //home button working
        mainActivityRefer.binding.homeBtn.setOnClickListener {
             changeTabs("Home", HomeFragment())
        }

        // to enable javaScript which is by default disable
        binding.webView.settings.javaScriptEnabled = true

        // zoom in , zoom out feature
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false

        // to load url in an app in place of other browser
        binding.webView.webViewClient = object : WebViewClient(){

            // working with desktop view
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                if (MainActivity.isDesktopView){
                    view?.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null);
                }
            }

            // it will set the changes in url in search field
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                mainActivityRefer.binding.topSearchField.text = SpannableStringBuilder(url)
                //or
                //mainActivityRefer.binding.topSearchField.setText(Url)

                MainActivity.fragsList[MainActivity.myView.currentItem].name = url.toString()
            }

            // progress bar visibility
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mainActivityRefer.binding.progressBar.progress = 0
                mainActivityRefer.binding.progressBar.visibility = View.VISIBLE
                // hiding toolbar for youtube
                if(url!!.contains("you", ignoreCase = false)){
                    mainActivityRefer.binding.root.transitionToEnd()
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mainActivityRefer.binding.progressBar.visibility = View.GONE
                view?.zoomOut()
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient(){

            //to set the favicon of website on search bar
            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                try {
                    mainActivityRefer.binding.webIC.setImageBitmap(icon)
                    favicon = icon

                    // to store website icon in model
                    MainActivity.bookmarkIndex = mainActivityRefer.isBookmark(view?.url!!)
                    if (MainActivity.bookmarkIndex != -1){
                        val array = ByteArrayOutputStream()
                        icon!!.compress(Bitmap.CompressFormat.PNG, 100, array)
                        MainActivity.bookmarkArray[MainActivity.bookmarkIndex].image = array.toByteArray()
                    }
                }catch (_: Exception){}
            }

            // full screen for youtube videos
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                binding.webView.visibility = View.GONE
                binding.customView.visibility = View.VISIBLE
                binding.customView.addView(view)
                // animation in custom view
                mainActivityRefer.binding.root.transitionToEnd()
            }
            override fun onHideCustomView() {
                super.onHideCustomView()
                binding.webView.visibility = View.VISIBLE
                binding.customView.visibility = View.GONE
            }
            // for progress bar
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                mainActivityRefer.binding.progressBar.progress = newProgress
            }
        }

        // load url
        when{
            URLUtil.isValidUrl(Url) -> binding.webView.loadUrl(Url)
            Url.contains(".com", ignoreCase = true) -> binding.webView.loadUrl(Url)
            else -> binding.webView.loadUrl("https://www.google.com/search?q=$Url")
        }

        // animation in browse fragment and it will also pass in main fragment
        binding.webView.setOnTouchListener { _, event ->
            mainActivityRefer.binding.root.onTouchEvent(event)
            return@setOnTouchListener false
        }
    }

    override fun onPause() {
        super.onPause()
        //calling save bookmark function of main Activity
        (requireActivity() as MainActivity).saveBookmark()
        // for clearing all data from webView
        binding.webView.apply {
            clearCache(true)
            clearHistory()
            clearMatches()
            clearFormData()
            clearSslPreferences()

            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
        }
    }

}