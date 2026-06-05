package com.example

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.DriverDashboardScreen
import com.example.ui.DriverJobDetailScreen
import com.example.ui.JiffyViewModel
import com.example.ui.LoginScreen
import com.example.ui.OwnerDashboardScreen
import com.example.ui.SignupScreen
import com.example.ui.StatusScreen

@Composable
fun MainNavigation(viewModel: JiffyViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    NavHost(
        navController = navController, 
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLogin = { email -> 
                    viewModel.loginWithValidation(email) { result ->
                        when (result) {
                            "Success_Owner" -> navController.navigate("owner") { popUpTo("login") { inclusive = true } }
                            "Success_Driver" -> navController.navigate("driver") { popUpTo("login") { inclusive = true } }
                            "Pending" -> navController.navigate("pending_approval") { popUpTo("login") { inclusive = true } }
                            "Denied" -> navController.navigate("denied") { popUpTo("login") { inclusive = true } }
                            "Not Found" -> Toast.makeText(context, "User not found. Please sign up.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onSignupComplete = {
                    navController.navigate("pending_approval") { popUpTo("login") { inclusive = true } }
                }
            )
        }

        composable("pending_approval") {
            StatusScreen(
                title = "Application Pending",
                message = "Your driver application has been received and is currently under review by the owner. You will be able to log in once approved.",
                onBackToIntro = { navController.navigate("login") { popUpTo(0) } }
            )
        }

        composable("denied") {
            StatusScreen(
                title = "Application Denied",
                message = "We're sorry, your driver application has been denied.",
                onBackToIntro = { navController.navigate("login") { popUpTo(0) } }
            )
        }
        
        composable("owner") {
            OwnerDashboardScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") { popUpTo("owner") { inclusive = true } }
                }
            )
        }
        
        composable("driver") {
            DriverDashboardScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") { popUpTo("driver") { inclusive = true } }
                },
                onJobClick = { jobId ->
                    navController.navigate("job/$jobId")
                }
            )
        }
        
        composable("job/{jobId}") { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")?.toIntOrNull()
            if (jobId != null) {
                DriverJobDetailScreen(
                    jobId = jobId,
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
