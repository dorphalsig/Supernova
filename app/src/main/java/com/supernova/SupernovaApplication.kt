package com.supernova

import android.app.Application
import com.supernova.utils.SecureDataStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SupernovaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecureDataStore.init(this)
    }

}
