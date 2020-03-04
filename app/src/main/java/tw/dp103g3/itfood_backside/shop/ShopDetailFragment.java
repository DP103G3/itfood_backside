package tw.dp103g3.itfood_backside.shop;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood_backside.main.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.main.Url;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.ShopImageTask;


public class ShopDetailFragment extends Fragment {
    private final static String TAG = "TAG_ShopDetailsFragment";
    private Activity activity;
    private ImageView ivShop, ivButton;
    private TextView tvShopId, tvShopState, tvShopName,tvShopPhone, tvShopEmail, tvShopJoinDate
            , tvShopAddress, tvShopTax, tvShopArea, tvShopInfo;
    private Shop shop, shopDetail, account;
    private Switch swShopState;
    private Toolbar toolbarShopDetail;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("shop") == null) {
            return;
        }
        shop = (Shop) bundle.getSerializable("shop");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textShopDetailPageTitle);
        return inflater.inflate(R.layout.fragment_shop_detail, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivShop = view.findViewById(R.id.ivShop);
        tvShopId = view.findViewById(R.id.tvShopId);
        tvShopState = view.findViewById(R.id.tvShopState);
        tvShopName = view.findViewById(R.id.tvShopName);
        tvShopPhone = view.findViewById(R.id.tvShopPhone);
        tvShopEmail = view.findViewById(R.id.tvShopEmail);
        tvShopJoinDate = view.findViewById(R.id.tvShopJoinDate);
        tvShopAddress = view.findViewById(R.id.tvShopAddress);
        tvShopTax = view.findViewById(R.id.tvShopTax);
        tvShopArea = view.findViewById(R.id.tvShopArea);
        tvShopInfo = view.findViewById(R.id.tvShopInfo);
        swShopState = view.findViewById(R.id.swShopState);
        ivButton = view.findViewById(R.id.ivButton);
        toolbarShopDetail = view.findViewById(R.id.toolbarShopDetail);

        toolbarShopDetail.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        swShopState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showShop();
            }
        });
        showShop();
    }
    private void showShop() {
        String url = Url.URL + "/ShopServlet";
        int id = shop.getId();
        shopDetail = null;
        int imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        Bitmap bitmap = null;
        try {
            bitmap = new ShopImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivShop.setImageBitmap(bitmap);
        } else {
            ivShop.setImageResource(R.drawable.no_image);
        }
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getShopAllById");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                shopDetail = gson.fromJson(jsonIn, Shop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        account = null;
        if (Common.networkConnected(activity)) {
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAccount");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            CommonTask getShopTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getShopTask.execute().get();
                account = gson.fromJson(jsonIn, Shop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }


        tvShopId.setText(String.valueOf(shopDetail.getId()));
        if(shopDetail.getState() == 0){
            tvShopState.setText("未上架/下架店家");
            tvShopState.setTextColor(Color.RED);
            swShopState.setChecked(false);
        }else if(shopDetail.getState() == 1){
            tvShopState.setText("已上架店家");
            swShopState.setChecked(true);
            tvShopState.setTextColor(Color.BLUE);
        }
        tvShopName.setText(shopDetail.getName());
        tvShopPhone.setText(shopDetail.getPhone());
        tvShopEmail.setText(shopDetail.getEmail());
        tvShopJoinDate.setText(shopDetail.getJointime());
        tvShopAddress.setText(shopDetail.getAddress());
        tvShopTax.setText(shopDetail.getTax());
        tvShopArea.setText(String.valueOf(shopDetail.getArea()));
        tvShopInfo.setText(shopDetail.getInfo());
        //設定蓋在Switch上的透明ImageView被按時,判斷Switch狀態並跳出對話框給使用這確認動作
        ivButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (shopDetail.getState() == 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將店家【" + shopDetail.getName() + "】的帳號 停用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是有效狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是有效狀態時,對話框按確定的話要做的事情

                                    int id = shopDetail.getId();
                                    byte state = 0;

                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/ShopServlet";
                                        account.Account(id, state);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("shop", new Gson().toJson(account));

                                        int count = 0;
                                        try {
                                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.ShowToast(activity, R.string.textUpdateFail);
                                        } else {
                                            Common.ShowToast(activity, R.string.textSuspendedShop);
                                            swShopState.setChecked(false);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })

                            .show();
                } else if (shopDetail.getState() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將店家 【" + shopDetail.getName() + "】 的帳號 啟用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是停權狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //店家是停權狀態時,對話框按確定的話要做的事情
                                    int id = shopDetail.getId();
                                    byte state = 1;
                                    account.Account(id, state);
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/ShopServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("shop", new Gson().toJson(account));

                                        int count = 0;
                                        try {
                                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.ShowToast(activity, R.string.textUpdateFail);

                                        } else {
                                            Common.ShowToast(activity, R.string.textStartShop);
                                            swShopState.setChecked(true);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })
                            .show();
                }
            }
        });



    }

}
