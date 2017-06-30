package com.lynkteam.tapmanager.UI.Settings;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.AdapterPrinterList;

import java.util.List;

public class FragmentSettingsPrinter extends android.support.v4.app.Fragment {

    private static FragmentSettingsPrinter thisInstance;

    public FragmentSettingsPrinter(){
        thisInstance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_printer, container, false);

        ListView convertView = (ListView) view.findViewById(R.id.lv_printers);

        AdapterPrinterList adapter = new AdapterPrinterList(getActivity().getApplicationContext(), getActivity(), DBHelper.getInstance(getActivity().getApplicationContext()).getProductionAreas());

        convertView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void loadData(){
        ListView convertView = (ListView) getActivity().findViewById(R.id.lv_printers);

        AdapterPrinterList adapter = new AdapterPrinterList(getActivity().getApplicationContext(), getActivity(), DBHelper.getInstance(getActivity().getApplicationContext()).getProductionAreas());

        convertView.setAdapter(adapter);
    }


    public static void reloadData(){
        if(FragmentSettingsPrinter.thisInstance!=null)
            FragmentSettingsPrinter.thisInstance.loadData();
    }

}
