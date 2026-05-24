package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.QuickBiteRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.QuickBiteViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = QuickBiteRepository.getInstance(applicationContext)
        val viewModel: QuickBiteViewModel by viewModels {
            QuickBiteViewModel.Factory(applicationContext, repository)
        }

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContainer(viewModel) {
                    // Trigger Intent to AuthActivity on Logout
                    val intent = Intent(this@MainActivity, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: QuickBiteViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tab routes identifying bottom-bar visibility
    val bottomNavRoutes = listOf("dashboard", "orders", "profile")
    val shouldShowBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(if (currentRoute == "dashboard") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "orders",
                        onClick = {
                            navController.navigate("orders") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(if (currentRoute == "orders") Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(if (currentRoute == "profile") Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard (Home)
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRestaurant = { restId ->
                        navController.navigate("restaurantDetail/$restId")
                    },
                    onNavigateToCart = {
                        navController.navigate("cart")
                    }
                )
            }

            // My Orders Screen
            composable("orders") {
                MyOrdersScreen(
                    viewModel = viewModel,
                    onTrackOrder = { orderId ->
                        navController.navigate("trackOrder/$orderId")
                    },
                    onRateOrder = { orderId ->
                        navController.navigate("rateOrder/$orderId")
                    }
                )
            }

            // User Profile Screen
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.logoutUser()
                        onLogout()
                    }
                )
            }

            // Restaurant Detail Screen (Dynamic ID argument)
            composable(
                route = "restaurantDetail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: "spice-grill"
                RestaurantDetailScreen(
                    viewModel = viewModel,
                    restaurantId = id,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToCart = { navController.navigate("cart") }
                )
            }

            // Cart Screen
            composable("cart") {
                CartScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToCheckout = { navController.navigate("checkout") }
                )
            }

            // Checkout Screen
            composable("checkout") {
                CheckoutScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToPlaced = { orderId ->
                        navController.navigate("placed/$orderId") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                )
            }

            // Order Placed Success Screen
            composable(
                route = "placed/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "QB-XXXX"
                OrderPlacedScreen(
                    orderId = orderId,
                    onTrackOrder = {
                        navController.navigate("trackOrder/$orderId") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onBackHome = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }

            // Track Order Screen
            composable(
                route = "trackOrder/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "QB-XXXX"
                TrackOrderScreen(
                    viewModel = viewModel,
                    orderId = orderId,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // Rate Order Rating star ratings and feedback Screen
            composable(
                route = "rateOrder/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "QB-XXXX"
                RateOrderScreen(
                    viewModel = viewModel,
                    orderId = orderId,
                    onFinish = {
                        navController.navigate("orders") {
                            popUpTo("orders") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
