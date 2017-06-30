package com.lynkteam.tapmanager.printers;

import android.content.Context;
import android.os.AsyncTask;

import com.lynkteam.tapmanager.DB.Logger;

/**
 * Created by robertov on 17/08/15.
 */
public class PrinterFiscalBG extends AsyncTask<Void, Void, String> {

    private PrinterFiscal printer;

    private boolean gotError = false;

    private Context context;
    private Callbacks callbacks;

    private String IP;
    private int port;

    public interface Callbacks{
        void ok(PrinterFiscal p);
        void ko();
    }

    public PrinterFiscalBG(Context cnt, String IP, int port, Callbacks cbs)
    {

        this.context = cnt;
        this.callbacks = cbs;
        this.IP = IP;
        this.port = port;
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            printer = new PrinterFiscal(context, IP, port);
            printer.initialize();
        }
        catch(Exception ex)
        {
            gotError = true;
            Logger.Error(context, "Errore Stampa Fiscale", ex.toString() );
        }

        return "";
    }

    @Override
    protected void onPostExecute( String token) {

        if(!gotError)
            callbacks.ok(printer);
        else
            callbacks.ko();


    }

}
