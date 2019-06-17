package com.jby.admin.adapter.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.others.CustomGridView;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

public class ProductExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList, unavailableProduct;

    private ArrayList<ProductDetailParentObject> stockControlArrayList;
    private ProductExpandableAdapterCallBack productExpandableAdapterCallBack;

    public ProductExpandableAdapter(Context context, ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList,
                                    ProductExpandableAdapterCallBack productExpandableAdapterCallBack) {
        this.context = context;
        this.productDetailParentObjectArrayList = productDetailParentObjectArrayList;
        this.productExpandableAdapterCallBack = productExpandableAdapterCallBack;
        stockControlArrayList = unavailableProduct = new ArrayList<>();

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

            groupViewHolder.status.setBackground(object.getAvailable_quantity().equals("0") ? context.getDrawable(R.drawable.product_list_view_item_unavailable) : context.getDrawable(R.drawable.product_list_view_item_available));
            groupViewHolder.status.setText(object.getAvailable_quantity().equals("0") ? R.string.product_adapter_stock_unavailable : R.string.product_adapter_stock_available);
            groupViewHolder.quantity.setVisibility(object.getAvailable_quantity().equals("0") ? View.INVISIBLE : View.VISIBLE);

            //taken
            if (!((MainActivity) context).getCustomerID().equals("-1")) {
                groupViewHolder.takenLayout.setVisibility(object.getTaken_quantity().equals("0") ? View.GONE : View.VISIBLE);
                groupViewHolder.statusLayout.setGravity(object.getTaken_quantity().equals("0") ? Gravity.END : Gravity.START);

            } else {
                groupViewHolder.takenLayout.setVisibility(View.GONE);
                groupViewHolder.statusLayout.setGravity(Gravity.END);
            }
            groupViewHolder.takenQuantity.setText(String.format(" x %s", object.getTaken_quantity()));
            groupViewHolder.quantity.setText(String.format(" x %s", object.getAvailable_quantity()));
            groupViewHolder.name.setText(object.getName());
            groupViewHolder.code.setText(object.getProduct_code());

            /*
             * unavailable layout (product)
             * */
            unavailableStock(groupViewHolder, object.getId());

            /*
             * parent stock control when searching or list refreshed
             * */
            stockControlPurpose(groupViewHolder, object.getId());

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

    private static class GroupViewHolder {
        private CardView parentLayout;
        private ImageView picture;
        private TextView code, name, status, quantity, takenQuantity;
        private LinearLayout statusLayout, takenLayout;

        GroupViewHolder(View view) {
            parentLayout = view.findViewById(R.id.product_parent_list_view_item_parent_layout);

            picture = view.findViewById(R.id.product_parent_list_view_item_picture);

            code = view.findViewById(R.id.product_parent_list_view_item_code);
            name = view.findViewById(R.id.product_parent_list_view_item_name);
            status = view.findViewById(R.id.product_parent_list_view_item_status);
            quantity = view.findViewById(R.id.product_parent_list_view_item_quantity);
            statusLayout = view.findViewById(R.id.product_parent_list_view_item_status_layout);
            takenLayout = view.findViewById(R.id.product_parent_list_view_item_taken_layout);

            takenQuantity = view.findViewById(R.id.product_parent_list_view_item_taken_quantity);
        }
    }

    @Override
    public int getGroupCount() {
        return productDetailParentObjectArrayList.size();
    }

    /*
     * when the product list is refresh or in searching this method will hold the user's selected item
     * */
    private void stockControlPurpose(GroupViewHolder groupViewHolder, String id) {
        if (stockControlArrayList.size() > 0) {
            for (int i = 0; i < stockControlArrayList.size(); i++) {
                if (stockControlArrayList.get(i).getId().equals(id)) {
                    groupViewHolder.status.setText(stockControlArrayList.get(i).getAvailable_quantity().equals("0") ? R.string.product_adapter_stock_unavailable : R.string.product_adapter_stock_available);
                    groupViewHolder.quantity.setVisibility(stockControlArrayList.get(i).getAvailable_quantity().equals("0") ? View.INVISIBLE : View.VISIBLE);
                    groupViewHolder.quantity.setText(String.format(" x %s", stockControlArrayList.get(i).getAvailable_quantity()));

                    groupViewHolder.takenQuantity.setText(String.format(" x %s", stockControlArrayList.get(i).getTaken_quantity()));
                    groupViewHolder.takenLayout.setVisibility(stockControlArrayList.get(i).getTaken_quantity().equals("0") ? View.GONE : View.VISIBLE);
                    groupViewHolder.statusLayout.setGravity(Gravity.START);
                    break;
                }
            }
        }
    }

    private void unavailableStock(GroupViewHolder groupViewHolder, String id) {
        if (unavailableProduct.size() > 0) {
            for (int i = 0; i < unavailableProduct.size(); i++) {
                if (unavailableProduct.get(i).getId().equals(id)) {
                    groupViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.light_red));
                    break;
                } else
                    groupViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        } else {
            groupViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        Handler handler;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.product_detail_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final ProductDetailChildObject object = getChild(groupPosition, childPosition);

        viewHolder.farmerName.setText(object.getFarmerName());
        viewHolder.availableQuantity.setText("A: " + object.getQuantity());
        //taken
        if (!((MainActivity) context).getCustomerID().equals("-1")) {
            viewHolder.takenQuantity.setVisibility(object.getTakenQuantity().equals("0") ? View.GONE : View.VISIBLE);
            viewHolder.takenQuantity.setText("T: " + object.getTakenQuantity());
        } else viewHolder.takenQuantity.setVisibility(View.GONE);

        /*
         * unavailable layout (farmer)
         * */
        childUnavailableFarmerList(viewHolder, object.getFarmerID(), productDetailParentObjectArrayList.get(groupPosition).getId());
        /*
         * child stock control when searching or list refreshed
         * */
        childStockControl(viewHolder, object.getFarmerID(), object.getTakenQuantity(), productDetailParentObjectArrayList.get(groupPosition).getId());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                productExpandableAdapterCallBack.childOnClick(childPosition, groupPosition);
                //prevent double click
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 200);
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return productDetailParentObjectArrayList.get(i).getProductDetailChildObjectArrayList().size();
    }

    @Override
    public ProductDetailChildObject getChild(int groupPosition, int childPosition) {
        return productDetailParentObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        CardView parentLayout;
        TextView farmerName, availableQuantity, takenQuantity;

        ChildViewHolder(View view) {
            parentLayout = view.findViewById(R.id.product_child_list_view_item_parent_layout);
            farmerName = view.findViewById(R.id.product_child_list_view_item_farmer_name);
            availableQuantity = view.findViewById(R.id.product_child_list_view_item__available_quantity);
            takenQuantity = view.findViewById(R.id.product_child_list_view_item_taken_quantity);
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    /*----------------------------------------------------------------------for unavailable list purpose-------------------------------------------------*/
    /*
     * unavailable product
     * */
    public void setUnavailableProduct(ArrayList<ProductDetailParentObject> unavailableProduct) {
        this.unavailableProduct = unavailableProduct;
    }

    /*
     * clear unavailable list
     * */
    public void clearUnavailableList() {
        unavailableProduct.clear();
    }

    /*
     * child unavailable list control
     * */
    private void childUnavailableFarmerList(ChildViewHolder childViewHolder, String farmerID, String productID) {
        if (unavailableProduct.size() > 0) {
            for (int i = 0; i < unavailableProduct.size(); i++) {
                if (unavailableProduct.get(i).getId().equals(productID)) {
                    /*
                     * get unavailable farmer based on unavailable product id
                     * */
                    String[] unavailableFarmer = unavailableProduct.get(i).getUnavailableFarmer();
                    /*
                     * if length > 0 then start loop
                     * */
                    if (unavailableFarmer.length > 0) {
                        for (String s : unavailableFarmer) {
                            if (s.equals(farmerID)) {
                                childViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.light_red));
                                break;
                            }
                        }
                    }
                    /*
                     * length <= 0 then set to white color
                     * */
                    else
                        childViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
                    break;
                }
                /*
                 * if not unavailable product id is not found then set white color as well
                 * */
                else
                    childViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        } else
            childViewHolder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
    }

    /*--------------------------------------------------------------------assign stock purpose-----------------------------------------------------------*/
    /*
     * this method use when select item from assign dialog for update the taken and available quantity
     * */
    public void setProductDetailParentObjectArrayList(ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList) {
        this.productDetailParentObjectArrayList = productDetailParentObjectArrayList;
        notifyDataSetChanged();
    }

    /*------------------------------------------------------------------stock control when the list is refrehsed------------------------------------------*/

    private void childStockControl(ChildViewHolder childViewHolder, String farmerId, String takenQuantity, String productID) {
        for (int j = 0; j < stockControlArrayList.size(); j++) {
            if (stockControlArrayList.get(j).getId().equals(productID)) {

                ArrayList<ProductDetailChildObject> childArray = stockControlArrayList.get(j).getProductDetailChildObjectArrayList();
                for (int i = 0; i < childArray.size(); i++) {
                    if (childArray.get(i).getFarmerID().equals(farmerId)) {
                        childViewHolder.takenQuantity.setVisibility(childArray.get(i).getTakenQuantity().equals("0") ? View.GONE : View.VISIBLE);
                        childViewHolder.takenQuantity.setText(String.format(" x %s", childArray.get(i).getTakenQuantity()));
                        childViewHolder.availableQuantity.setText(String.format(" x %s", childArray.get(i).getQuantity()));
                        break;
                    } else
                        childViewHolder.takenQuantity.setText(String.format(" x %s", takenQuantity));
                }
            }
        }
    }


    public void setStockControlArrayList(ArrayList<ProductDetailParentObject> stockControlArrayList) {
        this.stockControlArrayList = stockControlArrayList;
    }

    public void clearStockControlArrayList() {
        this.stockControlArrayList.clear();
    }


    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public interface ProductExpandableAdapterCallBack {
        void childOnClick(int position, int childPosition);
    }
}
