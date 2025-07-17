package com.supernova.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.supernova.ui.navigation.NavRoutes
import com.supernova.ui.theme.SupernovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SupernovaTheme { AppNavHost() } }
    }
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = NavRoutes.Splash, modifier = modifier) {
        composable(NavRoutes.Splash) { SplashScreen(navController) }
        composable(NavRoutes.Config) { ConfigurationScreen(navController) }
        composable(NavRoutes.Loading) { LoadingScreen(navController) }
        composable(NavRoutes.ProfileCreation) { ProfileCreationNavWrapper(navController) }
        composable(NavRoutes.ProfileSelection) { ProfileSelectionNavWrapper(navController) }
        composable(NavRoutes.Home) { HomeScreen(navController) }
        composable(NavRoutes.Content) { ContentScreen(navController) }
    }
}
