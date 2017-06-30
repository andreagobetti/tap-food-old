package com.lynkteam.tapmanager.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.Order;
import com.lynkteam.tapmanager.DB.OrderLineItem;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.DecimalUtil;
import com.lynkteam.tapmanager.util.OrderManagementUtil;
import com.lynkteam.tapmanager.util.PrintUtil;

import java.util.ArrayList;

public class FragmentOpenOrdersDetails extends android.support.v4.app.Fragment {

    //popolato dall'activity prima di chiamare il fragment
    public static int ORDER_ID;

    private ArrayList<Object> orderLineItemsIngredients = new ArrayList<>();
    private ArrayList<OrderLineItem> orderLineItems = new ArrayList<>();

    private Order myOrder;

    public static FragmentOpenOrdersDetails thisInstance;


    public FragmentOpenOrdersDetails() {
        // Required empty public constructor
        thisInstance = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisInstance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_orders_details, container, false);
    }

    public static void reloadData(){
        FragmentOpenOrdersDetails.thisInstance.loadData();
    }

    public void loadData(){
        ExpandableListView lines = (ExpandableListView) getActivity().findViewById(R.id.lv_open_order_details);

        lines.setDividerHeight(0);
        lines.setGroupIndicator(null);
        lines.setClickable(true);

        orderLineItemsIngredients.clear();
        myOrder = null;

        orderLineItems = DBHelper.getInstance(getActivity().getApplicationContext()).getOrderLineItems(FragmentOpenOrdersDetails.ORDER_ID);

        for(int i=0;i<orderLineItems.size();i++){

            ArrayList<String> child = new ArrayList<String>();
            for(int j=0;j<orderLineItems.get(i).ingredients.size();j++){

                child.add( orderLineItems.get(i).ingredients.get(j) );
            }
            orderLineItemsIngredients.add(child);
        }


        AdapterOrderDetail adapter = new AdapterOrderDetail(orderLineItems, orderLineItemsIngredients);

        adapter.setInflater((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE), getActivity());

        lines.setAdapter(adapter);

        myOrder = DBHelper.getInstance(getActivity().getApplicationContext()).getOrder(FragmentOpenOrdersDetails.ORDER_ID);


        ((TextView) getView().findViewById(R.id.txt_open_orders_detail_stato)).setText( myOrder.orderState );
        ((TextView) getView().findViewById(R.id.txt_open_orders_detail_cliente)).setText( myOrder.telephone );
        ((TextView) getView().findViewById(R.id.txt_open_orders_detail_indirizzo)).setText( myOrder.shipAddress );
        ((TextView) getView().findViewById(R.id.txt_open_orders_detail_totale)).setText( DecimalUtil.formatInt(myOrder.total));
        ((TextView) getView().findViewById(R.id.txt_open_orders_detail_shipping)).setText(DecimalUtil.formatInt(myOrder.shippingPrice));

        if(myOrder.agreedTime == null || myOrder.agreedTime.equals("null")){
            if(myOrder.requestedTime == null || myOrder.requestedTime.equals("null")) {
                ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setText("--.--");
                ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setTextColor( getActivity().getResources().getColor(R.color.black));
            }else {
                ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setText(myOrder.requestedTime.substring(0, 16));
                ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setTextColor(getActivity().getResources().getColor(R.color.red));
            }
        }else{
            ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setText(myOrder.agreedTime.substring(0, 16));
            ((TextView) getView().findViewById(R.id.txt_open_orders_detail_orario)).setTextColor(getActivity().getResources().getColor(R.color.black));
        }

        switch(myOrder.orderStateCode){
            case "new": case "viewed":

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_scontrino)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_preordine)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_comanda)).setVisibility(View.INVISIBLE);

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setText("ACCETTA");
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setText("RIFIUTA");

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { //accetta

                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
                            bldr.setMessage("Sei sicuro/a di voler accettare l'ordine?")
                                    .setTitle("Concludi Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            OrderManagementUtil.setNewState(getActivity().getApplicationContext(), myOrder, "con");
                                            myOrder.orderStateCode = "con";
                                            myOrder.orderState = DBHelper.getInstance(getActivity().getApplicationContext()).getStatoOrdine("con");
                                            loadData();
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    });
                            ;
                            bldr.create();
                            bldr.show();

                        } catch (Exception ex) {
                            Logger.Error(getActivity().getApplicationContext(), "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }

                    }
                });

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
                            bldr.setMessage("Sei sicuro/a di voler rifiutare l'ordine?")
                                    .setTitle("Annulla Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {
                                            OrderManagementUtil.setNewState(getActivity().getApplicationContext(), myOrder, "refCom");
                                            myOrder.orderStateCode = "refCom";
                                            myOrder.orderState = DBHelper.getInstance(getActivity().getApplicationContext()).getStatoOrdine("refCom");
                                            loadData();
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
                            Logger.Error(getActivity().getApplicationContext(), "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }

                    }
                });

                break;
            case "con":case "exec":
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_scontrino)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_preordine)).setVisibility(View.VISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_comanda)).setVisibility(View.VISIBLE);

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setText("CONCLUDI");
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setText("ANNULLA");


                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
                            bldr.setMessage("Sei sicuro/a di voler concludere l'ordine?")
                                    .setTitle("Concludi Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {
                                            OrderManagementUtil.setNewState(getActivity().getApplicationContext(), myOrder, "concluded");
                                            myOrder.orderStateCode = "concluded";
                                            myOrder.orderState = DBHelper.getInstance(getActivity().getApplicationContext()).getStatoOrdine("concluded");
                                            loadData();
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
                            Logger.Error(getActivity().getApplicationContext(), "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }

                    }
                });

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
                            bldr.setMessage("Sei sicuro/a di voler annullare l'ordine?")
                                    .setTitle("Annulla Ordine")
                                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int paymentCode) {
                                            OrderManagementUtil.setNewState(getActivity().getApplicationContext(), myOrder, "genProblem");
                                            myOrder.orderStateCode = "genProblem";
                                            myOrder.orderState = DBHelper.getInstance(getActivity().getApplicationContext()).getStatoOrdine("genProblem");
                                            loadData();
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
                            Logger.Error(getActivity().getApplicationContext(), "Errore Aggiornamento Stato", ex.toString());
                            ActivityHome.toast("Errore durante l'aggiornamento dello stato.");
                        }

                    }
                });


                ((Button) getView().findViewById(R.id.btn_open_orders_detail_scontrino)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            if(!myOrder.isPreorder){
                                AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
                                bldr.setMessage("Attenzione! Hai già stampato uno scontrino fiscale! Sei sicuro/a di volerlo stampare nuovamente?")
                                        .setTitle("Stampa Scontrino")
                                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int paymentCode) {
                                                try {
                                                    PrintUtil.PrintScontrino(thisInstance.getActivity(), orderLineItems, myOrder);
                                                    OrderManagementUtil.setIsPreorder(getActivity().getApplicationContext(), myOrder.orderId, myOrder.lastEdit, false);
                                                    OrderManagementUtil.setPrintedFiscal(getActivity().getApplicationContext(), myOrder);

                                                    myOrder.isPreorder = false;


                                                } catch (Exception ex) {
                                                    Logger.Error(getActivity().getApplicationContext(), "Errore Stampa Fiscale", ex.toString());
                                                    ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                                                }
                                            }
                                        })
                                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int paymentCode) {

                                            }
                                        });;
                                bldr.create();
                                bldr.show();
                            }else{
                                PrintUtil.PrintScontrino(thisInstance.getActivity(), orderLineItems, myOrder);
                                OrderManagementUtil.setIsPreorder(getActivity().getApplicationContext(), myOrder.orderId, myOrder.lastEdit, false);
                                OrderManagementUtil.setPrintedFiscal(getActivity().getApplicationContext(), myOrder);

                                myOrder.isPreorder = false;
                            }


                        } catch (Exception ex) {
                            Logger.Error(getActivity().getApplicationContext(), "Errore Stampa Fiscale", ex.toString());
                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                        }

                    }
                });


                ((Button) getView().findViewById(R.id.btn_open_orders_detail_preordine)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            PrintUtil.PrintPreordine(thisInstance.getActivity(), orderLineItems, myOrder);
                            OrderManagementUtil.setPrintedPreorder(getActivity().getApplicationContext(), myOrder.orderId, myOrder.lastEdit);


                        } catch (Exception ex) {
                            Logger.Error(getActivity().getApplicationContext(), "Errore Stampa Fiscale", ex.toString());
                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                        }
                    }
                });

                ((Button) getView().findViewById(R.id.btn_open_orders_detail_comanda)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            PrintUtil.PrintComanda(thisInstance.getActivity(), orderLineItems, myOrder);
                        } catch (Exception ex) {
                            Logger.Error(getActivity().getApplicationContext(), "Errore Stampa Fiscale", ex.toString());
                            ActivityHome.toast("Errore durante la stampa. Controllare la stampante e riprovare.");
                        }
                    }
                });

                break;
            default:
                //nascondi tutto
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_concludi)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_annulla)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_scontrino)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_preordine)).setVisibility(View.INVISIBLE);
                ((Button) getView().findViewById(R.id.btn_open_orders_detail_comanda)).setVisibility(View.INVISIBLE);
        }



        if(myOrder.orderStateCode.equals("new")) {
            OrderManagementUtil.setNewState(getActivity().getApplicationContext(), myOrder, "viewed");
            myOrder.orderStateCode = "viewed";
            myOrder.orderState = DBHelper.getInstance(getActivity().getApplicationContext()).getStatoOrdine("new");

            loadData();
        }
    }




}
