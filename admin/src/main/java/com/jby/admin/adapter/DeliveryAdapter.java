package com.jby.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.DeliveryOrderObject;
import com.jby.admin.object.ExpandableParentObject;

import java.util.ArrayList;

public class DeliveryAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ExpandableParentObject> expandableParentObjectArrayList;
    private boolean isMissing = true;

    public DeliveryAdapter(Context context, ArrayList<ExpandableParentObject> expandableParentObjectArrayList) {
        this.context = context;
        this.expandableParentObjectArrayList = expandableParentObjectArrayList;
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
        try {
            return expandableParentObjectArrayList.get(i);
        } catch (IndexOutOfBoundsException e) {
            return expandableParentObjectArrayList.get(i-1);
        }
    }

    private static class GroupViewHolder {
        TextView date;

        GroupViewHolder(View view) {
            date = view.findViewById(R.id.remark_parent_list_view_item_date);
        }
    }

    @Override
    public int getGroupCount() {
        return expandableParentObjectArrayList.size();
    }

    private static String getCurrentDate() {
        return String.valueOf(android.text.format.DateFormat.format("yyyy-MMM-dd", new java.util.Date()));
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.delivery_order_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final DeliveryOrderObject object = getChild(groupPosition, childPosition);

        viewHolder.do_id.setText(String.format("#%s%s", object.getPrefix(), setPlaceHolder(object.getId())));
        viewHolder.customer.setText(object.getCustomer());
        viewHolder.driver.setText(object.getDriver());

        viewHolder.print_status.setText(object.getPrint_status().equals("1") ? "Printed" : "Not Print Yet");
        viewHolder.print_status.setTextColor(object.getPrint_status().equals("1") ? context.getResources().getColor(R.color.blue) : context.getResources().getColor(R.color.green));

        viewHolder.status.setText(object.getStatus().equals("2") ? "Delivered" : "Processing");
        viewHolder.status.setTextColor(object.getStatus().equals("2") ? context.getResources().getColor(R.color.green) : context.getResources().getColor(R.color.blue));

        try {
            viewHolder.time.setText(object.getCreated_time().substring(0, object.getCreated_time().length() - 3));
        } catch (IndexOutOfBoundsException e) {
            viewHolder.time.setText(object.getCreated_time());
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public int getChildrenCount(int i) {
        return expandableParentObjectArrayList.get(i).getDeliveryOrderObjectArrayList().size();
    }

    @Override
    public DeliveryOrderObject getChild(int groupPosition, int childPosition) {
        return expandableParentObjectArrayList.get(groupPosition).getDeliveryOrderObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView do_id, customer, numItem, time, driver, status, print_status;

        ChildViewHolder(View view) {
            do_id = view.findViewById(R.id.delivery_order_child_list_view_item_do_id);
            customer = view.findViewById(R.id.delivery_order_child_list_view_item_customer);
            driver = view.findViewById(R.id.delivery_order_child_list_view_item_driver);
            status = view.findViewById(R.id.delivery_order_child_list_view_item_status);
            print_status = view.findViewById(R.id.delivery_order_child_list_view_item_print_status);
            time = view.findViewById(R.id.delivery_order_child_list_view_item_time);
        }
    }

    private String setPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return do_idBuilder.toString();
    }
    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }
}
