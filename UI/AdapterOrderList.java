package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.Order;
import com.lynkteam.tapmanager.DB.OrderLineItem;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.OrderManagementUtil;
import com.lynkteam.tapmanager.util.PrintUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by robertov on 30/07/2015.
 */
public class AdapterOrderList extends ArrayAdapter<Order> {
    protected Context c;
    protected Activity a;

    public AdapterOrderList(Context context, Activity activity, ArrayList<Order> orders) {
        super(context, 0, orders);
        c=context;
        a=activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position, the order of the orders is reversed, the newest orders are on top.

        final Order order = getItem(position);
        //final Order order = getItem(getCount()-position-1);


        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_order, parent, false);

        //img sulla sinistra
        ImageView imgLeft = (ImageView) convertView.findViewById(R.id.img_open_orders);

        //campi di testo
        TextView txtAddress = (TextView) convertView.findViewById(R.id.txt_open_orders_address);
        TextView txtHour = (TextView) convertView.findViewById(R.id.txt_open_orders_hour);
        TextView txtState = (TextView) convertView.findViewById(R.id.txt_open_orders_state);

        //bottoni sulla dx
        Button btn1 = (Button) convertView.findViewById(R.id.btn_open_orders_list1);
        Button btn2 = (Button) convertView.findViewById(R.id.btn_open_orders_list2);

        //ultimo imgButton
        ImageButton imgBtn = (ImageButton) convertView.findViewById(R.id.imgbtn_open_orders_dettaglio);

        //se ordine = nuovo -> riga rossa
        //se ordine che se ne sta andando -> riga grigia
        if(order.isNew)
            ((RelativeLayout) convertView.findViewById(R.id.rl_open_orders)).setBackgroundColor(convertView.getResources().getColor( R.color.red_light));

        //immagine sulla sinistra
        if(order.orderSource==1)
            imgLeft.setImageResource(R.drawable.ic_tapfood);
        else
            imgLeft.setImageResource(R.drawable.ic_store_black_48dp);

        //consegnare in locale?
        boolean locale = order.shipAddress == null || order.shipAddress == "null";

        if (locale)
              txtAddress.setText("IN LOCALE");
        else
            txtAddress.setText(order.shipAddress);


        String ora;
        if(order.agreedTime == null || order.agreedTime.equals("null")){
            if(order.requestedTime == null || order.requestedTime.equals("null")) {
                ora = "--.--";
                txtHour.setTextColor( c.getResources().getColor(R.color.black_overlay));
            }else {
                ora = order.requestedTime.substring(11, 16);
                txtHour.setTextColor( c.getResources().getColor(R.color.red));
            }
        }else{
            ora = order.agreedTime.substring(11,16);
            txtHour.setTextColor( c.getResources().getColor(R.color.black_overlay));
        }
        txtHour.setText(ora);

        //stato (testuale)
        txtState.setText(order.orderState);

        //TODO Sara: cambiare testi nei settext e metterli dentro strings

        //bottoni sulla riga a seconda dello stato dell'ordine
        switch(order.orderStateCode){
            case "new":case "viewed":
                btn1.setText("ACCETTA");
                btn2.setText("RIFIUTA");

                btn1.setBackgroundColor(convertView.getResources().getColor(R.color.green));
                btn2.setBackgroundColor(convertView.getResources().getColor(R.color.red));

                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(a);
                            bldr.setMessage("Sei sicuro/a di voler accettare l'ordine?")
                                    .setTitle("Concludi Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {
                                            OrderManagementUtil.setNewState(c, order, "con");
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {

                                        }
                                    });
                            ;
                            bldr.create();
                            bldr.show();

                        } catch (Exception ex) {
                            Logger.Error(c, "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }
                    }
                });

                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(a);
                            bldr.setMessage("Sei sicuro/a di voler rifiutare l'ordine?")
                                    .setTitle("Annulla Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {
                                            OrderManagementUtil.setNewState(c, order, "refCom");
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {

                                        }
                                    });
                            ;
                            bldr.create();
                            bldr.show();

                        } catch (Exception ex) {
                            Logger.Error(c, "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }
                    }
                });
                break;
            case "exec": case "con":
                switch(order.printerStatus){
                    case "00":
                        btn1.setText("SCONTRINO");
                        btn2.setText("COMANDA");

                        btn1.setBackgroundColor(convertView.getResources().getColor(R.color.yellow));
                        btn2.setBackgroundColor(convertView.getResources().getColor(R.color.blue));

                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (!order.isPreorder) {
                                        AlertDialog.Builder bldr = new AlertDialog.Builder(c);
                                        bldr.setMessage("Attenzione! Hai già stampato uno scontrino fiscale! Sei sicuro/a di volerlo stampare nuovamente?")
                                                .setTitle("Stampa Scontrino")
                                                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int paymentCode) {
                                                        try {
                                                            ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);

                                                            PrintUtil.PrintScontrino(a, myOrderLines, order);
                                                            OrderManagementUtil.setIsPreorder(c, order.orderId, order.lastEdit, false);
                                                            OrderManagementUtil.setPrintedFiscal(c, order);
                                                            order.isPreorder = false;

                                                        } catch (Exception ex) {
                                                            Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int paymentCode) {
                                                    }
                                                });
                                        ;
                                        bldr.create();
                                        bldr.show();
                                    } else {
                                        ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);
                                        PrintUtil.PrintScontrino(a, myOrderLines, order);
                                        OrderManagementUtil.setIsPreorder(c, order.orderId, order.lastEdit, false);
                                        OrderManagementUtil.setPrintedFiscal(c, order);
                                        order.isPreorder = false;
                                    }
                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                }
                            }
                        });

                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);
                                    PrintUtil.PrintComanda(a, myOrderLines, order);
                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                }
                            }
                        });
                        break;
                    case "01":
                        btn1.setText("COMANDA");
                        btn2.setText("CONCLUDI");

                        btn1.setBackgroundColor(convertView.getResources().getColor(R.color.blue));
                        btn2.setBackgroundColor(convertView.getResources().getColor(R.color.green));

                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);
                                    PrintUtil.PrintComanda(a, myOrderLines, order);
                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                }
                            }
                        });

                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AlertDialog.Builder bldr = new AlertDialog.Builder(a);
                                    bldr.setMessage("Sei sicuro/a di voler concludere l'ordine?")
                                            .setTitle("Concludi Ordine")
                                            .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {
                                                    OrderManagementUtil.setNewState(c, order, "concluded");
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {

                                                }
                                            });;
                                    bldr.create();
                                    bldr.show();

                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Aggiornamento Stato", ex.toString());
                                    ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                                }
                            }
                        });
                        break;
                    case "10":
                        btn1.setText("SCONTRINO");
                        btn2.setText("CONCLUDI");

                        btn1.setBackgroundColor(convertView.getResources().getColor(R.color.yellow));
                        btn2.setBackgroundColor(convertView.getResources().getColor(R.color.green));

                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (!order.isPreorder) {
                                        AlertDialog.Builder bldr = new AlertDialog.Builder(c);
                                        bldr.setMessage("Attenzione! Hai già stampato uno scontrino fiscale! Sei sicuro/a di volerlo stampare nuovamente?")
                                                .setTitle("Stampa Scontrino")
                                                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int paymentCode) {
                                                        try {
                                                            ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);

                                                            PrintUtil.PrintScontrino(a, myOrderLines, order);
                                                            OrderManagementUtil.setIsPreorder(c, order.orderId, order.lastEdit, false);
                                                            OrderManagementUtil.setPrintedFiscal(c, order);
                                                            order.isPreorder = false;

                                                        } catch (Exception ex) {
                                                            Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int paymentCode) {
                                                    }
                                                });
                                        ;
                                        bldr.create();
                                        bldr.show();
                                    } else {
                                        ArrayList<OrderLineItem> myOrderLines = DBHelper.getInstance(c).getOrderLineItems(order.orderId);
                                        PrintUtil.PrintScontrino(a, myOrderLines, order);
                                        OrderManagementUtil.setIsPreorder(c, order.orderId, order.lastEdit, false);
                                        OrderManagementUtil.setPrintedFiscal(c, order);
                                        order.isPreorder = false;
                                    }
                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Stampa Fiscale", ex.toString());
                                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                }
                            }
                        });
                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AlertDialog.Builder bldr = new AlertDialog.Builder(a);
                                    bldr.setMessage("Sei sicuro/a di voler concludere l'ordine?")
                                            .setTitle("Concludi Ordine")
                                            .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {
                                                    OrderManagementUtil.setNewState(c, order, "concluded");
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {

                                                }
                                            });;
                                    bldr.create();
                                    bldr.show();

                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Aggiornamento Stato", ex.toString());
                                    ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                                }
                            }
                        });
                        break;
                    case "11":
                        btn1.setText("DETTAGLIO");
                        btn2.setText("CONCLUDI");

                        btn1.setBackgroundColor(convertView.getResources().getColor(R.color.black_overlay));
                        btn2.setBackgroundColor(convertView.getResources().getColor(R.color.green));
                        btn1.setTag(order.orderId);

                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((ActivityHome) a).onClickOrderDetails(v);
                            }
                        });

                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AlertDialog.Builder bldr = new AlertDialog.Builder(a);
                                    bldr.setMessage("Sei sicuro/a di voler concludere l'ordine?")
                                            .setTitle("Concludi Ordine")
                                            .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {
                                                    OrderManagementUtil.setNewState(c, order, "concluded");
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int paymentCode) {

                                                }
                                            });
                                    ;
                                    bldr.create();
                                    bldr.show();

                                } catch (Exception ex) {
                                    Logger.Error(c, "Errore Aggiornamento Stato", ex.toString());
                                    ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                                }
                            }
                        });


                        break;
                    default:
                        btn1.setVisibility(View.INVISIBLE);
                        btn2.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                btn1.setVisibility(View.INVISIBLE);
                btn2.setVisibility(View.INVISIBLE);


        }

        imgBtn.setBackgroundColor(0x00);

        imgBtn.setTag(order.orderId);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends Order> collection) {
        super.addAll(collection);
    }
}