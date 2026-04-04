package com.example.gaspricesnearme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaspricesnearme.viewmodel.SettingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties
import com.example.gaspricesnearme.model.GasStation as GasStationModel

// ---------------------------------------------------------
// Settings Screen 1-6
// ---------------------------------------------------------

/**
 * Stateful version of the Settings Screen that interacts with the [SettingsViewModel].
 */
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToSubmenu: () -> Unit,
    onSignOut: () -> Unit = {},
    darkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    // Collect state from ViewModel to pass down to the stateless content
    val searchRadius by settingsViewModel.searchRadius.collectAsState()
    val searchResults by settingsViewModel.searchResults.collectAsState()
    val favoriteStation by settingsViewModel.favoriteStation.collectAsState()
    val currentLocation by settingsViewModel.currentLocation.collectAsState()

    SettingsScreenContent(
        searchRadius = searchRadius,
        onSearchRadiusChange = { settingsViewModel.updateSearchRadius(it) },
        searchResults = searchResults,
        favoriteStation = favoriteStation,
        currentLocation = currentLocation,
        onUpdateCurrentLocation = { settingsViewModel.updateCurrentLocation(it) },
        onClearCurrentLocation = { settingsViewModel.clearCurrentLocation() },
        onSearchStations = { settingsViewModel.searchStations(it) },
        onSaveFavoriteStation = { station ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                settingsViewModel.saveFavoriteStation(userId, station)
            }
        },
        onNavigateToSubmenu = onNavigateToSubmenu,
        onSignOut = {
            FirebaseAuth.getInstance().signOut()
            onSignOut()
        },
        darkModeEnabled = darkModeEnabled,
        onToggleDarkMode = onToggleDarkMode
    )
}

/**
 * Stateless version of the Settings Screen, for previews/testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    searchRadius: Float,
    onSearchRadiusChange: (Float) -> Unit,
    searchResults: List<GasStationModel>,
    favoriteStation: GasStationModel?,
    currentLocation: String?,
    onUpdateCurrentLocation: (String) -> Unit,
    onClearCurrentLocation: () -> Unit,
    onSearchStations: (String) -> Unit,
    onSaveFavoriteStation: (GasStationModel) -> Unit,
    onNavigateToSubmenu: () -> Unit,
    onSignOut: () -> Unit,
    darkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp)
    ) {
        // Title
        Text(
            text = "Settings",
            modifier = Modifier.padding(bottom = 25.dp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Search Radius Slider
        val searchRadiusRounded = searchRadius.roundToInt()

        Text(
            text = "Search Radius (${searchRadiusRounded} miles)",
            fontWeight = FontWeight.SemiBold
        )

        Slider(
            value = searchRadius,
            onValueChange = { value ->
                val roundToNearestFive = ((value / 5f).roundToInt() * 5f)
                val minValOneOrHigher = roundToNearestFive.coerceAtLeast(1f)
                onSearchRadiusChange(minValOneOrHigher)
            },
            valueRange = 1f..30f,
            steps = 5
        )

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Notifications Toggle
        var notificationsEnabled by remember { mutableStateOf(true) }

        SettingRow(
            title = "Enable Notifications",
            description = "Receive prompts to report gas prices",
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Notifications Submenu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSubmenu() }
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Notification Settings",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Customize alerts & reminders",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Text(
                ">",
                fontSize = 30.sp,
                color = Color.Gray
            )
        }

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Text Field Setting
        Text(
            text = "Set Favorite Gas Station",
            fontWeight = FontWeight.SemiBold
        )

        var searchQuery by remember { mutableStateOf("") }
        var isDropdownExpanded by remember { mutableStateOf(false) }

        // Trigger expansion when search results are available and query changes
        LaunchedEffect(searchResults) {
            isDropdownExpanded = searchResults.isNotEmpty()
        }

        // Trigger search when query changes
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotBlank()) {
                onSearchStations(searchQuery)
                isDropdownExpanded = true
            } else {
                isDropdownExpanded = false
            }
        }

        Box {
            OutlinedTextField(
                value = searchQuery,
                // Actively searches and matches query to gas station, during typing
                onValueChange = {
                    searchQuery = it
                    onSearchStations(it)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Search gas stations...") },
                singleLine = true
            )

            // Drop-down menu for Gas Stations
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 240.dp) // Show only a few items at once
            ) {
                if (searchResults.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No stations found.") },
                        onClick = { }
                    )
                } else {
                    searchResults.forEach { station ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(station.stationName)
                                    Text(
                                        station.address,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                onSaveFavoriteStation(station)
                                searchQuery = station.stationName
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Shows selected favorite Gas Station
        favoriteStation?.let { station ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Favorite Station:",
                fontWeight = FontWeight.SemiBold
            )

            Text(station.stationName)
            Text(station.address, color = Color.Gray)
        }

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Text Field Setting
        Text(
            text = "Set Current Location",
            fontWeight = FontWeight.SemiBold
        )

        var locationName by remember { mutableStateOf("") }

        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your current location/address.") },
            singleLine = true,
            trailingIcon = {
                if (locationName.isNotBlank()) {
                    Button(
                        onClick = {
                            onUpdateCurrentLocation(locationName)
                            locationName = ""
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        )

        // Shows saved current location
        currentLocation?.let { location ->
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Location:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(text = location, color = Color.Gray)
                }
                IconButton(onClick = onClearCurrentLocation) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear location",
                        tint = Color.Red
                    )
                }
            }
        }

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Dark Mode Toggle
        SettingRow(
            title = "Dark Mode",
            description = "Swaps to dark theme for the app",
            control = {
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { onToggleDarkMode(it) }
                )
            }
        )

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Sign out Button
        Button(
            onClick = {
                onSignOut()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Sign Out", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    description: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = description, color = Color.Gray, fontSize = 14.sp)
        }
        control()
    }
}

// PREVIEW
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    // Uses stateless version to avoid ViewModel instantiation issues
    SettingsScreenContent(
        searchRadius = 10f,
        onSearchRadiusChange = {},
        searchResults = emptyList(),
        favoriteStation = null,
        currentLocation = "123 Main St, Springfield",
        onUpdateCurrentLocation = {},
        onClearCurrentLocation = {},
        onSearchStations = {},
        onSaveFavoriteStation = {},
        onNavigateToSubmenu = {},
        onSignOut = {},
        darkModeEnabled = false,
        onToggleDarkMode = {}
    )
}