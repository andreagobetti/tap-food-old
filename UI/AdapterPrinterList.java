package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.ProductionArea;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.WS.WsUpdateProductionArea;
import com.lynkteam.tapmanager.util.PrintUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by robertov on 30/07/2015.
 */
public class AdapterPrinterList extends ArrayAdapter<ProductionArea> {
    protected Context c;
    protected Activity a;

    public AdapterPrinterList(Context context, Activity activity, ArrayList<ProductionArea> productionAreas) {
        super(context, 0, productionAreas);
        c=context;
        a=activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position, the order of the orders is reversed, the newest orders are on top.

        final ProductionArea area = getItem(position);
        //final Order order = getItem(getCount()-position-1);

        if(convertView==null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_printer, parent, false);

        //convertView.setBackgroundColor( convertView.getResources().getColor( R.color.red_dark));

        //img sulla sinistra
        //ImageView imgLeft = (ImageView) convertView.findViewById(R.id.img_open_orders);

        ((TextView) convertView.findViewById(R.id.lbl_printer_name)).setText(area.description);
        ((TextView) convertView.findViewById(R.id.lbl_printer_model)).setText(area.name);

        ((TextView) convertView.findViewById(R.id.lbl_printer_fiscal)).setText(area.isFiscal ? "Fiscale" : "Non Fiscale");

        if(area.isFiscal) {
            ((ImageButton) convertView.findViewById(R.id.imgbtn_printer_initialize_fiscal)).setVisibility(View.VISIBLE);

            ((ImageButton) convertView.findViewById(R.id.imgbtn_printer_initialize_fiscal)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrintUtil.FiscalPrinterSync(a, area.productionAreaId);
                }
            });
        }else {
            ((ImageButton) convertView.findViewById(R.id.imgbtn_printer_initialize_fiscal)).setVisibility(View.INVISIBLE);
        }

        ((ImageButton) convertView.findViewById(R.id.imgbtn_printer_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder bldr = new AlertDialog.Builder( a );
                bldr.setMessage("Confermi di voler eliminare la stampante " + area.description  + " ?" )
                        .setTitle("Conferma Eliminazione")
                        .setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                area.isDeleted = true;
                                new WsUpdateProductionArea(c, area, new WsUpdateProductionArea.Callbacks() {
                                    @Override
                                    public void ok() {
                                        //TODO salvataggio modifiche sul db locale
                                    }

                                    @Override
                                    public void ko() {
                                        ActivityHome.toast("C'è stato un errore durante l'aggiornamento della stampante");

                                    }
                                }).execute();

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

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder bldr = new AlertDialog.Builder(a);

                final View dialogView = a.getLayoutInflater().inflate(R.layout.alertdialog_printer_setup, null);

                ((TextView) dialogView.findViewById(R.id.txt_popup_printer_title)).setText( a.getResources().getString(R.string.sett_printer_edit));
                ((TextView) dialogView.findViewById(R.id.txt_popup_printer_name)).setText(  area.description );
                ((TextView) dialogView.findViewById(R.id.txt_popup_printer_port)).setText(Integer.toString(area.printerIpPort));
                ((TextView) dialogView.findViewById(R.id.txt_popup_printer_ip)).setText(area.printerIpAddress);
                ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setChecked(area.isFiscal);

                if(!area.canBeFiscal)
                    ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(false);
                else
                    ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(true);


                ArrayList<String[]> models = DBHelper.getInstance(a.getApplicationContext()).getPrinterModels();
                final String[] modelIDs = models.get(0);
                final String[] modelNames = models.get(1);
                final String[] canBeFiscal = models.get(2);

                Spinner spinner = (Spinner) dialogView.findViewById(R.id.spn_popup_printer_model);
                // Create an ArrayAdapter using the string array and a default spinner layout.
                // ArrayAdapter used to take only an array.

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(a,android.R.layout.simple_spinner_item, modelNames);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);

                int id = java.util.Arrays.binarySearch(modelNames, area.modelName);

                spinner.setSelection(id);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (canBeFiscal[position].equals("1")) {
                            ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(true);
                        } else {
                            ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setEnabled(false);
                            ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).setChecked(false);
                        }


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                //spinner.setOnItemSelectedListener(a);



                bldr.setView(dialogView);

                bldr.create();
                final AlertDialog dialog = bldr.show();

                ((ImageButton) dialogView.findViewById(R.id.imgbtn_popup_product_back)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                });

                ((Button) dialogView.findViewById(R.id.btn_popup_printer)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int modID = (int) ((Spinner) dialogView.findViewById(R.id.spn_popup_printer_model)).getSelectedItemId();


                        area.modelCode = modelIDs[modID];
                        area.name = ((EditText) dialogView.findViewById(R.id.txt_popup_printer_name)).getText().toString();
                        area.printerIpAddress = ((EditText) dialogView.findViewById(R.id.txt_popup_printer_ip)).getText().toString();
                        area.printerIpPort = Integer.parseInt(((EditText) dialogView.findViewById(R.id.txt_popup_printer_port)).getText().toString());
                        area.isFiscal = ((CheckBox) dialogView.findViewById(R.id.chk_popup_printer_fiscal)).isChecked();


                        new WsUpdateProductionArea(c, area, new WsUpdateProductionArea.Callbacks() {
                            @Override
                            public void ok() {
                                //TODO salvataggio modifiche sul db locale??
                            }

                            @Override
                            public void ko() {
                                ActivityHome.toast("C'è stato un errore durante l'aggiornamento della stampante");

                            }
                        }).execute();

                        dialog.dismiss();
                        dialog.cancel();
                    }
                });

            }
        });


        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends ProductionArea> collection) {
        super.addAll(collection);
    }
}