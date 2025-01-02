package com.ai.app.move.deskercise.ui.pdfView

import android.os.Bundle
import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.databinding.ActivityWebViewBinding

class WebViewActivity : BaseBindingActivity<ActivityWebViewBinding>() {

    companion object {
        const val URL_LINK = "url-link"
    }

    override fun getViewBinding(): ActivityWebViewBinding {
        return ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urlLink : String? = intent.getStringExtra(URL_LINK)
        if (urlLink != null) {
            binding.webView.loadUrl(urlLink)
        }
    }
}