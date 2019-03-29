package com.jby.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.others.CustomGridView;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

public class ProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;

    public ProductExpandableAdapter(Context context, ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList, ProductExpandableAdapterCallBack productExpandableAdapterCallBack) {
        this.context = context;
        this.productDetailParentObjectArrayList = productDetailParentObjectArrayList;
        this.productExpandableAdapterCallBack = productExpandableAdapterCallBack;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*-----------------------------------------------------------------------------PARENT VIEW-------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.product_detail_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        final ProductDetailParentObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().product_img + object.getPicture();

        if (object.getAvailable_quantity().equals("0")) {
            groupViewHolder.status.setBackground(context.getDrawable(R.drawable.product_list_view_item_unavailable));
            groupViewHolder.status.setText(R.string.product_adapter_stock_unavailable);
            groupViewHolder.quantity.setVisibility(View.INVISIBLE);
        } else {
            groupViewHolder.status.setBackground(context.getDrawable(R.drawable.product_list_view_item_available));
            groupViewHolder.status.setText(R.string.product_adapter_stock_available);

            groupViewHolder.quantity.setVisibility(View.VISIBLE);
            groupViewHolder.quantity.setTextColor(context.getResources().getColor(R.color.green));
        }
        //taken
        if (!((MainActivity) context).getCustomerID().equals("-1")) {
            if (object.getTaken_quantity().equals("0")) {
                groupViewHolder.takenLayout.setVisibility(View.GONE);
                groupViewHolder.statusLayout.setGravity(Gravity.END);

            } else {
                groupViewHolder.takenLayout.setVisibility(View.VISIBLE);
                groupViewHolder.statusLayout.setGravity(Gravity.START);

                groupViewHolder.takenQuantity.setText(" x " + object.getTaken_quantity());
            }
        } else {
            groupViewHolder.takenLayout.setVisibility(View.GONE);
            groupViewHolder.statusLayout.setGravity(Gravity.END);
        }
        groupViewHolder.quantity.setText(" x " + object.getAvailable_quantity());
        groupViewHolder.name.setText(object.getName());

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ProductDetailParentObject getGroup(int i) {
        return productDetailParentObjectArrayList.get(i);
    }

    private static class GroupViewHolder {
        private ImageView picture;
        private TextView name, status, quantity, labelTaken, takenQuantity;
        private LinearLayout statusLayout, takenLayout;
        private CardView parentLayout;

        GroupViewHolder(View view) {
            parentLayout = view.findViewById(R.id.product_parent_list_view_item_parent_layout);
            picture = view.findViewById(R.id.product_parent_list_view_item_picture);

            name = view.findViewById(R.id.product_parent_list_view_item_name);
            status = view.findViewById(R.id.product_parent_list_view_item_status);
            quantity = view.findViewById(R.id.product_parent_list_view_item_quantity);
            statusLayout = view.findViewById(R.id.product_parent_list_view_item_status_layout);
            takenLayout = view.findViewById(R.id.product_parent_list_view_item_taken_layout);

            labelTaken = view.findViewById(R.id.product_parent_list_view_item_label_taken);
            takenQuantity = view.findViewById(R.id.product_parent_list_view_item_taken_quantity);
        }
    }

    @Override
    public int getGroupCount() {
        return productDetailParentObjectArrayList.size();
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        Handler handler;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.product_detail_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductDetailChildObject object = getChild(groupPosition, childPosition);

        viewHolder.farmerName.setText(object.getFarmerName());
        viewHolder.availableQuantity.setText("A: " + object.getQuantity());
        //taken
        if (!((MainActivity) context).getCustomerID().equals("-1")) {
            if (object.getTakenQuantity().equals("0")) {
                viewHolder.takenQuantity.setVisibility(View.GONE);
            } else {
                viewHolder.takenQuantity.setVisibility(View.VISIBLE);
                viewHolder.takenQuantity.setText("T: " + object.getTakenQuantity());
            }
        } else viewHolder.takenQuantity.setVisibility(View.GONE);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                productExpandableAdapterCallBack.childOnClick(childPosition, groupPosition);
                //prevent double click
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 200);
            }
        });
        Log.d("haha", "haha refreshed: " + object.getTakenQuantity());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return productDetailParentObjectArrayList.get(i).getProductDetailChildObjectArrayList().size();
    }

    @Override
    public ProductDetailChildObject getChild(int groupPosition, int childPosition) {
        return productDetailParentObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        TextView farmerName, availableQuantity, takenQuantity;

        ChildViewHolder(View view) {
            farmerName = view.findViewById(R.id.product_child_list_view_item_farmer_name);
            availableQuantity = view.findViewById(R.id.product_child_list_view_item__available_quantity);
            takenQuantity = view.findViewById(R.id.product_child_list_view_item_taken_quantity);
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public interface ProductExpandableAdapterCallBack {
        void parentOnCluck(int position);

        void childOnClick(int position, int childPosition);
    }
}
