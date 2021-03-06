package com.lynkteam.tapmanager.WS;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by robertov on 31/08/15.
 */

public class WsUpdateWarehouse extends AsyncTask<Void, Void, String> {

    private boolean gotError = false;
    private boolean secondAttempt = false;
    private boolean expiredToken = false;

    private Callbacks myCallbacks;
    private Context myContext;
    private JSONObject myJSON;

    private ContentValues myValues;
    private ArrayList<ContentValues> myPrices;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsUpdateWarehouse(Context context, ContentValues values, ArrayList<ContentValues> prices, Callbacks callbacks) {
        constructor(context,values,prices,callbacks);
    }

    public WsUpdateWarehouse(Context context, ContentValues values, ArrayList<ContentValues> prices, boolean secondAttempt, Callbacks callbacks) {
        this.secondAttempt = secondAttempt;
        constructor(context,values,prices,callbacks);
    }

    private void constructor(Context context, ContentValues values, ArrayList<ContentValues> prices, Callbacks callbacks){
        try {
            myCallbacks = callbacks;
            myContext = context;
            myValues = values;
            myPrices = prices;

            myJSON = new JSONObject();


            myJSON.put("warehouseId",values.get("warehouseId"));
            myJSON.put("isAvailable",values.get("isAvailable"));
            myJSON.put("lastEdit",values.get("lastEdit"));


            myJSON.put("commercialActivityId", TapManager.COMMERCIAL_ACTIVITY_ID);

            JSONArray jPrices = new JSONArray();
            for(int i=0;i<prices.size();i++){

                JSONObject jPrice = new JSONObject();
                jPrice.put("ingredientId",prices.get(i).get("ingredientId"));
                jPrice.put("productId",prices.get(i).get("productId"));
                jPrice.put("price",prices.get(i).get("price"));

                jPrices.put(jPrice);
            }

            myJSON.put("prices",jPrices);

        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore durante invio update warehouse", ex.toString());
        }
    }


    @Override
    protected String doInBackground(Void... voids) {
        try {
            if(gotError) return "";

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            URL url = new URL("http://app.tap-food.com/ws/tm/updateWarehouse.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setDoInput(true);

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");



            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            wr.writeBytes(myJSON.toString());

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
            Logger.Error(myContext, "Errore durante invio update warehouse", ex.toString());

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
                        new WsUpdateWarehouse(myContext, myValues, myPrices, true, new Callbacks() {
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
        }else{
            Logger.Info(myContext, "Invio Update Warehouse completato");
            myCallbacks.ok();
        }


    }


}
