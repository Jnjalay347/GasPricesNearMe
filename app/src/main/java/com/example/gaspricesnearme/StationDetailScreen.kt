package com.example.gaspricesnearme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    station: GasStation,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // Empty title as per mockup (Name is in body)
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // 1. Gas Station Name (Centered, Bold)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = station.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Address
            Text(
                text = station.address,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 3. Price and Directions Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = station.price,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { /* TODO: Launch Google Maps Directions */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)), // iOS Blue
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, // Mockup shows a Star icon
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Directions")
                }
            }

            // 4. Distance
            Text(
                text = "${station.distance} miles away",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 5. Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .then(Modifier.padding(1.dp)) // Border effect
            ) {
                // Drawing the "Image Placeholder" icon manually to match mockup
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star, // Placeholder for the 'Landscape' icon
                            contentDescription = "Station Image",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Rating Info
            Text(
                text = "Rating: ${station.rating} / 5.0",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Based on user reports",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun StationDetailScreenPreview() {
    val sampleStation = GasStation(
        name = "Gas Station Name",
        address = "Gas Station Address",
        price = "$4.50",
        distance = "1.2",
        rating = 4.5f,
        location = LatLng(0.0, 0.0)
    )
    StationDetailScreen(station = sampleStation, onBack = {})
}