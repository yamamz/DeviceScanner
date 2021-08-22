package com.app.yamamz.yamamzipscanner.ui.details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.model.Port
import com.app.yamamz.yamamzipscanner.ui.home.DeviceItem
import com.app.yamamz.yamamzipscanner.ui.home.HomeViewModel
import com.app.yamamz.yamamzipscanner.ui.theme.Purple200
import com.app.yamamz.yamamzipscanner.ui.theme.Purple500
import com.stealthcopter.networktools.WakeOnLan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DetailsScreen(device: Device, pressOnBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<DetailsViewModel>()
    // Creates a CoroutineScope bound to the DetailsScreen's lifecycle
    val scope = rememberCoroutineScope()
    val isLoading: Boolean by viewModel.isLoading.observeAsState(false)
    val ping: String by viewModel.ping.observeAsState("Pinging")
    val externalIp: String by viewModel.externalIp.observeAsState("")
    val openPorts: List<Port> by viewModel.openPorts.observeAsState(emptyList())
    viewModel.pingIp(device.ipAddress)
    viewModel.getExternalIp()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch(Dispatchers.IO) {
                    viewModel.searchOpenPorts(device.ipAddress, scope = scope)
                }
            }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Localized description",
                    Modifier.size(32.dp),
                    Color.White
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Purple500)
                .padding(all = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        pressOnBack()
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Localized description",
                            Modifier.size(32.dp),
                            Color.White
                        )
                    }
                    Text(
                        device.ipAddress,
                        Modifier.padding(16.dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),

                        )
                }
                DeviceCard(device = device, ping = ping, externalIp = externalIp)
                Box(modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Purple200)
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Waking Device on Magic packets", Toast.LENGTH_LONG).show()
                        scope.launch(Dispatchers.IO) {
                            WakeOnLan.sendWakeOnLan(device.ipAddress, device.macAddress);
                        }
                    }
                    .padding(24.dp)


                ) {
                 Row(
                     horizontalArrangement = Arrangement.Center,
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Icon(
                         Icons.Default.CheckCircle,
                         contentDescription = "Localized description",
                         Modifier.size(24.dp),
                         Color.White
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("Wake on LAN", color = Color.White)
                 }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 60.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text(
                        "Scanning open ports",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                } else if (!isLoading && openPorts.isNotEmpty()) {
                    LazyColumn(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(all = 8.dp),
                        verticalArrangement = Arrangement.Center

                    ) {
                        items(openPorts) {
                            Card(
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "PORT:${it.port}",
                                        style = TextStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        it.date, color = Purple500,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Light,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }

                    }
                }  else if(openPorts.isEmpty() && !isLoading) {
                    Text(
                        "Press the scan button to scan ports",

                        modifier = Modifier.padding(vertical = 80.dp, horizontal=20.dp),
                        style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }


            }
        }
    }

}

@Composable
fun DeviceCard(device: Device, ping: String, externalIp:String) {
    Box(Modifier.padding(2.dp)) {
        Card(Modifier.padding(4.dp)) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "IP Address",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        device.ipAddress, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Mac",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        device.macAddress, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Vendor",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        device.macVendor, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Hostname",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        device.deviceName, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Ping",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        ping, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "External IP",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        externalIp, color = Purple500,
                        style = TextStyle(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}