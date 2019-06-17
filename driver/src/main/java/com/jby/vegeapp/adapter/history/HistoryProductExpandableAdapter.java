package com.jby.vegeapp.adapter.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.object.product.ProductParentObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HistoryProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductParentObject> addedProductParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;
    private String date;

    public HistoryProductExpandableAdapter(Context context, ArrayList<ProductParentObject> addedProductParentObjectArrayList,
                                           ProductExpandableAdapterCallBack productExpandableAdapterCallBack, String date) {
        this.context = context;
        this.date = date;
        this.addedProductParentObjectArrayList = addedProductParentObjectArrayList;
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
            convertView = layoutInflater.inflate(R.layout.added_product_dialog_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        final ProductParentObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().img_product + object.getPicture();

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);

        groupViewHolder.name.setText(object.getName());
        groupViewHolder.quantity.setText(String.format("x %s", addedProductParentObjectArrayList.get(groupPosition).getQuantity()));
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ProductParentObject getGroup(int i) {
        return addedProductParentObjectArrayList.get(i);
    }

    private static class GroupViewHolder {
        private ImageView picture;
        private TextView name, quantity;

        GroupViewHolder(View view) {
            picture = view.findViewById(R.id.added_product_dialog_parent_list_view_picture);

            name = view.findViewById(R.id.added_product_dialog_parent_list_view_product);
            quantity = view.findViewById(R.id.added_product_dialog_parent_list_view_quantity);
        }
    }

    @Override
    public int getGroupCount() {
        return addedProductParentObjectArrayList.size();
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
            view = layoutInflater.inflate(R.layout.delivery_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductChildObject object = getChild(groupPosition, childPosition);

        viewHolder.arrowRight.setVisibility(View.INVISIBLE);

        viewHolder.farmer.setVisibility(object.getFarmer() != null ? View.VISIBLE : View.GONE);
        viewHolder.farmer.setText(object.getFarmer() != null ? object.getFarmer() : "");

        viewHolder.quantity.setText(String.format("x%s", object.getQuantity()));
        viewHolder.weight.setText(String.format("%sKG", object.getWeight()));

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productExpandableAdapterCallBack.edit(groupPosition, childPosition);
            }
        });

        /*
         * check user type
         * */

        if (object.getFarmer() != null)
            viewHolder.edit.setVisibility(SharedPreferenceManager.getUserType(context).equals("0") ? (isToday(date) ? View.VISIBLE : View.GONE) : View.VISIBLE);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public int getChildrenCount(int i) {
        return addedProductParentObjectArrayList.get(i).getProductChildObjectArrayList().size();
    }

    @Override
    public ProductChildObject getChild(int groupPosition, int childPosition) {
        return addedProductParentObjectArrayList.get(groupPosition).getProductChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView weight, quantity, farmer;
        private ImageView arrowRight, edit;

        ChildViewHolder(View view) {
            farmer = view.findViewById(R.id.delivery_child_farmer);
            weight = view.findViewById(R.id.delivery_child_weight);
            quantity = view.findViewById(R.id.delivery_child_quantity);
            arrowRight = view.findViewById(R.id.delivery_child_arrow_right);
            edit = view.findViewById(R.id.delivery_child_edit);
        }
    }
    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private boolean isToday(String date) {
        return date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())));
    }

    public interface ProductExpandableAdapterCallBack {
        void edit(int groupPosition, int childPosition);
    }
}
