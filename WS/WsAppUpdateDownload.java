package com.lynkteam.tapmanager.WS;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.util.NetworkUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robertov on 31/07/2015.
 */
public class WsAppUpdateDownload extends AsyncTask<Void, Void, String> {
    private boolean gotError = false;

    private String myVersion;
    private Callbacks myCallbacks;

    private Context myContext;

    private String path = "/sdcard/YourApp.apk";

    public interface Callbacks{
        void ok();
        void ko();
    }


    public WsAppUpdateDownload(Context context, String version, Callbacks callbacks)
    {
        myCallbacks = callbacks;
        myContext = context;
        myVersion = version;
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

            URL url = new URL("http://app.tap-food.com/ws/tm/apk/tapmanager-" + myVersion + ".apk");  //// TODO: link download versione X

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);  /* milliseconds */
            conn.setConnectTimeout(15000);   /* milliseconds */

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authentication-Token", WsLogin.AUTH_TOKEN);

            if (conn.getResponseCode() != 200){
                throw new Exception("Status Code: " + Integer.toString(conn.getResponseCode()) + " Message: " + conn.getHeaderField("Http-Answer") );
            }

            int fileLength = conn.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

        }catch(Exception ex){
            gotError = true;
            Logger.Error(myContext, "Errore download nuova versione app", ex.toString());

        }
        finally {

            return "";

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
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myContext.startActivity(i);

                Logger.Info(myContext, "Download nuova app completata: " + version);

                myCallbacks.ok();

            }catch(Exception ex){
                Logger.Error(myContext,"Errore download nuova versione app",ex.toString());
                myCallbacks.ko();
            }
        }


    }


}

