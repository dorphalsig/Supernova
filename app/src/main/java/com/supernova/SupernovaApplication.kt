package com.supernova

import android.app.Application
import com.supernova.utils.SecureDataStore

class SupernovaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecureDataStore.init(this)
    }

}
