package com.lynkteam.tapmanager.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.Order;
import com.lynkteam.tapmanager.DB.OrderLineItem;
import com.lynkteam.tapmanager.DB.ProductionArea;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.printers.PrinterEpson;
import com.lynkteam.tapmanager.printers.PrinterEpsonBG;
import com.lynkteam.tapmanager.printers.PrinterFiscal;
import com.lynkteam.tapmanager.printers.PrinterFiscalBG;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by robertov on 28/08/15.
 */
public class PrintUtil {
    //OCCHIO: se si modifica il senso delle voci qui (o corrispondenza con metodi di pagamento stampanti),
    //modificare anche lo switch dentro PrintScontrinoConfirm
    //e nel alertDialog2 .onClick

    public static final String[] PAYMENT_METHODS = new String[]{ "Contanti (tutto)", "Contanti (importo)", "Carta di Credito", "Assegno"};

    public static void PrintPreordine(final Activity activity, final ArrayList<OrderLineItem> orderLineItems, final Order order) throws Exception{
        final ArrayList<ProductionArea> printers = DBHelper.getInstance(activity.getApplicationContext()).getProductionAreas(true);

        if(printers.size()>1) {
            ArrayList<String> printerNames = new ArrayList<>();
            for (int i = 0; i < printers.size(); i++) printerNames.add(printers.get(i).description);


            String[] names = new String[printers.size()];
            names = printerNames.toArray(names);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list_item_select_printer, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Seleziona la stampante");
            alertDialog.setCancelable(true);

            alertDialog.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    PrintPreordineConfirmed(activity, orderLineItems, order, printers.get(position).protocol, printers.get(position).printerIpAddress, printers.get(position).printerIpPort);

                }
            });

            alertDialog.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();

                }
            });

            alertDialog.show();
        }else{ //una sola stampante
            AlertDialog.Builder bldr = new AlertDialog.Builder( activity );
            bldr.setMessage("Confermi di voler stampare dalla stampante " + printers.get(0).description + " ?" )
                    .setTitle("Conferma")
                    .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nulla
                            PrintPreordineConfirmed(activity, orderLineItems, order, printers.get(0).protocol, printers.get(0).printerIpAddress,  printers.get(0).printerIpPort);
                        }
                    }).setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //annulla, non fare nulla
                }
            });
            bldr.create();
            bldr.show();
        }


    }

    private static void PrintPreordineConfirmed(final Activity activity, final ArrayList<OrderLineItem> myOrderLines, final Order myOrder, String protocol, String IP, int port){
        switch(protocol) {
            case "EpsonFiscal":

                new PrinterFiscalBG(activity.getApplicationContext(), IP, port, new PrinterFiscalBG.Callbacks() {
                    @Override
                    public void ok(PrinterFiscal p) {
                        try {
                            p.printerReset();

                            p.nonFiscalBegin();
                            p.nonFiscalFirstRow();
                            for (int i = 0; i < myOrderLines.size(); i++) {

                                p.nonFiscalSellProduct(myOrderLines.get(i).name, myOrderLines.get(i).price, 1000, myOrderLines.get(i).repart);
                                String notes = myOrderLines.get(i).notes;
                                if (notes != null && notes.length() > 0) {
                                    while (notes.length() > 42) {
                                        p.nonFiscalLine(" ** " + notes.substring(0, 42));
                                        notes = notes.substring(42);
                                    }
                                    p.nonFiscalLine(" ** " + notes);
                                }
                            }
                            int ship = myOrder.shippingPrice;
                            if (ship > 0) {
                                p.nonFiscalSellProduct("Consegna", ship, 1000, myOrder.shipVatRepart);
                            }

                            p.nonFiscalTotal();
                            p.nonFiscalEmptyLine();
                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("- - - - - -  INFORMAZIONI CLIENTE  - - - - - -");
                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("Cliente: " + (myOrder.shipNameSurname != null ? myOrder.shipNameSurname : "Non Specificato"));
                            p.nonFiscalBoldLine("Campanello: " + (myOrder.shipBell != null ? myOrder.shipBell : "Non Specificato"));
                            p.nonFiscalLine("Piano: " + (myOrder.shipFloor != null ? myOrder.shipFloor : "Non Specificato"));
                            p.nonFiscalLine((myOrder.shipStreet != null ? myOrder.shipStreet : "Non Specificato"));
                            p.nonFiscalLine((myOrder.shipCity != null ? myOrder.shipCity : "Non Specificato"));
                            p.nonFiscalLine("Telefono: " + (myOrder.shipTelephone != null ? myOrder.shipTelephone : "Non Specificato"));
                            p.nonFiscalEmptyLine();

                            String notes = myOrder.notes;
                            if (notes != null && notes.length() > 0) {
                                p.nonFiscalBoldLine("Note:");
                                while (notes.length() > 46) {
                                    p.nonFiscalBoldLine(notes.substring(0, 46));
                                    notes = notes.substring(46);
                                }
                                p.nonFiscalBoldLine(notes);
                            }

                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("- - - - - -  INFORMAZIONI  ORDINE  - - - - - -");
                            p.nonFiscalLine(" ");
                            p.nonFiscalBigLine("Numero Ordine: " + p.fillStr(Integer.toString(myOrder.orderId), 10, '0'));
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("Stato Pagamento: " + (myOrder.isPayed ? "Pagato" : "Non Pagato "));
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("- - - - - - - - - - - - - - - - - - - - - - - ");
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("Powered by TapFood!");
                            p.nonFiscalEmptyLine();
                            p.nonFiscalEmptyLine();
                            p.nonFiscalEmptyLine();
                            p.nonFiscalFooter();
                            p.endNonFiscal();
                            p.printerReset();

                            p.commit();

                        } catch (Exception ex) {
                            Logger.Error(activity.getApplicationContext(), "Errore Stampa Fiscale", ex.toString());

                            Toast mytoast = Toast.makeText(activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                            mytoast.setGravity(Gravity.TOP, 0, 200);
                            mytoast.show();
                        }
                    }

                    @Override
                    public void ko() {
                        Toast mytoast = Toast.makeText(activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                        mytoast.setGravity(Gravity.TOP, 0, 200);
                        mytoast.show();
                    }

                }).execute();
        }
    }

    public static void PrintScontrino(final Activity activity, final ArrayList<OrderLineItem> orderLineItems, final Order order) throws Exception{
        final ArrayList<ProductionArea> printers = DBHelper.getInstance(activity.getApplicationContext()).getProductionAreas(true);

        if(printers.size()>1) {
            ArrayList<String> printerNames = new ArrayList<>();
            for (int i = 0; i < printers.size(); i++) printerNames.add(printers.get(i).description);


            String[] names = new String[printers.size()];
            names = printerNames.toArray(names);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list_item_select_printer, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Seleziona la stampante");
            alertDialog.setCancelable(true);

            alertDialog.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int printerCode) {

                    final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(activity);
                    LayoutInflater inflater2 = activity.getLayoutInflater();
                    View convertView2 = inflater2.inflate(R.layout.list_item_select_payment, null);
                    alertDialog2.setView(convertView2);
                    alertDialog2.setTitle("Seleziona il metodo di pagamento");
                    alertDialog2.setCancelable(true);


                    alertDialog2.setItems(PrintUtil.PAYMENT_METHODS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int paymentCode) {

                            if (paymentCode == 0 || paymentCode == 2) {
                                PrintScontrinoConfirmed(activity, orderLineItems, order, paymentCode, 0, printers.get(printerCode).protocol, printers.get(printerCode).printerIpAddress, printers.get(printerCode).printerIpPort);
                            } else {

                                final AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(activity);
                                alertDialog3.setTitle("Inserisci l'importo pagato ( Totale Ordine €" + DecimalUtil.formatInt(order.total) + ")");

                                final EditText input = new EditText(activity);
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                alertDialog3.setView(input);

                                alertDialog3.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String ins = input.getText().toString();
                                        if(ins.length()>7){
                                            AlertDialog.Builder bldr = new AlertDialog.Builder(activity);
                                            bldr.setMessage("L'importo non può superare i 7 caratteri.")
                                                    .setTitle("Lunghezza Importo Eccessiva")
                                                    .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int paymentCode) {

                                                        }
                                                    });
                                            bldr.create();
                                            bldr.show();
                                        }else {
                                            if (ins == null || ins.equals("") || Double.parseDouble(ins)*100 < order.total) {
                                                AlertDialog.Builder bldr = new AlertDialog.Builder(activity);
                                                bldr.setMessage("L'importo indicato è minore del totale ordine.")
                                                        .setTitle("Importo Insufficiente")
                                                        .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int paymentCode) {

                                                            }
                                                        });
                                                bldr.create();
                                                bldr.show();

                                            }else{
                                                PrintScontrinoConfirmed(activity, orderLineItems, order, paymentCode, (int) (Double.parseDouble(ins) * 100), printers.get(printerCode).protocol, printers.get(printerCode).printerIpAddress, printers.get(printerCode).printerIpPort);
                                            }
                                        }
                                    }
                                });
                            alertDialog3.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            });


                            alertDialog3.show();
                        }
                    }
                });

                alertDialog2.setNegativeButton("Annulla",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog,int which){
                        dialog.cancel();
                        dialog.dismiss();

                    }
                });

                alertDialog2.show();
            }
        });

            alertDialog.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();

                }
            });

            alertDialog.show();
        }else if(printers.size()==1) { //una sola stampante

            final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(activity);
            LayoutInflater inflater2 = activity.getLayoutInflater();
            View convertView2 = inflater2.inflate(R.layout.list_item_select_payment, null);
            alertDialog2.setView(convertView2);
            alertDialog2.setTitle("Seleziona il metodo di pagamento");
            alertDialog2.setCancelable(true);


            alertDialog2.setItems(PrintUtil.PAYMENT_METHODS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int paymentCode) {

                    if (paymentCode == 0 || paymentCode == 2) {
                        PrintScontrinoConfirmed(activity, orderLineItems, order, paymentCode, 0, printers.get(0).protocol, printers.get(0).printerIpAddress, printers.get(0).printerIpPort);
                    } else {

                        final AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(activity);
                        alertDialog3.setTitle("Inserisci l'importo pagato ( Totale Ordine €" + DecimalUtil.formatInt(order.total) + ")");

                        final EditText input = new EditText(activity);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        alertDialog3.setView(input);

                        alertDialog3.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ins = input.getText().toString();
                                if(ins.length()>7){
                                    AlertDialog.Builder bldr = new AlertDialog.Builder(activity);
                                    bldr.setMessage("L'importo non può superare i 7 caratteri.")
                                            .setTitle("Lunghezza Importo Eccessiva")
                                            .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {

                                                }
                                            });
                                    bldr.create();
                                    bldr.show();
                                }else {
                                    if (ins == null || ins.equals("") || Double.parseDouble(ins)*100 < order.total) {
                                        AlertDialog.Builder bldr = new AlertDialog.Builder(activity);
                                        bldr.setMessage("L'importo indicato è minore del totale ordine.")
                                                .setTitle("Importo Insufficiente")
                                                .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int paymentCode) {

                                                    }
                                                });
                                        bldr.create();
                                        bldr.show();

                                    }else{
                                        PrintScontrinoConfirmed(activity, orderLineItems, order, paymentCode, (int) (Double.parseDouble(ins) * 100), printers.get(0).protocol, printers.get(0).printerIpAddress, printers.get(0).printerIpPort);
                                    }
                                }
                            }
                        });
                        alertDialog3.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        });


                        alertDialog3.show();
                    }
                }
            });

            alertDialog2.setNegativeButton("Annulla",new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick (DialogInterface dialog,int which){
                    dialog.cancel();
                    dialog.dismiss();

                }
            });

            alertDialog2.show();

        }



    }

    private static void PrintScontrinoConfirmed(final Activity activity, final ArrayList<OrderLineItem> myOrderLines, final Order myOrder, final int payment, final int amount, String protocol, String IP, int port){
        //importo non significativo in caso di contante(tutto) o carta di credito

        switch(protocol) {
            case "EpsonFiscal":

                new PrinterFiscalBG(activity.getApplicationContext(), IP, port, new PrinterFiscalBG.Callbacks() {
                    @Override
                    public void ok(PrinterFiscal p) {
                        try {
                            p.printerReset();

                            p.fiscalBegin();
                            for (int i = 0; i < myOrderLines.size(); i++) {

                                String name = myOrderLines.get(i).category + " - " +  myOrderLines.get(i).name;

                                if(name.length()<47) {
                                    p.fiscalSellProduct(name, myOrderLines.get(i).price, 1000, myOrderLines.get(i).repart);
                                }else{
                                    p.fiscalSellProduct(name.substring(0,46), myOrderLines.get(i).price, 1000, myOrderLines.get(i).repart);
                                    name = name.substring(46);
                                    while(name.length()>46){
                                        p.fiscalAdditionalDescription(name.substring(0,46));
                                        name = name.substring(46);
                                    }
                                    if(name.length()>0)
                                        p.fiscalAdditionalDescription(name.substring(46));
                                }

                                String notes = myOrderLines.get(i).notes;
                                if (notes != null && notes.length() > 0) {
                                    while (notes.length() > 34) {
                                        p.fiscalAdditionalDescription(" ** " + notes.substring(0, 34));
                                        notes = notes.substring(34);
                                    }
                                    p.fiscalAdditionalDescription(" ** " + notes);
                                }
                            }

                            int ship = myOrder.shippingPrice;
                            if (ship > 0) {
                                p.fiscalSellProduct("Consegna", ship, 1000, myOrder.shipVatRepart);
                            }


                            //p.fiscalSubtotal();

                            p.fiscalAdditionalRow("- - - - - -  INFORMAZIONI CLIENTE  - - - - - -");
                            p.fiscalAdditionalRow(" ");
                            p.fiscalAdditionalRow("Cliente: " + (myOrder.shipNameSurname != null ? myOrder.shipNameSurname : "Non Specificato"));
                            p.fiscalBoldAdditionalRow("Campanello: " + (myOrder.shipBell != null ? myOrder.shipBell : "Non Specificato"));
                            p.fiscalAdditionalRow("Piano: " + (myOrder.shipFloor != null ? myOrder.shipFloor : "Non Specificato"));
                            p.fiscalAdditionalRow((myOrder.shipStreet != null ? myOrder.shipStreet : "Non Specificato"));
                            p.fiscalAdditionalRow((myOrder.shipCity != null ? myOrder.shipCity : "Non Specificato"));
                            p.fiscalAdditionalRow("Telefono: " + (myOrder.shipTelephone != null ? myOrder.shipTelephone : "Non Specificato"));
                            p.fiscalAdditionalRow(" ");

                            String notes = myOrder.notes;
                            if (notes != null && notes.length() > 0) {
                                p.fiscalBoldAdditionalRow("Note:");
                                while (notes.length() > 46) {
                                    p.fiscalBoldAdditionalRow(notes.substring(0, 46));
                                    notes = notes.substring(46);
                                }
                                p.fiscalBoldAdditionalRow(notes);
                            }

                            p.fiscalAdditionalRow(" ");
                            p.fiscalAdditionalRow("- - - - - -  INFORMAZIONI  ORDINE  - - - - - -");
                            p.fiscalAdditionalRow(" ");
                            p.fiscalBigAdditionalRow("Numero Ordine: " + p.fillStr(Integer.toString(myOrder.orderId), 10, '0'));
                            p.fiscalAdditionalRow(" ");
                            p.fiscalAdditionalRow("Stato Pagamento: " + (myOrder.isPayed ? "Pagato" : "Non Pagato "));
                            p.fiscalAdditionalRow(" ");
                            p.fiscalAdditionalRow("- - - - - - - - - - - - - - - - - - - - - - - ");
                            p.fiscalAdditionalRow(" ");
                            p.fiscalAdditionalRow("Powered by TapFood!");

                            //{ "Contanti (tutto)", "Contanti (importo)", "Carta di Credito", "Assegno"};
                            switch (payment) {
                                case 0:
                                    p.fiscalPaymentContante(p.getFiscalTotal());
                                    break;
                                case 1:
                                    p.fiscalPaymentContante(amount);
                                    break;
                                case 2:
                                    p.fiscalPaymentCartaCredito();
                                    break;
                                case 3:
                                    p.fiscalPaymentAssegno(p.getFiscalTotal());
                                    break;
                            }

                            p.fiscalPaymentCartaCredito();

                            p.commit();


                        } catch (Exception ex) {
                            Logger.Error(activity.getApplicationContext(), "Errore Stampa Fiscale", ex.toString());

                            Toast mytoast = Toast.makeText(activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                            mytoast.setGravity(Gravity.TOP, 0, 200);
                            mytoast.show();

                        }
                    }

                    @Override
                    public void ko() {
                        Toast mytoast = Toast.makeText(activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                        mytoast.setGravity(Gravity.TOP, 0, 200);
                        mytoast.show();
                    }

                }).execute();
        }

    }

    public static void PrintComanda(final Activity activity, final ArrayList<OrderLineItem> orderLineItems, final Order order) throws Exception{
        final ArrayList<ProductionArea> printers = DBHelper.getInstance(activity.getApplicationContext()).getProductionAreas();

        if(printers.size()>1) {
            ArrayList<String> printerNames = new ArrayList<>();
            for (int i = 0; i < printers.size(); i++) printerNames.add(printers.get(i).description);


            String[] names = new String[printers.size()];
            names = printerNames.toArray(names);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list_item_select_printer, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Seleziona la stampante");
            alertDialog.setCancelable(true);

            alertDialog.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    PrintComandaConfirmed(activity, orderLineItems, order, printers.get(position).protocol, printers.get(position).printerIpAddress, printers.get(position).printerIpPort);
                }
            });

            alertDialog.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();
                }
            });

            alertDialog.show();
        }else{ //una sola stampante
            AlertDialog.Builder bldr = new AlertDialog.Builder( activity );
            bldr.setMessage("Confermi di voler stampare dalla stampante " + printers.get(0).description + " ?" )
                    .setTitle("Conferma")
                    .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nulla
                            PrintComandaConfirmed(activity, orderLineItems, order, printers.get(0).protocol, printers.get(0).printerIpAddress, printers.get(0).printerIpPort);
                        }
                    }).setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //annulla, non fare nulla
                }
            });
            bldr.create();
            bldr.show();
        }


    }

    private static void PrintComandaConfirmed(final Activity activity, final ArrayList<OrderLineItem> myOrderLines, final Order myOrder, String protocol, String IP, int port) {
        switch (protocol) {
            case "EpsonFiscal":
                new PrinterFiscalBG(activity.getApplicationContext(), IP,port, new PrinterFiscalBG.Callbacks() {
                    @Override
                    public void ok(PrinterFiscal p) {
                        try {
                            Date now = new Date();
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


                            p.printerReset();

                            p.nonFiscalBegin();

                            p.nonFiscalBigBoldLine("                   COMANDA                    ");
                            p.nonFiscalEmptyLine();

                            p.nonFiscalLine("             " + df.format(now ));

                            p.nonFiscalEmptyLine();
                            p.nonFiscalEmptyLine();

                            p.nonFiscalEmptyLine();
                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("- - - - - -  INFORMAZIONI CLIENTE  - - - - - -");
                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("Cliente: " + (myOrder.shipNameSurname != null ? myOrder.shipNameSurname : "Non Specificato"));
                            p.nonFiscalBoldLine("Campanello: " + (myOrder.shipBell != null ? myOrder.shipBell : "Non Specificato"));
                            p.nonFiscalLine("Piano: " + (myOrder.shipFloor != null ? myOrder.shipFloor : "Non Specificato"));
                            p.nonFiscalLine((myOrder.shipStreet != null ? myOrder.shipStreet : "Non Specificato"));
                            p.nonFiscalLine((myOrder.shipCity != null ? myOrder.shipCity : "Non Specificato"));
                            p.nonFiscalLine("Telefono: " + (myOrder.telephone != null ? myOrder.telephone : "Non Specificato"));
                            p.nonFiscalEmptyLine();

                            String notes = myOrder.notes;
                            if (notes != null && notes.length() > 0) {
                                p.nonFiscalBoldLine("Note:");
                                while (notes.length() > 46) {
                                    p.nonFiscalBoldLine(notes.substring(0, 46));
                                    notes = notes.substring(46);
                                }
                                p.nonFiscalBoldLine(notes);
                            }

                            p.nonFiscalEmptyLine();
                            p.nonFiscalLine("- - - - - -  INFORMAZIONI  ORDINE  - - - - - -");
                            p.nonFiscalLine(" ");
                            p.nonFiscalBigLine("Numero Ordine: " + p.fillStr(Integer.toString(myOrder.orderId), 10, '0'));
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("Stato Pagamento: " + (myOrder.isPayed ? "Pagato" : "Non Pagato "));
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("- - - - - - - - - - - - - - - - - - - - - - - ");
                            p.nonFiscalLine(" ");
                            p.nonFiscalLine("Powered by TapFood!");


                            p.nonFiscalBigLine(" ");

                            for(int i=0;i<myOrderLines.size();i++){
                                String name = myOrderLines.get(i).category + " - " +  myOrderLines.get(i).name;
                                while(name.length()>46){
                                    p.nonFiscalBigLine( name.substring(0,46));
                                    name = name.substring(46);
                                }
                                p.nonFiscalLine(name);

                                for(int j=0;j<myOrderLines.get(i).ingredients.size();j++){
                                    String nome = myOrderLines.get(i).ingredients.get(j);
                                    while( nome.length()> 46 ){
                                        p.nonFiscalLine( nome.substring(0,46));
                                        nome = "  " + nome.substring(46);
                                    }
                                    p.nonFiscalLine( nome   );


                                }

                                String OLInotes = myOrderLines.get(i).notes;
                                if(OLInotes != null && OLInotes.length()>0){
                                    while(OLInotes.length()>42){
                                        p.nonFiscalBoldLine("*** " + OLInotes.substring(0, 42));
                                        OLInotes = OLInotes.substring(42);
                                    }
                                    p.nonFiscalBoldLine("*** " + OLInotes);
                                }

                                p.nonFiscalEmptyLine();
                            }


                            p.endNonFiscal();

                            p.commit();


                        }catch(Exception ex)
                        {
                            Logger.Error(activity.getApplicationContext(), "Errore Stampa Fiscale", ex.toString());

                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                        }
                    }

                    @Override
                    public void ko(){
                        ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                    }

                }).execute();
            case "EpsonNonFiscal":
                new PrinterEpsonBG(activity.getApplicationContext(), IP, port, new PrinterEpsonBG.Callbacks() {
                    @Override

                    public void ok(PrinterEpson p) {
                        try {
                            p.textAlignCenter();
                            p.textSizeBig();
                            p.print("COMANDA");
                            p.textSizeSmall();
                            p.newLine();
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date date = new Date();
                            p.print(dateFormat.format(date));
                            p.newLine();
                            p.textAlignLeft();
                            p.print("Numero Ordine: " + p.fillStr(Integer.toString(myOrder.orderId), 10, '0'));
                            p.print("Destinatario: " + (myOrder.shipNameSurname != null ? myOrder.shipNameSurname : "Non Specificato"));
                            p.print("Campanello: " + (myOrder.shipBell != null ? myOrder.shipBell : "Non Specificato"));
                            p.print("Indirizzo: " + (myOrder.shipStreet != null ? myOrder.shipStreet : "Non Specificato"));
                            p.print((myOrder.shipCity != null ? myOrder.shipCity : "Non Specificato"));
                            p.print("Piano: " + (myOrder.shipFloor != null ? myOrder.shipFloor : "Non Specificato"));
                            p.print("Consegna: " + (myOrder.agreedTime != null ? myOrder.agreedTime : "Non Specificato"));
                            p.print("Telefono: " + (myOrder.telephone != null ? myOrder.telephone : "Non Specificato"));
                            p.print("");

                            p.textSizeNormal();
                            String notes = myOrder.notes;
                            if(notes != null && notes.length()>0){
                                p.print("Note:");
                                p.print(notes);
                            }

                            p.print(" ");

                            p.textSizeNormal();

                            for(int i=0;i<myOrderLines.size();i++){
                                String name = myOrderLines.get(i).category + " - " +  myOrderLines.get(i).name;
                                while(name.length()>46){
                                    p.print(name.substring(0, 46));
                                    name = name.substring(46);
                                }
                                p.print(name);

                                p.textSizeSmall();

                                for(int j=0;j<myOrderLines.get(i).ingredients.size();j++){
                                    p.print( myOrderLines.get(i).ingredients.get(j)   );
                                }

                                String OLInotes = myOrderLines.get(i).notes;
                                if(OLInotes != null && OLInotes.length()>0){
                                    p.print("*** " + OLInotes);
                                }

                                p.textSizeNormal();

                                p.print("");
                            }


                            p.print(" ");
                            p.partialCut();

                            Thread.sleep(100);

                            p.close();
                        }catch (Exception ex){
                            Logger.Error(activity.getApplicationContext(), "Errore stampa non fiscale", ex.toString());

                            Toast mytoast = Toast.makeText( activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                            mytoast.setGravity(Gravity.TOP, 0, 200);
                            mytoast.show();
                        }
                    }

                    @Override
                    public void ko() {
                        Toast mytoast = Toast.makeText( activity.getApplicationContext(), "Errore durante la stampa. Controllare la stampante e riprovare.", Toast.LENGTH_LONG);
                        mytoast.setGravity(Gravity.TOP, 0, 200);
                        mytoast.show();
                    }
                }).execute();
        }
    }

    public static void PrintersTest(final Activity activity) throws Exception{
        final ArrayList<ProductionArea> printers = DBHelper.getInstance(activity.getApplicationContext()).getProductionAreas();

        if(printers.size()>1) {
            ArrayList<String> printerNames = new ArrayList<>();
            for (int i = 0; i < printers.size(); i++) printerNames.add(printers.get(i).description);


            String[] names = new String[printers.size()];
            names = printerNames.toArray(names);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list_item_select_printer, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Seleziona la stampante da testare");
            alertDialog.setCancelable(true);

            alertDialog.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    PrinterTestConfirmed(activity, printers.get(position).protocol, printers.get(position).printerIpAddress, printers.get(position).printerIpPort);
                }
            });

            alertDialog.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();
                }
            });

            alertDialog.show();
        }else{ //una sola stampante
            AlertDialog.Builder bldr = new AlertDialog.Builder( activity );
            bldr.setMessage("Confermi di voler testare la stampante " + printers.get(0).description + " ?" )
                    .setTitle("Conferma")
                    .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PrinterTestConfirmed(activity,printers.get(0).protocol, printers.get(0).printerIpAddress, printers.get(0).printerIpPort);
                        }
                    }).setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //annulla, non fare nulla
                }
            });
            bldr.create();
            bldr.show();
        }
    }

    private static void PrinterTestConfirmed(final Activity activity, final String protocol, final String IP, final int port) {
        switch (protocol) {
            case "EpsonFiscal":
                new PrinterFiscalBG(activity.getApplicationContext(), IP,port, new PrinterFiscalBG.Callbacks() {
                    @Override
                    public void ok(PrinterFiscal p) {
                        try {
                            Date now = new Date();
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


                            p.printerReset();

                            p.nonFiscalBegin();


                            p.nonFiscalBigBoldLine("                   TAP-FOOD               ");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("                Test Stampante      ");
                            p.nonFiscalLine("");
                            p.nonFiscalBoldLine("Data/Ora:" + df.format(now));
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("IP Stampante:" + IP);
                            p.nonFiscalLine("Porta Stampante: " + port);
                            p.nonFiscalLine("Protocollo: TCP " + protocol);
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");

                            p.nonFiscalLine("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.nonFiscalBoldLine("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.nonFiscalBigLine("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.nonFiscalBigBoldLine("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("abcdefghijklmnopqrstuvwxyz");
                            p.nonFiscalBoldLine("abcdefghijklmnopqrstuvwxyz");
                            p.nonFiscalBigLine("abcdefghijklmnopqrstuvwxyz");
                            p.nonFiscalBigBoldLine("abcdefghijklmnopqrstuvwxyz");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("01234567890");
                            p.nonFiscalBoldLine("01234567890");
                            p.nonFiscalBigLine("01234567890");
                            p.nonFiscalBigBoldLine("01234567890");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");

                            p.nonFiscalBigBoldLine("            TEST STAMPANTE SUPERATO");

                            p.nonFiscalLine("");
                            p.nonFiscalLine("");
                            p.nonFiscalLine("");



                            p.nonFiscalEmptyLine();


                            p.endNonFiscal();

                            p.commit();


                        }catch(Exception ex)
                        {
                            Logger.Error(activity.getApplicationContext(), "Errore test stampante", ex.toString());

                            ActivityHome.toast("Errore durante il test stampante. Controllare la stampante e riprovare.");
                        }
                    }

                    @Override
                    public void ko(){
                        ActivityHome.toast("Errore durante il test stampante. Controllare la stampante e riprovare.");
                    }

                }).execute();
            case "EpsonNonFiscal":
                new PrinterEpsonBG(activity.getApplicationContext(), IP, port, new PrinterEpsonBG.Callbacks() {
                    @Override

                    public void ok(PrinterEpson p) {
                        try {
                            Date now = new Date();
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                            p.textAlignCenter();
                            p.textSizeBig();
                            p.print("TAP-FOOD");
                            p.textSizeSmall();
                            p.newLine();
                            p.print("Test Stampante");
                            p.newLine();
                            p.newLine();
                            p.print("Data/Ora:" + df.format(now));
                            p.textAlignLeft();
                            p.newLine();
                            p.newLine();
                            p.print("IP Stampante:" + IP);
                            p.print("Porta Stampante: " + port);
                            p.print("Protocollo: TCP " + protocol);
                            p.newLine();
                            p.newLine();
                            p.textSizeSmall();
                            p.print("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.textSizeNormal();
                            p.print("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.textSizeBig();
                            p.print("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                            p.textSizeSmall();
                            p.print("abcdefghijklmnopqrstuvwxyz");
                            p.textSizeNormal();
                            p.print("abcdefghijklmnopqrstuvwxyz");
                            p.textSizeBig();
                            p.print("abcdefghijklmnopqrstuvwxyz");
                            p.textSizeSmall();
                            p.print("01234567890");
                            p.textSizeNormal();
                            p.print("01234567890");
                            p.textSizeBig();
                            p.print("01234567890");
                            p.newLine();
                            p.newLine();
                            p.newLine();
                            p.textSizeNormal();
                            p.textAlignCenter();
                            p.print("TEST STAMPANTE SUPERATO");
                            p.newLine();
                            p.newLine();
                            p.newLine();



                            p.partialCut();

                            Thread.sleep(100);

                            p.close();

                        }catch (Exception ex){
                            Logger.Error(activity.getApplicationContext(), "Errore test stampante", ex.toString());

                            ActivityHome.toast("Errore durante il test stampante. Controllare la stampante e riprovare.");
                        }
                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("Errore durante il test stampante. Controllare la stampante e riprovare.");
                    }
                }).execute();
        }
    }

    public static void FiscalPrinterSync(final Activity activity, final int productionAreaId){

        //TODO: oltre a FiscalHeader impostare anche i vatReparts
        //TODO 2: salvare sul db lastSync e chiamare ws Update Production Area con lastSync modificato! (aggiungere campi all'oggetto e al ws)
        final String[] header = DBHelper.getInstance(activity.getApplicationContext()).getFiscalPrinterHeader();
        String strHeader = "";
        for(int i=0;i<header.length;i++)
            strHeader += header[i]+"\n";
        AlertDialog.Builder bldr = new AlertDialog.Builder( activity );

        bldr.setMessage("Sei sicuro di voler sincronizzare la stampante fiscale? \nVerranno sincronizzati i reparti IVA e l'intestazione. " +
                        "\n\n" + strHeader )
                .setTitle("Sincronizzazione Stampante Fiscale")
                .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] printer = DBHelper.getInstance(activity.getApplicationContext()).getPrinterIpPort(productionAreaId);
                        if(printer.length > 0)
                            FiscalHeaderSetupConfirmed(activity, "EpsonFiscal", printer[0], Integer.parseInt(printer[1]), header);

                    }
                }).setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //annulla, non fare nulla
                    }
        });
        bldr.create();
        bldr.show();
    }

    public static void FiscalHeaderSetupConfirmed(final Activity activity, final String protocol, String IP, int port, final String[] header){
        switch (protocol) {
            case "EpsonFiscal":
                new PrinterFiscalBG(activity.getApplicationContext(), IP,port, new PrinterFiscalBG.Callbacks() {
                    @Override
                    public void ok(PrinterFiscal p) {
                        try {


                            for(int i=0;i<header.length;i++){
                                while(header[i].length()>40){
                                    String line = header[i].substring(0,40);
                                    header[i] = header[i].substring(0,40);

                                    p.setUpFiscalHeader( p.alignCenter(line,40));
                                }
                                p.setUpFiscalHeader( p.alignCenter(header[i],40));
                            }
                            p.setUpFiscalHeaderTest();
                            p.setUpFiscalHeaderEnd();

                            p.commit();


                        }catch(Exception ex)
                        {
                            Logger.Error(activity.getApplicationContext(), "Errore inizializzazione  stampante", ex.toString());

                            ActivityHome.toast("Errore durante inizializzazione stampante. Controllare la stampante e riprovare.");
                        }
                    }

                    @Override
                    public void ko(){
                        ActivityHome.toast("Errore durante inizializzazione stampante. Controllare la stampante e riprovare.");
                    }

                }).execute();
        }

    }
}
