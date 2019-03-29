package com.jby.vegeapp.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.ProductChildObject;

import java.util.ArrayList;


public class DeliverProductGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductChildObject> productChildObjectArrayList;
    private ProductGridAdapterCallBack productGridAdapterCallBack;

    public DeliverProductGridAdapter(Context context, ArrayList<ProductChildObject> productChildObjectArrayList, ProductGridAdapterCallBack productGridAdapterCallBack)
    {
        this.context = context;
        this.productChildObjectArrayList = productChildObjectArrayList;
        this.productGridAdapterCallBack = productGridAdapterCallBack;
    }

    @Override
    public int getCount() {
        return productChildObjectArrayList.size();
    }

    @Override
    public ProductChildObject getItem(int i) {
        return productChildObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.delivert_product_grid_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final ProductChildObject object = getItem(i);

        viewHolder.weight.setText(object.getWeight() + " KG");
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                productGridAdapterCallBack.onClick(i);

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 200);
            }
        });

        //ticked purpose
        if(!object.getRemark().equals("0")) viewHolder.tickedLayout.setVisibility(View.VISIBLE);
        else viewHolder.tickedLayout.setVisibility(View.GONE);
        return view;
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private void tick(View view){
        if(view.getVisibility() == View.GONE) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }

    private static class ViewHolder{
        private TextView weight;
        private ImageView remark;
        private CardView parentLayout;
        private LinearLayout tickedLayout;

        ViewHolder (View view){
            weight = view.findViewById(R.id.deliver_product_grid_item_weight);
            remark = view.findViewById(R.id.deliver_product_grid_item_remark);
            parentLayout = view.findViewById(R.id.deliver_product_grid_item_parent_layout);
            tickedLayout = view.findViewById(R.id.deliver_product_grid_item_ticked_layout);
        }
    }

    public interface ProductGridAdapterCallBack{
        void onClick(int position);
    }
}
