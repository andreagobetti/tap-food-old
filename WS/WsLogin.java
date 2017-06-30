package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.NetworkUtil;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsLogin extends AsyncTask<Void, Void, String> {

    public static String AUTH_TOKEN;
    public static String USER_ID;


    boolean gotError = false;

    private String myUsername;
    private String myPassword;

    private Callbacks myCallbacks;

    private Context myContext;

    public interface Callbacks{
        void ok(String t, String u, String p);
        void ko(String u, String p);
    }

    public WsLogin(String username, String password, Context context, Callbacks callbacks)
    {
        myCallbacks = callbacks;
        myContext = context;

        try {
            myUsername = URLEncoder.encode(username, "UTF-8");
            myPassword = URLEncoder.encode(password, "UTF-8");
        }catch(UnsupportedEncodingException ex){
            String e = "Errore Encoding... non finira mai qui";
        }
    }


    @Override
    protected String doInBackground(Void... voids) {


        String authToken = "";

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connettività assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            URL url = new URL("http://app.tap-food.com/ws/tm/login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setDoInput(true);

            conn.setRequestMethod("POST");

            String urlParameters = "usr="+myUsername+"&pwd="+myPassword;


            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            wr.writeBytes(urlParameters);

            wr.flush();
            wr.close();

            authToken = conn.getHeaderField("Authentication-Token");
            USER_ID = conn.getHeaderField("User-Id");


        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext,"Errore durante il login", ex.toString());

        }
        finally {

            return authToken;

        }

    }

    @Override
    protected void onPostExecute( String token) {

        if(gotError){
            myCallbacks.ko(myUsername, myPassword);
            //errore WS
        }else{
            //ws ok
            WsLogin.AUTH_TOKEN = token;
            myCallbacks.ok(token, myUsername, myPassword);
        }


    }


}

