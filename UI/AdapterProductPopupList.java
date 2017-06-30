package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.IngredientOrProduct;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.DecimalUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by robertov on 30/07/2015.
 */
public class AdapterProductPopupList extends ArrayAdapter<IngredientOrProduct> {
    protected Context c;
    protected Activity a;

    public AdapterProductPopupList(Context context, Activity activity, ArrayList<IngredientOrProduct> products) {
        super(context, 0, products);
        c=context;
        a=activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position, the order of the orders is reversed, the newest orders are on top.

        final IngredientOrProduct product = getItem(position);
        //final Order order = getItem(getCount()-position-1);


        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_popup_product, parent, false);

        convertView.setBackgroundColor( convertView.getResources().getColor( R.color.red_dark));

        //img sulla sinistra
        //ImageView imgLeft = (ImageView) convertView.findViewById(R.id.img_open_orders);

        ((TextView) convertView.findViewById(R.id.lbl_list_popup_product_name)).setText(product.name);

        ((EditText) convertView.findViewById(R.id.txt_list_popup_product_price)).setText(DecimalUtil.formatInt(product.price));


        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends IngredientOrProduct> collection) {
        super.addAll(collection);
    }
}