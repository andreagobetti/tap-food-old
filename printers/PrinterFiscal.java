package com.lynkteam.tapmanager.printers;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.UI.ActivityHome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertov on 17/08/15.
 */

public class PrinterFiscal {

    private int messageCounter;
    private Socket socket;

    private List<String> commandList;

    private final int SERVER_PORT;
    private final String SERVER_IP;

    private static final char STX = (char) (Integer.parseInt("2", 16));
    private static final char ETX = (char) (Integer.parseInt("3", 16));
    private static final char ACK = (char) (Integer.parseInt("6", 16));
    private static final char IDN = 'E';

    private static final int PAGE_WIDTH = 46; //caratteri

    private static final String[] printerErrors = new String[] {
            "",                      //00
            "",                      //01
            "CARTA SCONTRINO",       //02
            "OFFLINE",               //03
            "",                      //04
            "",                      //05
            "",                      //06
            "SLIP KO",               //07
            "TASTO ERRATO",          //08
            "DATA INFERIORE",        //09
            "DATA ERRATA",           //10
            "SEQUENZA ERRATA",       //11
            "DATI INESISTENTI",      //12
            "VALORE ERRATO",         //13
            "PROG MATRICOLA",        //14
            "GIA ESISTENTE",         //15
            "NON PREVISTO",          //16
            "IMPOSSIBILE ORA",       //17
            "NON POSSIBILE",         //18
            "SCRITTA INVALIDA",      //19
            "SUPERA VALORE",         //20
            "SUPERA LIMITE",         //21
            "NON PROGRAMMATO",       //22
            "CHIUDI SCONTRINO",      //23
            "CHIUDI PAGAMENTO",      //24
            "MANCA OPERATORE",       //25
            "CASSA INFERIORE",       //26
            "OLTRE PROGRAMMAZIONE",  //27
            "P.C. NON CONNESSO",     //28
            "MANCA MODULO",          //29
            "CHECKSUM ERRATO",       //30
            "",                      //31
            "",                      //32
            "",                      //33
            "MANCA ATTIVAZIONE",     //34
            "SLIP CONNESSIONE?",     //35
            "",                      //36
            "RIMUOVERE MODULO",      //37
            "EFT-POS in ERRORE"      //38
    };



    //ID OPERATORE
    private static final String OPID = "01";

    protected BufferedReader in;
    private PrintWriter out;
    private OutputStream outs;

    private InputStream ins;
    private PrintStream pstream;

    private Context context;

    private boolean gotError = false;

    private PrinterFiscal thisPF = this;

    private int nonFiscalTotal = 0;
    private int fiscalTotal = 0;

    private int fiscalRowsCount[] = new int[]{ 0,0,0,0,0,0,0,0,0};

    public void close(){
        //post: closes the stream, used when printjob ended
        try {
            pstream.close();
        }catch(Exception ex){
            Logger.Error(context,"Errore chiusura socket stampante",ex.toString());
        }
    }


    public void initialize() throws Exception {
        //post: returns true iff stream to network printer successfully opened, streams for writing to esc/p printer created

        pstream = new PrintStream(socket.getOutputStream());
        pstream.println((char)27 + "@");

    }

    public PrinterFiscal(Context cnt, String IP, int port) throws Exception{
        this.context = cnt;
        this.SERVER_IP = IP;
        this.SERVER_PORT = port;

        socket = new Socket();
        socket.connect( new InetSocketAddress(this.SERVER_IP, this.SERVER_PORT), 1000);
        outs = socket.getOutputStream();
        ins = socket.getInputStream();

        if(!socket.isConnected()) throw new Exception("Socket non connesso");

        messageCounter = 0;
        commandList = new ArrayList<>();



    }

    private String getIncrementedMessageCounter() {
        if (messageCounter == 99)
            messageCounter = 0;
        else
            messageCounter++;

        if (messageCounter < 10)
            return "0" + messageCounter;
        else
            return String.valueOf(messageCounter);
    }

    private String calculateCks(String strIn) {
        int cd = 0;

        for (int li = 0; li < strIn.length(); li++) {
            cd += (int) (strIn.substring(li, li + 1).charAt(0));
        }

        cd %=  100;

        if (cd < 10)
            return "0" + Integer.toString(cd);
        else
            return Integer.toString(cd);
    }

    private void sendMessage(String msg) {
        String cnt = getIncrementedMessageCounter();
        String cks = this.calculateCks(cnt + IDN + msg);
        final String cmd = STX + cnt + IDN + msg + cks + ETX;
        commandList.add(cmd);
    }

    public String fillStr(String str, int len, char fillWith) {
        for (int i = str.length(); i < len; i++)
            str = fillWith + str;
        return str;
    }

    public void commit() throws Exception{
        class CommitAsync extends AsyncTask<Void, Void, String> {
            protected String doInBackground(Void... voids) {
                String res = "";

                try {

                    for(int i=0;i<commandList.size();i++){

                        String cmd = commandList.get(i);

                        out = new PrintWriter(outs, false);

                        if (out != null) {
                            out.write(cmd);
                            out.flush();
                        }

                        final BufferedReader input = new BufferedReader(new InputStreamReader(ins));

                        class Foo implements  Runnable{
                            private volatile  int car = -2;
                            public void run()
                            {
                                //stuff qui
                                try {
                                    car = input.read();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            public int getCar(){
                                return car;
                            }
                        }

                        Foo foo = new Foo();

                        Thread thread = new Thread(foo);
                        thread.start();

                        thread.join(3000);

                        int c= foo.getCar();

                        if(c==-2) throw new Exception("La stampante non risponde. Verificare accensione e/o stato");


                        while(c!=PrinterFiscal.ETX && c!= -1){
                            res += (char)c + "";
                            c = input.read();
                        }


                    }

                }
                catch(Exception ex)
                {
                    Logger.Error(context, "Errore Stampa Fiscale", ex.toString() );
                    gotError = true;

                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");

                }
                return res;
            }

            protected void onPostExecute(String out){

                if(!gotError){
                    try {
                        String[] res = out.split(PrinterFiscal.STX + "");

                        for (int i = 0; i < res.length; i++) {
                            if (res[i].length() > 5 && res[i].substring(3, 6).equals("ERR") ) {

                                throw new Exception("Errore Stampante: " + printerErrors[ Integer.parseInt( res[i].substring(8,10) )] + " Risposta completa: " + res[i]);

                            }
                        }

                        Logger.Info(context, "Stampa Fiscale Completata");

                    }catch(Exception ex){
                        Logger.Error(context, "Errore Stampa Fiscale", ex.toString());

                        ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                    }finally{
                        close();
                    }
                }
            }
        }

        new CommitAsync().execute();
    }


    /***********    MISC   *************/
    public void printerReset(){
        this.sendMessage("1088" + OPID);
    }

    public void openDrawer(){
        this.sendMessage("1050" + OPID);
    }


    /***********    FISCALI   *************/
    public void fiscalBegin(){
        this.sendMessage("1085" + OPID);
    }

    public void fiscalSubtotal(){
        this.sendMessage("1086" + OPID + "00");
    }

    //importo moltiplicato x 100
    public void fiscalPaymentContante(int importo) throws Exception{
        String imp = fillStr(Integer.toString(importo),9,'0');
        if(imp.length()>9)
            throw new Exception("Importo pagamento eccessivo (max 9 car.)");

        this.sendMessage("1036" +OPID+ imp);
    }

    public void fiscalAdditionalDescription(String riga){
        riga = riga + fillStr("",38-riga.length(),' ') ;
        this.sendMessage("1066"+OPID+riga);
    }

    public void fiscalAdditionalRow(String riga){

        fiscalRowsCount[2]++;
        String nriga;
        if (fiscalRowsCount[2]<10)
            nriga = "0" + Integer.toString( fiscalRowsCount[2]);
        else if(fiscalRowsCount[2] < 100)
            nriga = Integer.toString( fiscalRowsCount[2] );
        else
            return;

        riga = riga + fillStr("",46-riga.length(),' ') ;

        this.sendMessage("1078"+OPID+"2"+ nriga+"01" +riga);
    }
    public void fiscalBigAdditionalRow(String riga){
        fiscalRowsCount[2]++;
        String nriga;
        if (fiscalRowsCount[2]<10)
            nriga = "0" + Integer.toString( fiscalRowsCount[2]);
        else if(fiscalRowsCount[2] < 100)
            nriga = Integer.toString( fiscalRowsCount[2] );
        else
            return;

        riga = riga + fillStr("",46-riga.length(),' ') ;

        this.sendMessage("1078"+OPID+"2"+ nriga+"03" +riga);
    }

    public void fiscalBoldAdditionalRow(String riga){
        fiscalRowsCount[2]++;
        String nriga;
        if (fiscalRowsCount[2]<10)
            nriga = "0" + Integer.toString( fiscalRowsCount[2]);
        else if(fiscalRowsCount[2] < 100)
            nriga = Integer.toString( fiscalRowsCount[2] );
        else
            return;

        riga = riga + fillStr("",46-riga.length(),' ') ;

        this.sendMessage("1078"+OPID+"2"+ nriga+"02" +riga);
    }

    public void fiscalPaymentAssegno(int importo) throws Exception{
        String imp = fillStr(Integer.toString(importo),9,'0');
        if(imp.length()>9)
            throw new Exception("Importo pagamento eccessivo (max 9 car.)");

        this.sendMessage("1044" +OPID+ imp);
    }

    public void fiscalPaymentCartaCredito() throws Exception{
        this.sendMessage("1045" +OPID+ "01");
    }

    public int getFiscalTotal(){
        return fiscalTotal;
    }

    //prezzo  moltiplicato x 100, qta x 1000
    public void fiscalSellProduct(String prodName, int price, int qta, int reparto) throws Exception {
        fiscalTotal += qta*price/1000;

        if(prodName.length()>32)
            prodName = prodName.substring(0,32);

        String qty = fillStr(Integer.toString(qta),7,'0');
        if(qty.length()>7)
            throw new Exception("Quantità troppo lunga (max 7 car.)");

        String pce = fillStr(Integer.toString(price),9,'0');
        if(pce.length()>9)
            throw new Exception("Prezzo troppo lungo (max 9 car.)");

        String rep = fillStr(Integer.toString(reparto),2,'0');
        if(rep.length()>2)
            throw new Exception("Cod. Reparto troppo lungo (max 2 car.)");

        this.sendMessage("1080"+OPID+ prodName + qty + pce + rep + "1");
    }



    /***********    NON FISCALI   *************/

    public void nonFiscalFirstRow(){
        this.sendMessage("1064"+OPID+"                                         EURO ");
    }

    //prezzo  moltiplicato x 100, qta x 1000
    public void nonFiscalSellProduct(String prodName, int price, int qta, int reparto) throws Exception{
        // generare stringa e inviarla (46 caratteri)
        nonFiscalTotal += price*qta/1000;

        if(qta>1000){
            String qty = Integer.toString( (int) qta / 1000) + (qta % 1000==0? "" :  "," + Integer.toString( (int) qta%1000 ));

            String prz = Integer.toString((int) price / 100) + "," + fillStr(Integer.toString( (int) price%100 ), 2, '0');

            this.sendMessage( fillStr(qty,25,' ') + "x" + fillStr(prz,16,' ') +  fillStr(" ",10,' ') );



        }

        int tot = qta/1000*price;

        String p = fillStr(Integer.toString(tot),3,'0');

        String intero = p.substring(0,p.length()-2);
        String dec = p.substring(p.length()-2);

        String ciao = "1064"+OPID+"1"+ prodName + fillStr("", (PAGE_WIDTH-1 -prodName.length()-intero.length()-dec.length()), ' ') + intero + "," + dec;

        this.sendMessage(ciao);

    }

    public void nonFiscalLine(String line){ this.sendMessage("1064"+OPID+"1"+line); }

    public void nonFiscalBigLine(String line){
        this.sendMessage("1064"+OPID+"3"+line);
    }

    public void nonFiscalBoldLine(String line){
        this.sendMessage("1064"+OPID+"2"+line);
    }

    public void nonFiscalBigBoldLine(String line){
        this.sendMessage("1064"+OPID+"4"+line);
    }

    //totale moltiplicato x 100
    public void nonFiscalTotal(){

        String t = fillStr(Integer.toString(nonFiscalTotal),3,'0');

        String intero = t.substring(0,t.length()-2);
        String dec = t.substring(t.length()-2);



        this.sendMessage("1064"+OPID+"3TOTALE EURO" + fillStr("",( 32-intero.length() ),' ') + intero + "," + dec );
    }

    public void nonFiscalEmptyLine(){
        this.sendMessage("1064"+OPID+"1      ");
    }

    public void nonFiscalFooter(){
        this.sendMessage("1064"+OPID+"1       RITIRARE LO SCONTRINO ALLA CASSA       ");
    }


    public void nonFiscalBegin() {
        this.sendMessage("1063"+OPID);
    }

    public void nonFiscalHeading(String msg) throws Exception {
        int n = msg.length();

        if(n>46)
            throw new Exception("Messaggio troppo lungo");


        this.sendMessage("1064"+OPID+"4" + msg + fillStr("",(46-n),' '));
    }

    public void endNonFiscal(){
        this.sendMessage("1065"+OPID);
    }



    private int fiscalHeaderSetUpCounter = 0;

    // INIZIALIZZAZIONI  (descr 40 car)
    public void setUpFiscalHeader(String descr){
        fiscalHeaderSetUpCounter++;

        this.sendMessage("3016"+fillStr(Integer.toString(fiscalHeaderSetUpCounter),2,'0')+descr);
    }

    public void setUpFiscalHeaderTest(){
        this.sendMessage("301698                                        ");
    }

    public void setUpFiscalHeaderEnd(){
        this.sendMessage("301699                                        ");
    }


    public String alignCenter(String in, int length){
        int lr = (length - in.length())/2;
        String str = fillStr("",lr,' ') + in + fillStr("",lr,' ');
        return str + fillStr("",40-str.length(),' ');
    }

}