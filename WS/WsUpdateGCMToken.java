package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.util.NetworkUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsUpdateGCMToken extends AsyncTask<Void, Void, String> {

    private boolean gotError = false;
    private boolean secondAttempt = false;
    private boolean expiredToken = false;

    private String gcmToken;
    private String macAddress;

    private Callbacks myCallbacks;

    private Context myContext;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsUpdateGCMToken(String token, String mac, Context context, Callbacks callbacks)
    {
       constructor(token,mac,context,callbacks);
    }

    public WsUpdateGCMToken(String token, String mac, Context context, boolean secondAttempt, Callbacks callbacks)
    {
        this.secondAttempt = secondAttempt;
        constructor(token,mac,context,callbacks);
    }

    private void constructor(String token, String mac, Context context, Callbacks callbacks){
        myCallbacks = callbacks;
        myContext = context;

        try {
            gcmToken = URLEncoder.encode(token, "UTF-8");
            macAddress = URLEncoder.encode(mac, "UTF-8");
        }catch(Exception ex){
            gotError = true;
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connettività assente");
            }

            if(gotError)
                throw new Exception("Token assente/errore Encoding");


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            URL url = new URL("http://app.tap-food.com/ws/tm/updateGCMToken.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setDoInput(true);

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);

            String urlParameters = "commercialActivityId=" + TapManager.COMMERCIAL_ACTIVITY_ID+"&macAddress="+macAddress+"&GCMToken="+gcmToken;

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            wr.writeBytes(urlParameters);

            wr.flush();
            wr.close();

            if (conn.getResponseCode() != 200){
                if(conn.getResponseCode()==403 & !secondAttempt){
                    //il token è scaduto, finisce l'execute
                    gotError = true;
                    expiredToken = true;
                    return "";
                }else
                    throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );
            }



        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext,"Errore durante invio token GCM", ex.toString());

        }
        finally {

            return "";

        }

    }

    @Override
    protected void onPostExecute( String token) {

        if(gotError){
            if(expiredToken && !secondAttempt){
                String[] credentials = DBHelper.getInstance(myContext).getCurrentlyLoggedUserCredentials();

                new WsLogin(credentials[0], credentials[1], myContext, new WsLogin.Callbacks() {
                    @Override
                    public void ok(String t, String u, String p) {
                        new WsUpdateGCMToken(gcmToken, macAddress, myContext , true, new Callbacks() {
                            @Override
                            public void ok() {
                                myCallbacks.ok();
                            }

                            @Override
                            public void ko() {
                                myCallbacks.ko();
                            }
                        }).execute();
                    }

                    @Override
                    public void ko(String u, String p) {
                        myCallbacks.ko();

                    }
                }).execute();
            }else {
                myCallbacks.ko();
            }
            //errore WS
        }else{
            //ws ok
            Logger.Info(myContext, "invio token GCM riuscito");
            myCallbacks.ok();
        }


    }


}

