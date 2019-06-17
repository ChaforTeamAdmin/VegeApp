package com.jby.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DeliveryOrderDetailExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList;

    public DeliveryOrderDetailExpandableAdapter(Context context, ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList) {
        this.context = context;
        this.productDetailParentObjectArrayList = productDetailParentObjectArrayList;
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
            convertView = layoutInflater.inflate(R.layout.delivery_order_detail_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        final ProductDetailParentObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().product_img + object.getPicture();

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);

        groupViewHolder.name.setText(object.getName());
        groupViewHolder.quantity.setText(String.format("x %s", productDetailParentObjectArrayList.get(groupPosition).getQuantity()));
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
        private TextView name, quantity;

        GroupViewHolder(View view) {
            picture = view.findViewById(R.id.activity_delivery_order_detail_parent_list_view_picture);
            name = view.findViewById(R.id.activity_delivery_order_detail_parent_list_view_product);
            quantity = view.findViewById(R.id.activity_delivery_order_detail_parent_list_view_quantity);
        }
    }

    @Override
    public int getGroupCount() {
        return productDetailParentObjectArrayList.size();
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, final ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.delivery_order_detail_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductDetailChildObject object = getChild(groupPosition, childPosition);

        viewHolder.farmer.setText(object.getFarmerName());
        viewHolder.quantity.setText(String.format("x%s", object.getQuantity()));
//        viewHolder.weight.setText(String.format("%sKG", object.getWeight()));

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
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
        private TextView weight, quantity, farmer;

        ChildViewHolder(View view) {
            farmer = view.findViewById(R.id.activity_delivery_order_detail_farmer);
//            weight = view.findViewById(R.id.activity_delivery_order_detail_weight);
            quantity = view.findViewById(R.id.activity_delivery_order_detail_quantity);
        }
    }
    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public interface ProductExpandableAdapterCallBack {
    }
}
