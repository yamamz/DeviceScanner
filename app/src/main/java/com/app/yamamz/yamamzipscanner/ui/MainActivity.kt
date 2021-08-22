package com.app.yamamz.yamamzipscanner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.ui.details.DetailsScreen
import com.app.yamamz.yamamzipscanner.ui.home.HomeScreen
import com.google.accompanist.navigation.animation.navigation
import com.app.yamamz.yamamzipscanner.ui.theme.DeviceScannerTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            DeviceScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MyApp()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun MyApp() {
        val navController = rememberAnimatedNavController()
            AnimatedNavHost(navController = navController, startDestination = NavScreen.Home.route) {
                composable(NavScreen.Home.route,
                    enterTransition = { initial, _ ->
                        when (initial.destination.route) {
                            NavScreen.Details.route ->
                                slideInHorizontally(
                                    initialOffsetX = { 300 },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            else -> null
                        }
                    },
                    exitTransition = { _, target ->
                        when (target.destination.route) {
                            NavScreen.Details.route ->
                                slideOutHorizontally(
                                    targetOffsetX = { -300 },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            else -> null
                        }
                    },
                    popEnterTransition = { initial, _ ->
                        when (initial.destination.route) {
                            NavScreen.Details.route ->
                                slideInHorizontally(
                                    initialOffsetX = { -300 },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            else -> null
                        }
                    }
                ) {
                    HomeScreen() {
                        navController.navigate("${NavScreen.Details.route}/${it.ipAddress}/${it.macAddress}/${it.macVendor}/${it.deviceName}")
                    }
                }
                composable(
                    route = NavScreen.Details.routeWithArgument,
                    enterTransition = { initial, _ ->
                        when (initial.destination.route) {
                            NavScreen.Home.route ->
                                slideInHorizontally(
                                    initialOffsetX = { 300 },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            else -> null
                        }
                    },
                    exitTransition = { _, target ->
                        when (target.destination.route) {
                            NavScreen.Home.route->
                                slideOutHorizontally(
                                    targetOffsetX = { -300 },
                                    animationSpec = tween(1000)
                                ) + fadeOut(animationSpec = tween(1000))
                            else -> null
                        }
                    },
                    popExitTransition = { _, target ->
                        when (target.destination.route) {
                            NavScreen.Home.route ->
                                slideOutHorizontally(
                                    targetOffsetX = { 300 },
                                    animationSpec = tween(1000)
                                ) + fadeOut(animationSpec = tween(1000))
                            else -> null
                        }
                    },
                    arguments = listOf(
                        navArgument(NavScreen.Details.argument0) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val device = Device(ipAddress =  backStackEntry.arguments?.getString(NavScreen.Details.argument0)?:"",
                        macAddress = backStackEntry.arguments?.getString(NavScreen.Details.argument1)?:"",
                        macVendor = backStackEntry.arguments?.getString(NavScreen.Details.argument2)?:"",
                        deviceName = backStackEntry.arguments?.getString(NavScreen.Details.argument3)?:"",
                        isActive = true
                    )
                    DetailsScreen(device = device) {
                        navController.navigateUp()
                    }

            }
        }
    }

    sealed class NavScreen(val route: String) {

        object Home : NavScreen("Home")

        object Details : NavScreen("Details") {

            const val routeWithArgument: String = "Details/{ipAddress}/{mac}/{macVendor}/{deviceName}"

            const val argument0: String = "ipAddress"
            const val argument1: String = "mac"
            const val argument2: String = "macVendor"
            const val argument3: String = "deviceName"
        }
    }


}

