package tw.dp103g3.itfood_backside.delivery;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
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


public class DeliveryDetailFragment extends Fragment {
    private Activity activity;
    private CommonTask deliveryGetAllTask;
    private CommonTask deliveryDeleteTask;
    private final static String TAG = "TAG_DeliveryDetailFragment";
    private ImageView ivDelivery, ivButton, ivButtonLeave;
    private TextView tvDeliveryId, tvDeliveryState, tvDeliveryName, tvDeliveryPhone, tvDeliveryEmail, tvDeliveryJoinDate, tvDeliveryArea, tvDeliveryIdentityId;
    private Delivery delivery, deliveryDetail, account;
    private Switch swDeliveryState, swDeliveryLeaveState;
    private Toolbar toolbarDeliveryDetail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("delivery") == null) {
            return;
        }
        delivery = (Delivery) bundle.getSerializable("delivery");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_detail, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivButton = view.findViewById(R.id.ivButton);
        ivButtonLeave = view.findViewById(R.id.ivButtonLeave);
        tvDeliveryId = view.findViewById(R.id.tvDeliveryId);
        tvDeliveryState = view.findViewById(R.id.tvDeliveryState);
        tvDeliveryName = view.findViewById(R.id.tvDeliveryName);
        tvDeliveryPhone = view.findViewById(R.id.tvDeliveryPhone);
        tvDeliveryEmail = view.findViewById(R.id.tvDeliveryEmail);
        tvDeliveryJoinDate = view.findViewById(R.id.tvDeliveryJoinDate);
        tvDeliveryArea = view.findViewById(R.id.tvDeliveryArea);
        tvDeliveryIdentityId = view.findViewById(R.id.tvDeliveryIdentityId);
        swDeliveryState = view.findViewById(R.id.swDeliveryState);
        swDeliveryLeaveState = view.findViewById(R.id.swDeliveryLeaveState);
        toolbarDeliveryDetail = view.findViewById(R.id.toolbarDeliveryDetail);

        toolbarDeliveryDetail.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        swDeliveryState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showDelivery();
            }
        });
        swDeliveryLeaveState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showDelivery();
            }
        });
        showDelivery();
    }

    private void showDelivery() {

        int del_id = delivery.getDelId();
        deliveryDetail = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DeliveryServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("del_id", del_id);
            String jsonOut = jsonObject.toString();
            CommonTask getDeliveryTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getDeliveryTask.execute().get();
                deliveryDetail = gson.fromJson(jsonIn, Delivery.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        account = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DeliveryServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAccount");
            jsonObject.addProperty("del_id", del_id);
            String jsonOut = jsonObject.toString();
            CommonTask getDeliveryTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getDeliveryTask.execute().get();
                account = gson.fromJson(jsonIn, Delivery.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        tvDeliveryId.setText(String.valueOf(deliveryDetail.getDelId()));
        if (deliveryDetail.getDelState() == 2 ) {
            swDeliveryLeaveState.setText("離職外送員");
            swDeliveryLeaveState.setTextColor(Color.RED);
            swDeliveryLeaveState.setChecked(true);
        } else{
            swDeliveryLeaveState.setText("在職外送員");
            swDeliveryLeaveState.setTextColor(Color.BLUE);
            swDeliveryLeaveState.setChecked(false);
        }

        if (deliveryDetail.getDelState() == 1) {
            swDeliveryState.setText("有效外送員");
            swDeliveryState.setChecked(true);
            swDeliveryState.setTextColor(Color.BLUE);
        } else if (deliveryDetail.getDelState() == 0) {
            swDeliveryState.setText("停權外送員");
            swDeliveryState.setChecked(false);
            swDeliveryState.setTextColor(Color.RED);
        } else {
            swDeliveryState.setText("停權外送員");
            swDeliveryState.setChecked(false);
            swDeliveryState.setTextColor(Color.RED);
        }

        tvDeliveryName.setText(deliveryDetail.getDelName());
        tvDeliveryPhone.setText(deliveryDetail.getDelPhone());
        tvDeliveryEmail.setText(deliveryDetail.getDelEmail());
        tvDeliveryJoinDate.setText(deliveryDetail.getDelJoindate());
        tvDeliveryIdentityId.setText(deliveryDetail.getDelIdentityid());
        tvDeliveryArea.setText(String.valueOf(deliveryDetail.getDelArea()));

        //設定蓋在Switch上的透明ImageView被按時,判斷Switch狀態並跳出對話框給使用這確認動作
        ivButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (deliveryDetail.getDelState() == 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將外送員【" + deliveryDetail.getDelName() + "】的帳號 停用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @SuppressLint("LongLogTag")
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按確定的話要做的事情

                                    int del_id = deliveryDetail.getDelId();
                                    int del_state = 0;

                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DeliveryServlet";
                                        account.Account(del_id, del_state);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("delivery", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textSuspendedDelivery);
                                            swDeliveryState.setChecked(false);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })

                            .show();
                } else if (deliveryDetail.getDelState() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將外送員 【" + deliveryDetail.getDelName() + "】 的帳號 啟用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @SuppressLint("LongLogTag")
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按確定的話要做的事情
                                    int del_id = deliveryDetail.getDelId();
                                    int del_state = 1;
                                    account.Account(del_id, del_state);
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DeliveryServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("delivery", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textStartDelivery);
                                            swDeliveryState.setChecked(true);
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

        //設定蓋在Switch上的透明ImageView被按時,判斷Switch狀態並跳出對話框給使用這確認動作
        ivButtonLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (deliveryDetail.getDelState() == 0 || deliveryDetail.getDelState() == 1 ) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要讓外送員【" + deliveryDetail.getDelName() + "】離職 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @SuppressLint("LongLogTag")
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按確定的話要做的事情

                                    int del_id = deliveryDetail.getDelId();
                                    int del_state = 2;

                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DeliveryServlet";
                                        account.Account(del_id, del_state);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("delivery", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textLeaveDelivery);
                                            swDeliveryLeaveState.setChecked(true);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })

                            .show();
                } else if (deliveryDetail.getDelState() == 2) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要讓外送員 【" + deliveryDetail.getDelName() + "】 復職 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @SuppressLint("LongLogTag")
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按確定的話要做的事情
                                    int del_id = deliveryDetail.getDelId();
                                    int del_state = 0;
                                    account.Account(del_id, del_state);
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/DeliveryServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("delivery", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textStartDeliveryOk);
                                            swDeliveryLeaveState.setChecked(false);
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

