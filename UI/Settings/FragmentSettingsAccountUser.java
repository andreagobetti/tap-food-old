package com.lynkteam.tapmanager.UI.Settings;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Employee;
import com.lynkteam.tapmanager.R;

import javax.microedition.khronos.egl.EGLDisplay;

public class FragmentSettingsAccountUser extends android.support.v4.app.Fragment {
    private static FragmentSettingsAccountUser thisInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_account_user, container, false);

        loadData(view);

        thisInstance = this;

        return view;
    }

    private void loadData(View view){
        final Employee employee = DBHelper.getInstance(getActivity().getApplicationContext()).getCurrentEmployee();

        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_name)).setText(employee.name);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_surname)).setText(employee.surname);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_birthday_day)).setText(employee.birthdayDay);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_birthday_month)).setText(employee.birthdayMonth);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_birthday_year)).setText(employee.birthdayYear);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_street)).setText(employee.anagraphicInformation.street);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_streetNo)).setText(employee.anagraphicInformation.streetNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_city)).setText(employee.anagraphicInformation.city);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_district)).setText(employee.anagraphicInformation.district);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_zipCode)).setText(employee.anagraphicInformation.zipCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_countryCode)).setText(employee.anagraphicInformation.countryCode);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_telephoneNo)).setText(employee.anagraphicInformation.telephoneNumber);
        ((EditText) view.findViewById(R.id.edtxt_sett_account_user_emailAddress)).setText(employee.anagraphicInformation.emailAddress);

        ((Button) view.findViewById(R.id.btn_sett_account_user_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: salvare!
                employee.name = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_name)).getText().toString().trim();
                employee.surname = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_surname)).getText().toString().trim();
                employee.name = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_name)).getText().toString().trim();

                //TODO controllare validità data
                employee.birthdayDay = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_birthday_day)).getText().toString().trim();
                employee.birthdayMonth = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_birthday_month)).getText().toString().trim();
                employee.birthdayYear = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_birthday_year)).getText().toString().trim();

                employee.anagraphicInformation.street = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_street)).getText().toString().trim();
                employee.anagraphicInformation.streetNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_streetNo)).getText().toString().trim();
                employee.anagraphicInformation.city = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_city)).getText().toString().trim();
                employee.anagraphicInformation.district = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_district)).getText().toString().trim();
                employee.anagraphicInformation.zipCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_zipCode)).getText().toString().trim();
                employee.anagraphicInformation.countryCode = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_countryCode)).getText().toString().trim();
                employee.anagraphicInformation.telephoneNumber = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_telephoneNo)).getText().toString().trim();
                employee.anagraphicInformation.emailAddress = ((EditText) getActivity().findViewById(R.id.edtxt_sett_account_user_emailAddress)).getText().toString().trim();

                //TODO inviare al WS


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

