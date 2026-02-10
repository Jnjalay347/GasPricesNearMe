package com.example.gaspricesnearme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen() {
    var stationAddress by remember { mutableStateOf("") }
    var cashStandard by remember { mutableStateOf("") }
    var cashPlus by remember { mutableStateOf("") }
    var cashPremium by remember { mutableStateOf("") }
    var creditStandard by remember { mutableStateOf("") }
    var creditPlus by remember { mutableStateOf("") }
    var creditPremium by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(0) }
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.USER_REPORT)
    }
    var locationSearchBar by rememberSaveable {
        mutableStateOf("")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Price Report Form",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )

        Text(
            text = "Station Address",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = stationAddress,
            onValueChange = { stationAddress = it},
            placeholder = { Text("Defaults to closest station")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Text(
            text = "Gas Station Prices",
            fontSize =  16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.width(60.dp))
            Text("Standard", fontSize = 12.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
            Text("Plus", fontSize = 12.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
            Text("Premium", fontSize = 12.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Cash", fontSize = 14.sp, modifier = Modifier.width(60.dp))
            PriceTextField(cashStandard) { cashStandard = it}
            PriceTextField(cashPlus) { cashPlus = it }
            PriceTextField(cashPremium) { cashPremium = it }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Credit", fontSize = 14.sp, modifier = Modifier.width(60.dp))
            PriceTextField(creditStandard) { creditStandard = it}
            PriceTextField(creditPlus) { creditPlus = it }
            PriceTextField(creditPremium) { creditPremium = it }
        }

        Text(
            text = "Rate This Station",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                IconButton(onClick = { selectedRating = index + 1}) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Star ${index + 1}",
                        modifier = Modifier.size(40.dp),
                        tint = if (index < selectedRating) Color.Yellow else Color.LightGray
                    )
                }
            }
        }
    }

}

@Composable
fun PriceTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(80.dp)
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF5F5F5)
        )
    )
}


@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen()
}

