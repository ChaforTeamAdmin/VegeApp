package com.jby.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;

import java.util.ArrayList;


public class ProductGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList;
    private ProductGridAdapterCallBack productGridAdapterCallBack;
    private String deliveryOrderId = "0";

    public ProductGridAdapter(Context context, ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList, ProductGridAdapterCallBack productGridAdapterCallBack) {
        this.context = context;
        this.productDetailChildObjectArrayList = productDetailChildObjectArrayList;
        this.productGridAdapterCallBack = productGridAdapterCallBack;
    }

    @Override
    public int getCount() {
        return productDetailChildObjectArrayList.size();
    }

    @Override
    public ProductDetailChildObject getItem(int i) {
        return productDetailChildObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.product_detail_grid_child_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final ProductDetailChildObject object = getItem(i);
        //check delivery order is created or not
        deliveryOrderId = (!object.getDo_id().equals("0") ? object.getDo_id() : "0");

        viewHolder.weight.setText(object.getWeight());
        //date purpose
        viewHolder.grade.setBackgroundColor(isToday(object.getDate()) ? context.getResources().getColor(R.color.green) : context.getResources().getColor(R.color.red));
        //ticked purpose
        viewHolder.tickedLayout.setVisibility(object.getStatus().equals("1") ? View.VISIBLE : View.GONE);
        //warning icon
        viewHolder.warningIcon.setVisibility(object.getDeliveryRemarkStatus().equals("1") || object.getPickUpRemarkStatus().equals("1")  ||
                object.getDeliveryRemarkStatus().equals("2") || object.getPickUpRemarkStatus().equals("2")
                ? View.VISIBLE : View.GONE);
        //grade purpose
        viewHolder.parentLayout.setBackground(object.getGrade().equals("A") ? context.getResources().getDrawable(R.drawable.product_list_custom_linear_layout_grade_a) :
                context.getResources().getDrawable(R.drawable.product_list_custom_linear_layout_grade_unknow));

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainActivity) context).getCustomerID().equals("-1"))
                    Toast.makeText(context, "Please select a customer!", Toast.LENGTH_SHORT).show();
                else {
                    //new item
                    if(object.getDo_id().equals("0")){
                        if (viewHolder.warningIcon.getVisibility() == View.VISIBLE) {
                            remarkDialog();
                        } else {
                            tick(viewHolder.tickedLayout);
                            productGridAdapterCallBack.assignItem(i, getStatus(object.getStatus()), deliveryOrderId);
                            object.setStatus(getStatus(object.getStatus()));
                        }
                    }
                    //edit item
                    else editDialog(i, viewHolder.tickedLayout);
                }
            }
        });
        return view;
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private void tick(View view) {
        if (view.getVisibility() == View.GONE) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }

    private boolean isToday(String date) {
        return date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())));
    }

    private String getStatus(String status) {
        if (status.equals("0")) return "1";
        else return "0";
    }

    private void remarkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Note");
        builder.setMessage("This item is remarked by driver. Please take a look now!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "View",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        ((MainActivity) context).displaySelectedScreen(R.id.navigation_remark);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void editDialog(final int position, final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Note");
        builder.setMessage("Remove this item may brings effect to the delivery order. Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        productGridAdapterCallBack.removeItem(position);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static class ViewHolder {
        private TextView weight;
        private View grade;
        private CardView parentLayout;
        private LinearLayout tickedLayout;
        private ImageView warningIcon;

        ViewHolder(View view) {
            weight = view.findViewById(R.id.product_detail_child_list_view_weight);
            grade = view.findViewById(R.id.product_detail_child_list_view_grade);
            parentLayout = view.findViewById(R.id.product_detail_child_list_view_parent_layout);
            tickedLayout = view.findViewById(R.id.product_detail_child_list_view_ticked_layout);
            warningIcon = view.findViewById(R.id.product_detail_child_list_view_warning_icon);
        }
    }

    public interface ProductGridAdapterCallBack {
        void assignItem(int position, String status, String do_id);
        void removeItem(int position);
    }
}
