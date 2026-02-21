package com.example.gaspricesnearme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.gaspricesnearme.ui.theme.GasPricesNearMeTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
// Navigation Logic 1-1
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
// Main App (Map & Tabs) 1-1
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
                    AppDestinations.USER_REPORT -> ReportScreen()
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

// ---------------------------------------------------------
// Maps Screen and List View Scaffold 1-3 and 1-4
// ---------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationHelper by remember { mutableStateOf<CurLocationHelper?>(null) }

    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // Track currently selected station. Null = Show List; Not Null = Show Details
    var selectedStation by remember { mutableStateOf<GasStation?>(null) }

    // Mock data for the cards (Updated with Distance and Rating)
    val gasStations = remember {
        listOf(
            GasStation("Shell", "123 Main St", "$4.50", "0.2 mi", 4.5f),
            GasStation("Chevron", "456 Market St", "$4.65", "0.5 mi", 3.8f),
            GasStation("7-Eleven", "789 Mission St", "$4.45", "1.2 mi", 4.0f),
            GasStation("Costco", "101 10th St", "$4.20", "2.5 mi", 4.8f),
            GasStation("Arco", "202 Valencia St", "$4.35", "3.0 mi", 3.5f),
            GasStation("Safeway", "303 Diamond St", "$4.55", "3.2 mi", 4.2f)
        )
    }

    DisposableEffect(hasLocationPermission) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    locationHelper?.onLocationChanged(location)
                }
            }
        }

        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).build()
            
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // Handle missing permission if necessary
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 120.dp,
        sheetContent = {
            // Logic: If a station is selected, show DetailScreen. Otherwise, show List.
            if (selectedStation != null) {
                // Show Detail View
                // Note: Ensure DetailScreen.kt is created or pasted in this file
                DetailScreen(
                    station = selectedStation!!,
                    onDirectionsClick = {
                        // TODO: Handle Google Maps Intent here
                    }
                )
            } else {
                // Show List View
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
                            GasStationCard(
                                station = station,
                                onClick = { selectedStation = station } // Set selected station
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Main Map Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Clicking the map background deselects the station (returns to list)
                .clickable { selectedStation = null }
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    GLMapView(context).apply {
                        // Postpone setting renderer properties until it's initialized
                        post {
                            renderer.mapGeoCenter = MapGeoPoint(37.7749, -122.4194)
                            renderer.mapZoom = 10.0
                            locationHelper = CurLocationHelper(renderer)

                            val pinMarker = MapPinMarker(
                                renderer = renderer,
                                assets = context.assets
                            )

                            // Single Map pin (hard-coded, for now)
                            pinMarker.addMapPin(
                                37.7749,
                                -122.4194,
                                "pin.svg"
                            )

//                            // Multiple Map pins (hard-coded, for now)
//                            pinMarker.addMultipleMapPins(
//                                listOf(
//                                    37.8199 to -122.4783,
//                                    37.8267 to -122.4230,
//                                    37.8080 to -122.4177,
//                                    37.8021 to -122.4187
//                                ),
//                                "pin.svg"
//                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GasStationCard(station: GasStation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() } // Make the entire card clickable
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
            // Arrow Icon to indicate clickability
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Details"
            )
        }
    }
}

// Data Class for Gas Stations
data class GasStation(
    val name: String,
    val address: String,
    val price: String,
    val distance: String,
    val rating: Float
)

// Enum for Nav
enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    USER_REPORT("User Report", Icons.AutoMirrored.Filled.NoteAdd),
    SETTINGS("Settings", Icons.Default.Settings),
}