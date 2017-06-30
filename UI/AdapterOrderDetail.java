package com.lynkteam.tapmanager.UI;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lynkteam.tapmanager.DB.OrderLineItem;
import com.lynkteam.tapmanager.R;
import com.lynkteam.tapmanager.util.DecimalUtil;

import java.util.ArrayList;

/**
 * Created by robertov on 24/08/15.
 */
public class AdapterOrderDetail extends BaseExpandableListAdapter  {
    private Activity activity;
    private ArrayList<Object> childtems;
    private LayoutInflater inflater;
    public ArrayList<OrderLineItem> parentItems;
    public ArrayList<String> child;


    public AdapterOrderDetail(ArrayList<OrderLineItem> parents, ArrayList<Object> children) {
        this.parentItems = parents;
        this.childtems = children;
    }

    public void setInflater(LayoutInflater inflater, Activity activity) {
        this.inflater = inflater;
        this.activity = activity;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        child = (ArrayList<String>) childtems.get(groupPosition);

        TextView textView = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expandablelistviewitemchild_orderlineitem, null);
        }

        textView = (TextView) convertView.findViewById(R.id.textView1);
        textView.setText(child.get(childPosition));

        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expandablelistviewitemgroup_orderitem, null);
        }

        ((TextView) convertView.findViewById(R.id.txt_elv_order_details_name) ).setText(parentItems.get(groupPosition).name );

        ((TextView) convertView.findViewById(R.id.txt_elv_order_details_num) ).setText( parentItems.get(groupPosition).parts );

        ((TextView) convertView.findViewById(R.id.txt_elv_order_details_price) ).setText( "€ " + DecimalUtil.formatInt(parentItems.get(groupPosition).price)  );

        ((TextView) convertView.findViewById(R.id.txt_elv_order_details_category) ).setText( parentItems.get(groupPosition).category );

        if(isExpanded){
            convertView.setBackgroundColor(activity.getResources().getColor( R.color.yellow_dark) );

            ((ImageView) convertView.findViewById(R.id.img_open_orders_details)).setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);

        }else{
            ((ImageView) convertView.findViewById(R.id.img_open_orders_details)).setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);

            if(groupPosition%2==0){
                convertView.setBackgroundColor(activity.getResources().getColor( R.color.yellow_light) );
            }else{
                convertView.setBackgroundColor(activity.getResources().getColor( R.color.white) );
            }
        }

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ( (ArrayList<String>) childtems.get(groupPosition) ).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return parentItems.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }



    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
