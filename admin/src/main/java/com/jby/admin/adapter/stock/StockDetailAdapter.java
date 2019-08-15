package com.jby.admin.adapter.stock;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.StockObject;

import java.util.ArrayList;


public class StockDetailAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<StockObject> stockObjectArrayList;
    private String productType;

    public StockDetailAdapter(Context context, ArrayList<StockObject> stockObjectArrayList, String productType) {
        this.context = context;
        this.stockObjectArrayList = stockObjectArrayList;
        this.productType = productType;
    }

    @Override
    public int getCount() {
        return stockObjectArrayList.size();
    }

    @Override
    public StockObject getItem(int i) {
        return stockObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.stock_detail_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        StockObject object = getItem(i);
        viewHolder.name.setText(object.getTarget());
        viewHolder.totalWeight.setText(object.getTotalIn().equals("null") ? "- " + object.getTotalOut() : "+ " + object.getTotalIn());
        viewHolder.totalWeight.setTextColor(object.getTotalIn().equals("null") ? context.getResources().getColor(R.color.red) : context.getResources().getColor(R.color.green));
        viewHolder.editIcon.setVisibility(object.getTotalIn().equals("null") ? View.GONE : View.VISIBLE);
        viewHolder.labelWeight.setText(productType.equals("box") ? "Box" : "KG");

        return view;
    }

    private void setWeightColor(StockObject stockObject, TextView textView) {
        if (stockObject.getTotalIn().equals("null")) {

        }
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private TextView name, totalWeight, labelWeight;
        private ImageView editIcon;

        ViewHolder(View view) {
            name = view.findViewById(R.id.stock_detail_list_view_item_name);
            totalWeight = view.findViewById(R.id.stock_detail_list_view_item_total_weight);
            labelWeight = view.findViewById(R.id.stock_detail_list_view_item_label_total_weight);
            editIcon = view.findViewById(R.id.stock_detail_list_view_item_edit_icon);
        }
    }
}
