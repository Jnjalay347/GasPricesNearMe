package com.example.gaspricesnearme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

// ---------------------------------------------------------
// Settings Screen 1-6
// ---------------------------------------------------------

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToSubmenu: () -> Unit,
    onSignOut: () -> Unit = {},
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
        val searchRadius by settingsViewModel.searchRadius.collectAsState()
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
                settingsViewModel.updateSearchRadius(minValOneOrHigher)
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

        // Extra Space, if needed
        // Spacer(modifier = Modifier.height(1.dp))

        var nickname by remember { mutableStateOf("") }

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. ARCO, Shell on Harbor Blvd.") },
            singleLine = true
        )

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

        // Extra Space, if needed
        // Spacer(modifier = Modifier.height(1.dp))

        var locationName by remember { mutableStateOf("") }

        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your current location/address.") },
            singleLine = true
        )

        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Dark Mode Toggle
        // var darkModeEnabled by remember { mutableStateOf(false) }

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
                FirebaseAuth.getInstance().signOut()
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
    val dummyViewModel: SettingsViewModel = viewModel()

    SettingsScreen(
        settingsViewModel = dummyViewModel,
        onNavigateToSubmenu = {},
        onSignOut = {},
        darkModeEnabled = false,
        onToggleDarkMode = {}
    )
}