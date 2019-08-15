package com.jby.admin.adapter.product;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.jby.admin.R;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapterCallBack productAdapterCallBack;

    public ProductAdapter(Context context, ArrayList<ProductObject> productObjectArrayList, ProductAdapterCallBack productAdapterCallBack) {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.productAdapterCallBack = productAdapterCallBack;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_dialog_list_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, final int position) {
        final ProductObject object = productObjectArrayList.get(position);
        String imagePath = new ApiManager().product_img + object.getPicture();
        viewHolder.name.setText(object.getName());

        viewHolder.code.setText(object.getProduct_code());
        viewHolder.code.setVisibility(object.getProduct_code() != null ? View.VISIBLE : View.GONE);

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(90, 90)
                .into(viewHolder.picture);

        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productAdapterCallBack.openAddProductDialog(productObjectArrayList.get(position));
            }
        });
    }


    @Override
    public int getItemCount() {
        return productObjectArrayList.size();
    }

    public interface ProductAdapterCallBack {
        void openAddProductDialog(ProductObject productObject);
    }

    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView picture;
        TextView name, code;
        CardView parent;

        private MyViewHolder(View view) {
            super(view);
            picture = view.findViewById(R.id.product_dialog_list_view_item_picture);
            name = view.findViewById(R.id.product_dialog_list_view_item_name);
            code = view.findViewById(R.id.product_dialog_list_view_item_code);
            parent = view.findViewById(R.id.product_dialog_list_view_item_parent_layout);
        }
    }
}
