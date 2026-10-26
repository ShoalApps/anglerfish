package io.github.shoalapps.anglerfish

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import io.github.shoalapps.anglerfish.ui.PufferRoot
import io.github.shoalapps.anglerfish.ui.PufferTheme
import io.github.shoalapps.anglerfish.ui.RedditView

class MainActivity : ComponentActivity() {
    /** A reddit.com link the app was opened with. */
    private val link = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Telemetry.askOnce(this)
        enableEdgeToEdge()
        link.value = redditLink(intent)
        setContent {
            val settings by Graph.store.settings.collectAsState()
            PufferTheme(settings) { PufferRoot(link.value) { link.value = null } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        link.value = redditLink(intent)
    }

    private fun redditLink(i: Intent?): String? {
        val text = i?.dataString ?: i?.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val url = Regex("""https?://\S+""").find(text)?.value ?: return null
        return url.takeIf { RedditView.owns(android.net.Uri.parse(it).host.orEmpty()) }
    }
}
