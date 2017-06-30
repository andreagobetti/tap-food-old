package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.ProductionArea;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robertov on 31/08/15.
 */

public class WsUpdateProductionArea extends AsyncTask<Void, Void, String> {

    private boolean gotError = false;
    private boolean secondAttempt = false;
    private boolean expiredToken = false;

    private Callbacks myCallbacks;
    private Context myContext;
    private JSONObject myJSON;
    private ProductionArea myProductionArea;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsUpdateProductionArea(Context context, ProductionArea productionArea, Callbacks callbacks)
    {
        constructor(context,productionArea,callbacks);
    }

    public WsUpdateProductionArea(Context context, ProductionArea productionArea, boolean secondAttempt, Callbacks callbacks)
    {
        this.secondAttempt = secondAttempt;
        constructor(context,productionArea,callbacks);
    }

    private void constructor(Context context, ProductionArea productionArea, Callbacks callbacks){
        try {
            myCallbacks = callbacks;
            myContext = context;
            myProductionArea = productionArea;

            myJSON = new JSONObject();

            myJSON.put("productionAreaId",productionArea.productionAreaId);

            myJSON.put("printerModelCode",productionArea.modelCode);
            myJSON.put("description",productionArea.name);
            myJSON.put("printerIpAddress",productionArea.printerIpAddress);
            myJSON.put("printerIpPort",productionArea.printerIpPort);
            myJSON.put("isFiscal",productionArea.isFiscal);
            myJSON.put("commercialActivityId", TapManager.COMMERCIAL_ACTIVITY_ID);
            myJSON.put("lastEdit",productionArea.lastEdit);
            myJSON.put("isDeleted",productionArea.isDeleted);




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

            URL url = new URL("http://app.tap-food.com/ws/tm/updateProductionArea.php");
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
            Logger.Error(myContext, "Errore durante invio update/inserimento productionArea", ex.toString());

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
                        new WsUpdateProductionArea(myContext,myProductionArea,  true, new Callbacks() {
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
            Logger.Info(myContext, " update/inserimento productionArea completato");
            myCallbacks.ok();
        }


    }


}
