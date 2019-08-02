package com.jby.admin.adapter.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.StockObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;

    public ProductExpandableAdapter(Context context, ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList,
                                    ProductExpandableAdapterCallBack productExpandableAdapterCallBack) {
        this.context = context;
        this.productDetailParentObjectArrayList = productDetailParentObjectArrayList;
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
            convertView = layoutInflater.inflate(R.layout.product_detail_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        try {
            final ProductDetailParentObject object = getGroup(groupPosition);
            String imagePath = new ApiManager().product_img + object.getPicture();
            groupViewHolder.name.setText(object.getName());
            groupViewHolder.code.setText(object.getProduct_code());

            groupViewHolder.quantity.setText(String.format("x %s", setText(groupViewHolder.quantity, object.getAvailable_quantity())));
            setStatus(groupViewHolder.status, object.getAvailable_quantity());

            Picasso.get()
                    .load(imagePath)
                    .error(R.drawable.image_error)
                    .resize(70, 70)
                    .into(groupViewHolder.picture);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ProductDetailParentObject getGroup(int i) {
        try {
            return productDetailParentObjectArrayList.get(i);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void setStatus(TextView status, String value) {
        try {
            status.setText(Double.valueOf(value) > 0 ? "Available" : "Unavailable");
            status.setBackground(Double.valueOf(value) > 0 ? context.getResources().getDrawable(R.drawable.product_list_view_item_available) : context.getResources().getDrawable(R.drawable.product_list_view_item_unavailable));
        } catch (NullPointerException e) {
            status.setText("Unavailable");
            status.setBackground(context.getResources().getDrawable(R.drawable.product_list_view_item_unavailable));

        } catch (NumberFormatException e) {
            status.setText("Unavailable");
            status.setBackground(context.getResources().getDrawable(R.drawable.product_list_view_item_unavailable));
        }
    }

    private static class GroupViewHolder {
        private ImageView picture;
        private TextView code, name, quantity, weight, status;

        GroupViewHolder(View view) {
            picture = view.findViewById(R.id.product_parent_list_view_item_picture);
            code = view.findViewById(R.id.product_parent_list_view_item_code);
            name = view.findViewById(R.id.product_parent_list_view_item_name);
            quantity = view.findViewById(R.id.product_parent_list_view_item_total_quantity);
            weight = view.findViewById(R.id.product_parent_list_view_item_total_weight);
            status = view.findViewById(R.id.product_parent_list_view_item_status);
        }
    }

    @Override
    public int getGroupCount() {
        return productDetailParentObjectArrayList.size();
    }

    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.product_detail_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final StockObject object = getChild(groupPosition, childPosition);
        viewHolder.stockDate.setText(object.getDate());
        viewHolder.stockTotalQuantity.setText("Basket: " + setText(viewHolder.stockTotalQuantity, object.getTotalQuantity()));
        viewHolder.stockTotalWeight.setText("Weight: " + setText(viewHolder.stockTotalWeight, object.getTotalWeight()) + " KG");
        return view;
    }

    private String setText(TextView tv, String text) {
        try {
            if (Double.valueOf(text) > 0) {
                tv.setTextColor(context.getResources().getColor(R.color.green));
            } else tv.setTextColor(context.getResources().getColor(R.color.red));
            return text;
        } catch (NullPointerException e) {
            return "0";

        } catch (NumberFormatException e) {
            return "0";
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return productDetailParentObjectArrayList.get(i).getStockObjectArrayList().size();
    }

    @Override
    public StockObject getChild(int groupPosition, int childPosition) {
        return productDetailParentObjectArrayList.get(groupPosition).getStockObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        CardView parentLayout;
        TextView stockDate, stockTotalQuantity, stockTotalWeight;

        ChildViewHolder(View view) {
            parentLayout = view.findViewById(R.id.product_child_list_view_item_parent_layout);
            stockDate = view.findViewById(R.id.product_child_list_view_item_date);
            stockTotalQuantity = view.findViewById(R.id.product_child_list_view_item_stock_quantity);
            stockTotalWeight = view.findViewById(R.id.product_child_list_view_item_stock_weight);
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public interface ProductExpandableAdapterCallBack {
        void childOnClick(int position, int childPosition);
    }
}
