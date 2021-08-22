package com.app.yamamz.yamamzipscanner.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.ui.theme.DeviceScannerTheme
import com.app.yamamz.yamamzipscanner.ui.theme.Purple500
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(selectDevice: (Device) -> Unit) {
    Log.e("HomeScreen", "Recompose")
    val viewModel = hiltViewModel<HomeViewModel>()
    val scope = rememberCoroutineScope()

    val isWifiConnected: Boolean by viewModel.isWifiConnected.observeAsState(true)
    val wirelessInternalIpAddress: String by viewModel.wirelessInternalIpAddress.observeAsState("")
    val devices: List<Device> by viewModel.devices.observeAsState(listOf())
    val isLoading: Boolean by viewModel.isLoading.observeAsState(false)
    Log.e("HomeScreen", wirelessInternalIpAddress)
    LaunchedEffect(Unit) {
        Log.e("HomeScreen", "Recompose in LaunchedEffect")
        viewModel.checkIfWifiConnected()
        viewModel.getWirelessInternalIpAddress()
        scope.launch {
            viewModel.getCacheDevices(wirelessInternalIpAddress, scope = scope)
        }

    }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            if (isWifiConnected) {
                Log.e("HomeScreen", "Recompose")
                scope.launch {
                    viewModel.searDevices(wirelessInternalIpAddress, scope= scope)
                }
            }
        }) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Localized description",
                Modifier.size(32.dp),
                Color.White
            )
        }
    }) {

        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(color = Purple500)
                .padding(all = 8.dp),
            contentAlignment = Alignment.TopCenter

        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    wirelessInternalIpAddress,
                    Modifier.padding(8.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )
                HeaderComponent(devicesSize = devices.size)

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 60.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text(
                        "Scanning your network",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                } else if (devices.isEmpty()) {
                    Text(
                        "Press the refresh button to scan the network",

                        modifier = Modifier.padding(vertical = 80.dp, horizontal = 20.dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                } else if (!isWifiConnected) {
                    Text(
                        "Your are not connected to wireless",
                        modifier = Modifier.padding(top = 80.dp),
                        style = TextStyle(color = Color.White, fontSize = 16.sp)
                    )
                } else if (devices.isNotEmpty() && !isLoading) {
                    LazyColumn(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(all = 8.dp),
                        verticalArrangement = Arrangement.Center

                    ) {
                        items(devices) {
                            DeviceItem(device = it) { device ->
                                selectDevice(device)
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun HeaderComponent(devicesSize: Int) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(top = 24.dp)
            .background(Color.Black, shape = CircleShape)
            .layout() { measurable, constraints ->
                // Measure the composable
                val placeable = measurable.measure(constraints)
                //get the current max dimension to assign width=height
                val currentHeight = placeable.height
                var heightCircle = currentHeight
                if (placeable.width > heightCircle)
                    heightCircle = placeable.width
                //assign the dimension and the center position
                layout(heightCircle, heightCircle) {
                    // Where the composable gets placed
                    placeable.placeRelative(0, (heightCircle - currentHeight) / 2)
                }
            }
            .size(120.dp)
            .border(4.dp, Color.White, CircleShape)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = devicesSize.toString(),
                textAlign = TextAlign.Center,
                color = Color.White,
                style = TextStyle(fontSize = 30.sp),

                modifier = Modifier
                    .padding(4.dp)
                    .defaultMinSize(24.dp)
            )

            if (devicesSize > 1) {
                Text(
                    text = "Devices",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),

                    modifier = Modifier
                        .defaultMinSize(24.dp)
                )
            } else {
                Text(
                    text = "Device",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),

                    modifier = Modifier
                        .defaultMinSize(24.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceItem(device: Device, selectDevice: (Device) -> Unit) {
    Box(Modifier.padding(2.dp)) {
        Card(
            Modifier
                .padding(4.dp)
                .clickable { selectDevice(device) },
            elevation = 10.dp,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top

                ) {
                    Column() {
                        Text(
                            device.ipAddress,
                            color = if (device.isActive) Purple500 else Color.LightGray,
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            device.macAddress,
                            color = if (device.isActive) Color.Unspecified else Color.LightGray,
                            style = TextStyle(
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Text(
                        device.deviceName.uppercase(),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = if (device.isActive) Color.Unspecified else Color.LightGray,
                        )
                    )
                }

                Text(
                    device.macVendor.uppercase(),
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = if (device.isActive) Color.Unspecified else Color.LightGray,
                    )
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DeviceScannerTheme {
        HomeScreen(selectDevice = {

        })
    }
}