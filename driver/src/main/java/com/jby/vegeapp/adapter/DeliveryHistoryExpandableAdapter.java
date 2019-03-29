package com.jby.vegeapp.adapter;

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

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.HistoryParentObject;
import com.jby.vegeapp.object.DeliveryHistoryObject;

import java.util.ArrayList;

public class DeliveryHistoryExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<HistoryParentObject> historyParentObjectArrayList;
    private DeliveryHistoryExpandableAdapterCallBack deliveryHistoryExpandableAdapterCallBack;

    public DeliveryHistoryExpandableAdapter(Context context, ArrayList<HistoryParentObject> historyParentObjectArrayList, DeliveryHistoryExpandableAdapterCallBack deliveryHistoryExpandableAdapterCallBack) {
        this.context = context;
        this.historyParentObjectArrayList = historyParentObjectArrayList;
        this.deliveryHistoryExpandableAdapterCallBack = deliveryHistoryExpandableAdapterCallBack;
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
            convertView = layoutInflater.inflate(R.layout.history_activity_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        final HistoryParentObject object = getGroup(groupPosition);
        groupViewHolder.date.setText(isToday(object.getDate()) ? "Today" : object.getDate());
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public HistoryParentObject getGroup(int i) {
        return historyParentObjectArrayList.get(i);
    }

    private static class GroupViewHolder {
        private TextView date;

        GroupViewHolder(View view) {
            date = view.findViewById(R.id.history_activity_parent_list_view_item_date);
        }
    }

    @Override
    public int getGroupCount() {
        return historyParentObjectArrayList.size();
    }

    private boolean isToday(String date) {
        return date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())));
    }

    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.delivery_history_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final DeliveryHistoryObject object = getChild(groupPosition, childPosition);

        viewHolder.do_id.setText(object.getDo_id());
        viewHolder.customer.setText(object.getCustomer());
        viewHolder.numItem.setText(String.format(" x %s Items", object.getQuantity()));
        viewHolder.time.setText(object.getCreated_time().substring(0, object.getCreated_time().length() - 3));
        viewHolder.do_id.setText(String.format("DO%s", setPlaceHolder(object.getDo_id())));
//        viewHolder.edit.setVisibility(isToday(object.getCreated_time()) ? View.VISIBLE : View.GONE);
        
        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deliveryHistoryExpandableAdapterCallBack.edit(childPosition);
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
        return historyParentObjectArrayList.get(i).getDeliveryHistoryObjectArrayList().size();
    }

    @Override
    public DeliveryHistoryObject getChild(int groupPosition, int childPosition) {
        return historyParentObjectArrayList.get(groupPosition).getDeliveryHistoryObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView do_id,customer, numItem, time, edit;

        ChildViewHolder(View view) {
            do_id = view.findViewById(R.id.delivery_history_child_list_view_item_do_id);
            customer = view.findViewById(R.id.delivery_history_child_list_view_item_customer);
            numItem = view.findViewById(R.id.delivery_history_child_list_view_item_num_item);
            time = view.findViewById(R.id.delivery_history_child_list_view_item_time);
            edit = view.findViewById(R.id.delivery_history_child_list_view_item_edit);
        }
    }

    private String setPlaceHolder(String do_id){
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for(int i = do_idBuilder.length(); i < 5; i++){
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

    public interface DeliveryHistoryExpandableAdapterCallBack {
        void edit(int position);
        void delete(int position);
    }
}
