package com.jby.admin.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.DriverObject;

import java.util.ArrayList;


public class DriverAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DriverObject> driverObjectArrayList;

    public DriverAdapter(Context context, ArrayList<DriverObject> driverObjectArrayList)
    {
        this.context = context;
        this.driverObjectArrayList = driverObjectArrayList;
    }

    @Override
    public int getCount() {
        return driverObjectArrayList.size();
    }

    @Override
    public DriverObject getItem(int i) {
        return driverObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.driver_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        DriverObject object = getItem(i);
        viewHolder.name.setText(object.getName());

        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder{
        private TextView name;
        private LinearLayout parentLayout;

        ViewHolder (View view){
            name = view.findViewById(R.id.driver_dialog_list_view_item_name);
            parentLayout = view.findViewById(R.id.driver_dialog_list_view_item_parent_layout);
        }
    }
}
