package com.lynkteam.tapmanager.UI.Settings;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.R;


public class FragmentSettingsTechAssistSystemLog extends android.support.v4.app.Fragment {
    public static FragmentSettingsTechAssistSystemLog thisInstance;

    public FragmentSettingsTechAssistSystemLog() {
        thisInstance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_tech_assist_system_log, container, false);

        TextView tv = (TextView) view.findViewById(R.id.txt_logs);

        tv.setText(DBHelper.getInstance(getActivity().getApplicationContext()).getLogsString());

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void loadData(){
        TextView tv = (TextView) getActivity().findViewById(R.id.txt_logs);

        tv.setText(DBHelper.getInstance(getActivity().getApplicationContext()).getLogsString());
    }

    public static void reloadData(){
        FragmentSettingsTechAssistSystemLog.thisInstance.loadData();
    }
}
