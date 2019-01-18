package com.jby.vegeapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.ProductObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.vegeapp.shareObject.ApiManager.domain;
import static com.jby.vegeapp.shareObject.ApiManager.prefix;
import static com.jby.vegeapp.shareObject.ApiManager.sub_prefix;

public class AddedProductAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductObject> productObjectArrayList;
    private AddedProductAdapterCallBack addedProductAdapterCallBack;

    public AddedProductAdapter(Context context, ArrayList<ProductObject> productObjectArrayList, AddedProductAdapterCallBack addedProductAdapterCallBack)
    {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.addedProductAdapterCallBack = addedProductAdapterCallBack;
    }

    @Override
    public int getCount() {
        return productObjectArrayList.size();
    }

    @Override
    public ProductObject getItem(int i) {
        return productObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.added_product_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final ProductObject object = getItem(i);
        String imagePath = new ApiManager().img_product + object.getPicture();

        viewHolder.name.setText(object.getName());
        viewHolder.quantity.setText(object.getQuantity());

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(viewHolder.picture);

        viewHolder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(viewHolder.quantity.getText().toString().trim());
                if(quantity>1)quantity--;
                viewHolder.quantity.setText("");
                viewHolder.quantity.append(String.valueOf(quantity));
                //update quantity
                addedProductAdapterCallBack.update(object, String.valueOf(quantity));
            }
        });

        viewHolder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(viewHolder.quantity.getText().toString().trim());
                quantity++;
                viewHolder.quantity.setText("");
                viewHolder.quantity.append(String.valueOf(quantity));
                //update quantity
                addedProductAdapterCallBack.update(object, String.valueOf(quantity));
            }
        });

        viewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                alertMessage("Warning", "Are you sure that you want to delete this item?", object.getId());
                return true;
            }
        });
        
        return view;
    }

    public void alertMessage(String title,String content, final String productID){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addedProductAdapterCallBack.delete(productID);
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

    public interface AddedProductAdapterCallBack{
        void update(ProductObject productObject, String quantity);
        void delete(String id);
    }


    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder{
        private CircleImageView picture;
        private TextView name, quantity;
        private ImageView minus, plus;
        private LinearLayout parentLayout;
        
        ViewHolder (View view){
            parentLayout = view.findViewById(R.id.add_product_dialog_list_view_item_parent_layout);
            picture = view.findViewById(R.id.add_product_dialog_list_view_item_picture);
            name = view.findViewById(R.id.add_product_dialog_list_view_item_name);
            minus = view.findViewById(R.id.add_product_dialog_list_view_item_minus);
            plus = view.findViewById(R.id.add_product_dialog_list_view_item_plus);
            quantity = view.findViewById(R.id.add_product_dialog_list_view_item_quantity);
        }
    }
}
