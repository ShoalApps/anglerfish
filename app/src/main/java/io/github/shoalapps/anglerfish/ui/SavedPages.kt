package io.github.shoalapps.anglerfish.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.CookieManager
import io.github.shoalapps.anglerfish.Graph
import java.io.File
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class SavedPage(val url: String, val title: String, val file: String)
object SavedPages {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs get() = Graph.app.getSharedPreferences("saved-pages", android.content.Context.MODE_PRIVATE)
    private val root get() = File(Graph.app.filesDir, "saved-pages").apply { mkdirs() }
    private fun hash(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    val onlineState = kotlinx.coroutines.flow.MutableStateFlow(false)
    fun changed() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onlineState.value = online()
            RedditView.offlinePolicy()
        }
    }
    fun init() {
        changed()
        Graph.app.getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: android.net.Network, caps: NetworkCapabilities) = changed()
            override fun onLost(network: android.net.Network) = changed()
        })
    }
    fun owner(): String = hash(CookieManager.getInstance().getCookie("https://www.reddit.com").orEmpty().split(';')
        .firstOrNull { it.trim().startsWith("reddit_session=") }?.trim() ?: "signed-out")
    fun online(): Boolean {
        val cm = Graph.app.getSystemService(ConnectivityManager::class.java)
        return !Graph.store.settings.value.offlineMode && cm?.getNetworkCapabilities(cm.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }
    fun list(owner: String = owner()): List<SavedPage> = runCatching {
        json.decodeFromString<List<SavedPage>>(prefs.getString(owner, "[]")!!)
    }.getOrDefault(emptyList()).filter { File(root, it.file).isFile }
    fun destination(url: String, owner: String) = File(root, hash(owner + url) + ".mht")
    @Synchronized fun remember(owner: String, page: SavedPage) {
        val pages = (list(owner).filterNot { it.url == page.url } + page).toMutableList()
        var bytes = pages.sumOf { File(root, it.file).length() }
        while (pages.size > 50 || bytes > 100_000_000 && pages.size > 1) {
            val removed = pages.removeAt(0)
            val old = File(root, removed.file); bytes -= old.length(); old.delete()
        }
        check(prefs.edit().putString(owner, json.encodeToString(pages)).commit())
    }
    fun file(page: SavedPage): File? = File(root, page.file).takeIf { it.isFile && it.canonicalFile.parentFile == root.canonicalFile }
    @Synchronized fun remove(page: SavedPage) {
        prefs.edit().putString(owner(), json.encodeToString(list().filterNot { it.url == page.url })).commit()
        file(page)?.delete()
    }
    fun draft(url: String) = prefs.getString("draft:" + owner() + ":" + url, "").orEmpty()
    fun saveDraft(url: String, text: String) {
        check(prefs.edit().putString("draft:" + owner() + ":" + url, text).commit())
    }
}
