package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.FileSystemUtil;
import com.lynkteam.tapmanager.util.NetworkUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsDownloadWarehouseImages extends AsyncTask<Void, Void, String> {

    boolean gotError = false;

    private Callbacks myCallbacks;

    private Context myContext;

    private String myUrl;

    private int myWarehouseElementId;

    private Bitmap myBitmap;

    public interface Callbacks{
        void ok();
        void ko();
    }

    public WsDownloadWarehouseImages(Context context, int id, String url, Callbacks callbacks)
    {
        myCallbacks = callbacks;
        myContext = context;
        myUrl = url;
        myWarehouseElementId = id;
    }


    @Override
    protected String doInBackground(Void... voids) {

        try {

            if (!NetworkUtil.networkConnected(myContext)) {
                throw new Exception("Connessione Internet assente");
            }


            // NON USARE LO STEPPER QUI DENTRO
            //NON FUNZIONA DICE CONNECTION ALREADY ENSTABLISHED

            //

            URL url = new URL(myUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setRequestMethod("GET");

            myBitmap = BitmapFactory.decodeStream( conn.getInputStream());

            if (conn.getResponseCode() != 200){
                throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );
            }




        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore download immagini ingredienti", ex.toString());

        }
        finally {

            return "";

        }

    }

    @Override
    protected void onPostExecute( String json) {

        if(gotError){
            myCallbacks.ko();
            //errore WS
        }else{
            try {
                FileSystemUtil.saveWarehouseElementBitmap(myContext, myBitmap, Integer.toString(myWarehouseElementId));

                myCallbacks.ok();

            }
            catch(Exception ex)
            {
                Logger.Error(myContext, "Errore download immagini WarehouseElement", ex.toString());
                myCallbacks.ko();
            }

        }


    }


}

