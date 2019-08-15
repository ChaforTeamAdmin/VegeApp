package com.jby.admin.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.entity.FarmerObject;

import java.util.ArrayList;

public class FarmerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<FarmerObject> farmerObjectArrayList;

    public FarmerAdapter(Context context, ArrayList<FarmerObject> farmerObjectArrayList)
    {
        this.context = context;
        this.farmerObjectArrayList = farmerObjectArrayList;
    }

    @Override
    public int getCount() {
        return farmerObjectArrayList.size();
    }

    @Override
    public FarmerObject getItem(int i) {
        return farmerObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.farmer_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        FarmerObject object = getItem(i);
        viewHolder.name.setText(object.getName());
        if(object.getAddress().equals("null")) viewHolder.address.setVisibility(View.GONE);
        else viewHolder.address.setVisibility(View.VISIBLE);

        viewHolder.address.setText(object.getAddress());
        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder{
        private TextView name, address;
        private LinearLayout parentLayout;

        ViewHolder (View view){
            name = view.findViewById(R.id.farmer_dialog_list_view_item_name);
            address = view.findViewById(R.id.farmer_dialog_list_view_item_address);
            parentLayout = view.findViewById(R.id.farmer_dialog_list_view_item_parent_layout);
        }
    }
}
