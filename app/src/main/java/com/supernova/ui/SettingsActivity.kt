package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.supernova.data.database.SupernovaDatabase
import com.supernova.ui.screens.SettingsScreen
import com.supernova.ui.screens.SettingsViewModel
import com.supernova.ui.screens.SettingsViewModelFactory
import com.supernova.ui.theme.SupernovaTheme
import com.supernova.utils.SecureDataStore
import com.supernova.work.SyncManager

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecureDataStore.init(this)
        val database = SupernovaDatabase.getDatabase(this)
        val viewModelFactory = SettingsViewModelFactory(database, SyncManager(this))
        val viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            SupernovaTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onProviderSettings = {
                        startActivity(Intent(this, ConfigurationActivity::class.java))
                    },
                    onProfiles = {
                        startActivity(Intent(this, ProfileSelectionActivity::class.java))
                    }
                )
            }
        }
    }
}
