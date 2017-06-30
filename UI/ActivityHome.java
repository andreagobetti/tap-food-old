package com.lynkteam.tapmanager.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.ProductionArea;
import com.lynkteam.tapmanager.GCM.GcmHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssistSystemLog;
import com.lynkteam.tapmanager.WS.WsGetOrders;
import com.lynkteam.tapmanager.WS.WsGetUpdates;
import com.lynkteam.tapmanager.WS.WsLogout;
import com.lynkteam.tapmanager.WS.WsUpdateGCMToken;
import com.lynkteam.tapmanager.WS.WsUpdateProductionArea;
import com.lynkteam.tapmanager.util.PrintUtil;
import com.lynkteam.tapmanager.util.UIUtil;
import com.lynkteam.tapmanager.util.WifiUtil;

import java.util.ArrayList;
import java.util.Stack;


public class ActivityHome extends AppCompatActivity {

    private Toolbar toolbar;

    private GcmHelper gcmHelper;

    private ViewPager viewPager;
    private NonSwipeableViewPager viewPager2;

    private TabLayout tabLayout;

    public Stack<Integer> visits = new Stack<Integer>();

    //questo perche non posso usare this dentro i new Callbacks
    private ActivityHome thisC = this;

    private static Context thisContext;
    public static ActivityHome thisActivity;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActivityHome.thisContext = getApplicationContext();
        ActivityHome.thisActivity = this;

        //creo helper notifiche
        gcmHelper = new GcmHelper(getApplicationContext(), new GcmHelper.Callbacks() {
            @Override
            public void ok(String t) {
                new WsUpdateGCMToken(t, WifiUtil.getMacAddress(thisContext), thisContext, new WsUpdateGCMToken.Callbacks() {
                    @Override
                    public void ok() {

                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("C'è stato un errore nella sincronizzazione col server delle notifiche push.");
                    }
                }).execute();
            }
        });
        gcmHelper.getGcmTokenInBackground(getResources().getString(R.string.gcm_SenderId));

        //creo l'adapter per le tabs
        AdapterMenu adapterMenu = new AdapterMenu(getSupportFragmentManager(), ActivityHome.this, UIUtil.getDisplayWidth(this));


        //viewpager "di primo livello", collegato alle tabs e contiene le 3 schermate principali
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapterMenu);

        //assegno l'adapter al TabLayout
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //seleziona tab centrale
        tabLayout.getTabAt(1).select();

        //assegno le view personalizzate alle 3 singole tabs
        tabLayout.getTabAt(0).setCustomView(adapterMenu.getSettingsTabView());
        tabLayout.getTabAt(1).setCustomView(adapterMenu.getCounterTabView());
        tabLayout.getTabAt(2).setCustomView(adapterMenu.getOpenOrdersTabView());


        //secondo viewpager... per il contenuto (non collegato alle tabs)
        viewPager2 = (NonSwipeableViewPager) findViewById(R.id.viewpager2);
        viewPager2.setAdapter(new AdapterContent(getSupportFragmentManager(), ActivityHome.this));

        new WsGetUpdates(getApplicationContext(), new WsGetUpdates.Callbacks() {
            @Override
            public void ok() {

            }

            @Override
            public void ko() {
                ActivityHome.toast("C'è stato un errore nella sincronizzazione dell'app coi server Tap Manager.");
            }
        }).execute();

        new WsGetOrders(this, new WsGetOrders.Callbacks() {
            @Override
            public void ok() {
                refreshOrdersView();
            }

            @Override
            public void ko() {
                ActivityHome.toast("C'è stato un errore nell'aggiornamento degli ordini");
            }
        }).execute();

    }

    public static Stack getVisitsStack(){
        return ActivityHome.thisActivity.visits;
    }

    public static void toast(final String text){
        ActivityHome.thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast mytoast = Toast.makeText( ActivityHome.thisContext, text, Toast.LENGTH_LONG);
                mytoast.setGravity(Gravity.TOP, 0, 200);
                mytoast.show();
            }
        });
    }

    public void onClickSettings(View view){

        int id = view.getId();

        if(id == R.id.btn_settings_logout){

            //chiamo il WS di Logout passandogli i callback ok e ko ( ko chiamato in caso di errore)
            new WsLogout(this, new WsLogout.Callbacks() {
                @Override
                public void ok() {
                    Intent intent = new Intent(thisC, ActivityLogin.class);
                    startActivity(intent);
                }

                @Override
                public void ko() {
                    Intent intent = new Intent(thisC, ActivityLogin.class);
                    startActivity(intent);
                }
            }).execute();

            DBHelper.getInstance(getApplicationContext()).setUserLogout();

        }else {

            //codice del fragment rispettivamente al viewpager2
            int c = UIUtil.getPosition(id);

            visits.push(c);

            viewPager2.setCurrentItem(c);

            //"nasconde" viewpager1 e mostra viewpager2
            UIUtil.switchViewPager(viewPager, viewPager2);

            //"nasconde sliding_tabs e mostra bar_settings
            UIUtil.switchTopBar(this, R.id.sliding_tabs, R.id.bar_settings);

            //mette il titolo (restituito da UIUtil.getTitle....)
            ((TextView)findViewById(R.id.txt_settings_title)).setText(UIUtil.getTitle( c ));

            UIUtil.setColor(this, R.id.bar_settings, UIUtil.getToolbarColor(c));

        }

    }

    public void onClickOrderDetails(View view){
        //codice del fragment rispettivamente al viewpager2

        int id = view.getId();

        int c = UIUtil.getPosition(id);

        visits.push(c);

        viewPager2.setCurrentItem(c);

        FragmentOpenOrdersDetails.ORDER_ID = (int) view.getTag() ;

        FragmentOpenOrdersDetails.reloadData();

        //"nasconde" viewpager1 e mostra viewpager2
        UIUtil.switchViewPager(viewPager, viewPager2);

        //"nasconde sliding_tabs e mostra bar_settings
        UIUtil.switchTopBar(this, R.id.sliding_tabs, R.id.bar_settings);

        //mette il titolo (restituito da UIUtil.getTitle....)
        ((TextView)findViewById(R.id.txt_settings_title)).setText(UIUtil.getTitle(c));

        UIUtil.setColor(this, R.id.bar_settings, UIUtil.getToolbarColor(c));

    }

    public void onClickPrintersTest(View view){
        try {
            PrintUtil.PrintersTest(this);
        }catch(Exception ex){
            this.toast("Test stampante fallito");
            Logger.Error(getApplicationContext(),"Test stampante fallito",ex.toString());
        }
    }

    public void onClickSystemLogsRefresh(View view){
        FragmentSettingsTechAssistSystemLog.reloadData();
    }

    public void onClickSystemLogsClear(View view){

        DBHelper.getInstance(getApplicationContext()).clearLogs();

        onClickSystemLogsRefresh(view);
    }

    @Override
    public void onBackPressed() {

        if (visits.size() < 2){
            UIUtil.switchViewPager(viewPager2, viewPager);
            UIUtil.switchTopBar(this, R.id.bar_settings, R.id.sliding_tabs);


            visits.clear();
        }else{
            //bruttino... ma so che inserisce se stesso, quindi cosi lo scarto
            visits.pop();

            int c = visits.peek();

            UIUtil.switchViewPager(viewPager, viewPager2);

            ((TextView)findViewById(R.id.txt_settings_title)).setText(UIUtil.getTitle(c));

            UIUtil.setColor(this, R.id.bar_settings , UIUtil.getToolbarColor( c ));

            viewPager2.setCurrentItem(c);
        }

    }

    public void onClickBack(View view){

        this.onBackPressed();
    }

    public void onClickProductIsAvailable(View view){
        ((CheckBox) view.findViewById(R.id.chk_product_available)).setChecked(!((CheckBox) view.findViewById(R.id.chk_product_available)).isChecked());
    }

    public void onClickAddPrinter(View view){
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);

        final View dialogView = getLayoutInflater().inflate(R.layout.alertdialog_printer_setup, null);

        ArrayList<String[]> models = DBHelper.getInstance(getApplicationContext()).getPrinterModels();
        final String[] modelIDs = models.get(0);
        final String[] modelNames = models.get(1);
        final String[] canBeFiscal = models.get(2);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.spn_popup_printer_model);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        // ArrayAdapter used to take only an array.

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, modelNames);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canBeFiscal[position].equals("1")) {
                    ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(true);
                } else {
                    ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(false);
                    ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bldr.setView(dialogView);

        bldr.create();
        final AlertDialog dialog = bldr.show();

        ((ImageButton) dialogView.findViewById(R.id.imgbtn_popup_product_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });

        ((Button) dialogView.findViewById(R.id.btn_popup_printer)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProductionArea area = new ProductionArea();

                int modID = (int) ((Spinner) dialogView.findViewById(R.id.spn_popup_printer_model)).getSelectedItemId();


                area.modelCode = modelIDs[modID];
                area.name = ((EditText) dialogView.findViewById(R.id.txt_popup_printer_name)).getText().toString();
                area.printerIpAddress = ((EditText) dialogView.findViewById(R.id.txt_popup_printer_ip)).getText().toString();
                area.printerIpPort = Integer.parseInt(((EditText) dialogView.findViewById(R.id.txt_popup_printer_port)).getText().toString());
                area.isFiscal = ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).isChecked();


                area.productionAreaId=-1; //nuova productionArea

                new WsUpdateProductionArea(getApplicationContext(), area, new WsUpdateProductionArea.Callbacks() {
                    @Override
                    public void ok() {
                        //TODO salvataggio modifiche sul db locale??
                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("C'è stato un errore durante l'aggiunta della stampante");

                    }
                }).execute();

                dialog.dismiss();
                dialog.cancel();
            }
        });
    }


    public void refreshOrdersView(){

        try {
            ListView lv = (ListView) findViewById(R.id.lv_open_orders);

            lv.setAdapter( new AdapterOrderList(this , this, DBHelper.getInstance(getApplicationContext()).getOpenOrders() ) );

        }
        catch(Exception ex){
            Logger.Error(this, "Errore popolazione ordini (refreshOrders)", ex.toString());

        }

    }

}
