package com.lynkteam.tapmanager.UI;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettings;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccount;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountLocation;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountPermissions;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsAccountUser;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsContact;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsCounter;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsOrders;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsPrinter;
import com.lynkteam.tapmanager.UI.Settings.FragmentSettingsTechAssist;
import com.lynkteam.tapmanager.util.UIUtil;

/**
 * Created by robertov on 29/07/2015.
 */
public class AdapterContent extends FragmentPagerAdapter {
    final int PAGE_COUNT = UIUtil.PAGE_COUNT;

    private Context context;

    public AdapterContent(FragmentManager fm, Context context) {
        super(fm);

        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return UIUtil.getFragment(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return " ";
    }

}
