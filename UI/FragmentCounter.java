package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.UIUtil;


public class FragmentCounter extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        Activity activity = getActivity();

        int h = UIUtil.getDisplayHeight(activity);



        //imposto l'altezza dei layout nel counter
        //(altezza display - top bar - barra centrale - barra sotto) / 2
        int counterLayoutHeight = (h - 3 * ((int)getResources().getDimension(R.dimen.toolbar_height)))/2 - 10 ;

        UIUtil.setHeight(view, R.id.lay_counter_top, counterLayoutHeight);
        UIUtil.setHeight(view, R.id.lay_counter_bottom, counterLayoutHeight);

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

