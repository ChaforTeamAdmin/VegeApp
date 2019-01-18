package com.jby.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.shareObject.AnimationUtility;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;

    public ProductExpandableAdapter(Context context, ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList, ProductExpandableAdapterCallBack productExpandableAdapterCallBack){
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

        }
        else
            groupViewHolder = (GroupViewHolder)convertView.getTag();

        final ProductDetailParentObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().product_img + object.getPicture();

        if(object.getAvailable_quantity().equals("0")){
            groupViewHolder.status.setBackground(context.getDrawable(R.drawable.product_list_view_item_unavailable));
            groupViewHolder.status.setText(R.string.product_adapter_stock_unavailable);
            groupViewHolder.quantity.setVisibility(View.INVISIBLE);
        }
        else{
            groupViewHolder.status.setBackground(context.getDrawable(R.drawable.product_list_view_item_available));
            groupViewHolder.status.setText(R.string.product_adapter_stock_available);

            groupViewHolder.quantity.setVisibility(View.VISIBLE);
            groupViewHolder.quantity.setTextColor(context.getResources().getColor(R.color.green));
        }

        groupViewHolder.quantity.setText("Quantity: " +object.getAvailable_quantity());
        groupViewHolder.name.setText(object.getName());

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);

//        groupViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(object.getAvailable_quantity().equals("0")) showSnackBar("Stock Unavailable!", view);
//                else productExpandableAdapterCallBack.parentOnCluck(groupPosition);
//            }
//        });

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

    private static class GroupViewHolder{
        private ImageView picture;
        private TextView name, status, quantity;
        private CardView parentLayout;

        GroupViewHolder (View view){
            parentLayout = view.findViewById(R.id.product_parent_list_view_item_parent_layout);
            picture = view.findViewById(R.id.product_parent_list_view_item_picture);

            name = view.findViewById(R.id.product_parent_list_view_item_name);
            status = view.findViewById(R.id.product_parent_list_view_item_status);
            quantity = view.findViewById(R.id.product_parent_list_view_item_quantity);
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
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        boolean isDate = false;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.product_detail_child_list_view_item, null);
                viewHolder = new ChildViewHolder(view);
                view.setTag(viewHolder);
        }
         else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductDetailChildObject object = getChild(groupPosition, childPosition);
        if(!object.getId().equals("")) {
            isDate = true;

            viewHolder.quantity.setText("x" + object.getQuantity());
            viewHolder.farmer.setText(object.getFarmerName());
            viewHolder.time.setText(getTime(object.getTime()));
            viewHolder.grade.setText("Grade " + object.getGrade());
            viewHolder.driver.setText("Pick up by " + object.getDriverName());
            viewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            new AnimationUtility().fadeInVisible(context, viewHolder.parentLayout);

        }else{

            if(isToday(object.getDate()).equals("Today")){
                viewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.tiffany_blue));
                viewHolder.date.setText(isToday(object.getDate()));
            }
            else{
                viewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.red));
                viewHolder.date.setText(object.getDate());
            }
        }

        viewHolder.farmer.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.quantity.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.grade.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.time.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.driver.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.label.setVisibility(isDate ? View.VISIBLE : View.GONE);
        viewHolder.date.setVisibility(isDate ? View.GONE : View.VISIBLE);

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

    private static class ChildViewHolder{
        private TextView farmer, driver, grade, quantity, time, label, date;
        private RelativeLayout parentLayout;
        ChildViewHolder (View view){
            farmer = view.findViewById(R.id.product_detail_list_view_item_farmer);
            driver = view.findViewById(R.id.product_detail_list_view_item_driver);
            grade = view.findViewById(R.id.product_detail_list_view_item_grade);
            quantity = view.findViewById(R.id.product_detail_list_view_item_quantity);
            label = view.findViewById(R.id.product_detail_list_view_item_label_quantity);
            date = view.findViewById(R.id.product_detail_list_view_item_date);

            time = view.findViewById(R.id.product_detail_list_view_item_time);

            parentLayout = view.findViewById(R.id.farmer_dialog_list_view_item_parent_layout);
        }
    }
/*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private void showSnackBar(String message, View view){
        final Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private String isToday(String date){
        if(date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()))))
            return "Today";
        return date;
    }

    private String getTime(String time){
        if(time.length() >= 5) return  time  = time.substring(0, 5);
        else return time;
    }

    public interface ProductExpandableAdapterCallBack{
        void parentOnCluck(int position);
    }
}
