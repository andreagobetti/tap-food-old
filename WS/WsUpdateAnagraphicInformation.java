package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.AnagraphicInformation;
import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robertov on 31/08/15.
 */

public class WsUpdateAnagraphicInformation extends AsyncTask<Void, Void, String> {

    private boolean gotError = false;
    private boolean secondAttempt = false;
    private boolean expiredToken = false;

    private Callbacks myCallbacks;
    private AnagraphicInformation myAnagraphicInformation;
    private Context myContext;

    private JSONObject myJSON;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsUpdateAnagraphicInformation(Context context, AnagraphicInformation anagraphicInformation, Callbacks callbacks) {
        constructor(context,anagraphicInformation,callbacks);
    }

    public WsUpdateAnagraphicInformation(Context context, AnagraphicInformation anagraphicInformation, boolean secondAttempt, Callbacks callbacks) {
        this.secondAttempt = secondAttempt;
        constructor(context, anagraphicInformation,callbacks);
    }

    private void constructor(Context context,  AnagraphicInformation anagraphicInformation, Callbacks callbacks){
        try {
            myCallbacks = callbacks;
            myContext = context;
            myJSON = new JSONObject();
            myAnagraphicInformation = anagraphicInformation;


            myJSON.put("commercialActivityId", TapManager.COMMERCIAL_ACTIVITY_ID);
            myJSON.put("anagraphicInformationId",anagraphicInformation.anagraphicInformationId);
            myJSON.put("street",anagraphicInformation.street);
            myJSON.put("streetNumber",anagraphicInformation.streetNumber);
            myJSON.put("city",anagraphicInformation.city);
            myJSON.put("district",anagraphicInformation.district);
            myJSON.put("countryCode",anagraphicInformation.countryCode);
            myJSON.put("telephoneNumber",anagraphicInformation.telephoneNumber);
            myJSON.put("eMailAddress",anagraphicInformation.emailAddress);


        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore durante invio update AnagraphicInformation", ex.toString());
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

            URL url = new URL("http://app.tap-food.com/ws/tm/updateAnagraphicInformation.php");
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
            Logger.Error(myContext, "Errore durante invio update Anagraphic Information", ex.toString());

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
                        new WsUpdateAnagraphicInformation(myContext, myAnagraphicInformation, true, new Callbacks() {
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
            Logger.Info(myContext, "Invio Update AnagraphicInformation completato");
            myCallbacks.ok();
        }


    }


}
