package com.sflin.transitiondemo

import android.app.Application
import android.content.Context

class MyApp : Application() {
    companion object {
        var application: Application? = null
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }
}