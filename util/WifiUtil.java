package com.lynkteam.tapmanager.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by robertov on 21/08/15.
 */
public class WifiUtil {

    public static String getMacAddress(Context context){
        WifiManager wifiMan = (WifiManager) context.getSystemService( Context.WIFI_SERVICE);
        String mac = "";
        if(wifiMan.isWifiEnabled()){
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            mac = wifiInf.getMacAddress();
        }else{
            wifiMan.setWifiEnabled(true);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            mac = wifiInf.getMacAddress();
            wifiMan.setWifiEnabled(true);
        }

        return mac;
    }
}
