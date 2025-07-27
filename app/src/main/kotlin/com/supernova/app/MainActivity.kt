package com.supernova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.supernova.ui.navigation.SupernovaDestinations
import com.supernova.ui.theme.SupernovaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SupernovaApp() }
    }
}

@Composable
fun SupernovaApp() {
    SupernovaTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SupernovaDestinations.Home.route
        ) {
            composable(SupernovaDestinations.Home.route) { /*TODO Home Screen*/ }
            composable(SupernovaDestinations.Details.route) { /*TODO Details Screen*/ }
        }
    }
}
