package com.lynkteam.tapmanager.UI.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.lynkteam.tapmanager.DB.CommercialActivity;
import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.WS.WsUpdateCommercialActivity;
import com.lynkteam.tapmanager.util.DecimalUtil;

import java.util.ArrayList;

public class FragmentSettingsAccountLocation extends android.support.v4.app.Fragment {

    private static FragmentSettingsAccountLocation thisInstance;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisInstance = this;
        final View view = inflater.inflate(R.layout.fragment_settings_account_location, container, false);

        final CommercialActivity commercialActivity = DBHelper.getInstance(getActivity().getApplicationContext()).getCommercialActivity();


        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_name)).setText(commercialActivity.name);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_vatNumber)).setText(commercialActivity.vatNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_fiscalCode)).setText(commercialActivity.fiscalCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_description)).setText(commercialActivity.description);

        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_shippingPrice)).setText(DecimalUtil.formatInt(commercialActivity.shippingPrice));
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_minimumOrderForShipping)).setText(DecimalUtil.formatInt(commercialActivity.minimumOrderForShipping));
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_orderImportForFreeShipping)).setText(DecimalUtil.formatInt(commercialActivity.orderImportForFreeShipping));
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceLimit)).setText(Integer.toString(commercialActivity.shippingPriceLimit));
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceAfterLimit)).setText(DecimalUtil.formatInt(commercialActivity.shippingPriceAfterLimit));

        ArrayList<String[]> rates = DBHelper.getInstance(getActivity().getApplicationContext()).getVatRates();
        final String[] rateIDs = rates.get(0);
        final String[] rateVals = rates.get(1);

        Spinner spinner = (Spinner) view.findViewById(R.id.spn_sett_account_commActivity_shipingVatRate);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        // ArrayAdapter used to take only an array.

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, rateVals);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int id = java.util.Arrays.binarySearch(rateIDs, Integer.toString(commercialActivity.shippingVatRateId));

        spinner.setSelection(id);


        ((Button) view.findViewById(R.id.btn_sett_account_commActivity_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commercialActivity.name = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_name)).getText().toString();
                commercialActivity.vatNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_vatNumber)).getText().toString();
                commercialActivity.fiscalCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_fiscalCode)).getText().toString();
                commercialActivity.description = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_description)).getText().toString();
                commercialActivity.shippingPrice = DecimalUtil.formatString(((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPrice)).getText().toString());
                commercialActivity.minimumOrderForShipping = DecimalUtil.formatString(((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_minimumOrderForShipping)).getText().toString());
                commercialActivity.orderImportForFreeShipping = DecimalUtil.formatString(((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_orderImportForFreeShipping)).getText().toString());
                commercialActivity.shippingPriceLimit = Integer.parseInt(((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceLimit)).getText().toString());
                commercialActivity.shippingPriceAfterLimit = DecimalUtil.formatString(((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceAfterLimit)).getText().toString());
                commercialActivity.shippingVatRateId = Integer.parseInt( rateIDs[ (int)((Spinner)getActivity().findViewById(R.id.spn_sett_account_commActivity_shipingVatRate)).getSelectedItemId() ]);

                //TODO WS
                new WsUpdateCommercialActivity(getActivity().getApplicationContext(), commercialActivity, new WsUpdateCommercialActivity.Callbacks() {
                    @Override
                    public void ok() {
                        ActivityHome.toast("Informazioni salvate con successo! Ricorda di sincronizzare eventuali stampanti fiscali.");
                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("C'è stato un errore durante il salvataggio delle informazioni.");

                    }
                }).execute();
            }
        });
        return view;
    }


    private void loadData(){
        final CommercialActivity commercialActivity = DBHelper.getInstance(getActivity().getApplicationContext()).getCommercialActivity();


        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_name)).setText(commercialActivity.name);
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_vatNumber)).setText(commercialActivity.vatNumber);
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_fiscalCode)).setText(commercialActivity.fiscalCode);
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_description)).setText(commercialActivity.description);

        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPrice)).setText(DecimalUtil.formatInt(commercialActivity.shippingPrice));
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_minimumOrderForShipping)).setText(DecimalUtil.formatInt(commercialActivity.minimumOrderForShipping));
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_orderImportForFreeShipping)).setText(DecimalUtil.formatInt(commercialActivity.orderImportForFreeShipping));
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceLimit)).setText(Integer.toString(commercialActivity.shippingPriceLimit));
        ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_shippingPriceAfterLimit)).setText(DecimalUtil.formatInt(commercialActivity.shippingPriceAfterLimit));

        ArrayList<String[]> rates = DBHelper.getInstance(getActivity().getApplicationContext()).getVatRates();
        final String[] rateIDs = rates.get(0);
        final String[] rateVals = rates.get(1);

        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spn_sett_account_commActivity_shipingVatRate);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        // ArrayAdapter used to take only an array.

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, rateVals);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int id = java.util.Arrays.binarySearch(rateIDs, Integer.toString(commercialActivity.shippingVatRateId));

        spinner.setSelection(id);

    }

    public static void reloadData(){
        if (thisInstance!=null)
            thisInstance.loadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}

