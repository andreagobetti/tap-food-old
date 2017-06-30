package com.lynkteam.tapmanager.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.TapManager;
import com.lynkteam.tapmanager.WS.WsLogin;
import com.lynkteam.tapmanager.WS.WsLoginCA;
import com.lynkteam.tapmanager.util.CryptoUtil;

public class ActivityLogin extends AppCompatActivity {
    DBHelper myDb;
    String caName;

    ActivityLogin thisC = this;

    private void toast(String msg){
        Toast mytoast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        mytoast.setGravity(Gravity.TOP, 0, 250);
        mytoast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        manageAssociation();

    }

    //gestisce la UI dell'associazione (ricaricamento dati)
    private void manageAssociation(){
        myDb = DBHelper.getInstance(getApplicationContext());

        caName = myDb.getLoggedCommercialActivityName();

        final Button btnComm = ((Button) findViewById(R.id.btn_login_commercial_activity));

        if(caName!=null){
            btnComm .setText(getResources().getString(R.string.login_txv_commact_dissociate));
            ((TextView) findViewById(R.id.txt_login_commercial_activity)).setText("Dispositivo Associato con \" " + caName + " \"");

            btnComm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder bldr = new AlertDialog.Builder( ActivityLogin.this );
                    bldr.setMessage("Confermi di voler dissociare il dispositivo dall'attività commerciale \"" + caName + "\"?" )
                            .setTitle("Dissocia dispositivo")
                            .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //nulla
                                    DBHelper.getInstance(getApplicationContext()).dissociateCommercialActivity();

                                    Logger.Info(getApplicationContext(),"Dissociata CommercialActivity");
                                    manageAssociation();
                                }
                            }).setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //annulla, non fare nulla
                        }
                    });
                    bldr.create();
                    bldr.show();
                }
            });


        } else {
            btnComm.setText(getResources().getString(R.string.login_txv_commact_associate));
            ((TextView) findViewById(R.id.txt_login_commercial_activity)).setText( "Dispositivo non Associato con Attività Commerciale ");

            btnComm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder bldr = new AlertDialog.Builder(ActivityLogin.this);

                    final View convertView = getLayoutInflater().inflate(R.layout.alertdialog_commercial_activity, null);

                    Button btn = (Button) convertView.findViewById(R.id.btn_popup_commercialactivity);
                    ImageButton imgBtn = (ImageButton) convertView.findViewById(R.id.imgbtn_popup_product_back);



                    bldr.setView(convertView);

                    bldr.create();
                    final AlertDialog dialog = bldr.show();

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String CA = ((EditText) convertView.findViewById(R.id.txt_popup_commercialactivity_id)).getText().toString().trim();
                            String code = ((EditText) convertView.findViewById(R.id.txt_popup_commercialactivity_code)).getText().toString().trim();

                            new WsLoginCA(getApplicationContext(), CA,code, new WsLoginCA.Callbacks() {
                                @Override
                                public void ok(String name) {
                                    Logger.Info(getApplicationContext(), "Associata nuova CommercialActivity " + name);
                                    TapManager.COMMERCIAL_ACTIVITY_ID = myDb.getLoggedCommercialActivityId();
                                    manageAssociation();
                                }

                                @Override
                                public void wrong() {
                                    toast("ID attività commerciale e/o codice di sicurezza errati. \nRiprova");

                                }

                                @Override
                                public void ko() {
                                    toast("Errore durante l'associazione attività commerciale. Riprova");

                                }
                            }).execute();
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    });

                    imgBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    });

                }
            });

        }
    }

    public void btnOnClick(View view){
        boolean result = false;
        if(view.getId() == R.id.btn_login)
        {
            if(caName==null){
                toast("Prima di effettuare il login devi associare un'attività commerciale");
                return;
            }

            TapManager.COMMERCIAL_ACTIVITY_ID = myDb.getLoggedCommercialActivityId();

            String username = ((EditText)findViewById(R.id.editxt_login_name)).getText().toString().trim();
            String password = CryptoUtil.SHA256(((EditText) findViewById(R.id.editxt_login_pass)).getText().toString().trim());

            new WsLogin(username, password, getApplicationContext(), new WsLogin.Callbacks() {
                @Override
                public void ok(String t, String u, String p) {
                    if(t==null || t.equals("") ){
                        myDb.deleteInvalidUser(u, p);
                        toast("Nome utente e/o password errati.\nRiprova");
                    }else{
                        myDb.saveValidUser(u, p);
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void ko(String u, String p) {
                    if (myDb.checkLogin(u, p)){
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }else{
                        toast("\"Nome utente e/o password errati.\\nOppure errore server\"");
                    }
                }
            }).execute();

        }
        else if(view.getId()==R.id.btn_login_signin)
        {
            /*
            var builder = new AlertDialog.Builder (this);
            builder.SetTitle ("Hello Dialog")
                    .SetMessage ("Is this material design?")
                    .SetPositiveButton ("Yes", delegate { Console.WriteLine("Yes"); })
            .SetNegativeButton ("No", delegate { Console.WriteLine("No"); });
            builder.Create().Show ();*/


        }

        /*else if(view.getId() == R.id.btn_loginAbusivo)
        {
            //CODICE DUPLICATO, MA TANTO E TEMPORANEO

            String username = "pinco";
            String password = CryptoUtil.SHA256("pallino");

            new WsLogin(username, password, this, new WsLogin.Callbacks() {
                @Override
                public void ok(String t, String u, String p) {
                    if(t==null || t.equals("") ){
                        myDb.deleteInvalidUser(u, p);
                        Toast mytoast = Toast.makeText(getApplicationContext(), "Nome utente e/o password errati.\nRiprova", Toast.LENGTH_SHORT);
                        mytoast.setGravity(Gravity.TOP, 0, 200);
                        mytoast.show();
                    }else{
                        myDb.saveValidUser(u, p);
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void ko(String u, String p) {
                    if (myDb.checkLogin(u, p)){
                        Intent intent = new Intent(thisC, ActivityHome.class);
                        startActivity(intent);
                    }else{
                        Toast mytoast = Toast.makeText(getApplicationContext(), "Nome utente e/o password errati.\nOppure errore server" , Toast.LENGTH_SHORT);
                        mytoast.setGravity(Gravity.TOP, 0, 200);
                        mytoast.show();
                    }
                }
            }).execute();
        }*/
    }

    @Override
    public void onBackPressed(){
        //Prevengo l'uso del tasto back (porterebbe sulla pagina CARICAMENTO senza possibilità di tornare sul login)
    }


}
