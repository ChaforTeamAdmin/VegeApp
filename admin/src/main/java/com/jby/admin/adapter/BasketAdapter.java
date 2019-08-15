package com.jby.admin.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.entity.BasketObject;

import java.util.ArrayList;


public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<BasketObject> basketObjectArrayList;

    public BasketAdapter(Context context, ArrayList<BasketObject> basketObjectArrayList) {
        this.context = context;
        this.basketObjectArrayList = basketObjectArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.basket_detail_list_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        final BasketObject object = basketObjectArrayList.get(position);
        viewHolder.name.setText(object.getName());
        viewHolder.quantity.setText(object.getQuantity());
        viewHolder.quantity.setTextColor(Integer.valueOf(object.getQuantity()) >= 0 ? context.getResources().getColor(R.color.green) : context.getResources().getColor(R.color.red));
    }


    @Override
    public int getItemCount() {
        return basketObjectArrayList.size();
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity;

        private MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.basket_detail_list_view_item_name);
            quantity = view.findViewById(R.id.basket_detail_list_view_item_quantity);
        }
    }
}
