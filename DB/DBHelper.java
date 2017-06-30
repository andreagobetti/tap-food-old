package com.lynkteam.tapmanager.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.WS.WsLogin;
import com.lynkteam.tapmanager.util.CryptoUtil;
import com.lynkteam.tapmanager.util.StreamUtil;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sarab on 24/07/2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TapManager.db";

    private static final String LOGGED_USR_STR = "u4b7dh5dd8a97ad6fha8sash";
    private static final String LOGGED_ACT_STR = "nxyd6smnvyjh0fqs5tofcwiy";

    private Context myContext;

    private static DBHelper sInstance;

    //private HashMap hp;
    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx

        if (sInstance == null) {
            sInstance = new DBHelper(context);
        }
        return sInstance;
    }


    public DBHelper(Context context)
    {

        super(context, DATABASE_NAME, null, 1);

        myContext = context;
    }

    //chiamato durante installazione app
    @Override
    public void onCreate(SQLiteDatabase db) {

        InputStream is = myContext.getResources().openRawResource(R.raw.create_db);


        String SQL = StreamUtil.streamToString(is);

        String[] SQLs = SQL.split(";");

        for(String q: SQLs){
            if(q.trim() != "")
                db.execSQL(q);
        }

        db.execSQL(
                "CREATE TABLE util (id INTEGER PRIMARY KEY, version INTEGER NOT NULL);"
        );
        db.execSQL(
                "INSERT INTO util (id, version) VALUES (1,1);"
        );


    }

    //aggiornamento app
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: gestione aggiornamento db app
    }

    public boolean checkLogin(String username, String password)
    {
        boolean toReturn = false;
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("SELECT Count(*) FROM TapManagerUser WHERE username = ? AND password = ?", new String[]{username, password});
            res.moveToFirst();

            toReturn = (res.getInt(0) > 0);

            //db.close();
        }
        catch(Exception ex){
            Log.e("CHECK_LOGIN", "errore nel select sul db, eccezione: " + ex.getMessage());
            toReturn = false;
        }
        finally{
            return toReturn;
        }
    }

    //elimino dal DB (se esiste) l'utente specificato
    public void deleteInvalidUser(String username, String password){
        try{
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete("TapManagerUser","username = ? AND password = ?", new String[]{username, password} );

            //db.close();
        }catch(Exception ex){

        }
    }

    public String getStatoOrdine(String id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT name FROM OrderState WHERE orderStateCode = ?", new String[]{id});
        res.moveToFirst();


        //db.close();
        return res.getString(0);

    }

    public String getPrinterStatusOrdine(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT printerStatus FROM 'Order' WHERE orderId = ?", new String[]{Integer.toString(id)});
        res.moveToFirst();


        //db.close();
        return res.getString(0);

    }

    public String getAddressShort(int id, String separator){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT street,streetNumber,city FROM AnagraphicInformation WHERE anagraphicInformationId = ?", new String[]{Integer.toString(id)} );
        res.moveToFirst();

        //db.close();

        return res.getString(0) + " " + res.getString(1) + separator + res.getString(2);
    }

    //inserisco log
    public void saveLog(String type, String text, String exception){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Type",type);
        values.put("Text",text);
        values.put("Exception",exception);
        values.put("logDateTime",getDateTime());

        db.insert("Logs", null, values);

        //db.close();
    }

    //salvo nel DB (se non esiste) l'utente specificato
    public void saveValidUser(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (!this.checkLogin(username, password)){
            //se non c'e gia nel DB, lo inserisco e lo salvo come logged
            values.put("username",username);
            values.put("password",password);
            values.put("loginHash", LOGGED_USR_STR);

            db.insert("TapManagerUser", null, values);

        }else{
            //se gia c'e allora solo lo salvo come lgoged

            values.put("loginHash", LOGGED_USR_STR);
            db.update("TapManagerUser", values, "username = ? AND password = ?", new String[]{username, password});
        }
        //db.close();
    }

    public String getLoggedCommercialActivityName(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("CommercialActivity", new String[]{"name"}, "loginHash = ?", new String[]{LOGGED_ACT_STR}, null, null, null);

        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public String getLoggedCommercialActivityId(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("CommercialActivity", new String[]{"commercialActivityId"}, "loginHash = ?", new String[]{LOGGED_ACT_STR}, null, null, null);

        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    public void associateCommercialActivity(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("loginHash", LOGGED_ACT_STR);

        db.update("CommercialActivity", contentValues, "commercialActivityId = ? ", new String[]{Integer.toString(id)});
    }

    public void dissociateCommercialActivity(){

        String randomString = "";

        do {
            randomString = CryptoUtil.getRandomString();
        } while (randomString.equals("") || randomString.equals(LOGGED_ACT_STR));

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("loginHash", randomString);

        db.update("CommercialActivity", contentValues, null, null);

        db.delete("Logs", null, null);
        db.delete("wsUpdates",null,null);
        db.delete("BusinessHours",null,null);
        db.delete("Holiday",null,null);
        db.delete("AnagraphicInformation",null,null);
        db.delete("CommercialActivity",null,null);
        db.delete("VatRepart",null,null);
        db.delete("CustomizedProduct",null,null);
        db.delete("Category",null,null);
        db.delete("Ingredient",null,null);
        db.delete("Product",null,null);
        db.delete("AssemblagePart",null,null);
        db.delete("OrderLineItem",null,null);
        db.delete("Warehouse",null,null);
        db.delete("'Order'",null,null);
        db.delete("ProductionArea",null,null);
        db.delete("TapManagerUser",null,null);
        db.delete("Employee",null,null);

    }

    public String[] getCurrentlyLoggedUserCredentials(){
        String[] toReturn = new String[0];
        SQLiteDatabase db = this.getReadableDatabase();
        try{

            Cursor res = db.query("TapManagerUser",new String[]{"username","password"},"loginHash = ? ", new String[]{ LOGGED_USR_STR}, null,null,null);
            res.moveToFirst();

            if(res.getCount()>0)
                toReturn = new String[]{res.getString(0),res.getString(1)};
            else
                toReturn = new String[0];

            res.close();

        }
        catch(Exception ex){
            toReturn = new String[0];;
            Log.e("CHECK_LOGIN", "errore nel select sul db, eccezione: " + ex.getMessage());
        }
        finally{

            //db.close();
            return toReturn;
        }
    }

    public void setUserLogout(){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        String randomString = "";

        do {
            randomString = CryptoUtil.getRandomString();
        } while (randomString.equals("") || randomString.equals(LOGGED_USR_STR));


        values.put("loginHash",randomString);


        db.update("TapManagerUser", values, null,null);

        //db.close();
    }


    public int getVersion(){
        int version = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res =  db.rawQuery("SELECT version FROM util LIMIT 1;", null);
        if(res.getCount() > 0){
            res.moveToFirst();
            version = res.getInt(0);
        }

        //db.close();
        return  version;
    }

    public boolean setVersion(int newVersion){
        boolean toReturn = false;
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("version", newVersion);

            if(db.update("util", contentValues, null, null) > 0){
                toReturn = true;
            }

            //db.close();

        }
        catch(Exception ex){

        }
        finally {
            return toReturn;
        }
    }



    private String formatDate(String date){
        if(date != null && date != ""){
            String[] split1 = date.split(" ");
            String[] split2 = split1[0].split("/");

            StringBuilder sb = new StringBuilder();
            sb.append(split2[1]);
            sb.append("/");
            sb.append(split2[0]);
            sb.append("/");
            sb.append(split2[2]);
            sb.append(" ");
            sb.append(split1[1]);

            return sb.toString();

        }
        else{
            return date;
        }
    }

    private String getDateTime() {
        return new SimpleDateFormat(  "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format( new Date() );
    }

    public void addOrUpdateOrders(JSONArray ordini) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<ordini.length();i++){
            contentValues = new ContentValues();
            JSONObject ordine = ordini.getJSONObject(i);

            contentValues.put("orderId",ordine.getInt("orderId"));
            contentValues.put("orderStateCode",ordine.getString("orderStateCode"));
            contentValues.put("CommercialActivityId",ordine.getInt("commercialActivityId"));

            if (!ordine.isNull("transportId"))
                contentValues.put("transportId",ordine.getInt("transportId"));

            if (!ordine.isNull("isPayed"))
                contentValues.put("isPayed",ordine.getInt("isPayed"));

            if(!ordine.isNull("cashier"))
                contentValues.put("cashier",ordine.getInt("cashier"));

            if(!ordine.isNull("deliveryBoy"))
                contentValues.put("deliveryBoy",ordine.getInt("deliveryBoy"));

            contentValues.put("shippingAddress",ordine.getInt("shippingAddress"));
            contentValues.put("billingAddress",ordine.getInt("billingAddress"));

            if(!ordine.isNull("requestedDeliveryTime"))
                contentValues.put("requestedDeliveryTime", ordine.getString("requestedDeliveryTime"));

            if(!ordine.isNull("agreedDeliveryTime"))
                contentValues.put("agreedDeliveryTime",ordine.getString("agreedDeliveryTime"));

            contentValues.put("isPreOrder",ordine.getBoolean("isPreOrder"));

            if(!ordine.isNull("localOrderNumber"))
                contentValues.put("localOrderNumber",ordine.getInt("localOrderNumber"));

            if(!ordine.isNull("printerStatus"))
                contentValues.put("printerStatus",ordine.getString("printerStatus"));

            if(!ordine.isNull("notes"))
                contentValues.put("notes", ordine.getString("notes"));


            contentValues.put("isPreferred",ordine.getInt("isPreferred"));
            contentValues.put("lastEdit",ordine.getString("lastEdit"));

            contentValues.put("creationDateTime",ordine.getString("creationDateTime"));

            if(!ordine.isNull("orderSource"))
                contentValues.put("orderSource",ordine.getInt("orderSource"));

            if(!ordine.isNull("shippingPriceAtSellTime"))
                contentValues.put("shippingPriceAtSellTime",ordine.getInt("shippingPriceAtSellTime"));

            if(!ordine.isNull("savedName"))
                contentValues.put("savedName",ordine.getString("savedName"));

            if(db.update("'Order'",contentValues,"orderID = ? ",new String[]{ ordine.getString("orderId") }) == 0){
                db.insert("'Order'",null,contentValues);

            }
        }

        //db.close();

    }


    public void addOrUpdateOrderLineItems(JSONArray linee) throws Exception{

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<linee.length();i++){
            contentValues = new ContentValues();
            JSONObject linea = linee.getJSONObject(i);

            contentValues.put("orderLineItemId",linea.getInt("orderLineItemId"));

            contentValues.put("orderId",linea.getInt("orderId"));

            if(!linea.isNull("productId"))
                contentValues.put("productId", linea.getInt("productId"));

            if (!linea.isNull("customizedProductId"))
                contentValues.put("customizedProductId",linea.getInt("customizedProductId"));

            contentValues.put("priceAtSellTime",linea.getInt("priceAtSellTime"));

            contentValues.put("vatRateAtSellTime", linea.getDouble("vatRateAtSellTime"));

            if(!linea.isNull("notes"))
                contentValues.put("notes", linea.getString("notes"));

            if(db.update("OrderLineItem", contentValues, "orderLineItemId = ? ", new String[]{linea.getString("orderLineItemId")}) == 0){
                db.insert("'OrderLineItem'", null, contentValues);
            }

        }


        //db.close();
    }

    public void addOrUpdateCustomizedProducts(JSONArray prodotti) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<prodotti.length();i++) {


            JSONObject prod = prodotti.getJSONObject(i);

            if(prod.getBoolean("isDeleted")){
                db.delete("CustomizedProduct", "customizedProductId = ? ", new String[]{prod.getString("customizedProductId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("customizedProductId",prod.getInt("customizedProductId"));
                contentValues.put("vatRateId", prod.getInt("vatRateId"));

                if(!prod.isNull("commercialActivityId"))
                    contentValues.put("commercialActivityId", prod.getInt("commercialActivityId"));

                if(!prod.isNull("name"))
                    contentValues.put("name", prod.getString("name"));

                if(!prod.isNull("price"))
                    contentValues.put("price", prod.getInt("price"));

                contentValues.put("lastEdit",prod.getString("lastEdit"));

                if(!prod.isNull("categoryId")) contentValues.put("categoryId",prod.getInt("categoryId"));

                if(db.update("CustomizedProduct",contentValues,"customizedProductId = ? ", new String[]{ prod.getString("customizedProductId") }) == 0){
                    db.insert("CustomizedProduct",null,contentValues);
                }
            }
        }
        //db.close();
    }

    public void addOrUpdateMasterCategorys(JSONArray categorie) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<categorie.length();i++) {
            org.json.JSONObject cat = categorie.getJSONObject(i);

            if(cat.getBoolean("isDeleted")){
                db.delete("MasterCategory", "categoryId = ? ", new String[]{cat.getString("categoryId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("categoryId",cat.getInt("categoryId"));
                contentValues.put("name", cat.getString("name"));
                contentValues.put("lastEdit",cat.getString("lastEdit"));

                if(!cat.isNull("vatRate")) contentValues.put("vatRate", cat.getInt("vatRate"));


                if(db.update("MasterCategory",contentValues,"categoryId = ? ", new String[]{ cat.getString("categoryId") }) == 0){
                    db.insert("MasterCategory", null, contentValues);
                }
            }


        }
        //db.close();
    }

    public void addOrUpdateMasterProducts(JSONArray prodotti) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<prodotti.length();i++) {
            JSONObject prod = prodotti.getJSONObject(i);

            if(prod.getBoolean("isDeleted")){
                db.delete("MasterProduct","productId = ? ", new String[]{ prod.getString("productId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("productId",prod.getInt("productId"));
                contentValues.put("masterCategoryId", prod.getInt("masterCategoryId"));
                contentValues.put("lastEdit",prod.getString("lastEdit"));
                contentValues.put("name",prod.getString("name"));
                contentValues.put("imagePath",prod.getString("imagePath"));

                if(db.update("MasterProduct",contentValues,"productId = ? ", new String[]{ prod.getString("productId") }) == 0){
                    db.insert("MasterProduct",null,contentValues);
                }
            }
        }
        //db.close();
    }

    public void addOrUpdateWarehouses(JSONArray magazzini) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<magazzini.length();i++) {
            JSONObject mag = magazzini.getJSONObject(i);

            if(mag.getBoolean("isDeleted")){
                db.delete("Warehouse","warehouseId = ? ", new String[]{ mag.getString("warehouseId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("warehouseId",mag.getInt("warehouseId"));
                contentValues.put("commercialActivityId",mag.getInt("commercialActivityId"));
                contentValues.put("isAvailable", mag.getBoolean("isAvailable")? 1:0);
                contentValues.put("isSetUp", mag.getBoolean("isSetUp") ? 1 : 0);
                contentValues.put("lastEdit", mag.getString("lastEdit"));

                if(!mag.isNull("warehouseElementId"))
                    contentValues.put("warehouseElementId",mag.getInt("warehouseElementId"));

                if(!mag.isNull("name"))
                    contentValues.put("name",mag.getString("name"));

                if(!mag.isNull("price"))
                    contentValues.put("price",mag.getInt("price"));

                if(!mag.isNull("qty")) contentValues.put("qty", mag.getDouble("qty"));

                if(db.update("Warehouse",contentValues,"warehouseId = ? ", new String[]{ mag.getString("warehouseId") }) == 0){
                    db.insert("Warehouse",null,contentValues);
                }
            }

        }
    }

    public void addOrUpdateWarehouseElements(JSONArray elementi) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for (int i = 0; i < elementi.length(); i++) {
            JSONObject el = elementi.getJSONObject(i);

            if(el.getBoolean("isDeleted")){
                db.delete("WarehouseElement", "warehouseElementId = ? ", new String[]{ el.getString("warehouseElementId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("warehouseElementId",el.getInt("warehouseElementId"));
                contentValues.put("name",el.getString("name"));
                contentValues.put("presentationImage",el.getString("presentationImage"));
                contentValues.put("lastEdit", el.getString("lastEdit"));

                if(db.update("WarehouseElement",contentValues,"warehouseElementId = ? ", new String[]{ el.getString("warehouseElementId") }) == 0){
                    db.insert("WarehouseElement", null, contentValues);
                }
            }


        }
    }

    public void addOrUpdateCategorys(JSONArray categorie) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<categorie.length();i++) {

            org.json.JSONObject cat = categorie.getJSONObject(i);

            if(cat.getBoolean("isDeleted")){
                db.delete("Category", "categoryId = ? ", new String[]{cat.getString("categoryId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("categoryId",cat.getInt("categoryId"));
                contentValues.put("commercialActivityId",cat.getInt("commercialActivityId"));
                contentValues.put("name",cat.getString("name"));
                contentValues.put("lastEdit",cat.getString("lastEdit"));

                if(!cat.isNull("masterCategoryId"))
                    contentValues.put("masterCategoryId",cat.getInt("masterCategoryId"));

                if(!cat.isNull("vatRate"))
                    contentValues.put("vatRate",cat.getInt("vatRate"));

                if(db.update("Category",contentValues,"categoryId = ? ", new String[]{ cat.getString("categoryId") }) == 0){
                    db.insert("Category", null, contentValues);
                }
            }
        }
    }

    public void addOrUpdateIngredients(JSONArray ingredienti) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<ingredienti.length();i++) {
            JSONObject ing = ingredienti.getJSONObject(i);

            if(ing.getBoolean("isDeleted")){
                db.delete("Ingredient", "ingredientId = ? ", new String[]{ing.getString("ingredientId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("ingredientId",ing.getInt("ingredientId"));
                contentValues.put("commercialActivityId",ing.getInt("commercialActivityId"));
                contentValues.put("lastEdit",ing.getString("lastEdit"));
                contentValues.put("name",ing.getString("name"));
                contentValues.put("isAvailable",ing.getBoolean("isAvailable")? 1 : 0);

                if(!ing.isNull("masterIngredient"))
                    contentValues.put("masterIngredient",ing.getInt("masterIngredient"));

                contentValues.put("categoryId",ing.getInt("categoryId"));

                if(!ing.isNull("productionAreaId"))
                    contentValues.put("productionAreaId",ing.getInt("productionAreaId"));

                if(!ing.isNull("price"))
                    contentValues.put("price",ing.getInt("price"));

                if(!ing.isNull("warehouseId"))
                    contentValues.put("warehouseId",ing.getInt("warehouseId"));

                if(!ing.isNull("warehouseQtyForUnit"))
                    contentValues.put("warehouseQtyForUnit",ing.getInt("warehouseQtyForUnit"));

                if(!ing.isNull("minQty"))
                    contentValues.put("minQty",ing.getInt("minQty"));

                if(!ing.isNull("maxQty"))
                    contentValues.put("maxQty",ing.getInt("maxQty"));

                if(db.update("Ingredient",contentValues,"ingredientId = ? ", new String[]{ ing.getString("ingredientId") }) == 0){
                    db.insert("Ingredient",null,contentValues);
                }
            }
        }
    }

    public void addOrUpdateProducts(JSONArray prodotti) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<prodotti.length();i++) {
            JSONObject prod = prodotti.getJSONObject(i);

            if(prod.getBoolean("isDeleted")){
                db.delete("Product","productId = ? ", new String[]{ prod.getString("productId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("productId", prod.getInt("productId"));
                contentValues.put("commercialActivityId",prod.getInt("commercialActivityId"));
                contentValues.put("categoryId",prod.getInt("categoryId"));
                contentValues.put("name",prod.getString("name"));
                contentValues.put("lastEdit",prod.getString("lastEdit"));

                if(!prod.isNull("masterProduct"))
                    contentValues.put("masterProduct", prod.getInt("masterProduct"));

                if(!prod.isNull("vatRateId"))
                    contentValues.put("vatRateId",prod.getInt("vatRateId"));

                if(!prod.isNull("productionAreaId"))
                    contentValues.put("productionAreaId", prod.getInt("productionAreaId"));

                if(!prod.isNull("price"))
                    contentValues.put("price",prod.getInt("price"));

                if(!prod.isNull("warehouseId"))
                    contentValues.put("warehouseId", prod.getInt("warehouseId"));

                if(db.update("Product",contentValues,"productId = ? ", new String[]{ prod.getString("productId") }) == 0){
                    db.insert("Product",null,contentValues);
                }
            }
        }
    }

    public void addOrUpdateAnagraphicInformations(JSONArray infos) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<infos.length();i++) {

            contentValues = new ContentValues();
            JSONObject info = infos.getJSONObject(i);

            contentValues.put("anagraphicInformationId", info.getInt("anagraphicInformationId"));
            contentValues.put("street",info.getString("street"));
            contentValues.put("streetNumber",info.getString("streetNumber"));
            contentValues.put("city",info.getString("city"));
            contentValues.put("district",info.getString("district"));
            contentValues.put("countryCode",info.getString("countryCode"));


            if(!info.isNull("zipCode"))
                contentValues.put("zipCode",info.getString("zipCode"));

            if(!info.isNull("name"))
                contentValues.put("name",info.getString("name"));

            if(!info.isNull("surname"))
                contentValues.put("surname",info.getString("surname"));

            if(!info.isNull("co"))
                contentValues.put("co",info.getString("co"));

            if(!info.isNull("floor"))
                contentValues.put("floor",info.getString("floor"));

            if(!info.isNull("addressName"))
                contentValues.put("addressName",info.getString("addressName"));

            if(!info.isNull("latitude"))
                contentValues.put("latitude",info.getString("latitude"));

            if(!info.isNull("longitude"))
                contentValues.put("longitude",info.getString("longitude"));

            if(!info.isNull("telephoneNumber"))
                contentValues.put("telephoneNumber",info.getString("telephoneNumber"));

            if(!info.isNull("eMailAddress"))
                contentValues.put("eMailAddress",info.getString("eMailAddress"));

            if(!info.isNull("notes"))
                contentValues.put("notes",info.getString("notes"));

            if(db.update("AnagraphicInformation",contentValues,"anagraphicInformationId = ? ", new String[]{ info.getString("anagraphicInformationId") }) == 0){
                db.insert("AnagraphicInformation",null,contentValues);
            }
        }

    }

    public void addOrUpdateAssemblageParts(JSONArray parts) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<parts.length();i++) {

            contentValues = new ContentValues();
            JSONObject part = parts.getJSONObject(i);

            contentValues.put("assemblagePartId", part.getInt("assemblagePartId"));

            contentValues.put("customizedProductId",part.getInt("customizedProductId"));
            contentValues.put("ingredientId",part.getInt("ingredientId"));
            contentValues.put("customizedProductPart",part.getInt("customizedProductPart"));



            if(db.update("AssemblagePart",contentValues,"assemblagePartId = ? ", new String[]{ part.getString("assemblagePartId") }) == 0){
                db.insert("AssemblagePart", null, contentValues);
            }
        }
    }

    public void addOrUpdateVatReparts(JSONArray reparts) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<reparts.length();i++) {

            JSONObject repart = reparts.getJSONObject(i);

            if(repart.getBoolean("isDeleted")){
                db.delete("VatRepart", "vatRepartId = ? ", new String[]{repart.getString("vatRepartId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("vatRepartId", repart.getInt("vatRepartId"));
                contentValues.put("commercialActivityId",repart.getInt("commercialActivityId"));
                contentValues.put("vatRateId",repart.getInt("vatRateId"));
                contentValues.put("repartNumber",repart.getInt("repartNumber"));
                contentValues.put("lastEdit", repart.getString("lastEdit"));

                if(db.update("VatRepart",contentValues,"vatRepartId = ? ", new String[]{ repart.getString("vatRepartId") }) == 0){
                    db.insert("VatRepart", null, contentValues);
                }
            }
        }
    }



    public void addOrUpdateCommercialActivitys(JSONArray acts) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<acts.length();i++) {
            JSONObject act = acts.getJSONObject(i);

            if(act.getBoolean("isDeleted")){
                db.delete("CommercialActivity", "commercialActivityId = ? ", new String[]{act.getString("commercialActivityId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("commercialActivityId", act.getInt("commercialActivityId"));
                contentValues.put("lastEdit",act.getString("lastEdit"));

                if(!act.isNull("registeredOffice"))
                    contentValues.put("registeredOffice", act.getInt("registeredOffice"));

                if(!act.isNull("operationalOffice"))
                    contentValues.put("operationalOffice", act.getInt("operationalOffice"));

                if(!act.isNull("vatNumber"))
                    contentValues.put("vatNumber",act.getString("vatNumber"));

                if(!act.isNull("fiscalCode"))
                    contentValues.put("fiscalCode",act.getString("fiscalCode"));

                if(!act.isNull("likes"))
                    contentValues.put("likes",act.getInt("likes"));

                if(!act.isNull("description"))
                    contentValues.put("description",act.getString("description"));

                if(!act.isNull("shippingPrice"))
                    contentValues.put("shippingPrice",act.getInt("shippingPrice"));

                if(!act.isNull("orderImportForFreeShipping"))
                    contentValues.put("orderImportForFreeShipping",act.getInt("orderImportForFreeShipping"));

                if(!act.isNull("minimumOrderForShipping"))
                    contentValues.put("minimumOrderForShipping", act.getInt("minimumOrderForShipping"));

                if(!act.isNull("shippingPriceLimit"))
                    contentValues.put("shippingPriceLimit", act.getInt("shippingPriceLimit"));

                if(!act.isNull("shippingPriceAfterLimit"))
                    contentValues.put("shippingPriceAfterLimit", act.getInt("shippingPriceAfterLimit"));

                if(!act.isNull("name"))
                    contentValues.put("name",act.getString("name"));

                if(!act.isNull("shippingVatRateId"))
                    contentValues.put("shippingVatRateId", act.getInt("shippingVatRateId"));

                if(db.update("CommercialActivity",contentValues,"commercialActivityId = ? ", new String[]{ act.getString("commercialActivityId") }) == 0){
                    db.insert("CommercialActivity",null,contentValues);
                }
            }
        }
    }
    public void addOrUpdateVatRates(JSONArray rates) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<rates.length();i++) {

            JSONObject rate = rates.getJSONObject(i);

            if(rate.getBoolean("isDeleted")){
                db.delete("VatRate","vatRateId = ? ", new String[]{ rate.getString("vatRateId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("vatRateId", rate.getInt("vatRateId"));
                contentValues.put("ratePercent",rate.getInt("ratePercent"));
                contentValues.put("lastEdit",rate.getString("lastEdit"));

                if(!rate.isNull("name"))
                    contentValues.put("name",rate.getString("name"));

                if(db.update("VatRate",contentValues,"vatRateId = ? ", new String[]{ rate.getString("vatRateId") }) == 0){
                    db.insert("VatRate", null, contentValues);
                }
            }
        }
    }

    public void addOrUpdateProductionAreas(JSONArray areas) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<areas.length();i++) {


            JSONObject area = areas.getJSONObject(i);

            if(area.getBoolean("isDeleted")){
                db.delete("ProductionArea","productionAreaId = ? ", new String[]{ area.getString("productionAreaId") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("productionAreaId", area.getInt("productionAreaId"));
                contentValues.put("printerModelCode",area.getString("printerModelCode"));
                contentValues.put("isFiscal", area.getBoolean("isFiscal")? 1: 0);
                contentValues.put("lastEdit",area.getString("lastEdit"));

                if(!area.isNull("description"))
                    contentValues.put("description",area.getString("description"));

                if(!area.isNull("printerIpAddress"))
                    contentValues.put("printerIpAddress",area.getString("printerIpAddress"));

                if(!area.isNull("printerIpPort"))
                    contentValues.put("printerIpPort",area.getString("printerIpPort"));

                if(!area.isNull("lastSync"))
                    contentValues.put("lastSync",area.getString("lastSync"));

                if(db.update("ProductionArea",contentValues,"productionAreaId = ? ", new String[]{ area.getString("productionAreaId") }) == 0){
                    db.insert("ProductionArea", null, contentValues);
                }
            }
        }
    }

    public void addOrUpdatePrinterModels(JSONArray types) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<types.length();i++) {

            contentValues = new ContentValues();
            JSONObject type = types.getJSONObject(i);

            contentValues.put("printerModelCode",type.getString("printerModelCode"));
            contentValues.put("lastEdit",type.getString("lastEdit"));

            if(!type.isNull("name"))
                contentValues.put("name",type.getString("name"));

            if(!type.isNull("protocol"))
                contentValues.put("protocol",type.getString("protocol"));

            if(!type.isNull("canBeFiscal"))
                contentValues.put("canBeFiscal",type.getBoolean("canBeFiscal")?1:0);

            if(db.update("PrinterModel",contentValues,"printerModelCode = ? ", new String[]{ type.getString("printerModelCode") }) == 0){
                db.insert("PrinterModel",null,contentValues);
            }
        }
    }

    public void addOrUpdateHolidays(JSONArray holidays) throws Exception{
        //TODO metodo
    }

    public void addOrUpdateBusinessHours(JSONArray hours) throws Exception{
        //TODO metodo
    }

    public void addOrUpdateOrderStates(JSONArray states) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<states.length();i++) {
            JSONObject state = states.getJSONObject(i);

            if(state.getBoolean("isDeleted")) {
                db.delete("OrderState", "orderStateCode = ? ", new String[]{ state.getString("orderStateCode") });
            }else{
                contentValues = new ContentValues();

                contentValues.put("orderStateCode", state.getString("orderStateCode"));
                contentValues.put("name",state.getString("name"));
                contentValues.put("lastEdit",state.getString("lastEdit"));

                if(db.update("OrderState",contentValues,"orderStateCode = ? ", new String[]{ state.getString("orderStateCode") }) == 0){
                    db.insert("OrderState", null, contentValues);
                }
            }
        }
    }

    public void addOrUpdateOrderSources(JSONArray sources) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<sources.length();i++) {

            contentValues = new ContentValues();
            JSONObject source = sources.getJSONObject(i);

            contentValues.put("orderSourceId", source.getInt("orderSourceId"));

            contentValues.put("name",source.getString("name"));
            contentValues.put("lastEdit",source.getString("lastEdit"));


            if(db.update("OrderSource",contentValues,"orderSourceId = ? ", new String[]{ source.getString("orderSourceId") }) == 0){
                db.insert("OrderSource", null, contentValues);
            }
        }
    }

    public void addOrUpdateEmployees(JSONArray employees) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;

        for(int i=0;i<employees.length();i++) {
            JSONObject emp = employees.getJSONObject(i);

            if(emp.getBoolean("isDeleted")){
                db.delete("Employee", "employeeId = ? ", new String[]{emp.getString("employeeId")});
            }else{
                contentValues = new ContentValues();

                contentValues.put("employeeId", emp.getInt("employeeId"));
                contentValues.put("tapManagerUserId", emp.getInt("tapManagerUserId"));
                contentValues.put("name", emp.getString("name"));
                contentValues.put("lastEdit", emp.getString("lastEdit"));

                if(!emp.isNull("anagraphicInformationId"))
                    contentValues.put("anagraphicInformationId", emp.getInt("anagraphicInformationId"));

                if(!emp.isNull("surname"))
                    contentValues.put("surname", emp.getString("surname"));

                if(!emp.isNull("birthday"))
                    contentValues.put("birthday", emp.getString("birthday"));

                if(db.update("Employee",contentValues,"employeeId = ? ", new String[]{ emp.getString("employeeId") }) == 0){
                    db.insert("Employee", null, contentValues);
                }
            }


        }

    }

    public void addOrUpdateEmployeePermissions(JSONArray permissions) throws Exception{
        //TODO
    }

    public ArrayList<Order> getOpenOrders(){
        ArrayList<Order> ordini = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT O.orderId, O.orderStateCode, O.agreedDeliveryTime, " +
                "O.requestedDeliveryTime, S.name, O.isPreOrder, O.printerStatus, O.lastEdit, " +
                "Ship.street, Ship.streetNumber, Ship.city, " +
                "Bill.street, Bill.streetNumber, Bill.city," +
                "O.orderSource, Ship.co, Ship.floor, Ship.telephoneNumber, Ship.city, Ship.street, Ship.streetNumber, Ship.zipCode, O.notes, O.isPayed  " +
                " FROM 'Order' AS O " +
                "INNER JOIN OrderState AS S ON O.orderStateCode = S.orderStateCode " +
                "INNER JOIN AnagraphicInformation AS Ship ON Ship.anagraphicInformationId = O.shippingAddress "+
                "INNER JOIN AnagraphicInformation AS Bill ON Bill.anagraphicInformationId = O.billingAddress " +
                "WHERE " +
                "    (O.orderStateCode NOT IN ('comProblem','concluded','cusProblem','genProblem','refCom','refCus','tecProblem') )" +
                "OR " +
                "    (O.orderStateCode IN ('comProblem','concluded','cusProblem','genProblem','refCom','refCus','tecProblem') " +
                "    AND O.lastEdit > datetime('now','localtime','-30 seconds') ) " +
                "ORDER BY O.requestedDeliveryTime, O.agreedDeliveryTime ;", new String[0]);

        while( cursor.moveToNext() ){
            Order ordine = new Order();

            ordine.orderId = cursor.getInt(0);
            ordine.orderStateCode = cursor.getString(1) ;
            ordine.agreedTime = cursor.getString(2);
            ordine.requestedTime  = cursor.getString(3);
            ordine.orderState = cursor.getString(4);
            ordine.isPreorder = cursor.getInt(5)==1;
            ordine.printerStatus = cursor.getString(6);
            ordine.lastEdit = cursor.getString(7);
            ordine.shipAddress = cursor.getString(8)+ " " + cursor.getString(9) + "\n" + cursor.getString(10);
            ordine.billAddress = cursor.getString(11)+ " " + cursor.getString(12) + "\n" + cursor.getString(13);

            ordine.isNew= ordine.orderStateCode.equals("new");
            ordine.orderSource = cursor.getInt(14);

            ordine.shipBell = cursor.getString(15);
            ordine.shipFloor = cursor.getString(16);
            ordine.shipTelephone = cursor.getString(17);
            ordine.telephone = cursor.getString(17);
            ordine.shipCity = cursor.getString(21) + " " +  cursor.getString(18);
            ordine.shipStreet = cursor.getString(19) + " " + cursor.getString(20);
            ordine.notes = cursor.getString(22);
            ordine.isPayed = cursor.getInt(23)==1;
            ordini.add(ordine);

        }

        return ordini;
    }

    public ArrayList<Order> getClosedOrders(){
        ArrayList<Order> ordini = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT O.orderId, O.orderStateCode, O.agreedDeliveryTime, " +
                "O.requestedDeliveryTime, S.name, O.isPreOrder, O.printerStatus, O.lastEdit, " +
                "Ship.street, Ship.streetNumber, Ship.city, " +
                "Bill.street, Bill.streetNumber, Bill.city, " +
                "O.orderSource  " +
                " FROM 'Order' AS O " +
                "INNER JOIN OrderState AS S ON O.orderStateCode = S.orderStateCode " +
                "INNER JOIN AnagraphicInformation AS Ship ON Ship.anagraphicInformationId = O.shippingAddress "+
                "INNER JOIN AnagraphicInformation AS Bill ON Bill.anagraphicInformationId = O.billingAddress " +
                "WHERE O.orderStateCode IN ('comProblem','concluded','cusProblem','genProblem','refCom','refCus','tecProblem') ;", new String[0]);

        while( cursor.moveToNext() ){
            Order ordine = new Order();

            ordine.orderId = cursor.getInt(0);
            ordine.orderStateCode = cursor.getString(1) ;
            ordine.agreedTime = cursor.getString(2);
            ordine.requestedTime  = cursor.getString(3);
            ordine.orderState = cursor.getString(4);
            ordine.isPreorder = cursor.getInt(5)==1;
            ordine.printerStatus = cursor.getString(6);
            ordine.lastEdit = cursor.getString(7);
            ordine.shipAddress = cursor.getString(8)+ " " + cursor.getString(9) + "\n" + cursor.getString(10);
            ordine.billAddress = cursor.getString(11)+ " " + cursor.getString(12) + "\n" + cursor.getString(13);

            ordine.isNew=false;
            ordine.orderSource = cursor.getInt(14);

            ordini.add(ordine);

        }

        return ordini;
    }

    public void updateOrder(int orderId, ContentValues contentValues){
        SQLiteDatabase db = this.getWritableDatabase();

        db.update("'Order'",contentValues,"orderId = ?",new String[]{ Integer.toString(orderId)});

    }

    public Order getOrder(int orderId){
        Order ordine = new Order();

        SQLiteDatabase db = this.getReadableDatabase();


       String sql = "SELECT O.orderId, O.orderStateCode, O.shippingAddress, O.billingAddress, O.agreedDeliveryTime, " +
                "O.requestedDeliveryTime, S.name, Ship.street, Ship.streetNumber, Ship.city, Ship.telephoneNumber, " +
                "(( SELECT SUM(priceAtSellTime) FROM OrderLineItem AS OLI2 WHERE OLI2.orderId = ? ) + O.shippingPriceAtSellTime) AS Totale, " +
               " Ship.co, Ship.floor, Ship.zipCode, Ship.telephoneNumber, O.notes, O.localOrderNumber, O.isPayed, Ship.name, Ship.surname, O.lastEdit, O.shippingPriceAtSellTime, " +
               "(SELECT repartNumber FROM VatRepart WHERE vatRateId = (SELECT shippingVatRateId FROM CommercialActivity)) AS ShippingVatRate, O.isPreOrder, O.printerStatus " +
                "FROM 'Order' AS O " +
                "INNER JOIN OrderState AS S ON O.orderStateCode = S.orderStateCode " +
                "INNER JOIN AnagraphicInformation AS Ship ON Ship.anagraphicInformationId = O.shippingAddress " +
                "WHERE O.orderId = ? ;";


        Cursor cursor = db.rawQuery(sql, new String[]{Integer.toString(orderId),Integer.toString(orderId) });

        if(cursor.moveToFirst()) {

            ordine.orderId = cursor.getInt(0);
            ordine.orderStateCode = cursor.getString(1);
            ordine.orderState = cursor.getString(6);

            ordine.shipAddress = cursor.getString(7) + " " + cursor.getString(8) + ", " + cursor.getString(9);
            ordine.agreedTime = cursor.getString(4);
            ordine.requestedTime = cursor.getString(5);
            ordine.telephone = cursor.getString(10);
            ordine.total = cursor.getInt(11);
            ordine.shipBell = cursor.getString(12);
            ordine.shipFloor = cursor.getString(13);
            ordine.shipStreet = cursor.getString(7) + " " + cursor.getString(8);
            ordine.shipCity = cursor.getString(14) + " "  + cursor.getString(9);
            ordine.shipTelephone = cursor.getString(15);
            ordine.notes = cursor.getString(16);
            ordine.orderNumber = cursor.getString(17);
            ordine.isPayed = cursor.getInt(18)==1;

            if(!cursor.isNull(19))
                ordine.shipNameSurname = cursor.getString(19) + " " + cursor.getString(20);
            else
                ordine.shipNameSurname = null;

            ordine.lastEdit = cursor.getString(21);
            ordine.shippingPrice = cursor.getInt(22);
            ordine.shipVatRepart = cursor.getInt(23);
            ordine.isPreorder = cursor.getInt(24)==1;
            ordine.printerStatus = cursor.getString(25);
            ordine.isNew= ordine.orderStateCode.equals("new");

        }
        return ordine;
    }

    //solo fiscali o solo non fiscali
    public ArrayList<ProductionArea> getProductionAreas(boolean fiscal) {
        ArrayList<ProductionArea> areas = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT A.description, A.printerIpAddress, A.printerIpPort, M.name, M.protocol, A.isFiscal, M.canBeFiscal, A.productionAreaId, M.name, A.lastEdit, A.printerModelCode  " +
                "FROM ProductionArea AS A INNER JOIN PrinterModel AS M ON M.printerModelCode = A.printerModelCode " +
                "WHERE A.isFiscal = ?", new String[]{ (fiscal? "1":"0")});

        while( cursor.moveToNext() ){
            ProductionArea area = new ProductionArea();

            area.description = cursor.getString(0);
            area.printerIpAddress = cursor.getString(1);
            area.printerIpPort = cursor.getInt(2);
            area.name = cursor.getString(3);
            area.protocol = cursor.getString(4);
            area.isFiscal = cursor.getInt(5)==1;
            area.canBeFiscal = cursor.getInt(6)==1;
            area.productionAreaId = cursor.getInt(7);
            area.modelName = cursor.getString(8);
            area.lastEdit = cursor.getString(9);
            area.modelCode = cursor.getString(10);

            areas.add(area);
        }

        return areas;
    }

    //tutte le stampanti
    public ArrayList<ProductionArea> getProductionAreas() {
        ArrayList<ProductionArea> areas = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        //Cursor cursor = db.query("ProductionArea", new String[]{"productionAreaId","printerTypeCode","description","printeripAddress","printerIpPort"}, "isFiscal = ? ", new String[]{ (fiscal? "1" : "0") },null,null,null);

 /*       Cursor cursor = db.rawQuery("SELECT A.description, A.printerIpAddress, A.printerIpPort, M.name, M.protocol, A.isFiscal, M.canBeFiscal, A.productionAreaId, M.name, A.lastEdit, A.printerModelCode, A.isDeleted " +
                "FROM ProductionArea AS A INNER JOIN PrinterModel AS M ON M.printerModelCode = A.printerModelCode WHERE A.isDeleted = 0 ",
                null, null);*/
        Cursor cursor = db.rawQuery("SELECT A.description, A.printerIpAddress, A.printerIpPort, M.name, M.protocol, A.isFiscal, M.canBeFiscal, A.productionAreaId, M.name, A.lastEdit, A.printerModelCode " +
                "FROM ProductionArea AS A INNER JOIN PrinterModel AS M ON M.printerModelCode = A.printerModelCode ",
                null, null);

        while( cursor.moveToNext() ){
            ProductionArea area = new ProductionArea();

            area.description = cursor.getString(0);
            area.printerIpAddress = cursor.getString(1);
            area.printerIpPort = cursor.getInt(2);
            area.name = cursor.getString(3);
            area.protocol = cursor.getString(4);
            area.isFiscal = cursor.getInt(5)==1;
            area.canBeFiscal = cursor.getInt(6)==1;
            area.productionAreaId = cursor.getInt(7);
            area.modelName = cursor.getString(8);
            area.lastEdit = cursor.getString(9);
            area.modelCode = cursor.getString(10);
           //area.isDeleted = cursor.getInt(11)==1;

            areas.add(area);
        }

        return areas;
    }

    public ArrayList<String[]> getPrinterModels(){
        ArrayList<String[]>  models = new ArrayList<>() ;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("PrinterModel", new String[]{"printerModelCode", "name", "canBeFiscal"}, null, null, null, null, null, null);

        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> canBeFiscal = new ArrayList<>();

        while(cursor.moveToNext()){
            ids.add(cursor.getString(0));
            names.add(cursor.getString(1));
            canBeFiscal.add(cursor.getString(2));
        }

        models.add(ids.toArray(new String[0]));
        models.add(names.toArray(new String[0]));
        models.add(canBeFiscal.toArray(new String[0]));

        return models;
    }

    public ArrayList<String[]> getVatRates(){
        ArrayList<String[]>  rates = new ArrayList<>() ;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("VatRate", new String[]{"vatRateId", "ratePercent"}, null, null, null, null, null, null);

        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> percs = new ArrayList<>();

        while(cursor.moveToNext()){
            ids.add(cursor.getString(0));
            percs.add(cursor.getString(1));
        }

        rates.add(ids.toArray(new String[0]));
        rates.add(percs.toArray(new String[0]));

        return rates;
    }

    public String[] getPrinterIpPort(int productionAreaId){
        ArrayList<OrderLineItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("ProductionArea", new String[]{"printerIpAddress", "printerIpPort"}, "productionAreaId = ? ", new String[]{Integer.toString(productionAreaId)}, null, null, null);

        if(cursor.moveToFirst()) {
            return new String[]{cursor.getString(0), cursor.getString(1)};
        }else{
            return new String[0];
        }
    }

    public String[] getFiscalPrinterHeader(){
        ArrayList<OrderLineItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT A.name, I.street, I.streetNumber, I.zipCode, I.city, I.district, A.vatNumber FROM " +
                " CommercialActivity AS A INNER JOIN AnagraphicInformation AS I ON I.anagraphicInformationId = A.registeredOffice ", null);

        if(cursor.moveToFirst()){
            return new String[]{
                cursor.getString(0).toUpperCase(),
                cursor.getString(1) + " " + cursor.getString(2),
                cursor.getString(3) + " " + cursor.getString(4) + " (" + cursor.getString(5)+ ")",
                "P.IVA: " + cursor.getString(6)
            };
        }else{
            return new String[0];
        }
    }

    public ArrayList<OrderLineItem> getOrderLineItems(int orderId){

        ArrayList<OrderLineItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "" +
                "SELECT orderLineItemId, priceAtSellTime, productName, ingredients,  repartNumber, notes, categoryName, COUNT(*) AS Num " +
                "FROM " +
                " (" +
                "    SELECT `MyTable1`.customizedProductPart, `MyTable1`.customizedProductId, `MyTable1`.orderLineItemId, `MyTable1`.priceAtSellTime, " +
                "         GROUP_CONCAT(`MyTable1`.name, ', ') AS ingredients, ifnull(`CustomizedProduct`.name, \"Pizza Personalizzata\") AS productName, `VatRepart`.repartNumber, `MyTable1`.notes," +
                "         Category.name AS categoryName " +
                "    FROM" +
                "        (" +
                "           SELECT `AssemblagePart`.customizedProductPart, `AssemblagePart`.customizedProductId, `Ingredient`.name, " +
                "                  `OrderLineItem`.orderLineItemId, `OrderLineItem`.priceAtSellTime, `OrderLineItem`.notes " +
                "           FROM `OrderLineItem`" +
                "           LEFT JOIN `AssemblagePart`" +
                "              ON `OrderLineItem`.customizedProductId = `AssemblagePart`.customizedProductId" +
                "           LEFT JOIN `Ingredient`" +
                "              ON `AssemblagePart`.ingredientId = `Ingredient`.ingredientId " +
                "           WHERE " +
                "              `OrderLineItem`.orderId = ? " +
                "               AND `OrderLineItem`.productId IS NULL " +
                "           ORDER BY `AssemblagePart`.assemblagePartId ASC, `OrderLineItem`.orderLineItemId ASC" +
                "        ) AS MyTable1 " +
                "    LEFT JOIN `CustomizedProduct` " +
                "        ON `MyTable1`.customizedProductId = `CustomizedProduct`.customizedProductId " +
                "    INNER JOIN `VatRepart` " +
                "        ON `CustomizedProduct`.vatRateId = `VatRepart`.vatRateId " +
                "    LEFT JOIN Category " +
                "        ON Category.categoryId = CustomizedProduct.categoryId " +
                "    GROUP BY " +
                "        `MyTable1`.customizedProductPart, `MyTable1`.customizedProductId, `MyTable1`.orderLineItemId, `MyTable1`.priceAtSellTime, `MyTable1`.notes" +
                "    ORDER BY " +
                "        `MyTable1`.orderLineItemId" +
                ") AS MyTable2" +
                " GROUP BY orderLineItemId, priceAtSellTime, productName, ingredients, repartNumber, notes, categoryName " +
                " UNION" +
                " SELECT OLI.orderLineItemId, OLI.priceAtSellTime, P.name, '' AS ingredients, `VatRepart`.repartNumber, OLI.notes, C.name, '1' AS Num " +
                " FROM OrderLineItem AS OLI INNER JOIN Product AS P ON OLI.productId = P.productId LEFT JOIN Category AS C ON C.categoryId = P.categoryId " +
                " LEFT JOIN `VatRepart` " +
                " ON P.vatRateId = `VatRepart`.vatRateId" +
                " WHERE OLI.orderId = ?;";

        Cursor cursor = db.rawQuery(sql, new String[]{Integer.toString(orderId), Integer.toString(orderId)}, null);

        // 0 -> Nome
        // 1 -> ID
        // 2-> Prezzo
        // 3 -> Ingredienti

        int rows = 0;
        int lastId = -1;
        boolean addMe = false;

        OrderLineItem item = new OrderLineItem();

        while ( cursor.moveToNext() ){

            if(lastId != cursor.getInt(0)){
                if(addMe) {
                    int parts = item.ingredients.size() ;
                    item.parts = "";
                    if(parts>1) {
                        item.parts = Integer.toString(parts) + " GUSTI";

                        for (int i = 0; i < parts; i++) {
                            String ing = item.ingredients.get(i);

                            ing = "- " + ing.substring(0, 1) + "/" + Integer.toString(rows) + ing.substring(1);

                            item.ingredients.set(i, ing);
                        }
                    }else if(parts == 1){
                        item.parts = "1 GUSTO";

                        if(item.ingredients.get(0).equals("")) {
                            item.ingredients = new ArrayList<>();
                            item.parts = "";
                        }

                        for (int i = 0; i < parts; i++) {

                            item.ingredients.set(i, item.ingredients.get(i).substring(1));
                        }
                    }

                    items.add(item);
                    rows = 0;
                }
                //cambio OLI
                item = new OrderLineItem();

                item.repart = cursor.getInt(4);
                item.name = cursor.getString(2);
                item.price = cursor.getInt(1);
                item.ingredients = new ArrayList<>();
                item.notes = cursor.getString(5);
                item.category = cursor.getString(6);


            }

            rows+=cursor.getInt(7);

            if(cursor.getString(3) != null && !cursor.getString(3).equals(""))
                item.ingredients.add(cursor.getString(7) + " " + cursor.getString(3));

            lastId = cursor.getInt(0);
            addMe = true;
        }


        if(addMe) {
            int parts = item.ingredients.size() ;
            item.parts="";
            if(parts>1) {
                item.parts = Integer.toString(parts) + " GUSTI";
                for (int i = 0; i < parts; i++) {
                    String ing = item.ingredients.get(i);

                    ing = "- " + ing.substring(0, 1) + "/" + Integer.toString(rows) + ing.substring(1);

                    item.ingredients.set(i, ing);

                }

            }else if(parts == 1){
                item.parts = "1 GUSTO";

                if(item.ingredients.get(0).equals("")) {
                    item.ingredients = new ArrayList<>();
                    item.parts = "";
                }

                for (int i = 0; i < parts; i++) {

                    item.ingredients.set(i, item.ingredients.get(i).substring(1));
                }


            }
            items.add(item);
            rows=0;
        }

        return items;
    }

    public ArrayList<Warehouse> getWarehouses() throws Exception{
        ArrayList<Warehouse> whs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT W.warehouseId, W.warehouseElementId, W.qty, W.price, ifnull(E.name, W.name), W.isSetUp, W.isAvailable, W.lastEdit  " +
                "FROM Warehouse AS W LEFT JOIN WarehouseElement AS E ON W.warehouseElementId = E.warehouseElementId", null);

        int i=0;
        while( cursor.moveToNext() ){
            Warehouse wh = new Warehouse();

            i++;

            wh.warehouseId = cursor.getInt(0);

            if(!cursor.isNull(1))
                wh.warehouseElementId = cursor.getInt(1);

            wh.qty = cursor.getInt(2);
            wh.price = cursor.getInt(3);
            wh.name = cursor.getString(4);
            wh.isSetUp = cursor.getInt(5)==1;
            wh.lastEdit = cursor.getString(6);

            if(wh.isSetUp)
                wh.isAvailable = cursor.getInt(6)==1;
            else
                wh.isAvailable=false;

            whs.add(wh);
        }

        return  whs;
    }

    public ArrayList<IngredientOrProduct> getIngredientOrProducts(int warehouseId){

        ArrayList<IngredientOrProduct> products = new ArrayList<>();

        String sql = "SELECT C.name, I.price, I.IngredientId, NULL " +
                "FROM Ingredient AS I INNER JOIN Category AS C ON C.categoryId = I.categoryId " +
                "WHERE I.warehouseId = ? " +
                "UNION " +
                "SELECT C.name, P.price, NULL, P.productId " +
                "FROM Product AS P INNER JOIN Category AS C ON C.categoryId = P.categoryId " +
                "WHERE P.warehouseId = ? ;";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(sql,new String[]{ Integer.toString(warehouseId),Integer.toString(warehouseId)});

        while(cursor.moveToNext()){
            IngredientOrProduct product = new IngredientOrProduct();

            product.name = cursor.getString(0);
            product.price = cursor.getInt(1);

            if(!cursor.isNull(2))
                product.ingredientId = cursor.getInt(2);

            if(!cursor.isNull(3))
                product.productId = cursor.getInt(3);

            products.add(product);
        }

        return products;
    }

    public void updateWarehouse(int warehouseId, boolean isAvailable, ArrayList<ContentValues> prices) throws Exception{
        //TODO
    }

    public String getLogsString(){
        String logs = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Logs", new String[]{ "logDateTime","Type","Text","Exception"}, null,null, null, null, "logDateTime DESC");

        while(cursor.moveToNext()){
            logs = logs + "\n" + cursor.getString(0) +"  " + cursor.getString(1)  + "  " + cursor.getString(2) + "  " + cursor.getString(3);
        }

        return logs;
    }

    public void clearLogs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Logs", null, null);

    }

    public String getLastWsUpdate(String wsName){
        //restituisce data ultimo aggiornamento db relativamente a nome web service
        //se mai aggiornato restituisce default

        String toReturn;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("wsUpdates", new String[] { "lastUpdate"}, "wsName = ?",new String[]{ wsName }, null,null,null);

        cursor.moveToFirst();

        if (cursor.getCount() > 0){
            toReturn = cursor.getString(0);
        }else{
            //inserisce default (così scarica tutto)

            toReturn = "2001-01-01 01:01:01";

            ContentValues contentValues = new ContentValues();
            contentValues.put("wsName",wsName);
            contentValues.put("lastUpdate",toReturn);

            db.insert("wsUpdates",null,contentValues);

        }

        //db.close();
        return toReturn;
    }

    public void setLastWsUpdate(String wsName, String lastUpdate){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("wsName",wsName);
        contentValues.put("lastUpdate",lastUpdate);

        db.update("wsUpdates", contentValues, "wsName = ?", new String[]{wsName});

        //db.close();
    }

    public CommercialActivity getCommercialActivity(){
        SQLiteDatabase db = this.getReadableDatabase();
        CommercialActivity commercialActivity = new CommercialActivity();

        Cursor cursor = db.query("CommercialActivity", new String[]{"name", "registeredOffice", "operationalOffice", "vatNumber",
                "fiscalCode", "description", "shippingPrice", "shippingVatRateId", "orderImportForFreeShipping",
                "minimumOrderForShipping", "shippingPriceLimit", "shippingPriceAfterLimit", "commercialActivityId"}, null, null, null, null, null);

        if(cursor.moveToFirst()){
            commercialActivity.name = cursor.getString(0);
            commercialActivity.registeredOffice = cursor.getInt(1);
            commercialActivity.operationalOffice = cursor.getInt(2);
            commercialActivity.vatNumber=cursor.getString(3);
            commercialActivity.fiscalCode=cursor.getString(4);
            commercialActivity.description=cursor.getString(5);
            commercialActivity.shippingPrice=cursor.getInt(6);
            commercialActivity.shippingVatRateId=cursor.getInt(7);
            commercialActivity.orderImportForFreeShipping=cursor.getInt(8);
            commercialActivity.minimumOrderForShipping = cursor.getInt(9);
            commercialActivity.shippingPriceLimit = cursor.getInt(10);
            commercialActivity.shippingPriceAfterLimit = cursor.getInt(11);
            commercialActivity.commercialActivityId = cursor.getInt(12);
        }
        return commercialActivity;
    }

    public AnagraphicInformation getOperationalOffice(){
        SQLiteDatabase db = this.getReadableDatabase();

        AnagraphicInformation address = new AnagraphicInformation();

        Cursor cursor = db.rawQuery("SELECT street, streetNumber, city, district, zipCode, countryCode, telephoneNumber, emailAddress, anagraphicInformationId " +
                "FROM AnagraphicInformation WHERE anagraphicInformationId = (SELECT operationalOffice FROM CommercialActivity)", null);

        if(cursor.moveToFirst()){
            address.street = cursor.getString(0);
            address.streetNumber = cursor.getString(1);
            address.city = cursor.getString(2);
            address.district = cursor.getString(3);
            address.zipCode = cursor.getString(4);
            address.countryCode = cursor.getString(5);
            address.telephoneNumber = cursor.getString(6);
            address.emailAddress = cursor.getString(7);
            address.anagraphicInformationId = cursor.getInt(8);

        }

        return  address;
    }

    public AnagraphicInformation getRegisteredOffice(){
        SQLiteDatabase db = this.getReadableDatabase();

        AnagraphicInformation address = new AnagraphicInformation();

        Cursor cursor = db.rawQuery("SELECT street, streetNumber, city, district, zipCode, countryCode, telephoneNumber, emailAddress, anagraphicInformationId " +
                "FROM AnagraphicInformation WHERE anagraphicInformationId = (SELECT registeredOffice FROM CommercialActivity)",null);

        if(cursor.moveToFirst()){
            address.street = cursor.getString(0);
            address.streetNumber = cursor.getString(1);
            address.city = cursor.getString(2);
            address.district = cursor.getString(3);
            address.zipCode = cursor.getString(4);
            address.countryCode = cursor.getString(5);
            address.telephoneNumber = cursor.getString(6);
            address.emailAddress = cursor.getString(7);
            address.anagraphicInformationId = cursor.getInt(8);

        }

        return  address;
    }

    public Employee getCurrentEmployee(){
        SQLiteDatabase db = this.getReadableDatabase();

        Employee employee = new Employee();
        AnagraphicInformation anagraphicInformation = new AnagraphicInformation();

        Cursor cursor = db.rawQuery("SELECT I.street, I.streetNumber, I.city, I.district, I.zipCode, I.countryCode, I.telephoneNumber, I.eMailAddress, E.name, E.surname, E.birthday, I.anagraphicInformationId " +
                                   " FROM Employee AS E LEFT JOIN AnagraphicInformation AS I ON I.anagraphicInformationId = E.anagraphicInformationId" +
                                   " WHERE E.tapManagerUserId = ? ", new String[]{WsLogin.USER_ID});

        if(cursor.moveToFirst()) {
            anagraphicInformation.street = cursor.getString(0);
            anagraphicInformation.streetNumber = cursor.getString(1);
            anagraphicInformation.city = cursor.getString(2);
            anagraphicInformation.district = cursor.getString(3);
            anagraphicInformation.zipCode = cursor.getString(4);
            anagraphicInformation.countryCode = cursor.getString(5);
            anagraphicInformation.telephoneNumber = cursor.getString(6);
            anagraphicInformation.emailAddress = cursor.getString(7);

            if(!cursor.isNull(11))
                anagraphicInformation.anagraphicInformationId = cursor.getInt(11);



            employee.name = cursor.getString(8);
            employee.surname = cursor.getString(9);

            if(!cursor.isNull(10)) {
                String bd = cursor.getString(10);
                employee.birthdayDay = bd.substring(8, 10);
                employee.birthdayMonth = bd.substring(5, 7);
                employee.birthdayYear = bd.substring(0, 4);
            }
        }

        employee.anagraphicInformation = anagraphicInformation;
        return  employee;


    }

}