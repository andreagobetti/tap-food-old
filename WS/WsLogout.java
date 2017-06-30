package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.NetworkUtil;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsLogout extends AsyncTask<Void, Void, String> {

    public static String AUTH_TOKEN;

    private boolean gotError = false;
    private Callbacks myCallbacks;
    private Context myContext;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsLogout(Context context, Callbacks callbacks)
    {
        myCallbacks = callbacks;
        myContext = context;
    }


    @Override
    protected String doInBackground(Void... voids) {
        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED



            URL url = new URL("http://app.tap-food.com/ws/tm/logout.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setDoInput(true);

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);


        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore durante il logout", ex.toString());

        }
        finally {

            return "";

        }

    }

    @Override
    protected void onPostExecute( String token) {

        if(gotError){
            myCallbacks.ko();
        }else{
            //ws ok
            WsLogout.AUTH_TOKEN = "";
            myCallbacks.ok();
        }


    }


}

