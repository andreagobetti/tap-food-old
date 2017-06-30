package com.lynkteam.tapmanager.GCM;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.UI.FragmentOpenOrders;
import com.lynkteam.tapmanager.UI.FragmentOpenOrdersDetails;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocation;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocationOperationalOffice;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocationRegisteredOffice;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountUser;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrders;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersHistory;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsPrinter;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsProducts;
import com.lynkteam.tapmanager.WS.WsGetOrders;
import com.lynkteam.tapmanager.WS.WsGetUpdates;

import java.util.Stack;

/**
 * Created by robertov on 30/07/2015.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
            String registrationId = intent.getStringExtra("registration_id");

            String error = intent.getStringExtra("error");
            String unregistered = intent.getStringExtra("unregistered");
        } else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {


            Object cmd = intent.getExtras().get("command");
            int command = Integer.parseInt( cmd.toString() );

            switch (command){
                case 1:
                    new WsGetOrders(context, new WsGetOrders.Callbacks() {
                        @Override
                        public void ok() {

                            int idDetaglio = R.id.imgbtn_open_orders_dettaglio;
                            int idStorico = R.id.btn_settings_orders_history;

                            Stack visits = ActivityHome.getVisitsStack();

                            int size = visits.size();
                            if(size==0) {
                                FragmentOpenOrders.reloadData();
                            }else{
                                int last = (int) visits.get(size-1);

                                if(last==idStorico || (last==idDetaglio && (int) visits.get( visits.size()-1) == idStorico )){
                                    FragmentSettingsOrdersHistory.reloadData();
                                }else {
                                    FragmentOpenOrders.reloadData();
                                }
                            }
                        }

                        @Override
                        public void ko() {

                        }
                    }).execute();
                    break;
                case 2:
                    new WsGetUpdates(context, new WsGetUpdates.Callbacks() {
                        @Override
                        public void ok() {
                            //ognuno di questi metodi fa qualcosa solo nel caso il fragment sia ancora in memoria,
                            // quindi al massimo verranno aggiornati 2-3 fragments
                            FragmentSettingsProducts.reloadData();
                            FragmentSettingsPrinter.reloadData();
                            FragmentSettingsAccountLocation.reloadData();
                            FragmentSettingsAccountUser.reloadData();
                            FragmentSettingsAccountLocationOperationalOffice.reloadData();
                            FragmentSettingsAccountLocationRegisteredOffice.reloadData();
                        }

                        @Override
                        public void ko() {

                        }
                    }).execute();
                    break;
                case 3:
                    //TODO: Send Us Logs (invia i log al WS e puliscili dal DB)
                    break;
            }

        }


    }
}
