package com.app.yamamz.yamamzipscanner.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by yamamz on 10/9/2016.
 */
public class Wireless {
    private final Context context;
    private String internalIpAddress;
    private int subnetBits;
    private String ssid;

    /**
     * Constructor to set the activity for context
     *
     * @param context The activity to use for context
     */
    public Wireless(Context context) {
        this.context = context;
    }



    /**
     * Gets the device's wireless address
     *
     * @return Wireless address
     */
    private InetAddress getWifiInetAddress() {
        String ipAddress = this.getInternalWifiIpAddress();
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Determines if the network that the device is connected to is a hidden network
     *
     * @return True if the network is hidden, false if it's not
     */
    public boolean isHidden() {
        return this.getWifiInfo().getHiddenSSID();
    }

    /**
     * Gets the signal strength of the wireless network that the device is connected to
     *
     * @return Signal strength
     */
    public int getSignalStrength() {
        return this.getWifiInfo().getRssi();
    }

    /**
     * Gets the BSSID of the wireless network that the device is connected to
     *
     * @return BSSID
     */
    public String getBSSID() {
        return this.getWifiInfo().getBSSID();
    }

    /**
     * Gets the SSID of the wireless network that the device is connected to
     *
     * @return SSID
     */
    public String getSSID() {
        String ssid = this.getWifiInfo().getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        return ssid;
    }

    /**
     * Gets the device's internal LAN IP address associated with the WiFi network
     *
     * @return Local WiFi network LAN IP address
     */
    public String getInternalWifiIpAddress() {
        int ip = this.getWifiInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

        try {
            return InetAddress.getByAddress(ipByteArray).getHostAddress();

        } catch (UnknownHostException ex) {
            return null;
        }

    }

    /*
     * Gets the Wifi Manager DHCP information and returns the Netmask of the
     * internal Wifi Network as an int
     *
     * @return Internal Wifi Subnet Netmask
     */
    public int getInternalWifiSubnet() {
        WifiManager wifiManager = this.getWifiManager();
        if (wifiManager == null) {
            return 0;
        }

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null) {
            return 0;
        }

        int netmask = Integer.bitCount(dhcpInfo.netmask);
        /*
         * Workaround for #82477
         * https://code.google.com/p/android/issues/detail?id=82477
         * If dhcpInfo returns a subnet that cannot exist, then
         * look up the Network interface instead.
         */
        if (dhcpInfo.netmask < 8 || dhcpInfo.netmask > 32) {
            try {
                InetAddress inetAddress = this.getWifiInetAddress();
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    if (inetAddress != null && inetAddress.equals(address.getAddress())) {
                        return address.getNetworkPrefixLength(); // This returns a short of the CIDR notation.
                    }
                }
            } catch (SocketException ignored) {
            }
        }

        return netmask;
    }

    /**
     * Gets the device's internal LAN IP address associated with the cellular network
     *
     * @return Local cellular network LAN IP address
     */
    public static String getInternalMobileIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en != null && en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            return "Unknown";
        }

        return "Unknown";
    }

    /**
     * Gets the device's external (WAN) IP address
     *
     * @param delegate Called when the external IP address has been fetched
     */


    /**
     * Gets the current link speed of the wireless network that the device is connected to
     *
     * @return Wireless link speed
     */
    public int getLinkSpeed() {
        return this.getWifiInfo().getLinkSpeed();
    }

    /**
     * Determines if the device is connected to a WiFi network or not
     *
     * @return True if the device is connected, false if it isn't
     */
    public boolean isConnectedWifi() {
        NetworkInfo info = this.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    /**
     * Gets the Android WiFi manager in the context of the current activity
     *
     * @return WifiManager
     */
    private WifiManager getWifiManager() {
        return (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Gets the Android WiFi information in the context of the current activity
     *
     * @return WiFi information
     */
    private WifiInfo getWifiInfo() {
        return this.getWifiManager().getConnectionInfo();
    }

    /**
     * Gets the Android connectivity manager in the context of the current activity
     *
     * @return Connectivity manager
     */
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Gets the Android network information in the context of the current activity
     *
     * @return Network information
     */
    private NetworkInfo getNetworkInfo(int type) {
        ConnectivityManager manager = this.getConnectivityManager();
        if (manager != null) {
            return manager.getNetworkInfo(type);
        }
        return null;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif: all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b: macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {}
        return "02:00:00:00:00:00";
    }

}
