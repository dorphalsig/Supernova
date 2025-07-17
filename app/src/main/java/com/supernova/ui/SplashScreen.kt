package com.supernova.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.supernova.data.database.SupernovaDatabase
import com.supernova.ui.navigation.NavRoutes
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val start = System.currentTimeMillis()
    LaunchedEffect(Unit) {
        val next = try {
            val configured = SecureDataStore.getBoolean(SecureStorageKeys.IS_CONFIGURED)
            val lastSuccess = SecureDataStore.getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)
            if (!configured || !lastSuccess) {
                NavRoutes.Config
            } else {
                val db = SupernovaDatabase.getDatabase(navController.context)
                val profiles = db.profileDao().getProfileCount()
                if (profiles == 0) NavRoutes.ProfileCreation else NavRoutes.ProfileSelection
            }
        } catch (_: Exception) {
            NavRoutes.Config
        }
        val remaining = 3000L - (System.currentTimeMillis() - start)
        if (remaining > 0) delay(remaining)
        navController.navigate(next) { popUpTo(NavRoutes.Splash) { inclusive = true } }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
