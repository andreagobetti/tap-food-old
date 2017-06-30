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


public class FragmentSettingsAccountLocationRegisteredOffice extends android.support.v4.app.Fragment {

    private static FragmentSettingsAccountLocationRegisteredOffice thisInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_account_registered_office, container, false);

        loadData(view);

        thisInstance = this;

        return view;
    }

    private void loadData(View view){
        final AnagraphicInformation anagraphicInformation = DBHelper.getInstance(getActivity().getApplicationContext()).getRegisteredOffice();

        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_street_registered)).setText(anagraphicInformation.street);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_streetNo_registered)).setText(anagraphicInformation.streetNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_city_registered)).setText(anagraphicInformation.city);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_countryCode_registered)).setText(anagraphicInformation.countryCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_district_registered)).setText(anagraphicInformation.district);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_zipcode_registered)).setText(anagraphicInformation.zipCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_telephoneNo_registered)).setText(anagraphicInformation.telephoneNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_commActivity_email_registered)).setText(anagraphicInformation.emailAddress);

        ((Button) view.findViewById(R.id.btn_sett_account_commActivity_save_registered)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anagraphicInformation.street = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_street_registered)).getText().toString().trim();
                anagraphicInformation.streetNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_streetNo_registered)).getText().toString().trim();
                anagraphicInformation.city = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_city_registered)).getText().toString().trim();
                anagraphicInformation.countryCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_countryCode_registered)).getText().toString().trim();
                anagraphicInformation.district = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_district_registered)).getText().toString().trim();
                anagraphicInformation.zipCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_zipcode_registered)).getText().toString().trim();
                anagraphicInformation.telephoneNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_telephoneNo_registered)).getText().toString().trim();
                anagraphicInformation.emailAddress = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_commActivity_email_registered)).getText().toString().trim();

                //Chiamata ws
                new WsUpdateAnagraphicInformation(getActivity().getApplicationContext(), anagraphicInformation, new WsUpdateAnagraphicInformation.Callbacks() {
                    @Override
                    public void ok() {
                        ActivityHome.toast("Informazioni sede legale salvate con successo. Ricordati di sincronizzare eventuali stampanti fiscali!");
                    }

                    @Override
                    public void ko() {
                        ActivityHome.toast("C'è stato un errore durante l'aggiornamento dei dati della sede legale");
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
