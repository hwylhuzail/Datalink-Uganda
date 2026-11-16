
package com.datalink.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

sealed class DataLinkScreen(val route: String) {
    data object Home : DataLinkScreen("home")
    data object Earn : DataLinkScreen("earn")
    data object Wifi : DataLinkScreen("wifi")
    data object Rewards : DataLinkScreen("rewards")
    data object Referrals : DataLinkScreen("referrals")
    data object Wallet : DataLinkScreen("wallet")
    data object Profile : DataLinkScreen("profile")
    data object Settings : DataLinkScreen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DataLinkScreen.Home.route
    ) {
        composable(DataLinkScreen.Home.route) {
            ScreenPlaceholder("Home", navController)
        }

        composable(DataLinkScreen.Earn.route) {
            ScreenPlaceholder("Earn Data", navController)
        }

        composable(DataLinkScreen.Wifi.route) {
            ScreenPlaceholder("Free Wi-Fi", navController)
        }

        composable(DataLinkScreen.Rewards.route) {
            ScreenPlaceholder("Daily Rewards", navController)
        }

        composable(DataLinkScreen.Referrals.route) {
            ScreenPlaceholder("Invite Friends", navController)
        }

        composable(DataLinkScreen.Wallet.route) {
            ScreenPlaceholder("My Wallet", navController)
        }

        composable(DataLinkScreen.Profile.route) {
            ScreenPlaceholder("My Profile", navController)
        }

        composable(DataLinkScreen.Settings.route) {
            ScreenPlaceholder("Settings", navController)
        }
    }
}

@Composable
fun ScreenPlaceholder(
    title: String,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                text = "$title screen",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Welcome to DataLink Uganda!")

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    navController.navigate(DataLinkScreen.Home.route) {
                        popUpTo(DataLinkScreen.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            ) {
                Text("Back to Home")
            }
        }
    }
}
