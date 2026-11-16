
package com.datalink.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

sealed class DataLinkScreen(
    val route: String,
    val label: String
) {
    data object Home : DataLinkScreen("home", "Home")
    data object Earn : DataLinkScreen("earn", "Earn")
    data object Wifi : DataLinkScreen("wifi", "Wi-Fi")
    data object Rewards : DataLinkScreen("rewards", "Rewards")
    data object Referrals : DataLinkScreen("referrals", "Referrals")
    data object Wallet : DataLinkScreen("wallet", "Wallet")
    data object Profile : DataLinkScreen("profile", "Profile")
    data object Settings : DataLinkScreen("settings", "Settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomScreens = listOf(
        DataLinkScreen.Home,
        DataLinkScreen.Earn,
        DataLinkScreen.Wifi,
        DataLinkScreen.Wallet
    )

    val backStackEntry by
        navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(
                                    DataLinkScreen.Home.route
                                ) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(
                                when (screen) {
                                    DataLinkScreen.Home -> "⌂"
                                    DataLinkScreen.Earn -> "＋"
                                    DataLinkScreen.Wifi -> "◉"
                                    else -> "▣"
                                }
                            )
                        },
                        label = {
                            Text(screen.label)
                        }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = DataLinkScreen.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(DataLinkScreen.Home.route) {
                ScreenPlaceholder("Home", navController)
            }

            composable(DataLinkScreen.Earn.route) {
                ScreenPlaceholder("Earn Free Data", navController)
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
}

@Composable
fun ScreenPlaceholder(
    title: String,
    navController: androidx.navigation.NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text("Welcome to DataLink Uganda!")

        Spacer(Modifier.height(20.dp))

        val otherScreens = listOf(
            DataLinkScreen.Rewards,
            DataLinkScreen.Referrals,
            DataLinkScreen.Profile,
            DataLinkScreen.Settings
        )

        if (title == "Home") {
            otherScreens.forEach { screen ->
                OutlinedButton(
                    onClick = {
                        navController.navigate(screen.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(screen.label)
                }
                Spacer(Modifier.height(8.dp))
            }
        } else {
            Button(
                onClick = {
                    navController.navigate(DataLinkScreen.Home.route) {
                        popUpTo(DataLinkScreen.Home.route) {
                            inclusive = false
                        }
                    }
                }
            ) {
                Text("Back to Home")
            }
        }
    }
}
