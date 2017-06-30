package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.NetworkUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsAppUpdateCheck extends AsyncTask<Void, Void, String> {
    private boolean gotError = false;


    private Callbacks myCallbacks;

    private Context myContext;

    public interface Callbacks{
        void ok(String version);
        void ko();
    }


    public WsAppUpdateCheck(Context context, Callbacks callbacks)
    {
        myCallbacks = callbacks;
        myContext = context;
    }


    @Override
    protected String doInBackground(Void... voids) {

        String version = "";

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            //

            URL url = new URL("http://app.tap-food.com/ws/tm/getLatestAppVersion.php");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);

            if (conn.getResponseCode() != 200){
                throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );
            }

            DataInputStream st = new DataInputStream(conn.getInputStream());
            BufferedReader d = new BufferedReader(new InputStreamReader(st));

            StringBuffer sb = new StringBuffer();
            String s = "";

            while( (s = d.readLine()) != null){
                sb.append(s);
            }

            version = sb.toString(); // CONTENUTO PAGINA

        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore controllo aggiornamenti app", ex.toString());

        }
        finally {

            return version;

        }

    }

    @Override
    protected void onPostExecute( String version) {

         if(gotError){
            myCallbacks.ko();
            //errore WS
        }else{
            //ws ok
            try{

                Logger.Info(myContext, "Ricerca aggiornamenti app completata: " + version);

                myCallbacks.ok(version);

            }catch(Exception ex){
                Logger.Error(myContext,"Errore controllo aggiornamenti app",ex.toString());
                myCallbacks.ko();
            }
        }


    }


}

