package com.jby.admin.adapter.product;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.jby.admin.R;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AddedProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductObject> addedProductParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;
    private String fromWhichFragment;

    public AddedProductExpandableAdapter(Context context, ArrayList<ProductObject> addedProductParentObjectArrayList, ProductExpandableAdapterCallBack productExpandableAdapterCallBack, String fromWhichFragment) {
        this.context = context;
        this.addedProductParentObjectArrayList = addedProductParentObjectArrayList;
        this.productExpandableAdapterCallBack = productExpandableAdapterCallBack;
        this.fromWhichFragment = fromWhichFragment;
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

        final ProductObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().product_img + object.getPicture();

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);

        groupViewHolder.name.setText(object.getName());
        groupViewHolder.quantity.setText(countTotalChildQuantity(groupPosition));
        return convertView;
    }

    public String countTotalChildQuantity(int groupPosition) {
        double totalChildQuantity = 0;
        try {
            for (int i = 0; i < addedProductParentObjectArrayList.get(groupPosition).getAddedProductChildArrayList().size(); i++) {
                totalChildQuantity += Double.valueOf(addedProductParentObjectArrayList.get(groupPosition).getAddedProductChildArrayList().get(i).getQuantity());
            }
            return "x " + totalChildQuantity;
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ProductObject getGroup(int i) {
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
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.added_product_dialog_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductObject object = getChild(groupPosition, childPosition);
        viewHolder.weight.setText(String.format("%sKG", object.getWeight()));
        viewHolder.quantity.setText(String.format("x %s", object.getQuantity()));
        viewHolder.price.setText(object.getPrice().equals("") ? "Price: - " : String.format("Price: RM %s", object.getPrice()));

        if (SharedPreferenceManager.getGrade(context)) {
            viewHolder.grade.setVisibility(View.VISIBLE);
            viewHolder.grade.setText(String.format("Grade: %s", object.getGrade()));
        }

        if (SharedPreferenceManager.getLocation(context)) {
            viewHolder.location.setVisibility(View.VISIBLE);
            viewHolder.location.setText(String.format("Location: %s", object.getLocation()));
        }

        if (fromWhichFragment.equals("delivery_fragment")) {
            viewHolder.stockDate.setVisibility(View.VISIBLE);
            viewHolder.stockDate.setText(object.getAvailable_stock());
        }

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productExpandableAdapterCallBack.deleteConfirmation(groupPosition, childPosition);
            }
        });

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productExpandableAdapterCallBack.update(groupPosition, childPosition);
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public int getChildrenCount(int i) {
        return addedProductParentObjectArrayList.get(i).getAddedProductChildArrayList().size();
    }

    @Override
    public ProductObject getChild(int groupPosition, int childPosition) {
        return addedProductParentObjectArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private LinearLayout parentView;
        private TextView weight, quantity, price;
        private TextView grade, location, stockDate;
        private ImageView delete;

        ChildViewHolder(View view) {
            parentView = view.findViewById(R.id.parent_view);

            weight = view.findViewById(R.id.added_product_dialog_child_weight);
            quantity = view.findViewById(R.id.added_product_dialog_child_quantity);
            price = view.findViewById(R.id.added_product_dialog_child_price);

            grade = view.findViewById(R.id.added_product_dialog_child_grade);
            location = view.findViewById(R.id.added_product_dialog_child_location);
            stockDate = view.findViewById(R.id.added_product_dialog_child_stock_date);

            delete = view.findViewById(R.id.added_product_dialog_child_delete_view);
        }
    }
    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private String getGrade(String grade) {
        if (grade.equals("unknown")) return "(grade -)";
        else return "(grade " + grade + ")";
    }

    public interface ProductExpandableAdapterCallBack {
        void deleteConfirmation(int groupPosition, int childPosition);

        void update(int groupPosition, int childPosition);
    }
}
