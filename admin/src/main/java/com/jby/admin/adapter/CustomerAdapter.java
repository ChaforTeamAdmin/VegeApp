package com.jby.admin.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.entity.CustomerObject;

import java.util.ArrayList;


public class CustomerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CustomerObject> customerObjectArrayList;

    public CustomerAdapter(Context context, ArrayList<CustomerObject> customerObjectArrayList)
    {
        this.context = context;
        this.customerObjectArrayList = customerObjectArrayList;
    }

    @Override
    public int getCount() {
        return customerObjectArrayList.size();
    }

    @Override
    public CustomerObject getItem(int i) {
        return customerObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.customer_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        CustomerObject object = getItem(i);
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
            name = view.findViewById(R.id.customer_dialog_list_view_item_name);
            address = view.findViewById(R.id.customer_dialog_list_view_item_address);
            parentLayout = view.findViewById(R.id.customer_dialog_list_view_item_parent_layout);
        }
    }
}
