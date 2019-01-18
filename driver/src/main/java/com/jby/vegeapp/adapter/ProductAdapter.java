package com.jby.vegeapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.object.FarmerObject;
import com.jby.vegeapp.object.ProductObject;
import com.jby.vegeapp.pickUp.product.ProductDialog;
import com.jby.vegeapp.shareObject.ApiManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.vegeapp.shareObject.ApiManager.domain;
import static com.jby.vegeapp.shareObject.ApiManager.prefix;
import static com.jby.vegeapp.shareObject.ApiManager.sub_prefix;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapterCallBack productAdapterCallBack;

    public ProductAdapter(Context context, ArrayList<ProductObject> productObjectArrayList, ProductAdapterCallBack productAdapterCallBack)
    {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.productAdapterCallBack = productAdapterCallBack;
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
            view = View.inflate(this.context, R.layout.product_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final ProductObject object = getItem(i);
        String imagePath = new ApiManager().img_product + object.getPicture();

        viewHolder.name.setText(object.getName());
        viewHolder.quantity.setText("0");

        Picasso.get()
                .load(imagePath)
                .error(R.drawable.image_error)
                .resize(100, 100)
                .into(viewHolder.picture);

        viewHolder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(viewHolder.quantity.getText().toString().trim());
                if(quantity>0)quantity--;
                viewHolder.quantity.setText("");
                viewHolder.quantity.append(String.valueOf(quantity));

                if(quantity > 0) {
                    viewHolder.add.setEnabled(true);
                    viewHolder.add.setBackground(context.getDrawable(R.drawable.custom_button));
                }
                else {
                    viewHolder.add.setEnabled(false);
                    viewHolder.add.setBackground(context.getDrawable(R.drawable.custom_disable_button));
                }
            }
        });

        viewHolder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(viewHolder.quantity.getText().toString().trim());
                quantity++;
                viewHolder.quantity.setText("");
                viewHolder.quantity.append(String.valueOf(quantity));

                if(quantity > 0) {
                    viewHolder.add.setEnabled(true);
                    viewHolder.add.setBackground(context.getDrawable(R.drawable.custom_button));
                }

            }
        });

        viewHolder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productAdapterCallBack.add(object, viewHolder.quantity.getText().toString().trim());
                //reset quantity
                viewHolder.quantity.setText("");
                viewHolder.quantity.append("0");
                //reset
                viewHolder.add.setEnabled(false);
                viewHolder.add.setBackground(context.getDrawable(R.drawable.custom_disable_button));
            }
        });
        return view;
    }

    public interface ProductAdapterCallBack{
        void add(ProductObject productObject, String quantity);
    }


    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder{
        private CircleImageView picture;
        private TextView name;
        private ImageView minus, plus;
        private EditText quantity;
        private Button add;

        ViewHolder (View view){
            picture = view.findViewById(R.id.product_dialog_list_view_item_picture);
            name = view.findViewById(R.id.product_dialog_list_view_item_name);
            minus = view.findViewById(R.id.product_dialog_list_view_item_minus);
            plus = view.findViewById(R.id.product_dialog_list_view_item_plus);
            quantity = view.findViewById(R.id.product_dialog_list_view_item_quantity);
            add = view.findViewById(R.id.product_dialog_list_view_item_add);
        }
    }
}
