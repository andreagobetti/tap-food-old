package com.lynkteam.tapmanager.UI.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.Warehouse;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.UI.AdapterOrderList;
import com.lynkteam.tapmanager.UI.AdapterProductGridView;

import java.util.ArrayList;

public class FragmentSettingsProducts extends android.support.v4.app.Fragment {

    public static FragmentSettingsProducts thisInstance;

    private AdapterProductGridView adapter;

    public FragmentSettingsProducts(){
        thisInstance=this;
    }

    public static void reloadData(){
        if(thisInstance!=null && thisInstance.adapter!=null)
            thisInstance.loadData();
    }

    private void loadData(){
        GridView gv = (GridView) getActivity().findViewById(R.id.gv_products);

        //adapter = new OrderListAdapter(getActivity().getApplicationContext(), DBHelper.getInstance(getActivity().getApplicationContext()).getOpenOrders() );

        try {
            adapter.clear();
            adapter.addAll( DBHelper.getInstance(getActivity().getApplicationContext()).getWarehouses() );
            adapter.notifyDataSetChanged();

            gv.setAdapter(adapter);
        }
        catch(Exception ex){
            Logger.Error(getActivity(), "Errore popolazione Products (onCreateView)", ex.toString());

            ActivityHome.toast("Errore popolazione prodotti");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_products, container, false);

        try {
            GridView gv = (GridView) view.findViewById(R.id.gv_products);


             adapter = new AdapterProductGridView(getActivity().getApplicationContext(), getActivity(), DBHelper.getInstance(getActivity().getApplicationContext()).getWarehouses() );


            gv.setAdapter( adapter );

        }
        catch(Exception ex){
            Logger.Error(getActivity(), "Errore popolazione Products (onCreateView)", ex.toString());

            ActivityHome.toast("Errore popolazione prodotti");

        }

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
