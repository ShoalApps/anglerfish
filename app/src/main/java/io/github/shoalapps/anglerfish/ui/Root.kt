@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.shoalapps.anglerfish.ui

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.shoalapps.anglerfish.Graph

private data class Tab(val label: String, val icon: ImageVector, val url: String)

private val tabs = listOf(
    Tab("Home", Icons.Rounded.Home, "https://www.reddit.com/"),
    Tab("Popular", Icons.Rounded.Whatshot, "https://www.reddit.com/r/popular/"),
    Tab("Search", Icons.Rounded.Search, ""),
    Tab("Inbox", Icons.Rounded.Inbox, "https://www.reddit.com/notifications/"),
    Tab("You", Icons.Rounded.Person, "https://www.reddit.com/user/me/"),
)

/** Which tab a Reddit address belongs to, so the bar follows where you are. */
private fun tabFor(url: String): Int = when {
    url.contains("/r/popular") -> 1
    url.contains("/search") -> 2
    url.contains("/notifications") || url.contains("/message/") -> 3
    url.contains("/user/") || url.contains("/settings") -> 4
    else -> 0
}

@Composable
fun PufferRoot(link: String?, onLinkHandled: () -> Unit) {
    val context = LocalContext.current
    val settings by Graph.store.settings.collectAsState()
    val dark = when (settings.theme) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    val web = remember { RedditView.get(context, dark, settings.tidy) }
    var searching by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val selected = tabFor(RedditView.url)

    LaunchedEffect(link) { link?.let { RedditView.open(it); onLinkHandled() } }
    BackHandler(enabled = RedditView.canGoBack && !showSettings) { RedditView.back() }
    if (showSettings) { SettingsScreen(onBack = { showSettings = false }); return }

    fun go(i: Int) {
        if (i == 2) { searching = true; return }
        if (i == selected) RedditView.reload() else RedditView.open(tabs[i].url)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxSize()) {
            if (wide) {
                WideNavigationRail(header = {
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Rounded.Settings, "Settings") }
                }) {
                    tabs.forEachIndexed { i, t ->
                        WideNavigationRailItem(selected = selected == i, onClick = { go(i) }, icon = { Icon(t.icon, null) }, label = { Text(t.label) }, railExpanded = false)
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth().statusBarsPadding()) {
                    AndroidView(factory = { (web.parent as? ViewGroup)?.removeView(web); web }, modifier = Modifier.fillMaxSize())
                    if (RedditView.loading) LinearWavyProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
                    if (RedditView.blocked && !RedditView.signedIn) BlockedCard(Modifier.align(Alignment.Center))
                    if (!wide) {
                        // Phones: settings sit in a small button at the bottom corner, clear of Reddit's own header.
                        androidx.compose.material3.SmallFloatingActionButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) { Icon(Icons.Rounded.Settings, "Settings", Modifier.size(20.dp)) }
                    }
                }
                if (!wide) {
                    ShortNavigationBar {
                        tabs.forEachIndexed { i, t ->
                            ShortNavigationBarItem(selected = selected == i, onClick = { go(i) }, icon = { Icon(t.icon, null) }, label = { Text(t.label) })
                        }
                    }
                }
            }
        }
    }
    if (searching) SearchDialog(onDismiss = { searching = false }) { q ->
        searching = false
        RedditView.open(if (q.startsWith("r/") || q.startsWith("/r/")) "https://www.reddit.com/${q.trimStart('/')}" else "https://www.reddit.com/search/?q=" + android.net.Uri.encode(q))
    }
}

/** Reddit blocks signed-out visitors on some networks; say so plainly and offer Reddit's sign-in. */
@Composable
private fun BlockedCard(modifier: Modifier) {
    Surface(modifier.padding(24.dp).widthIn(max = 420.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 6.dp) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sign in to see Reddit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Reddit only shows itself to signed-in people on this network. Sign in on Reddit's own page and Anglerfish remembers you.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { RedditView.open("https://www.reddit.com/login/") }, modifier = Modifier.padding(top = 8.dp)) { Text("Sign in to Reddit") }
        }
    }
}

@Composable
private fun SearchDialog(onDismiss: () -> Unit, onSearch: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Reddit") },
        text = {
            OutlinedTextField(
                q, { q = it }, singleLine = true, placeholder = { Text("Posts, or r/community") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Rounded.Close, "Clear") } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (q.isNotBlank()) onSearch(q.trim()) }),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(enabled = q.isNotBlank(), onClick = { onSearch(q.trim()) }) { Text("Search") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
