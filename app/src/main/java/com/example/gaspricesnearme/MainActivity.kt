package com.example.gaspricesnearme

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaspricesnearme.model.GasStation as GasStationModel
import com.example.gaspricesnearme.ui.theme.GasPricesNearMeTheme
import com.example.gaspricesnearme.ui.theme.GpnmBlue
import com.example.gaspricesnearme.ui.theme.GpnmBlueDark
import com.example.gaspricesnearme.viewmodel.SettingsViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import globus.glmap.GLMapView
import globus.glmap.MapGeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.location.Geocoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TASK 6-2: Initialize the notification channel when the app starts
        createNotificationChannel()

        enableEdgeToEdge()
        setContent {
            var darkModeEnabled by rememberSaveable { mutableStateOf(false) }

            GasPricesNearMeTheme(
                darkTheme = darkModeEnabled
            ) {
                RootApp(
                    darkModeEnabled = darkModeEnabled,
                    onToggleDarkMode = { darkModeEnabled = it }
                )
            }
        }
    }

    // TASK 6-2: Create the Notification Channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Price Report Prompts"
            val descriptionText = "Reminders to report gas prices when you are at a station"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("GEOFENCE_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
fun RootApp(
    darkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {

    //Change to false to test sign in
    val testing = false

    //Checks if user is logged in already
    val initialState = if (testing) {
        AuthState.LOGGED_IN
    } else if (FirebaseAuth.getInstance().currentUser != null) {
        AuthState.LOGGED_IN
    } else {
        AuthState.SIGN_IN
    }


    // This state tracks where the user is: Sign In, Sign Up, or Home
    var currentAuthState by rememberSaveable { mutableStateOf(initialState) }


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
            GasPricesNearMeApp(
                onSignOut = {currentAuthState = AuthState.SIGN_IN},
                darkModeEnabled = darkModeEnabled,
                onToggleDarkMode = onToggleDarkMode)
        }
    }
}

// ---------------------------------------------------------
// Main App (Map & Tabs) 1-1
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun GasPricesNearMeApp(
    onSignOut: () -> Unit = {},
    darkModeEnabled: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {}) {

    val settingsViewModel: SettingsViewModel? =
        // Avoids instantiating ViewModel, during Preview
        if (LocalInspectionMode.current) null else viewModel()

    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    var prefillData by remember { mutableStateOf<GasStation?>(null) }


    // Map Search Bar States
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    val searchResults by settingsViewModel?.searchResults?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val favoriteStation by settingsViewModel?.favoriteStation?.collectAsState() ?: remember { mutableStateOf(null) }

    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    LaunchedEffect(userId) {
        if (userId != null) {
            settingsViewModel?.fetchFavoriteStation(userId)
        }
    }

    // Map centering request: Triple(Type, Data, Timestamp)
    var mapActionTrigger by remember { mutableStateOf<Triple<String, Any?, Long>?>(null) }


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
                    onClick = { 
                        if (it != AppDestinations.USER_REPORT) {
                            prefillData = null
                        }
                        currentDestination = it 
                    }
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
                            Box(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        if (it.isNotBlank()) {
                                            settingsViewModel?.searchStations(it)
                                            isDropdownExpanded = true
                                        } else {
                                            isDropdownExpanded = false
                                        }
                                    },
                                    label = { Text("Search location...") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Icon"
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = {
                                                searchQuery = ""
                                                isDropdownExpanded = false
                                            }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    properties = PopupProperties(focusable = false),
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .heightIn(max = 300.dp)
                                ) {
                                    // Quick Select: Current Location
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                                Text("Current Location")
                                            }
                                        },
                                        onClick = {
                                            mapActionTrigger = Triple("current", null, System.currentTimeMillis())
                                            searchQuery = "Current Location"
                                            isDropdownExpanded = false
                                        }
                                    )

                                    // Quick Select: Favorite Station
                                    favoriteStation?.let { station ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                                    Text("Favorite: ${station.stationName}")
                                                }
                                            },
                                            onClick = {
                                                mapActionTrigger = Triple("favorite", null, System.currentTimeMillis())
                                                searchQuery = station.stationName
                                                isDropdownExpanded = false
                                            }
                                        )
                                    }

                                    if (searchResults.isNotEmpty()) {
                                        HorizontalDivider()
                                        searchResults.forEach { station ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(station.stationName)
                                                        Text(station.address, fontSize = 12.sp, color = Color.Gray)
                                                    }
                                                },
                                                onClick = {
                                                    mapActionTrigger = Triple("coord", (station.latitude to station.longitude), System.currentTimeMillis())
                                                    searchQuery = station.stationName
                                                    isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
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
                    AppDestinations.HOME -> {
                        if (settingsViewModel != null) {
                            MapsScreen(
                                settingsViewModel, 
                                mapActionTrigger,
                                onReportPrices = { station ->
                                    prefillData = station
                                    currentDestination = AppDestinations.USER_REPORT
                                },
                                darkModeEnabled = darkModeEnabled
                            )
                        } else {
                            MapsScreenPreview()
                        }
                    }
                    AppDestinations.USER_REPORT -> ReportScreen(
                        settingsViewModel = settingsViewModel,
                        initialAddress = prefillData?.address ?: "",
                        initialPrices = prefillData?.fullPrices ?: ""
                    )
                    AppDestinations.SETTINGS -> {
                        if (settingsViewModel != null) {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToSubmenu = {
                                    // Placeholder
                                },
                                onSignOut = onSignOut,
                                darkModeEnabled = darkModeEnabled,
                                onToggleDarkMode = onToggleDarkMode
                            )
                        } else {
                            // Renders a placeholder/stateless preview version
                            SettingsScreenPreview()
                        }
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
fun MapsScreen(
    settingsViewModel: SettingsViewModel,
    mapActionTrigger: Triple<String, Any?, Long>? = null,
    onReportPrices: (GasStation) -> Unit = {},
    darkModeEnabled: Boolean = false
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationHelper by remember { mutableStateOf<CurLocationHelper?>(null) }

    // Variable to allow adjusting the size of the currentLocation marker
    val currentLocationMarkerScale = 0.5f

    // Collect settings from ViewModel
    val searchRadius by settingsViewModel.searchRadius.collectAsState()
    val favoriteStation by settingsViewModel.favoriteStation.collectAsState()
    val currentLocationStr by settingsViewModel.currentLocation.collectAsState()

    var pinMarker by remember { mutableStateOf<MapPinMarker?>(null) }

    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // TASK 6-1: Background Location Permission State
    var hasBackgroundLocationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Background location implicitly granted below Android 10
            }
        )
    }

    // TASK 6-2: Notification Permission State
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Implicitly granted below Android 13
            }
        )
    }

    // TASK 6-1 & 6-2: Updated Launcher for Multiple Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasBackgroundLocationPermission = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: hasBackgroundLocationPermission
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
    }

    // TASK 6-1 & 6-2: Requesting all necessary permissions
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // Track currently selected station. Null = Show List; Not Null = Show Details
    var selectedStation by remember { mutableStateOf<GasStation?>(null) }

    // TASK 6-1: Store database entities for geofencing
    var dbStations by remember { mutableStateOf<List<StationEntity>>(emptyList()) }

    var gasStations by remember {
        mutableStateOf<List<GasStation>>(emptyList())
    }

    LaunchedEffect(Unit) {
        val appDb = AppDatabase.getInstance(context)
        val repo = StationsRepository()

        withContext(Dispatchers.IO) {
            repo.syncStationsToLocal(appDb)
        }

        val stations = withContext(Dispatchers.IO) {
            appDb.stationDao().getAll()
        }

        dbStations = stations // TASK 6-1: Save to local state for Geofences

        gasStations = stations.map {
            val price = it.prices.split("`").getOrNull(0) ?: "?"
            GasStation(
                coordinates = it.coordinates,
                name = it.stationName,
                address = it.address,
                price = "$$price",
                distance = "",
                rating = it.rating.toFloat(),
                fullPrices = it.prices
            )
        }
    }

    // TASK 6-1: Setup Geofences automatically when data and permissions are ready
    LaunchedEffect(dbStations, hasBackgroundLocationPermission) {
        if (dbStations.isNotEmpty() && hasBackgroundLocationPermission) {
            val geofenceHelper = GeofenceHelper(context)
            geofenceHelper.setupGeofences(dbStations)
        }
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

    // Automatically updates or clears the circle marker for "Current Location"
    LaunchedEffect(currentLocationStr, pinMarker) {
        val markerManager = pinMarker ?: return@LaunchedEffect

        if (currentLocationStr.isNullOrBlank()) {
            markerManager.updateCurrentLocationMarker(null, null, "circle.svg")
        } else {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(currentLocationStr!!, 1)
                }
                if (addresses?.isNotEmpty() == true) {
                    val addr = addresses[0]
                    withContext(Dispatchers.Main) {
                        markerManager.updateCurrentLocationMarker(
                            addr.latitude,
                            addr.longitude,
                            "circle.svg",
                            currentLocationMarkerScale.toDouble()
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore geocoding errors
            }
        }
    }

    val sheetBgColor = if (darkModeEnabled) GpnmBlueDark else GpnmBlue

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 120.dp,
        sheetContainerColor = sheetBgColor,
        sheetContent = {
            // Logic: If a station is selected, show DetailScreen. Otherwise, show List.
            if (selectedStation != null) {
                // Show Detail View
                DetailScreen(
                    station = selectedStation!!,
                    onDirectionsClick = {
                        // TODO: Handle Google Maps Intent here
                    },
                    onBackClick = {
                        selectedStation = null
                    },
                    onReportPricesClick = {
                        onReportPrices(selectedStation!!)
                    },
                    darkModeEnabled = darkModeEnabled
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
                                onClick = {

                                    CoroutineScope(Dispatchers.IO).launch {

                                        val dbStation = AppDatabase
                                            .getInstance(context)
                                            .stationDao()
                                            .getStation(station.coordinates)

                                        withContext(Dispatchers.Main) {
                                            if (dbStation != null) {
                                                val price = dbStation.prices.split("`").getOrNull(0) ?: "?"
                                                selectedStation = GasStation(
                                                    coordinates = dbStation.coordinates,
                                                    name = dbStation.stationName,
                                                    address = dbStation.address,
                                                    price ="$$price",
                                                    distance = "",
                                                    rating = dbStation.rating.toFloat(),
                                                    fullPrices = dbStation.prices
                                                )
                                            }
                                        }
                                    }
                                } // Set selected station
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Adjusts Zoom in/Zoom out values for the Floating Action Button
        val rendererState = remember { mutableStateOf<GLMapView?>(null) }
        
        // Effect to handle map changes and UI when a station is selected
        LaunchedEffect(selectedStation, pinMarker, rendererState.value) {
            val station = selectedStation
            val renderer = rendererState.value?.renderer
            if (station != null && renderer != null && pinMarker != null) {
                val parts = station.coordinates.split("`")
                val lat = parts.getOrNull(0)?.toDoubleOrNull()
                val lon = parts.getOrNull(1)?.toDoubleOrNull()
                if (lat != null && lon != null) {
                    // Overlay with ChosenMapPin.svg
                    pinMarker?.updateChosenMarker(lat, lon, "ChosenMapPin.svg", 1.2)

                    // Zooms in and centers pin at top-middle
                    renderer.mapZoom = 16.0

                    // Offset to put the pin in the top-middle (roughly 0.003 degrees south of pin)
                    val offset = 0.003
                    renderer.mapGeoCenter = MapGeoPoint(lat - offset, lon)

                    // Ensures bottom sheet is expanded to show details
                    scaffoldState.bottomSheetState.expand()
                }
            } else if (station == null) {
                // Clear chosen marker when no station is selected
                pinMarker?.updateChosenMarker(null, null, "")

                // Return sheet to peek height when returning to list
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        fun zoomIn() {
            rendererState.value?.renderer?.let {
                it.mapZoom += 0.5
            }
        }
        fun zoomOut() {
            rendererState.value?.renderer?.let {
                it.mapZoom -= 0.5
            }
        }

        // Toggles between Favorite and Current Location
        var isFavoriteToggled by remember { mutableStateOf(false) }

        // Helper function to calculate zoom level from radius (miles)
        // Approx: 15 miles ~ 10.0 zoom, 5 miles ~ 12.0 zoom, 1 mile ~ 14.5 zoom
        fun getZoomFromRadius(radius: Float): Double {
            return when {
                radius <= 1f -> 14.5
                radius <= 5f -> 12.5
                radius <= 10f -> 11.5
                radius <= 20f -> 10.5
                else -> 9.5
            }
        }

        fun centerOnFavorite() {
            favoriteStation?.let { station ->
                rendererState.value?.renderer?.let { renderer ->
                    renderer.mapGeoCenter = MapGeoPoint(station.latitude, station.longitude)
                    renderer.mapZoom = getZoomFromRadius(searchRadius)
                }
            }
        }

        fun centerOnCurrentLocation() {
            currentLocationStr?.let { address ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocationName(address, 1)
                        if (addresses?.isNotEmpty() == true) {
                            val addr = addresses[0]
                            withContext(Dispatchers.Main) {
                                rendererState.value?.renderer?.let { renderer ->
                                    renderer.mapGeoCenter = MapGeoPoint(addr.latitude, addr.longitude)
                                    renderer.mapZoom = getZoomFromRadius(searchRadius)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Handle Geocoding error
                    }
                }
            }
        }

        // Controls External Map Actions, from Top Search Bar
        LaunchedEffect(mapActionTrigger) {
            when (mapActionTrigger?.first) {
                "current" -> {
                    isFavoriteToggled = false
                    centerOnCurrentLocation()
                }
                "favorite" -> {
                    isFavoriteToggled = true
                    centerOnFavorite()
                }
                "coord" -> {
                    val coords = mapActionTrigger.second as? Pair<Double, Double>
                    coords?.let { (lat, lon) ->
                        rendererState.value?.renderer?.let { renderer ->
                            renderer.mapGeoCenter = MapGeoPoint(lat, lon)
                            renderer.mapZoom = getZoomFromRadius(searchRadius)
                        }
                    }
                }
            }
        }

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
                            // Maintains Zoom in/Zoom out value
                            rendererState.value = this

                            renderer.mapGeoCenter = MapGeoPoint(37.7749, -122.4194)
                            renderer.mapZoom = 10.0
                            locationHelper = CurLocationHelper(renderer)

                            val markerManager = MapPinMarker(
                                renderer = renderer,
                                assets = context.assets
                            )
                            pinMarker = markerManager

                            CoroutineScope(Dispatchers.IO).launch {

                                val stations = AppDatabase
                                    .getInstance(context)
                                    .stationDao()
                                    .getAll()

                                withContext(Dispatchers.Main) {

                                    val first = stations.firstOrNull()
                                    if (first != null) {
                                        val parts = first.coordinates.split("`")
                                        val lat = parts.getOrNull(0)?.toDoubleOrNull()
                                        val lon = parts.getOrNull(1)?.toDoubleOrNull()

                                        if (lat != null && lon != null) {
                                            renderer.mapGeoCenter = MapGeoPoint(lat, lon)
                                            renderer.mapZoom = 12.0
                                        }
                                    }

                                    stations.forEach { station ->

                                        val parts = station.coordinates.split("`")

                                        val lat = parts.getOrNull(0)?.toDoubleOrNull()
                                        val lon = parts.getOrNull(1)?.toDoubleOrNull()

                                        if (lat != null && lon != null) {
                                            markerManager.addMapPin(lat, lon, "pin.svg")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
            // Zoom in/Zoom out Floating Action Buttons for Map
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Location/Favorite Toggle Button
                FloatingActionButton(
                    onClick = {
                        isFavoriteToggled = !isFavoriteToggled
                        if (isFavoriteToggled) {
                            centerOnFavorite()
                        } else {
                            centerOnCurrentLocation()
                        }
                    },
                    containerColor = if (isFavoriteToggled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = if (isFavoriteToggled) Icons.Default.Star else Icons.Default.MyLocation,
                        contentDescription = "Toggle Location/Favorite"
                    )
                }

                FloatingActionButton(
                    onClick = { zoomIn() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                FloatingActionButton(
                    onClick = { zoomOut() }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }
        }
    }
}

@Composable
fun MapsScreenPreview() {
    // Basic placeholder for previewing maps screen structure
    Text("Map Screen Preview")
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
    val coordinates: String,
    val name: String,
    val address: String,
    val price: String,
    val distance: String,
    val rating: Float,
    val fullPrices: String? = null
)

// Enum for Nav
enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    USER_REPORT("User Report", Icons.AutoMirrored.Filled.NoteAdd),
    SETTINGS("Settings", Icons.Default.Settings),
}
