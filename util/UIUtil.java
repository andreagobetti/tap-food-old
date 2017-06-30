package com.lynkteam.tapmanager.util;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.FragmentOpenOrdersDetails;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettings;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccount;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocation;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountPermissions;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountUser;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCalendar;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsContact;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCounter;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCounterCalculator;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCounterFiscal;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCustomers;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCustomersManagement;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCustomersMessages;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsLocalAssist;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsLocalAssistContactUs;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsLocalAssistTutorial;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrders;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersHistory;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersOnline;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersPreorders;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrdersStats;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsPrinter;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocationRegisteredOffice;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocationOperationalOffice;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsProducts;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsSocial;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsSocialGetLink;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsSocialSocialNetwork;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssist;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssistBug;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssistHardwareTest;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssistSystemLog;

/**
 * Created by robertov on 06/08/2015.
 */
public class UIUtil {


    public static int PAGE_COUNT = 32;

    public static void switchViewPager(ViewPager from, ViewPager to){
        from.setVisibility(View.INVISIBLE);
        to.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = from.getLayoutParams();
        layoutParams.height = 0;
        from.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams layoutParams2 = to.getLayoutParams();
        layoutParams2.height = ViewGroup.LayoutParams.MATCH_PARENT;
        to.setLayoutParams(layoutParams2);
    }

    //Cambia la top bar... prende l'activity e gli ID
    public static void switchTopBar(Activity activity, int from, int to){

        //imposto height di from a 0
        //height di to a dimen.toolbar_height

        UIUtil.setHeight(activity, from, 0);
        UIUtil.setHeight(activity, to, (int) activity.getResources().getDimension(R.dimen.toolbar_height));
    }

    //PULSANTE -> Stringa che deve apparire sopra
    public static String getTitle(int id){
        switch(id) {
            case 0:
                return "ACCOUNT";
            case 1:
                return "BANCO";
            case 2:
                return "STAMPANTI";
            case 3:
                return "ORDINI";
            case 4:
                return "ASSISTENZA TECNICA";
            case 5:
                return "ASSISTENZA LOCALE";
            case 6:
                return "SOCIAL";
            case 7:
                return "PRODOTTI";
            case 8:
                return "CONTATTI";
            case 9:
                return "CALENDARIO";
            case 10:
                return "CLIENTI";

            case 11:
                return "ACCOUNT LOCALE";
            case 12:
                return "ACCOUNT UTENTE";
            case 13:
                return "ACC. PERMESSI COLLAB.";
            case 14:
                return "FISCALI";
            case 15:
                return "CALCOLATRICE";
            case 16:
                return "SEDE LEGALE";
            case 17:
                return "SEDE OPERATIVA";
            case 18:
                return "STORICO";
            case 19:
                return "STATISTICHE";
            case 20:
                return "PREORDINI";
            case 21:
                return "ONLINE";
            case 22:
                return "TEST HARDWARE";
            case 23:
                return "LOG DI SISTEMA";
            case 24:
                return "SEGNALAZIONE BUG";
            case 25:
                return "TUTORIAL";
            case 26:
                return "CONTATTACI";
            case 27:
                return "OTTENIMENTO LINK";
            case 28:
                return "SOCIAL NETWORK";
            case 29:
                return "MESSAGGI CLIENTI";
            case 30:
                return "GESTIONE CLIENTI";

            case 31:
                return "DETTAGLIO ORDINE";

            default:
                return "";

        }
    }

    //PULSANTE -> ID relativo al viePager2 (quello dei contenuti, svinolato dalle tab)
    public static int getPosition(int id){
        switch(id) {
            case R.id.btn_settings_account:
                return 0;
            case R.id.btn_settings_counter:
                return 1;
            case R.id.btn_settings_printer:
                return 2;
            case R.id.btn_settings_orders:
                return 3;
            case R.id.btn_settings_techAssist:
                return 4;
            case R.id.btn_settings_locAssist:
                return 5;
            case R.id.btn_settings_social:
                return 6;
            case R.id.btn_settings_products:
                return 7;
            case R.id.btn_settings_contact:
                return 8;
            case R.id.btn_settings_calendar:
                return 9;
            case R.id.btn_settings_customers:
                return 10;

            case R.id.btn_settings_account_location:
                return 11;
            case R.id.btn_settings_account_user:
                return 12;
            case R.id.btn_settings_account_permissions:
                return 13;
            case R.id.btn_settings_counter_fiscal:
                return 14;
            case R.id.btn_settings_counter_calculator:
                return 15;
            case R.id.btn_sett_account_commActivity_registeredOffice:
                return 16;
            case R.id.btn_sett_account_commActivity_operationalOffice:
                return 17;
            case R.id.btn_settings_orders_history:
                return 18;
            case R.id.btn_settings_orders_stats:
                return 19;
            case R.id.btn_settings_orders_preorders:
                return 20;
            case R.id.btn_settings_orders_online:
                return 21;
            case R.id.btn_settings_techAssist_hardwareTest:
                return 22;
            case R.id.btn_settings_techAssist_systemLog:
                return 23;
            case R.id.btn_settings_techAssist_bug:
                return 24;
            case R.id.btn_settings_locAssist_tutorial:
                return 25;
            case R.id.btn_settings_locAssist_contactUs:
                return 26;
            case R.id.btn_settings_social_getLink:
                return 27;
            case R.id.btn_settings_social_socialNetwork:
                return 28;
            case R.id.btn_settings_customers_messages:
                return 29;
            case R.id.btn_settings_customers_management:
                return 30;

            case R.id.imgbtn_open_orders_dettaglio:case R.id.btn_open_orders_list1:
                return 31;

            default:
                return 0;

        }
    }

    public static int getToolbarColor(int position){
        switch (position) {
            case 0:
                return R.color.red;
            case 1:
                return R.color.red;
            case 2:
                return R.color.red;
            case 3:
                return R.color.red;
            case 4:
                return R.color.red;
            case 5:
                return R.color.red;
            case 6:
                return R.color.red;
            case 7:
                return R.color.red;
            case 8:
                return R.color.red;
            case 9:
                return R.color.red;
            case 10:
                return R.color.red;

            case 11:
                return R.color.red;
            case 12:
                return R.color.red;
            case 13:
                return R.color.red;
            case 14:
                return R.color.red;
            case 15:
                return R.color.red;
            case 16:
                return R.color.red;
            case 17:
                return R.color.red;
            case 18:
                return R.color.red;
            case 19:
                return R.color.red;
            case 20:
                return R.color.red;
            case 21:
                return R.color.red;
            case 22:
                return R.color.red;
            case 23:
                return R.color.red;
            case 24:
                return R.color.red;
            case 25:
                return R.color.red;
            case 26:
                return R.color.red;
            case 27:
                return R.color.red;
            case 28:
                return R.color.red;
            case 29:
                return R.color.red;
            case 30:
                return R.color.red;

            case 31:
                return R.color.yellow_dark;

            default:
                return -1;
        }
    }


    //ID (relativo al viewPager2 ) -> NEW fragment
    public static android.support.v4.app.Fragment getFragment(int position){
        switch (position) {
            case 0:
                return new FragmentSettingsAccount();
            case 1:
                return new FragmentSettingsCounter();
            case 2:
                return new FragmentSettingsPrinter();
            case 3:
                return new FragmentSettingsOrders();
            case 4:
                return new FragmentSettingsTechAssist();
            case 5:
                return new FragmentSettingsLocalAssist();
            case 6:
                return new FragmentSettingsSocial();
            case 7:
                return new FragmentSettingsProducts();
            case 8:
                return new FragmentSettingsContact();
            case 9:
                return new FragmentSettingsCalendar();
            case 10:
                return new FragmentSettingsCustomers();

            case 11:
                return new FragmentSettingsAccountLocation();
            case 12:
                return new FragmentSettingsAccountUser();
            case 13:
                return new FragmentSettingsAccountPermissions();
            case 14:
                return new FragmentSettingsCounterFiscal();
            case 15:
                return new FragmentSettingsCounterCalculator();
            case 16:
                return new FragmentSettingsAccountLocationRegisteredOffice();
            case 17:
                return new FragmentSettingsAccountLocationOperationalOffice();
            case 18:
                return new FragmentSettingsOrdersHistory();
            case 19:
                return new FragmentSettingsOrdersStats();
            case 20:
                return new FragmentSettingsOrdersPreorders();
            case 21:
                return new FragmentSettingsOrdersOnline();
            case 22:
                return new FragmentSettingsTechAssistHardwareTest();
            case 23:
                return new FragmentSettingsTechAssistSystemLog();
            case 24:
                return new FragmentSettingsTechAssistBug();
            case 25:
                return new FragmentSettingsLocalAssistTutorial();
            case 26:
                return new FragmentSettingsLocalAssistContactUs();
            case 27:
                return new FragmentSettingsSocialGetLink();
            case 28:
                return new FragmentSettingsSocialSocialNetwork();
            case 29:
                return new FragmentSettingsCustomersMessages();
            case 30:
                return new FragmentSettingsCustomersManagement();

            case 31:
                return new FragmentOpenOrdersDetails();

            default:
                return new FragmentSettings();
        }
    }

    public static void setColor(Activity activity, int id, int color){
        activity.findViewById( id ).setBackgroundColor( activity.getResources().getColor( color ) );
    }

    //imposta la height di un qualsiasi elemento di un activity (da dentro un'activity il primo parametro è this
    public static void setHeight(Activity activity, int id, int height){

        ViewGroup.LayoutParams layoutParams = activity.findViewById( id ).getLayoutParams();

        layoutParams.height = height;



        activity.findViewById( id ).setLayoutParams(layoutParams);
    }

    //per i fragment dove ho un oggetto view
    public static void setHeight(View view, int id, int height){
        ViewGroup.LayoutParams layoutParams = view.findViewById( id ).getLayoutParams();

        layoutParams.height = height;

        view.findViewById( id ).setLayoutParams(layoutParams);
    }

    public static int getDisplayWidth(Activity activity){
        //ottengo dimensione (width) del display da passare all'adapter delle tabs per calcolare la larghezza delle singole tab
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;

    }

    public static int getDisplayHeight(Activity activity){
        //ottengo dimensione (width) del display da passare all'adapter delle tabs per calcolare la larghezza delle singole tab
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.heightPixels;

    }


}
