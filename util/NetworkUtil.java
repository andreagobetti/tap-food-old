package com.lynkteam.tapmanager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by robertov on 31/07/2015.
 */
public class NetworkUtil {

    //Verifica se il dispositivo e connesso ad internet
    public static boolean networkConnected(Context myContext){
        ConnectivityManager cm = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&  activeNetwork.isConnectedOrConnecting();

    }
}
