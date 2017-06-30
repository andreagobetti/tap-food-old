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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 21/08/15.
 */
public class WsGetUpdates  extends AsyncTask<Void, Void, String> {

    private final String WS_NAME = "getUpdates";

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

    public WsGetUpdates(Context context, Callbacks callbacks)
    {
        constructor(context,callbacks);
    }

    public WsGetUpdates(Context context, boolean secondAttempt, Callbacks callbacks)
    {
        this.secondAttempt = secondAttempt;
        constructor(context,callbacks);
    }

    private void constructor(Context context, Callbacks callbacks){
        myCallbacks = callbacks;
        myContext = context;

        DBHelper db = DBHelper.getInstance(myContext);

        myLastUpdate  = db.getLastWsUpdate(WS_NAME);


        myLastUpdate.replace(" ","%20");


        try {
            myLastUpdate= URLEncoder.encode(myLastUpdate, "UTF-8");

        }catch(UnsupportedEncodingException ex){
            String ciao = "Encoding non supportato";
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

            //

            //temporaneo
            URL url = new URL("http://app.tap-food.com/ws/tm/getUpdates.php?lastUpdate="+myLastUpdate+"&commercialActivityId="+ TapManager.COMMERCIAL_ACTIVITY_ID);

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
            Logger.Error(myContext, "Errore ricezione aggiornamenti", ex.toString());

        }
        finally {

            return json;

        }

    }

    @Override
    protected void onPostExecute( String j) {

        if(gotError){
            if(expiredToken && !secondAttempt){
                String[] credentials = DBHelper.getInstance(myContext).getCurrentlyLoggedUserCredentials();

                new WsLogin(credentials[0], credentials[1], myContext, new WsLogin.Callbacks() {
                        @Override
                    public void ok(String t, String u, String p) {
                        new WsGetUpdates(myContext, true, new Callbacks() {
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

                JSONObject reader = new JSONObject(j);


                JSONArray commercialActivitys = reader.getJSONArray("CommercialActivity");
                JSONArray masterCategorys = reader.getJSONArray("MasterCategory");
                JSONArray masterProducts = reader.getJSONArray("MasterProduct");
                JSONArray warehouses = reader.getJSONArray("Warehouse");
                JSONArray warehouseElements = reader.getJSONArray("WarehouseElement");
                JSONArray categorys = reader.getJSONArray("Category");
                JSONArray ingredients = reader.getJSONArray("Ingredient");
                JSONArray products = reader.getJSONArray("Product");
                JSONArray customizedProducts = reader.getJSONArray("CustomizedProduct");
                JSONArray vatReparts = reader.getJSONArray("VatRepart");
                JSONArray vatRates = reader.getJSONArray("VatRate");
                JSONArray productionAreas = reader.getJSONArray("ProductionArea");
                JSONArray holidays = reader.getJSONArray("Holiday");
                JSONArray businessHourss = reader.getJSONArray("BusinessHours");
                JSONArray orderStates = reader.getJSONArray("OrderState");
                JSONArray orderSources = reader.getJSONArray("OrderSource");
                JSONArray assemblageParts = reader.getJSONArray("AssemblagePart");
                JSONArray printerModels = reader.getJSONArray("PrinterModel");
                JSONArray employees = reader.getJSONArray("Employee");
                JSONArray anagraphicInformations = reader.getJSONArray("AnagraphicInformation");

                int downloaded = commercialActivitys.length() + masterCategorys.length() + masterProducts.length() + warehouses.length() + warehouseElements.length() +
                        categorys.length() + ingredients.length() + products.length() + customizedProducts.length() + vatReparts.length() + vatRates.length() + productionAreas.length() +
                        holidays.length() + businessHourss.length() + orderStates.length() + orderSources.length() + assemblageParts.length() + printerModels.length() + employees.length() +
                        anagraphicInformations.length();

                db.addOrUpdateCommercialActivitys(commercialActivitys);
                db.addOrUpdateMasterCategorys(masterCategorys);
                db.addOrUpdateMasterProducts(masterProducts);
                db.addOrUpdateWarehouses(warehouses);
                db.addOrUpdateWarehouseElements(warehouseElements);
                db.addOrUpdateCategorys(categorys);
                db.addOrUpdateIngredients(ingredients);
                db.addOrUpdateProducts(products);
                db.addOrUpdateCustomizedProducts(customizedProducts);
                db.addOrUpdateVatReparts(vatReparts);
                db.addOrUpdateVatRates(vatRates);
                db.addOrUpdateProductionAreas(productionAreas);
                db.addOrUpdateHolidays(holidays);
                db.addOrUpdateBusinessHours(businessHourss); //unica tb al plurale
                db.addOrUpdateOrderStates(orderStates);
                db.addOrUpdateOrderSources(orderSources);
                db.addOrUpdateAssemblageParts(assemblageParts);
                db.addOrUpdatePrinterModels(printerModels);
                db.addOrUpdateEmployees(employees);
                db.addOrUpdateAnagraphicInformations(anagraphicInformations);


                for(int i=0;i<warehouseElements.length();i++){
                    JSONObject wElement = warehouseElements.getJSONObject(i);

                    String imgUrl = wElement.getString("presentationImage");
                    int weId = wElement.getInt("warehouseElementId");

                    new WsDownloadWarehouseImages(myContext, weId, imgUrl, new WsDownloadWarehouseImages.Callbacks() {
                        @Override
                        public void ok() {

                        }

                        @Override
                        public void ko() {

                        }
                    }).execute();
                }

                Logger.Info(myContext, "Download aggiornamenti sul DB locale completato, Count: " + Integer.toString(downloaded));

                db.setLastWsUpdate(WS_NAME, reader.getString("CurrentTimestamp"));

                myCallbacks.ok();

            }catch(Exception ex){
                Logger.Error(myContext,"Errore salvataggio aggiornamenti sul DB locale",ex.toString());
                myCallbacks.ko();
            }
        }


    }
}
