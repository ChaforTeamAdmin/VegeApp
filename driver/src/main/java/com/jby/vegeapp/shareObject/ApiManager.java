package com.jby.vegeapp.shareObject;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ApiManager {
    private static String domain = "http://www.chafor.net/";
    private static String prefix = "vegetable/";
    private static String sub_prefix = "driver/";

    public String registration = domain + prefix + sub_prefix + "registration/registration.php";

    public String vege_manage = domain + prefix + sub_prefix + "vege_management/pick_up.php";
    public String deliver = domain + prefix + sub_prefix + "vege_management/deliver.php";
    public String delivery_remark = domain + prefix + sub_prefix + "vege_management/remark.php";
    public String basket = domain + prefix + sub_prefix + "vege_management/basket.php";

    public String history = domain + prefix + sub_prefix + "history/history.php";

    public String farmer = domain + prefix + "farmer/farmer.php";
    public String product = domain + prefix + "product/product.php";
    public String customer = domain + prefix + "customer/customer.php";

    //img path
    public String img_product = domain + prefix + "product/vege_img/";

    public String setData(ArrayList<ApiDataObject> apiDataObjectArrayList){
        String apiDataPost = "";
        String anApiDataPost = "";

        for (int position = 0 ; position < apiDataObjectArrayList.size() ; position++) {
            if (apiDataObjectArrayList.size() > 0){
                try {
                    anApiDataPost = URLEncoder.encode(apiDataObjectArrayList.get(position).getDataKey(), "UTF-8")
                                    + "=" +
                                    URLEncoder.encode(apiDataObjectArrayList.get(position).getDataContent(), "UTF-8");

                    apiDataPost += anApiDataPost;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiDataObjectArrayList.size() - 1))
                        apiDataPost += "&";
                }
            }
        }

        return apiDataPost;
    }

    /*
    *       Set Data <key>=<data> OR Model <model-name>[<key>]=<data>
    * */
    public String setModel(ArrayList<ApiModelObject> apiModelObjectArrayList){
        String apiModelPost = "";
        String anApiDataPost = "";

        /*
        *
        *       Build Post Data In Model Format
        *
        * */
        for (int position = 0 ; position < apiModelObjectArrayList.size() ; position++) {
            if (apiModelObjectArrayList.size() > 0){
                try {
                    anApiDataPost = URLEncoder.encode(apiModelObjectArrayList.get(position).getModelName(), "UTF-8")
                                    + URLEncoder.encode("[", "UTF-8")
                                    + URLEncoder.encode(apiModelObjectArrayList.get(position).getApiDataObject().getDataKey(), "UTF-8")
                                    + URLEncoder.encode("]", "UTF-8")
                                    + "="
                                    + URLEncoder.encode(apiModelObjectArrayList.get(position).getApiDataObject().getDataContent(), "UTF-8");

                    apiModelPost += anApiDataPost;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiModelObjectArrayList.size() - 1))
                        apiModelPost += "&";
                }
            }
        }

        return apiModelPost;
    }

    /*
    *       List Data Or Model
    * */
    public String setListModel(String Model, ArrayList<ArrayList<ApiDataObject>> apiListModelObjectArrayList){
        String apiListModelPost = "";

        String anApiModelPost = "";

        /*
        *
        *       Build Post Data In List Model Format
        *
        * */
        for (int position = 0 ; position < apiListModelObjectArrayList.size() ; position++) {
            if (apiListModelObjectArrayList.size() > 0){
                try {
                    for (int innerPosition = 0 ; innerPosition < apiListModelObjectArrayList.get(position).size() ; innerPosition++){
                        anApiModelPost = URLEncoder.encode(Model, "UTF-8")
                                        + URLEncoder.encode("[", "UTF-8")
                                        + position
                                        + URLEncoder.encode("]", "UTF-8")
                                        + URLEncoder.encode("[", "UTF-8")
                                        + URLEncoder.encode(apiListModelObjectArrayList.get(position).get(innerPosition).getDataKey(), "UTF-8")
                                        + URLEncoder.encode("]", "UTF-8")
                                        + "="
                                        + URLEncoder.encode(apiListModelObjectArrayList.get(position).get(innerPosition).getDataContent(), "UTF-8");

                        Log.i("Each Api", anApiModelPost);

                        apiListModelPost += anApiModelPost;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiListModelObjectArrayList.size() - 1))
                        apiListModelPost += "&";
                }
            }
        }

        return apiListModelPost;
    }

    /*
    *       Data OR Model OR List Model Joining
    * */
    public String getResultParameter(String data, String model, String listModel){

        if ((!data.equals("")) && (!model.equals("")) && (!listModel.equals("")))
            return data + "&" + model + "&" + listModel;

        else if ((!data.equals("")) && (!model.equals("")) && (listModel.equals("")))
            return data + "&" + model;
        else if ((!data.equals("")) && (model.equals("")) && (!listModel.equals("")))
            return data + "&" + listModel;
        else if ((data.equals("")) && (!model.equals("")) && (!listModel.equals("")))
            return model + "&" + listModel;

        else if ((!data.equals("")) && (model.equals("")) && (listModel.equals("")))
            return data;
        else if ((data.equals("")) && (!model.equals("")) && (listModel.equals("")))
            return model;
        else if ((data.equals("")) && (model.equals("")) && (!listModel.equals("")))
            return listModel;

        else
            return "";
    }
}
