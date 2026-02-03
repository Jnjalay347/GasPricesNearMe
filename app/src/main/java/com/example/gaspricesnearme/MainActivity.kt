package com.example.gaspricesnearme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.gaspricesnearme.ui.theme.GasPricesNearMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GasPricesNearMeTheme {
                RootApp()
            }
        }
    }
}

// ---------------------------------------------------------
// Navigation Logic
// ---------------------------------------------------------

enum class AuthState {
    SIGN_IN,
    SIGN_UP,
    LOGGED_IN
}

@Composable
fun RootApp() {
    // This state tracks where the user is: Sign In, Sign Up, or Home
    var currentAuthState by rememberSaveable { mutableStateOf(AuthState.SIGN_IN) }

    when (currentAuthState) {
        AuthState.SIGN_IN -> {
            SignInScreen(
                onLoginSuccess = { currentAuthState = AuthState.LOGGED_IN },
                onNavigateToSignUp = { currentAuthState = AuthState.SIGN_UP }
            )
        }
        AuthState.SIGN_UP -> {
            SignUpScreen(
                onLoginSuccess = { currentAuthState = AuthState.LOGGED_IN },
                onNavigateToSignIn = { currentAuthState = AuthState.SIGN_IN }
            )
        }
        AuthState.LOGGED_IN -> {
            GasPricesNearMeApp()
        }
    }
}

// ---------------------------------------------------------
// Main App (Map & Tabs)
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun GasPricesNearMeApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    var locationSearchBar by rememberSaveable {
        mutableStateOf("")
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentDestination == AppDestinations.HOME) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = locationSearchBar,
                                onValueChange = { locationSearchBar = it },
                                label = { Text("Location") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Icon"
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> Text("Insert Google Maps Here")
                    AppDestinations.USER_REPORT -> Text("User Report Form Goes Here")
                    AppDestinations.SETTINGS -> Text("Settings Page Goes Here")
                }
            }
        }
    }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    USER_REPORT("User Report", Icons.AutoMirrored.Filled.NoteAdd),
    SETTINGS("Settings", Icons.Default.Settings),
}