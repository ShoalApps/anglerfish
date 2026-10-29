package io.github.shoalapps.anglerfish.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Settings(
    /** 0 = follow the phone, 1 = light, 2 = dark */
    val theme: Int = 0,
    val offlineMode: Boolean = false,
    val dynamicColor: Boolean = true,
    /** Hide Reddit's open-the-app prompts and banners. */
    val tidy: Boolean = true,
)

class Store(context: Context) {
    private val prefs = context.getSharedPreferences("anglerfish", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _settings = MutableStateFlow(prefs.getString("settings", null)?.let { runCatching { json.decodeFromString<Settings>(it) }.getOrNull() } ?: Settings())
    val settings: StateFlow<Settings> = _settings

    fun updateSettings(change: (Settings) -> Settings) {
        _settings.update(change)
        io.github.shoalapps.anglerfish.ui.SavedPages.changed()
        prefs.edit().putString("settings", json.encodeToString(Settings.serializer(), _settings.value)).apply()
    }
}
