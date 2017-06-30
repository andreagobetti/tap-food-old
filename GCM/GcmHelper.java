package com.lynkteam.tapmanager.GCM;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.WS.WsUpdateGCMToken;
import com.lynkteam.tapmanager.printers.PrinterEpsonBG;
import com.lynkteam.tapmanager.util.WifiUtil;

import java.io.IOException;

/**
 * Created by robertov on 30/07/2015.
 */
public class GcmHelper {

    private final Context mContext;
    private String token;


    public static interface  Callbacks{
       void ok(String t);
    }

    public GcmHelper(Context context, Callbacks callbacks) {
        mContext = context;
        myCallbacks = callbacks;
    }

    public GcmHelper.Callbacks myCallbacks;

    /**
     * Register for GCM
     *
     * @param senderId the project id used by the app's server
     */

    public void getGcmTokenInBackground(final String senderId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    token =  InstanceID.getInstance(mContext).getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    myCallbacks.ok(token);

                } catch (final IOException e) {
                    String bottato = "Eccezione";
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);



            }
        }.execute();
    }

    /**
     * Unregister by deleting the token
     *
     * @param senderId the project id used by the app's server
     */
    public void deleteGcmTokeInBackground(final String senderId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InstanceID.getInstance(mContext).deleteToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);

                } catch (final IOException e) {
                    String bottato = "Eccezione";
                }
                return null;
            }
        }.execute();
    }

    public String getToken(){
        return token;
    }
}
