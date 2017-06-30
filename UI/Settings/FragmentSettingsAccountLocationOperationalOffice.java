package com.lynkteam.tapmanager.UI.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.lynkteam.tapmanager.DB.AnagraphicInformation;
import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.WS.WsUpdateAnagraphicInformation;


public class FragmentSettingsAccountLocationOperationalOffice extends android.support.v4.app.Fragment {

    private static FragmentSettingsAccountLocationOperationalOffice thisInstance;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_account_operational_office, container, false);

        loadData(view);

        thisInstance = this;


        return view;
    }


    private void loadData(View view){
        final AnagraphicInformation anagraphicInformation = DBHelper.getInstance(getActivity().getApplicationContext()).getOperationalOffice();

        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_street_operational)).setText(anagraphicInformation.street);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_streetNo_operational)).setText(anagraphicInformation.streetNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_city_operational)).setText(anagraphicInformation.city);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_countryCode_operational)).setText(anagraphicInformation.countryCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_district_operational)).setText(anagraphicInformation.district);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_zipcode_operational)).setText(anagraphicInformation.zipCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_telephoneNo_operational)).setText(anagraphicInformation.telephoneNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_email_operational)).setText(anagraphicInformation.emailAddress);

        ((Button) view.findViewById(R.id.btn_sett_account_commActivity_save_operational)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anagraphicInformation.street = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_street_operational)).getText().toString().trim();
                anagraphicInformation.streetNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_streetNo_operational)).getText().toString().trim();
                anagraphicInformation.city = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_city_operational)).getText().toString().trim();
                anagraphicInformation.countryCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_countryCode_operational)).getText().toString().trim();
                anagraphicInformation.district = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_district_operational)).getText().toString().trim();
                anagraphicInformation.zipCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_zipcode_operational)).getText().toString().trim();
                anagraphicInformation.telephoneNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_telephoneNo_operational)).getText().toString().trim();
                anagraphicInformation.emailAddress = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_email_operational)).getText().toString().trim();

                //Chiamata ws
                new WsUpdateAnagraphicInformation(getActivity().getApplicationContext(), anagraphicInformation, new WsUpdateAnagraphicInformation.Callbacks() {
                    @Override
                    public void ok() {
                        ActivityHome.toast("Informazioni sede operativa salvate con successo");
                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("C'è stato un errore durante l'aggiornamento dei dati della sede operativa");
                    }
                }).execute();
            }
        });

    }

    //Metodo statico forza ricaricamento
    public static void reloadData(){
        if(thisInstance!=null)
            thisInstance.loadData( thisInstance.getView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
