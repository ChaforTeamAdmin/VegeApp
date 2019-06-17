package com.jby.admin.adapter.stock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;

import java.util.ArrayList;
import java.util.List;

public class ProductGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList;
    private ProductGridAdapterCallBack productGridAdapterCallBack;
    private String fromWhere;
    private String[] unavailableIDList;
    /*
     ** for delete purpose
     */
    private SparseBooleanArray deleteItem;

    public ProductGridAdapter(Context context, ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList,
                              ProductGridAdapterCallBack productGridAdapterCallBack, String fromWhere) {
        this.context = context;
        this.productDetailChildObjectArrayList = productDetailChildObjectArrayList;
        this.productGridAdapterCallBack = productGridAdapterCallBack;
        deleteItem = new SparseBooleanArray();
        this.fromWhere = fromWhere;
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
        /*
         * open from stock fragment
         * */
        if (fromWhere.equals("stock_fragment")) {
            try {
                //date purpose
                viewHolder.grade.setBackgroundColor(isToday(object.getDate()) ? context.getResources().getColor(R.color.green) : context.getResources().getColor(R.color.red));
                //ticked purpose
                viewHolder.tickedLayout.setVisibility(object.getStatus().equals("1") || object.getStatus().equals("2") ? View.VISIBLE : View.GONE);
            } catch (NullPointerException e) {
                //ticked purpose
                viewHolder.tickedLayout.setVisibility(View.GONE);
            }
        }
        //weight
        viewHolder.weight.setText(getWeight(object.getWeight(), object.getSelfAbsorbWeight()));
        //grade purpose
        setGradeColor(object.getGrade(), viewHolder.parentLayout);
        //delete and spoil purpose
        if (deleteItem.size() > 0)
            viewHolder.tickedLayout.setVisibility(deleteItem.get(i) ? View.VISIBLE : View.GONE);
        //set unavailable stock
        if (unavailableIDList != null)
            setUnavailableStock(viewHolder, object.getId());
        return view;
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/
    private String getWeight(String sFarmerWeight, String sSelfAbsorbWeight) {
        /*
         * since self absorb wont deduce the farmer weight so we deduce it here for display purpose
         * */
        try {
            double farmerWeight = Double.valueOf(sFarmerWeight);
            double selfAbsorbWeight = Double.valueOf(sSelfAbsorbWeight);
            if (selfAbsorbWeight > 0) return String.valueOf(farmerWeight - selfAbsorbWeight);
        } catch (NumberFormatException e) {
            return sFarmerWeight;
        } catch (NullPointerException e) {
            return sFarmerWeight;
        }
        return sFarmerWeight;
    }

    private void setGradeColor(String grade, View view) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(context.getResources().getColor(R.color.white));
        gradientDrawable.setCornerRadius(15);
        switch (grade) {
            case "A":
                gradientDrawable.setStroke(10, context.getResources().getColor(R.color.purple));
                break;
            case "B":
                gradientDrawable.setStroke(10, context.getResources().getColor(R.color.sky_blue));
                break;
            case "FA":
                gradientDrawable.setStroke(10, context.getResources().getColor(R.color.peach));
                break;
            case "FB":
                gradientDrawable.setStroke(10, context.getResources().getColor(R.color.grey));
                break;
            default:
                gradientDrawable.setStroke(10, context.getResources().getColor(R.color.red));
                break;
        }
        view.setBackground(gradientDrawable);
    }

    public void tick(View view) {
        if (view.getVisibility() == View.GONE) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }

    private boolean isToday(String date) {
        return date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())));
    }

    private void setUnavailableStock(ViewHolder view, String id) {
        for (String s : unavailableIDList) {
            if (s.equals(id)) {
                view.tickIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.warning_icon));
                break;
            } else {
                view.tickIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tick_icon));
            }
        }
    }

    private static class ViewHolder {
        private TextView weight;
        private View grade;
        private CardView parentLayout;
        private LinearLayout tickedLayout;
        private ImageView tickIcon;

        ViewHolder(View view) {
            weight = view.findViewById(R.id.product_detail_child_list_view_weight);
            grade = view.findViewById(R.id.product_detail_child_list_view_grade);
            parentLayout = view.findViewById(R.id.product_detail_child_list_view_parent_layout);
            tickedLayout = view.findViewById(R.id.product_detail_child_list_view_ticked_layout);
            tickIcon = view.findViewById(R.id.product_detail_child_list_view_tick_icon);
        }
    }

    public interface ProductGridAdapterCallBack {
        void assignItem(int position, String status, String do_id);

        void removeItem(int position);
    }

    /*
     * this array is used when a stock is crash with other admin while uploading
     * */
    public void setUnavailableIDList(String[] unavailableIDList) {
        this.unavailableIDList = unavailableIDList;
    }

    /*
     * multiple delete or remark purpose
     * */
    /*-------------------------------------------------------------------delete purpose-------------------------------------------------------------------*/
    public void remove(int position) {
        productDetailChildObjectArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !deleteItem.get(position));
    }

    // Remove selection after unchecked
    public void removeSelection() {
        deleteItem = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    private void selectView(int position, boolean value) {
        if (value) {
            deleteItem.put(position, true);
        } else {
            deleteItem.delete(position);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return deleteItem;
    }

}
