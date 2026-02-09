package com.example.gaspricesnearme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gaspricesnearme.ui.theme.GasPricesNearMeTheme
import globus.glmap.GLMapView
import globus.glmap.MapGeoPoint

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
                    AppDestinations.HOME -> MapsScreen()
                    AppDestinations.USER_REPORT -> Text("User Report Form Goes Here")
                    AppDestinations.SETTINGS -> {
                        SettingsScreen(
                            onNavigateToSubmenu = {
                                // Placeholder
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // Mock data for the cards
    val gasStations = remember {
        listOf(
            GasStation("Shell", "123 Main St", "$4.50"),
            GasStation("Chevron", "456 Market St", "$4.65"),
            GasStation("7-Eleven", "789 Mission St", "$4.45"),
            GasStation("Costco", "101 10th St", "$4.20"),
            GasStation("Arco", "202 Valencia St", "$4.35"),
            GasStation("Safeway", "303 Diamond St", "$4.55")
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 120.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nearby Gas Prices",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(gasStations) { station ->
                        GasStationCard(station)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                GLMapView(context).apply {
                    renderer.setMapGeoCenter(MapGeoPoint(37.7749, -122.4194))
                    renderer.mapZoom = 12.0
                }
            }
        )
    }
}

@Composable
fun GasStationCard(station: GasStation) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = station.name, style = MaterialTheme.typography.titleMedium)
                Text(text = station.address, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = station.price,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = { /* TODO */ }) {
                // Empty button
            }
        }
    }
}

data class GasStation(val name: String, val address: String, val price: String)

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    USER_REPORT("User Report", Icons.AutoMirrored.Filled.NoteAdd),
    SETTINGS("Settings", Icons.Default.Settings),
}
