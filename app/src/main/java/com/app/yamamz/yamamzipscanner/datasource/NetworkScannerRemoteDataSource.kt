package com.app.yamamz.yamamzipscanner.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.model.Port
import com.app.yamamz.yamamzipscanner.utils.*
import com.stealthcopter.networktools.Ping
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

interface NetworkScannerRemoteDataSource {
    suspend fun getDevicesOnNetwork(ipString: String, scope: CoroutineScope): List<Device>
    suspend fun getOpenPort(ipString: String, scope: CoroutineScope): List<Port>
    suspend fun checkWifiConnected(): Boolean
    suspend fun getWifiInternalIpAddress(): String
    suspend fun getPing(ip: String): String
    suspend fun getExternalIp(): String
}

class NetworkScannerRemoteDataSourceImpl @Inject constructor() : NetworkScannerRemoteDataSource {
    @Inject
    lateinit var context: Context
    override suspend fun getDevicesOnNetwork(
        ipString: String,
        scope: CoroutineScope
    ): List<Device> {
        val devices: ArrayList<Device> = ArrayList()
        try {
            val bounds: IntArray = getRangeFromStringIp(ipString)
            val ips: Array<Int> = (bounds[0]..bounds[1]).toList().toTypedArray()

            Log.e("IP", ips.size.toString())
            val jobs = List(ips.size) {
                // launch a lot of coroutines and list their jobs
                scope.launch(Dispatchers.IO) {
                    scope.launch(Dispatchers.IO) {
                        val host: String = InitRange.intToIp(ips[it])
                        // Add the device that scan
                        if(host == Wireless(context).internalWifiIpAddress) {
                            devices.add(
                                Device(
                                    host,
                                    "Your Device",
                                    Wireless.getMacAddr(),
                                    "",
                                    true
                                )
                            )
                        } else {
                          val  device = checkIpIsAlive(ips[it])
                            if (device != null) {
                                devices.add(device)
                            }
                        }
                    }

                }
            }

            jobs.forEach { it.join() }


            return devices
        } catch (e: Exception) {
            Log.e("ErrorSearch", e.stackTraceToString())
        }
        return devices
    }


    override suspend fun getOpenPort(ipString: String, scope: CoroutineScope): List<Port> {
        val ports: ArrayList<Port> = ArrayList()
        try {
            val jobs = List(65535) {
                // launch a lot of coroutines and list their jobs
                var count = 0
                scope.launch(Dispatchers.IO) {
                    scope.launch(Dispatchers.IO) {
                        val isOpenPort = isOpenPort(ipString, it)
                        if (isOpenPort) {
                            Log.e("OPENPORTS", it.toString())
                            @SuppressLint("SimpleDateFormat")
                            val df = SimpleDateFormat("dd/MM/yy HH:mm:ss")
                            val calendar = Calendar.getInstance()
                            ports.add(
                                Port(
                                    it.toString(),
                                    df.format(calendar.time).toString(),
                                    count++.toString()
                                )
                            )
                        }
                    }

                }
            }
            jobs.forEach { it.join() }
            Log.e("PORTS", ports.size.toString())
        } catch (e: Exception) {
            Log.e("OPENPORTS", e.stackTraceToString())
        }
        return ports
    }

    private fun getRangeFromStringIp(ip: String): IntArray {
        return rangeFromCidr(ip)
    }

    private fun isOpenPort(ip: String, port: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 200)
            socket.close()
            Log.e("OPENPORTS", "TRUE")
            true
        } catch (ex: java.lang.Exception) {
            return false
        }
    }

    private fun checkIpIsAlive(ip: Int): Device? {
        val DPORTS = intArrayOf(139, 445, 22, 80)
        val NOMAC = "00:00:00:00:00:00"
        var device: Device? = null
        try {
            val host: String = InitRange.intToIp(ip)
            val a: InetAddress = InetAddress.getByName(host)
            val isReachable = InetAddress.getByName(host).isReachable(800)
            var mac =""
            val hostAddress = a.hostAddress
            if (hostAddress != null && isReachable) {
                Log.e("IP", hostAddress)
                // Arp Check 1
                 mac = GetMacAddress.getMacFromArpCache(hostAddress)
                if (mac != NOMAC || mac !="") {
                    Log.e("MAC", mac)
                    device = Device(
                        hostAddress,
                        a.hostName,
                        mac,
                        "",
                        true
                    )
                }
                // Arp Check 2
                mac = GetMacAddress.getMacFromArpCache(hostAddress)
                if (mac != NOMAC) {
                    Log.e("MAC", mac)
                    device = Device(
                        hostAddress,
                        a.hostName,
                        mac,
                        "",
                        true
                    )
                }

                val s = Socket()
                for (i in DPORTS.indices) {
                    try {
                        s.bind(null)
                        s.connect(InetSocketAddress(hostAddress, DPORTS[i]), 800)
                        Log.v(
                            "ERROR",
                            "found using TCP connect " + hostAddress + " on port=" + DPORTS[i]
                        )
                    } catch (e: IOException) {
                    } catch (e: IllegalArgumentException) {
                    } finally {
                        try {
                            s.close()
                        } catch (e: java.lang.Exception) {
                        }
                    }
                }

                // Arp Check 3
                mac = GetMacAddress.getMacFromArpCache(hostAddress)
                if (mac != NOMAC) {
                    Log.e("MAC", mac)
                    device = Device(
                        hostAddress,
                        a.hostName,
                        mac,
                        "",
                        true
                    )
                }

            }
        } catch (e: Exception) {
            Log.e("ERRORR", e.stackTraceToString())
        }
        return device

    }

    override suspend fun checkWifiConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            }
        }
        return false
    }

    override suspend fun getWifiInternalIpAddress(): String {
        val ip = Wireless(context).internalWifiIpAddress
        val cidrBits = Wireless(context).internalWifiSubnet
        val subnet = Subnet()
        subnet.ipAddress = ip
        val subnetMask: String = getSubnetMaskFromBits(cidrBits)
        subnet.subnetMask = subnetMask
        val broadcastAddress = subnet.broadcastAddress
        val bits = subnet.maskedBits
        return "$broadcastAddress/$bits"
    }

    @SuppressLint("DefaultLocale")
    override suspend fun getPing(ip: String): String {
        var ping = "Pinging"

        try {
            if (Ping.onAddress(ip).doPing().isReachable) {
                val pingResult = Ping.onAddress(ip).setTimeOutMillis(1000).setTimes(5)
                    .doPing()
                ping = String.format("%.2f ms", pingResult.getTimeTaken())
            }
            // Asynchronously
        } catch (e: Exception) {
            ping = "Unreachable"
        }
        return ping
    }

    override suspend fun getExternalIp(): String {
        var resp = "Getting External Ip"
        coroutineScope {
            resp = try {
                val whatismyip = URL("https://icanhazip.com")
                val input = BufferedReader(InputStreamReader(whatismyip.openStream()))
                input.readLine() //you get the IP as a String
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                "Error Occured"
            }
        }
        return resp
    }

}