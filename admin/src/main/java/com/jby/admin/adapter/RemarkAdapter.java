package com.jby.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.RemarkChildObject;
import com.jby.admin.object.ExpandableParentObject;

import java.util.ArrayList;

public class RemarkAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ExpandableParentObject> remarkParentObjectArrayList;
    private boolean isMissing = true;
    private WeightViewHolder weightViewHolder;
    private MissingViewHolder missingViewHolder;
    private RemarkAdapterCallBack remarkAdapterCallBack;

    public RemarkAdapter(Context context, ArrayList<ExpandableParentObject> remarkParentObjectArrayList, RemarkAdapterCallBack remarkAdapterCallBack) {
        this.context = context;
        this.remarkParentObjectArrayList = remarkParentObjectArrayList;
        this.remarkAdapterCallBack = remarkAdapterCallBack;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*-----------------------------------------------------------------------------PARENT VIEW-------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.remark_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        ExpandableParentObject object = getGroup(groupPosition);
        groupViewHolder.date.setText(object.getDate().equals(getCurrentDate()) ? "Today" : object.getDate());

        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ExpandableParentObject getGroup(int i) {
        return remarkParentObjectArrayList.get(i);
    }

    private static class GroupViewHolder {
        TextView date;

        GroupViewHolder(View view) {
            date = view.findViewById(R.id.remark_parent_list_view_item_date);
        }
    }

    @Override
    public int getGroupCount() {
        return remarkParentObjectArrayList.size();
    }

    private static String getCurrentDate() {
        return String.valueOf(android.text.format.DateFormat.format("yyyy-MMM-dd", new java.util.Date()));
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        RemarkChildObject object = getChild(groupPosition, childPosition);
        //select view based on send or receive
        view = selectView(object, childPosition);
        //check send view or receive view
        if (!isMissing)
            //send object
            weightViewHolder.bindMessage(object);
        else
            //send object
            missingViewHolder.bindMessage(object);

        return view;
    }

    private View selectView(RemarkChildObject childObject, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = null;

        if (!childObject.getRemark().equals("")) {
            view = layoutInflater.inflate(R.layout.remark_weight_child_list_view_item, null);
            weightViewHolder = new WeightViewHolder(view, position);
            view.setTag(weightViewHolder);
            isMissing = false;
        } else {
            view = layoutInflater.inflate(R.layout.remark_missing_child_list_view_item, null);
            missingViewHolder = new MissingViewHolder(view, position);
            view.setTag(missingViewHolder);
            isMissing = true;
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return remarkParentObjectArrayList.get(i).getRemarkChildObjectArrayList().size();
    }

    @Override
    public RemarkChildObject getChild(int groupPosition, int childPosition) {
        Log.d("haha", "size: " + remarkParentObjectArrayList.size());
        return remarkParentObjectArrayList.get(groupPosition).getRemarkChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private class WeightViewHolder implements View.OnClickListener {
        TextView driver, customer, product, newWeight, weightStatus;
        TextView date, type;
        Button approve, edit;
        boolean hideButton = false;
        int position;

        WeightViewHolder(View view, int position) {
            driver = view.findViewById(R.id.remark_weight_child_list_view_item_driver);
            customer = view.findViewById(R.id.remark_weight_child_list_view_item_customer);
            product = view.findViewById(R.id.remark_weight_child_list_view_item_product);
            newWeight = view.findViewById(R.id.remark_weight_child_list_view_item_new_weight);
            weightStatus = view.findViewById(R.id.remark_weight_child_list_view_item_weight_status);
            type = view.findViewById(R.id.remark_weight_child_list_view_item_type);

            date = view.findViewById(R.id.remark_weight_child_list_view_item_time);
            approve = view.findViewById(R.id.remark_weight_child_list_view_item_approve);
            edit = view.findViewById(R.id.remark_weight_child_list_view_item_edit);
            this.position = position;
        }

        void bindMessage(RemarkChildObject object) {
            date.setText(object.getDate());

            product.setText(String.format("%s%s", object.getProduct(), String.format("(%s KG)", object.getRemark_type().equals("pick_up") ? object.getFarmer_weight() : object.getCustomer_weight())));
            newWeight.setText(String.format("%s KG", object.getRemark()));

            approve.setVisibility(object.getStatus().equals("1") ? View.GONE : View.VISIBLE);
            edit.setVisibility(object.getStatus().equals("1") ? View.VISIBLE : View.GONE);

            type.setText(object.getRemark_type().equals("deliver") ? "Delivery To" : "Pick Up From");
            type.setTextColor(object.getRemark_type().equals("deliver") ? context.getResources().getColor(R.color.blue) : context.getResources().getColor(R.color.green));

            driver.setText(object.getRemark_type().equals("pick_up") ? object.getPickUpDriver() : object.getDeliveryDriver());
            customer.setText(object.getRemark_type().equals("pick_up") ? object.getFarmer() : object.getCustomer());

            weightStatus.setText(object.getRemark_status().equals("2") ? " - Less" : " - More");
            weightStatus.setTextColor(object.getRemark_status().equals("2") ? context.getResources().getColor(R.color.red) : context.getResources().getColor(R.color.green));

            approve.setOnClickListener(this);
            edit.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.remark_weight_child_list_view_item_approve:
                    remarkAdapterCallBack.approved(position);
                    break;
                case R.id.remark_weight_child_list_view_item_edit:
                    remarkAdapterCallBack.edit(position);
                    break;
            }
        }
    }

    private class MissingViewHolder implements View.OnClickListener {
        TextView driver, customer, product, actionStatus;
        TextView date;
        Button approve, undo;
        int position;

        MissingViewHolder(View view, int position) {
            driver = view.findViewById(R.id.remark_missing_child_list_view_item_driver);
            customer = view.findViewById(R.id.remark_missing_child_list_view_item_customer);
            product = view.findViewById(R.id.remark_missing_child_list_view_item_product);
            actionStatus = view.findViewById(R.id.remark_missing_child_list_view_item_action);

            date = view.findViewById(R.id.remark_missing_child_list_view_item_time);
            approve = view.findViewById(R.id.remark_missing_child_list_view_item_approve);
            undo = view.findViewById(R.id.remark_missing_child_list_view_item_undo);
            this.position = position;
        }

        private void bindMessage(RemarkChildObject object) {
            product.setText(String.format("%s(%s KG)", object.getProduct(), object.getRemark_type().equals("deliver") ? object.getCustomer_weight() : object.getFarmer_weight()));
            driver.setText(object.getRemark_type().equals("deliver") ? object.getDeliveryDriver() : object.getPickUpDriver());
            customer.setText(object.getRemark_type().equals("deliver") ? object.getCustomer() : object.getFarmer());

            actionStatus.setText(object.getRemark_type().equals("pick_up") ? "Pick up from" : "Deliver to");
            actionStatus.setTextColor(object.getRemark_type().equals("deliver") ? context.getResources().getColor(R.color.blue) : context.getResources().getColor(R.color.green));

            approve.setVisibility(object.getStatus().equals("1") ? View.GONE : View.VISIBLE);
            undo.setVisibility(object.getStatus().equals("1") ? View.VISIBLE : View.GONE);

            date.setText(object.getDate());
            undo.setOnClickListener(this);
            approve.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.remark_missing_child_list_view_item_approve:
                    remarkAdapterCallBack.approved(position);
                    break;
                case R.id.remark_missing_child_list_view_item_undo:
                    remarkAdapterCallBack.undo(position);
                    break;
            }
        }
    }

    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    public interface RemarkAdapterCallBack {
        void approved(int position);
        void undo(int position);
        void edit(int position);
    }
}
