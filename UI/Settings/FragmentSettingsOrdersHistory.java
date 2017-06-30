package com.lynkteam.tapmanager.UI.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.ActivityHome;
import com.lynkteam.tapmanager.UI.AdapterOrderList;




public class FragmentSettingsOrdersHistory extends android.support.v4.app.Fragment {

    public static FragmentSettingsOrdersHistory thisInstance;

    public FragmentSettingsOrdersHistory(){
        thisInstance = this;
    }

    private AdapterOrderList adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_open_orders, container, false);

        try {
            ListView lv = (ListView) view.findViewById(R.id.lv_open_orders);

            adapter = new AdapterOrderList(getActivity() , getActivity(), DBHelper.getInstance(getActivity().getApplicationContext()).getClosedOrders() );

            lv.setAdapter( adapter );

        }
        catch(Exception ex){
            Logger.Error(getActivity(), "Errore popolazione ordini (onCreateView)", ex.toString());

            ActivityHome.toast("Errore popolazione ordini");

        }

        return view;

    }

    private void loadData(){
        ListView lv = (ListView) getActivity().findViewById(R.id.lv_open_orders);


        //adapter = new OrderListAdapter(getActivity().getApplicationContext(), DBHelper.getInstance(getActivity().getApplicationContext()).getOpenOrders() );

        adapter.clear();
        adapter.addAll( DBHelper.getInstance(getActivity().getApplicationContext()).getClosedOrders() );
        adapter.notifyDataSetChanged();

        lv.setAdapter(adapter);
    }

    public static void reloadData(){
        FragmentSettingsOrdersHistory.thisInstance.loadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }
}
