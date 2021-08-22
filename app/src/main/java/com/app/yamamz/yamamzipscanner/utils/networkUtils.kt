package com.app.yamamz.yamamzipscanner.utils

import android.util.Log
import com.app.yamamz.yamamzipscanner.utils.InitRange.intToIp
import com.app.yamamz.yamamzipscanner.utils.InitRange.ipToInt
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Exception

fun getMacFromArpCache(ip: String?): String? {
    if (ip == null) return null
    var br: BufferedReader? = null
     val pattern = "..:..:..:..:..:.."
    try {
        br = BufferedReader(FileReader("/proc/net/arp"))
        var line:String
        while (br.readLine().also { line = it } != null) {
            val split = line.split(" +").toTypedArray()
            if (split.size >= 4 && ip == split[0]) {
                // Basic sanity check
                val mac = split[3]
                return if (mac.matches(Regex(pattern))) {
                    mac
                } else {
                    null
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            br?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return null
}


fun getSubnetMask(ip: String?): String? {
    val checkclass = ip?.substring(0, 3)
    val cc = checkclass?.toInt()
    var mask: String? = null
    if (cc in 1..223) {
        if (cc != null) {
            if (cc < 128) {
                mask = "255.0.0.0"
            }
        }
        if (cc in 128..191) {
            mask = "255.255.0.0"
        }
        if (cc != null) {
            if (cc > 191) {
                mask = "255.255.255.0"
            }
        }
    }
    return mask
}

fun rangeFromCidr(cidrIp: String): IntArray {
    val maskStub = 1 shl 31
    val atoms = cidrIp.split("/").toTypedArray()

    val mask = atoms[1].toInt()
    val result = IntArray(2)
    result[0] =
       ipToInt(atoms[0]) and (maskStub shr mask - 1) // lower bound
    result[1] = ipToInt(atoms[0]) // upper bound
    println(intToIp(result[0]))
    println(intToIp(result[1]))
    return result
}

fun getSubnetMaskFromBits(cidrMask:Int): String {
    var bits: Long = 0
    bits = (-0x1 xor (1 shl 32 - cidrMask) - 1).toLong()
  return String.format(
        "%d.%d.%d.%d",
        bits and 0x0000000000ff000000L shr 24,
        bits and 0x0000000000ff0000 shr 16,
        bits and 0x0000000000ff00 shr 8,
        bits and 0xff
    )
}


