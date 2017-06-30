package com.lynkteam.tapmanager.UI;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.FragmentCounter;
import com.lynkteam.tapmanager.UI.FragmentOpenOrders;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettings;

/**
 * Created by robertov on 29/07/2015.
 */
public class AdapterMenu extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;

    private Context context;

    private int width;

    public AdapterMenu(FragmentManager fm, Context context, int w) {
        super(fm);

        this.context = context;
        this.width = w / PAGE_COUNT;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentSettings();
            case 1:
                return new FragmentCounter();
            case 2:
                return new FragmentOpenOrders();
            default:
                return new FragmentCounter();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return " ";
    }

    public View getSettingsTabView(){

        View v = LayoutInflater.from(context).inflate(R.layout.tab_settings,null);
        v.setMinimumWidth(width);

        return v;
    }
    public View getCounterTabView(){

        View v = LayoutInflater.from(context).inflate(R.layout.tab_counter,null);
        v.setMinimumWidth(width);
        return v;
    }
    public View getOpenOrdersTabView(){

        View v = LayoutInflater.from(context).inflate(R.layout.tab_open_orders,null);
        v.setMinimumWidth(width);
        return v;
    }

}
