package com.lynkteam.tapmanager.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.WS.WsAppUpdateCheck;
import com.lynkteam.tapmanager.WS.WsAppUpdateDownload;
import com.lynkteam.tapmanager.WS.WsLogin;

public class ActivityMain extends AppCompatActivity {

    ActivityMain thisC = this;

    DBHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO qui: meccanismo di autoupdate
        /*
            http://stackoverflow.com/questions/15213211/update-an-android-app-without-google-play

        Get the current application versionCode

        PackageInfo packageInfo = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                int curVersionCode = packageInfo.versionCode;

        Have a server where you host the apk file and create a simple plain file containing only one integer, which represents the latest application version code.

        When the app starts (or whenever you want to check for an update), retrieve the latest versionCode from the server (i.e via an HTTP request) and compare it with the current app version.

        If there is a new version, download the apk and install it (will prompt a dialog for the user).


         */

        //AUTO-UPDATE
        new WsAppUpdateCheck(getApplicationContext(), new WsAppUpdateCheck.Callbacks() {
            @Override
            public void ok(String version) {
                int newVersion = Integer.parseInt(version);

                if(newVersion>TapManager.APP_VERSION){
                    //TODO fare aggiornamento app
                    new WsAppUpdateDownload(getApplicationContext(), version, new WsAppUpdateDownload.Callbacks() {
                        @Override
                        public void ok() {
                            //FATTO
                        }

                        @Override
                        public void ko() {
                            //KO download
                        }
                    }).execute();

                }
            }

            @Override
            public void ko() {
               //KO check
            }
        }).execute();

        myDb = DBHelper.getInstance(getApplicationContext());

        TapManager.COMMERCIAL_ACTIVITY_ID = myDb.getLoggedCommercialActivityId();

        String[] credentials = myDb.getCurrentlyLoggedUserCredentials();

        if(credentials.length > 0){


            new WsLogin(credentials[0], credentials[1], this, new WsLogin.Callbacks() {
                @Override
                public void ok(String t, String u, String p) {
                    if(t==null || t.equals("") ){
                        myDb.deleteInvalidUser(u, p);
                        Intent intent = new Intent(thisC, ActivityLogin.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void ko(String u, String p) {
                    if (myDb.checkLogin(u, p)){
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }else {
                        myDb.deleteInvalidUser(u, p);
                        Intent intent = new Intent(thisC, ActivityLogin.class);
                        startActivity(intent);
                    }
                }
            }).execute();
        }else{
            Intent intent = new Intent(thisC, ActivityLogin.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){

    }
}
