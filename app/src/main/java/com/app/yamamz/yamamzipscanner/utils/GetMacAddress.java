package com.app.yamamz.yamamzipscanner.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GetMacAddress {
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" +");
                if (split.length >= 4 && ip.equals(split[0])) {
                    // Basic sanity check
                    String mac = split[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
