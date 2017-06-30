package com.lynkteam.tapmanager.util;

import android.content.ContentValues;
import android.content.Context;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Order;
import com.lynkteam.tapmanager.UI.FragmentOpenOrders;
import com.lynkteam.tapmanager.WS.WsUpdateOrder;

/**
 * Created by robertov on 28/08/15.
 */
public class OrderManagementUtil {

    public static void setIsPreorder(Context context, int orderId, String lastEdit, boolean isPreorder){
        ContentValues contentValues = new ContentValues();

        contentValues.put("orderId",orderId);
        contentValues.put("lastEdit",lastEdit);
        contentValues.put("isPreOrder",isPreorder);

        DBHelper.getInstance(context).updateOrder(orderId, contentValues);

        new WsUpdateOrder(context, contentValues, new WsUpdateOrder.Callbacks() {
            @Override
            public void ok() {
                FragmentOpenOrders.reloadData();
            }

            @Override
            public void ko() {

            }
        }).execute();
    }

    public static void setNewState(Context context, Order order, String newState){
        ContentValues contentValues = new ContentValues();

        if(newState.equals("con"))
            contentValues.put("agreedDeliveryTime", order.requestedTime);

        contentValues.put("orderId",order.orderId);
        contentValues.put("lastEdit",order.lastEdit);
        contentValues.put("orderStateCode",newState);


        DBHelper.getInstance(context).updateOrder(order.orderId, contentValues);

        new WsUpdateOrder(context, contentValues, new WsUpdateOrder.Callbacks() {
            @Override
            public void ok() {

            }

            @Override
            public void ko() {

            }
        }).execute();
    }

    public static void setPrintedFiscal(Context context, Order order){
        String printerStatus = DBHelper.getInstance(context).getPrinterStatusOrdine(order.orderId);

        printerStatus = printerStatus.substring(0,1)+"1";

        ContentValues contentValues = new ContentValues();

        contentValues.put("orderId",order.orderId);
        contentValues.put("lastEdit",order.lastEdit);
        contentValues.put("printerStatus", printerStatus);
        contentValues.put("isPayed",1);

        DBHelper.getInstance(context).updateOrder(order.orderId, contentValues);

        new WsUpdateOrder(context, contentValues, new WsUpdateOrder.Callbacks() {
            @Override
            public void ok() {
                FragmentOpenOrders.reloadData();
            }

            @Override
            public void ko() {

            }
        }).execute();
    }

    public static void setPrintedPreorder(Context context, int orderId, String lastEdit){
        String printerStatus = DBHelper.getInstance(context).getPrinterStatusOrdine(orderId);

        printerStatus = "1"+printerStatus.substring(1);

        ContentValues contentValues = new ContentValues();

        contentValues.put("orderId",orderId);
        contentValues.put("lastEdit",lastEdit);
        contentValues.put("printerStatus", printerStatus);

        DBHelper.getInstance(context).updateOrder(orderId, contentValues);

        new WsUpdateOrder(context, contentValues, new WsUpdateOrder.Callbacks() {
            @Override
            public void ok() {
                FragmentOpenOrders.reloadData();
            }

            @Override
            public void ko() {

            }
        }).execute();
    }

    public static void setNewAgreedDeliveryTime(Context context, int orderId, String lastEdit, String newAgreedDeliveryTime){
        ContentValues contentValues = new ContentValues();

        contentValues.put("orderId",orderId);
        contentValues.put("lastEdit",lastEdit);
        contentValues.put("agreedDeliveryTime",newAgreedDeliveryTime);


        DBHelper.getInstance(context).updateOrder(orderId, contentValues);

        new WsUpdateOrder(context, contentValues, new WsUpdateOrder.Callbacks() {
            @Override
            public void ok() {
                FragmentOpenOrders.reloadData();
            }

            @Override
            public void ko() {

            }
        }).execute();
    }

}
