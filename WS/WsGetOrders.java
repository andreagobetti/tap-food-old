package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsGetOrders extends AsyncTask<Void, Void, String> {

    private final String WS_NAME = "getOrders";

    private boolean gotError = false;
    private boolean secondAttempt = false;
    private boolean expiredToken = false;

    private String myLastUpdate;

    private Callbacks myCallbacks;

    private Context myContext;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsGetOrders(Context context, boolean secondAttempt, Callbacks callbacks ){
        this.secondAttempt = secondAttempt;

        constructor(context, callbacks);
    }

    public WsGetOrders(Context context, Callbacks callbacks)
    {
        constructor(context,callbacks);
    }

    private void constructor(Context context, Callbacks callbacks){
        myCallbacks = callbacks;
        myContext = context;

        DBHelper db = DBHelper.getInstance(myContext);

        myLastUpdate  = db.getLastWsUpdate(WS_NAME);

        myLastUpdate.replace(" ","%20");

        try {
            myLastUpdate= URLEncoder.encode(myLastUpdate,"UTF-8");

        }catch(UnsupportedEncodingException ex){
            String ciao = "Encoding non supportato";
        }
    }


    @Override
    protected String doInBackground(Void... voids) {

        String json = "";

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            //

            URL url = new URL("http://app.tap-food.com/ws/tm/getOrders.php?lastUpdate="+myLastUpdate+"&commercialActivityId="+ TapManager.COMMERCIAL_ACTIVITY_ID);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);

            if (conn.getResponseCode() != 200){
                if(conn.getResponseCode()==403 & !secondAttempt){
                    //il token è scaduto, finisce l'execute
                    gotError = true;
                    expiredToken = true;
                    return "";
                }else
                    throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );
            }

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
            Logger.Error(myContext, "Errore download ordini", ex.toString());

        }
        finally {

            return json;

        }

    }

    @Override
    protected void onPostExecute( String json) {

         if(gotError){
            //se il token è scaduto e non è il secondo tentativo, faccio un altro login e richiamo il ws
            if(expiredToken && !secondAttempt){
                String[] credentials = DBHelper.getInstance(myContext).getCurrentlyLoggedUserCredentials();

                new WsLogin(credentials[0], credentials[1], myContext, new WsLogin.Callbacks() {
                    @Override
                    public void ok(String t, String u, String p) {
                        new WsGetOrders(myContext, true, new Callbacks() {
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
            try{
                DBHelper db = DBHelper.getInstance(myContext);

                JSONObject reader = new JSONObject(json);


                JSONArray orders = reader.getJSONArray("Order");
                JSONArray orderLineItems = reader.getJSONArray("OrderLineItem");
                JSONArray customizedProducts = reader.getJSONArray("CustomizedProduct");
                JSONArray anagraphicInformations = reader.getJSONArray("AnagraphicInformation");
                JSONArray assemblageParts = reader.getJSONArray("AssemblagePart");

                int downlaoded = orders.length() + orderLineItems.length() + customizedProducts.length() + anagraphicInformations.length() + assemblageParts.length();

                db.addOrUpdateOrders(orders);
                db.addOrUpdateOrderLineItems(orderLineItems);
                db.addOrUpdateCustomizedProducts(customizedProducts);
                db.addOrUpdateAnagraphicInformations(anagraphicInformations);
                db.addOrUpdateAssemblageParts(assemblageParts);

                Logger.Info(myContext, "Download getOrders sul DB locale completato, Count: " + Integer.toString(downlaoded));

                String lastUpdate = reader.getString("CurrentTimestamp");
                db.setLastWsUpdate(WS_NAME, lastUpdate);

                db.close();

                myCallbacks.ok();



            }catch(Exception ex){
                Logger.Error(myContext,"Errore salvataggio ordini sul DB locale",ex.toString());
                myCallbacks.ko();
            }
        }


    }


}

