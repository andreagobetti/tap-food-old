package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 21/08/15.
 */
public class WsLoginCA extends AsyncTask<Void, Void, String> {

    boolean gotError = false;


    private Callbacks myCallbacks;

    private Context myContext;

    private String myCA;
    private String myCode;

    public interface Callbacks{
        void ok(String name);
        void wrong();
        void ko();
    }

    public WsLoginCA(Context context, String commercialActivityId, String securityCode, Callbacks callbacks )
    {
        myCallbacks = callbacks;
        myContext = context;

        try {
            myCA = URLEncoder.encode(commercialActivityId, "UTF-8");
            myCode = URLEncoder.encode(securityCode, "UTF-8");
        }catch(UnsupportedEncodingException ex){
            String e = "Errore Encoding... non finira mai qui";
        }

    }

    @Override
    protected String doInBackground(Void... voids) {

        InputStream is = null;

        String json = "";

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }

            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            String urlString = "http://app.tap-food.com/ws/tm/login-ca.php?commercialActivityId=" + myCA + "&securityCode=" + myCode;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200)
                throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );

            DataInputStream st = new DataInputStream(conn.getInputStream());
            BufferedReader d = new BufferedReader(new InputStreamReader(st));

            StringBuffer sb = new StringBuffer();
            String s = "";

            while( (s = d.readLine()) != null){
                sb.append(s);
            }

            json = sb.toString(); // CONTENUTO PAGINA


        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore login CommercialActivity", ex.toString());

        }
        finally {

            return json;

        }

    }

    @Override
    protected void onPostExecute( String j) {

        if(gotError){
            myCallbacks.ko();
            //errore WS
        }else{
            //ws ok
            DBHelper db = DBHelper.getInstance(myContext);
            try{



                JSONObject reader = new JSONObject(j);

                boolean logged = reader.getBoolean("Logged");

                if(logged){

                    JSONArray commercialActivitys = reader.getJSONArray("CommercialActivity");

                    db.addOrUpdateCommercialActivitys(commercialActivitys);
                    db.addOrUpdateAnagraphicInformations(reader.getJSONArray("AnagraphicInformation"));

                    int id = commercialActivitys.getJSONObject(0).getInt("commercialActivityId");

                    db.associateCommercialActivity(id);




                    myCallbacks.ok(commercialActivitys.getJSONObject(0).getString("name"));
                }else{
                    myCallbacks.wrong();
                }

            }catch(Exception ex){
                db.dissociateCommercialActivity();

                Logger.Error(myContext,"Errore login CommercialActivity",ex.toString());
                myCallbacks.ko();
            }
        }


    }
}
