package com.jby.vegeapp.adapter.delivery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.CustomerObject;
import com.jby.vegeapp.object.FarmerObject;

import java.util.ArrayList;

public class DeliveryCustomerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CustomerObject> customerObjectArrayList;

    public DeliveryCustomerAdapter(Context context, ArrayList<CustomerObject> customerObjectArrayList) {
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
        if (view == null) {
            view = View.inflate(this.context, R.layout.activity_deliver_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        CustomerObject object = getItem(i);
        String item = object.getTotalDeliverQuantity() + " items";
        viewHolder.name.setText(object.getName());
        viewHolder.address.setText(object.getAddress());
        viewHolder.phone.setText(object.getPhone());
        viewHolder.item.setText(item);
        viewHolder.do_id.setText(String.format("#%s%s", object.getDo_prefix(), setPlaceHolder(object.getDo_id())));
        return view;
    }

    private String setPlaceHolder(String do_id){
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for(int i = do_idBuilder.length(); i < 5; i++){
            do_idBuilder.insert(0, "0");
        }
        return do_idBuilder.toString();
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private TextView name, address, phone, item, do_id;

        ViewHolder(View view) {
            name = view.findViewById(R.id.activity_deliver_list_view_name);
            address = view.findViewById(R.id.activity_deliver_list_view_address);
            phone = view.findViewById(R.id.activity_deliver_list_view_phone);
            item = view.findViewById(R.id.activity_deliver_list_view_item);
            do_id = view.findViewById(R.id.activity_deliver_list_view_do_id);
        }
    }
}
