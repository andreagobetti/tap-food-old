package com.lynkteam.tapmanager.printers;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by robertov on 17/08/15.
 */
public class PrinterEpsonBG extends AsyncTask<Void, Void, String> {

    private PrinterEpson printer;

    private boolean gotError = false;

    private Callbacks callbacks;

    private String IP;
    private int port;

    private Context myContext;

    public interface Callbacks{
        void ok(PrinterEpson p);
        void ko();
    }

    public PrinterEpsonBG(Context context, String IP, int port, Callbacks cbs)
    {
        this.callbacks = cbs;
        this.IP = IP;
        this.port   = port;
        this.myContext = context;
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            printer = new PrinterEpson(IP,port);
            printer.initialize();
        }
        catch(Exception ex)
        {
            Logger.Error(myContext, "Errore durante stampa non fiscale", ex.toString());
            gotError = true;
        }

        return "";
    }

    @Override
    protected void onPostExecute( String token) {

        if(gotError)
            callbacks.ko();
        else
            callbacks.ok(printer);


    }

}
