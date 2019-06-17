package com.jby.vegeapp.adapter.pick_up;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.jby.vegeapp.Utils.VariableUtils.DELETE_CONFIRMATION;

public class AddedProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductParentObject> addedProductParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;

    public AddedProductExpandableAdapter(Context context, ArrayList<ProductParentObject> addedProductParentObjectArrayList
    , ProductExpandableAdapterCallBack productExpandableAdapterCallBack){
        this.context = context;
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

        }
        else
            groupViewHolder = (GroupViewHolder)convertView.getTag();

        final ProductParentObject object = getGroup(groupPosition);
        String imagePath = new ApiManager().img_product + object.getPicture();

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(groupViewHolder.picture);

        groupViewHolder.name.setText(object.getName());
        groupViewHolder.quantity.setText("x " + object.getQuantity());
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

    private static class GroupViewHolder{
        private ImageView picture;
        private TextView name, quantity;

        GroupViewHolder (View view){
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
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.added_product_dialog_child_list_view_item, null);
                viewHolder = new ChildViewHolder(view);
                view.setTag(viewHolder);
        }
         else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductChildObject object = getChild(groupPosition, childPosition);
        viewHolder.grade.setText(getGrade(object.getGrade()));
        viewHolder.quantity.setText("x" + object.getQuantity());
        viewHolder.weight.setText(object.getWeight() + "KG");

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productExpandableAdapterCallBack.confirmationDialog("Warning", "Are you sure that you want to delete this id ?", childPosition, DELETE_CONFIRMATION);
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

    private static class ChildViewHolder{
        private TextView weight, grade, quantity;
        private ImageView delete;
        ChildViewHolder (View view){
            weight = view.findViewById(R.id.added_product_dialog_child_weight);
            grade = view.findViewById(R.id.added_product_dialog_child_grade);
            quantity = view.findViewById(R.id.added_product_dialog_child_quantity);
            delete = view.findViewById(R.id.added_product_dialog_child_delete);
        }
    }
/*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private String getGrade(String grade){
        if(grade.equals("unknown")) return "(grade -)";
        else return "(grade " + grade + ")";
    }

    public interface ProductExpandableAdapterCallBack{
        void confirmationDialog(String title, String content, int position, int action);
    }
}
