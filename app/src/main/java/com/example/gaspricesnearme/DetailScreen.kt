package com.example.gaspricesnearme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaspricesnearme.ui.theme.GpnmBlue
import com.example.gaspricesnearme.ui.theme.GpnmBlueDark

// ---------------------------------------------------------
// Details View 1-5
// ---------------------------------------------------------

@Composable
fun DetailScreen(
    station: GasStation,
    onDirectionsClick: () -> Unit,
    onBackClick: () -> Unit,
    onReportPricesClick: () -> Unit = {},
    darkModeEnabled: Boolean = false
) {
    val bgColor = if (darkModeEnabled) GpnmBlueDark else GpnmBlue
    val textColor = if (darkModeEnabled) Color.LightGray else Color.DarkGray
    val primaryText = if (darkModeEnabled) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Drag Handle (Visual only, to match mockup style)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Station Name
        Text(
            text = station.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Address
        Text(
            text = station.address,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Row for Price, Distance, and Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = station.price,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (station.distance.isBlank()) "Distance unavailable" else "${station.distance} away",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }

            Button(
                onClick = onDirectionsClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)) // iOS Blue style
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Directions")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Big Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Station Image",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Rating Info
        Text(
            text = "Rating: ${station.rating} ★",
            style = MaterialTheme.typography.bodyLarge,
            color = primaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Back to List Button
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (darkModeEnabled) Color.White else Color.Black)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Return to List")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8. Report Prices Button
        Button(
            onClick = onReportPricesClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report Prices")
        }

        // Add extra space at bottom for scrolling
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    // Mock data for preview
    val mockStation = GasStation(
        coordinates = "33.7451`-117.8670",
        name = "Shell",
        address = "123 Main St",
        price = "$4.50",
        distance = "0.5 mi",
        rating = 4.5f
    )
    DetailScreen(
        station = mockStation,
        onDirectionsClick = {},
        onBackClick = {}
    )
}