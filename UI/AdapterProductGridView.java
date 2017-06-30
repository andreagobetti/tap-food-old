package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.DBHelper;
import com.lynkteam.tapmanager.DB.IngredientOrProduct;
import com.lynkteam.tapmanager.DB.Logger;
import com.lynkteam.tapmanager.DB.Warehouse;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.DecimalUtil;
import com.lynkteam.tapmanager.util.FileSystemUtil;
import com.lynkteam.tapmanager.util.WarehouseManagementUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by robertov on 02/09/15.
 */

public class AdapterProductGridView extends ArrayAdapter<Warehouse> {
    protected Context mContext;
    protected Activity mActivity;

    protected ArrayList<Warehouse> myWarehouses;

    protected ArrayList<Bitmap> bitmaps;


    public AdapterProductGridView(Context c, Activity a, ArrayList<Warehouse> warehouses) {
        super(c, 0, warehouses);

        mContext = c;
        mActivity = a;
        myWarehouses = warehouses;

        bitmaps = new ArrayList<>();

        for(int i=0; i<myWarehouses.size();i++)
        {
            try {
                Bitmap b = FileSystemUtil.loadWarehouseElementBitmap(mContext, Integer.toString(myWarehouses.get(i).warehouseElementId));
                bitmaps.add(b);
            }catch (Exception ex)
            {
                Logger.Error(mContext,"Errore Adapter ProductGridView",ex.toString());
            }

        }
    }

    public int getCount() {
        return myWarehouses.size();
    }

    public Warehouse getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, final ViewGroup parent) {

        convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item_product, parent, false);

        final ImageView productImage = (ImageView) convertView.findViewById(R.id.img_gv_product);
        productImage.setImageBitmap(bitmaps.get(position));

        TextView label = (TextView) convertView.findViewById(R.id.lbl_gv_product);
        label.setText(myWarehouses.get(position).name);


        final ImageView isAvIcon = (ImageView) convertView.findViewById(R.id.chk_gv_product_available);
        final ImageView notAvIcon  = (ImageView) convertView.findViewById(R.id.chk_gv_product_notavailable);

        if(myWarehouses.get(position).isSetUp) {
            if (myWarehouses.get(position).isAvailable) {
                isAvIcon.setVisibility(View.VISIBLE);

            } else {
                isAvIcon.setVisibility(View.GONE);
                notAvIcon.setVisibility(View.VISIBLE);
            }
        }else{
            isAvIcon.setVisibility(View.GONE);
            notAvIcon.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder bldr = new AlertDialog.Builder(mActivity);

                final View convertView = mActivity.getLayoutInflater().inflate(R.layout.alertdialog_products, null);

                final ListView lv = (ListView) convertView.findViewById(R.id.lv_product_prices);
                final CheckBox cb = (CheckBox) convertView.findViewById(R.id.chk_product_available);
                final ImageButton ib = (ImageButton) convertView.findViewById(R.id.imgbtn_popup_product_back);
                final Button b = (Button) convertView.findViewById(R.id.btn_popup_product_confirm);


                cb.setChecked(myWarehouses.get(position).isAvailable);

                ((TextView) convertView.findViewById(R.id.lbl_popup_product_title)).setText( myWarehouses.get(position).name);

                ArrayList<IngredientOrProduct> products = DBHelper.getInstance(mContext).getIngredientOrProducts(myWarehouses.get(position).warehouseId);

                final AdapterProductPopupList adapter = new AdapterProductPopupList(mContext, mActivity, products);

                lv.setAdapter(adapter);

                bldr.setView(convertView);

                bldr.create();
                final AlertDialog dialog = bldr.show();

                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                    }
                });

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ArrayList<ContentValues> prices = new ArrayList<>();

                        boolean isAvailable = cb.isChecked();

                        for (int i = 0; i < lv.getCount(); i++) {
                            String price = ((EditText) lv.getChildAt(i).findViewById(R.id.txt_list_popup_product_price)).getText().toString();

                            ContentValues pcv = new ContentValues();
                            pcv.put("price", DecimalUtil.formatString(price));

                            int ingredientId = adapter.getItem(i).ingredientId;
                            int productId = adapter.getItem(i).productId;

                            pcv.put("ingredientId", ingredientId == 0 ? null : ingredientId);
                            pcv.put("productId", productId == 0 ? null : productId);

                            prices.add(pcv);

                        }

                        WarehouseManagementUtil.setAvailabilityAndPrice(mContext, myWarehouses.get(position).warehouseId, isAvailable, myWarehouses.get(position).lastEdit , prices);

                        dialog.cancel();
                        dialog.dismiss();

                    }
                });


            }

        });

        return convertView;
    }

    @Override
    public void addAll(Collection<? extends Warehouse> collection) {
        super.addAll(collection);
    }
}