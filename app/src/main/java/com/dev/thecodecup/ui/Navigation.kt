package com.dev.thecodecup.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dev.thecodecup.model.data.coffeeList
import com.dev.thecodecup.model.db.cart.CartViewModel
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.model.db.user.UserViewModel
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel
import com.dev.thecodecup.auth.AuthViewModel
import com.dev.thecodecup.ui.screens.CartScreen
import com.dev.thecodecup.ui.screens.CoffeeDetailScreen
import com.dev.thecodecup.ui.screens.HomeScreen
import com.dev.thecodecup.ui.screens.LoginScreen
import com.dev.thecodecup.ui.screens.OrderScreen
import com.dev.thecodecup.ui.screens.OrderSuccessScreen
import com.dev.thecodecup.ui.screens.ProductDetailScreen
import com.dev.thecodecup.ui.screens.ProfileScreen
import com.dev.thecodecup.ui.screens.RedeemScreen
import com.dev.thecodecup.ui.screens.RewardScreen
import com.dev.thecodecup.ui.screens.SplashScreen
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

@Composable
fun NavGraph(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    
    LaunchedEffect(Unit) {
        userViewModel.ensureDefaultUserExists()
    }
    val cartViewModel: CartViewModel = viewModel()
    val cartItems = cartViewModel.cartItems.collectAsState().value

    val user = userViewModel.user.collectAsState().value
    val authState by authViewModel.authState.collectAsState()
    
    NavHost(navController, startDestination = "splash") {
        composable("splash") {
            userViewModel.ensureDefaultUserExists()
            SplashScreen(onSplashFinished = {
                // Check if user is signed in
                if (authState.isSignedIn) {
                    navController.navigate("home") {
                        popUpTo("splash") {inclusive = true}
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") {inclusive = true}
                    }
                }
            })
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("rewards") {
            RewardScreen(
                orderViewModel = orderViewModel,
                userViewModel = userViewModel,
                onNavClick = { destination ->
                    when (destination) {
                        "home" -> navController.navigate("home")
                        "orders" -> navController.navigate("orders")
                        "redeem" -> navController.navigate("redeem")
                    }
                }
            )
        }

        composable("redeem") {
            RedeemScreen(
                orderViewModel = orderViewModel,
                userViewModel = userViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("orders") {
            OrderScreen(
                orderViewModel = orderViewModel,
                onNavClick = { destination ->
                    when (destination) {
                        "rewards" -> navController.navigate("rewards")
                        "home" -> navController.navigate("home")
                    }
                }
            )
        }

        composable("home") {
            var showEmptyCartDialog by remember { mutableStateOf(false) }
            HomeScreen(
                userViewModel = userViewModel,
                productViewModel = productViewModel,
                coffeeList = coffeeList,
                onCoffeeClick = {
                    navController.navigate("coffee-detail/${it.id}")
                },
                onProductClick = { product ->
                    navController.navigate("product-detail/${product.productId}")
                },
                onNavClick = {
                    when (it) {
                        "cart" -> navController.navigate("cart")
                        "profile" -> navController.navigate("profile")
                        "rewards" -> navController.navigate("rewards")
                        "orders" -> navController.navigate("orders")
                    }
                },
                showEmptyCartDialog = showEmptyCartDialog,
                onDismissEmptyCartDialog = { showEmptyCartDialog = false }
            )
            val navBackStackEntry = navController.currentBackStackEntry
            val fromCart = navBackStackEntry?.savedStateHandle?.get<Boolean>("showEmptyCartDialog") == true
            if (fromCart) {
                showEmptyCartDialog = true
                navBackStackEntry.savedStateHandle["showEmptyCartDialog"] = false
            }
        }

        composable("coffee-detail/{coffeeId}") { backStackEntry ->
            val coffeeId = backStackEntry.arguments?.getString("coffeeId")
            val coffeeItem = coffeeList.firstOrNull { it.id.toString() == coffeeId }
                ?: return@composable
            CoffeeDetailScreen(
                coffeeItem = coffeeItem,
                onBack = {
                    navController.popBackStack()
                },
                onAddToCart = {
                    cartViewModel.addCartItem(it)
                    navController.navigate("cart") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                cartViewModel = cartViewModel
            )
        }

        composable("product-detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            val products by productViewModel.products.collectAsState()
            val product = products.firstOrNull { it.productId == productId }
            
            if (product != null) {
                ProductDetailScreen(
                    product = product,
                    onBack = {
                        navController.popBackStack()
                    },
                    onAddToCart = {
                        cartViewModel.addCartItem(it)
                        navController.navigate("cart") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    cartViewModel = cartViewModel
                )
            } else {
                // Product not found, go back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable("order-success") {
            OrderSuccessScreen(
                onTrackOrder = {
                    navController.navigate("orders")
                }
            )
        }

        composable("cart") {
            CartScreen(
                onBack = {
                    navController.popBackStack()
                },
                onCheckout = {
                    if(cartItems.isEmpty()) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        navController.currentBackStackEntry?.savedStateHandle?.set("showEmptyCartDialog", true)
                    } else {
                        var newPoint = user?.point ?: 0
                        for(item in cartItems) {
                            val now = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("d MMMM | hh:mm a", Locale.ENGLISH)
                            val formatted = now.format(formatter)

                            orderViewModel.insertOrder(
                                OrderEntity(
                                    cartItem = item,
                                    location = user?.address ?: "",
                                    orderTime = formatted
                                )
                            )
                            newPoint += item.point * item.quantity
                        }
                        user?.let {
                            val newStamp = min(it.stamp + cartItems.size, 8)
                            userViewModel.updateUser(it.copy(point = newPoint, stamp = newStamp))
                        }
                        cartViewModel.clearCart()
                        navController.navigate("order-success")
                    }
                },
                cartViewModel = cartViewModel
            )
        }

        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                authViewModel = authViewModel,
                onBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}