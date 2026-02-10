package com.example.gaspricesnearme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.gaspricesnearme.ui.theme.GasPricesNearMeTheme

// Google Maps Imports
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
    // State to track if a specific station is selected for Detail View
    var selectedStation by remember { mutableStateOf<GasStation?>(null) }

    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    var locationSearchBar by rememberSaveable {
        mutableStateOf("")
    }

    // IF a station is selected, we show the Detail Screen fullscreen
    if (selectedStation != null) {
        StationDetailScreen(
            station = selectedStation!!,
            onBack = { selectedStation = null } // Go back to Map/List
        )
    } else {
        // ELSE show the standard Bottom Navigation App
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = it.label) },
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
                                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                        AppDestinations.HOME -> MapsScreen(
                            onStationClick = { station -> selectedStation = station }
                        )
                        AppDestinations.USER_REPORT -> ReportScreen()
                        AppDestinations.SETTINGS -> SettingsScreen(onNavigateToSubmenu = {})
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(onStationClick: (GasStation) -> Unit) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // Mock data with added distance and rating
    val gasStations = remember {
        listOf(
            GasStation("Shell", "123 Main St", "$4.50", "0.5", 4.2f, LatLng(37.7749, -122.4194)),
            GasStation("Chevron", "456 Market St", "$4.65", "0.8", 3.8f, LatLng(37.7849, -122.4094)),
            GasStation("7-Eleven", "789 Mission St", "$4.45", "1.1", 4.0f, LatLng(37.7649, -122.4294)),
            GasStation("Costco", "101 10th St", "$4.20", "2.3", 4.8f, LatLng(37.7549, -122.4394)),
            GasStation("Arco", "202 Valencia St", "$4.35", "3.0", 3.5f, LatLng(37.7449, -122.4494)),
            GasStation("Safeway", "303 Diamond St", "$4.55", "3.2", 4.1f, LatLng(37.7349, -122.4594))
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 12f)
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
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(gasStations) { station ->
                        GasStationCard(station, onClick = { onStationClick(station) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            gasStations.forEach { station ->
                Marker(
                    state = MarkerState(position = station.location),
                    title = station.name,
                    snippet = station.price,
                    onClick = {
                        onStationClick(station)
                        true
                    }
                )
            }
        }
    }
}

@Composable
fun GasStationCard(station: GasStation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() } // Make the whole card clickable
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = station.name, style = MaterialTheme.typography.titleMedium)
                Text(text = station.address, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = station.price,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = onClick) {
                Text("View")
            }
        }
    }
}

// Updated Data Class to support Detail View
data class GasStation(
    val name: String,
    val address: String,
    val price: String,
    val distance: String, // Added
    val rating: Float,    // Added
    val location: LatLng
)

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    USER_REPORT("User Report", Icons.AutoMirrored.Filled.NoteAdd),
    SETTINGS("Settings", Icons.Default.Settings),
}