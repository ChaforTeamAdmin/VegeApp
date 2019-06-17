package com.jby.vegeapp.adapter.history;

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
import com.jby.vegeapp.object.history.BasketHistoryObject;
import com.jby.vegeapp.object.history.HistoryParentObject;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;


import java.util.ArrayList;

public class BasketHistoryExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<HistoryParentObject> historyParentObjectArrayList;
    private BasketHistoryExpandableAdapterCallBack basketHistoryExpandableAdapterCallBack;

    public BasketHistoryExpandableAdapter(Context context, ArrayList<HistoryParentObject> historyParentObjectArrayList, BasketHistoryExpandableAdapterCallBack basketHistoryExpandableAdapterCallBack) {
        this.context = context;
        this.historyParentObjectArrayList = historyParentObjectArrayList;
        this.basketHistoryExpandableAdapterCallBack = basketHistoryExpandableAdapterCallBack;
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
            view = layoutInflater.inflate(R.layout.basket_history_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final BasketHistoryObject object = getChild(groupPosition, childPosition);
        viewHolder.label_action.setText(setMessage(object, true));
        viewHolder.target.setText(setMessage(object, false));
        viewHolder.basket.setTextColor(Integer.valueOf(object.getQuantity()) > 0 ? context.getColor(R.color.green) : context.getColor(R.color.red));
        viewHolder.basket.setText(object.getQuantity());

        try {
            viewHolder.time.setText(object.getCreated_time().substring(0, object.getCreated_time().length() - 3));
        } catch (IndexOutOfBoundsException e) {
            viewHolder.time.setText(object.getCreated_time());
        }
        /*
        * if user type = 0(normal user)
        * */
        if (SharedPreferenceManager.getUserType(context).equals("0")){
            viewHolder.edit.setVisibility(isToday(object.getCreated_date()) ? View.VISIBLE : View.GONE);
            viewHolder.delete.setVisibility(isToday(object.getCreated_date()) ? View.VISIBLE : View.GONE);
        }

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                basketHistoryExpandableAdapterCallBack.edit(childPosition);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                basketHistoryExpandableAdapterCallBack.delete(childPosition);
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
        return historyParentObjectArrayList.get(i).getBasketHistoryObjectArrayList().size();
    }

    @Override
    public BasketHistoryObject getChild(int groupPosition, int childPosition) {
        return historyParentObjectArrayList.get(groupPosition).getBasketHistoryObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView basket, target, time, edit, label_action, delete;

        ChildViewHolder(View view) {
            label_action = view.findViewById(R.id.basket_history_child_list_view_item_label_action);
            basket = view.findViewById(R.id.basket_history_child_list_view_item_num_basket);
            time = view.findViewById(R.id.basket_history_child_list_view_item_time);
            target = view.findViewById(R.id.basket_history_child_list_view_item_target);
            edit = view.findViewById(R.id.basket_history_child_list_view_item_edit);
            delete = view.findViewById(R.id.basket_history_child_list_view_item_delete);
        }
    }

    private String setMessage(BasketHistoryObject object, boolean getTarget) {
        switch (object.getType()) {
            case "1":
                return (getTarget ? "Basket From" : " Admin");
            case "2":
                return (getTarget ? "Basket From" : " Admin");
            case "3":
                return (getTarget ? "Basket To " : object.getFarmer());
            case "4":
                return (getTarget ? "Basket From " : object.getFarmer());
            case "5":
                return (getTarget ? "Basket To" : object.getCustomer());
            case "6":
                return (getTarget ? "Basket From " : object.getCustomer());
        }
        return "--";
    }

    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    public interface BasketHistoryExpandableAdapterCallBack {
        void edit(int position);

        void delete(int position);
    }
}
