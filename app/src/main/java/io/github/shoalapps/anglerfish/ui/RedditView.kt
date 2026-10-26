package io.github.shoalapps.anglerfish.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Reddit's own website, signed in on Reddit's own page. Reddit's data API now needs Reddit's
 * approval, so this shows reddit.com the way a browser would. One view for the app's lifetime,
 * so switching tabs keeps your place.
 */
object RedditView {
    private var view: WebView? = null
    private val hosts = listOf("reddit.com", "redd.it", "redditmedia.com", "redditstatic.com", "redditinc.com")
    fun owns(host: String) = hosts.any { host == it || host.endsWith(".$it") }

    var loading by mutableStateOf(true); private set
    var canGoBack by mutableStateOf(false); private set
    var url by mutableStateOf("https://www.reddit.com/"); private set
    /** Reddit refuses signed-out visitors on some networks; the app then offers to sign in. */
    var blocked by mutableStateOf(false); private set

    val signedIn: Boolean get() = CookieManager.getInstance().getCookie("https://www.reddit.com")?.contains("reddit_session=") == true

    private class Bridge {
        @JavascriptInterface fun blocked(b: Boolean) { android.os.Handler(android.os.Looper.getMainLooper()).post { RedditView.blocked = b } }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun get(context: Context, dark: Boolean, tidy: Boolean): WebView = view ?: WebView(context.applicationContext).apply {
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) WebView.setWebContentsDebuggingEnabled(true)
        setBackgroundColor(if (dark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        // Reddit serves its full site to Chrome, a cut-down one to in-app browsers.
        settings.userAgentString = settings.userAgentString.replace("; wv)", ")").replace(Regex(" Version/\\d+\\.\\d+"), "")
        // The site follows the app's light or dark theme.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        addJavascriptInterface(Bridge(), "Anglerfish")
        val script = context.assets.open("reddit.js").bufferedReader().use { it.readText() }
        val early = tidy && WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
        if (early) WebViewCompat.addDocumentStartJavaScript(this, script, setOf("https://www.reddit.com", "https://reddit.com"))
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.scheme == "https" && owns(uri.host.orEmpty())) return false
                runCatching { view.context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                return true
            }
            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                loading = true
                if (tidy && !early) view.evaluateJavascript(script, null)
            }
            override fun onPageFinished(view: WebView, url: String?) {
                loading = false
                canGoBack = view.canGoBack()
                url?.let { RedditView.url = it }
                CookieManager.getInstance().flush()
                view.evaluateJavascript("Anglerfish.blocked(/blocked by network security/i.test(document.body&&document.body.innerText||''))", null)
            }
            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                canGoBack = view.canGoBack()
                url?.let { RedditView.url = it }
            }
        }
        webChromeClient = WebChromeClient()
        loadUrl("https://www.reddit.com/")
        view = this
    }

    fun open(url: String) { view?.loadUrl(url) }
    fun back() { view?.goBack() }
    fun reload() { view?.reload() }

    fun signOut() {
        val cm = CookieManager.getInstance()
        cm.getCookie("https://www.reddit.com")?.split(";")?.map { it.substringBefore("=").trim() }?.forEach { cm.setCookie("https://www.reddit.com", "$it=; Max-Age=0; Path=/; Domain=.reddit.com") }
        cm.flush()
        view?.loadUrl("https://www.reddit.com/")
    }
}
