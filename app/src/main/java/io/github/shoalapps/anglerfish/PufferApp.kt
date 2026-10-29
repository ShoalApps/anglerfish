package io.github.shoalapps.anglerfish

import android.app.Application
import io.github.shoalapps.anglerfish.data.Store

class PufferApp : Application() {
    override fun attachBaseContext(base: android.content.Context) { super.attachBaseContext(AppLanguage.wrap(base)) }
    override fun onCreate() {
        super.onCreate()
        AppLanguage.init(this)
        Telemetry.init(this)
        Graph.app = this
    }
}

object Graph {
    lateinit var app: Application
    val store by lazy { Store(app) }
}
