package com.lynkteam.tapmanager.util;

import android.content.ContentValues;
import android.content.Context;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersHistory;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsProducts;
import com.lynkteam.tapmanager.WS.WsUpdateWarehouse;

import java.util.ArrayList;

/**
 * Created by robertov on 04/09/15.
 */
public class WarehouseManagementUtil {

    public static void setAvailabilityAndPrice(Context context, int warehouseId, boolean isAvailable, String lastEdit, ArrayList<ContentValues> prices){

        try {

            DBHelper.getInstance(context).updateWarehouse(warehouseId, isAvailable, prices);

            ContentValues contentValues = new ContentValues();

            contentValues.put("isAvailable", isAvailable);
            contentValues.put("warehouseId", warehouseId);
            contentValues.put("lastEdit", lastEdit);

            new WsUpdateWarehouse(context, contentValues, prices, new WsUpdateWarehouse.Callbacks() {
                @Override
                public void ok() {
                    FragmentSettingsProducts.reloadData();
                }

                @Override
                public void ko() {
                    ActivityHome.toast("C'è stato un errore durante l'aggiornamento delle informazioni sul prodotto.");

                }
            }).execute();

        }catch(Exception ex){
            ActivityHome.toast("Errore durante l'aggiornamento dei prodotti");
            Logger.Error(context,"Errore update Warehouse",ex.toString());
        }
    }
}
