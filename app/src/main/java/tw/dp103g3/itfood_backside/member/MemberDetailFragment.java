package tw.dp103g3.itfood_backside.member;

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


public class MemberDetailFragment extends Fragment {
    private Activity activity;
    private CommonTask memberGetAllTask;
    private CommonTask memberDeleteTask;
    private final static String TAG = "TAG_BookDetailsFragment";
    private ImageView ivMember, ivButton;
    private TextView tvMemberId, tvMemberState, tvMemberName, tvMemberPhone, tvMemberEmail, tvMemberJoinDate;
    private Member member,memberDetail,account;
    private Switch swMemberState;
    private Toolbar toolbarMemberDetail;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            return;
        }
        member = (Member) bundle.getSerializable("member");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textMemberDetailPageTitle);
        return inflater.inflate(R.layout.fragment_member_detail, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivMember = view.findViewById(R.id.ivMember);
        ivButton = view.findViewById(R.id.ivButton);
        tvMemberId = view.findViewById(R.id.tvMemberId);
        tvMemberState = view.findViewById(R.id.tvMemberState);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberPhone = view.findViewById(R.id.tvMemberPhone);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        tvMemberJoinDate = view.findViewById(R.id.tvMemberJoinDate);
        swMemberState = view.findViewById(R.id.swMemberState);
        toolbarMemberDetail = view.findViewById(R.id.toolbarMemberDetail);

        toolbarMemberDetail.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        swMemberState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showMember();
            }
        });
        showMember();
    }

    private void showMember() {

        int mem_id = member.getMemId();
        memberDetail = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                memberDetail = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        account = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAccount");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                account = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        tvMemberId.setText(String.valueOf(memberDetail.getMemId()));
        if (memberDetail.getMemState() == 0) {
            tvMemberState.setText("停權會員");
            tvMemberState.setTextColor(Color.RED);
            swMemberState.setChecked(false);
        } else if (memberDetail.getMemState() == 1) {
            tvMemberState.setText("有效會員");
            swMemberState.setChecked(true);
            tvMemberState.setTextColor(Color.BLUE);
        }

        tvMemberName.setText(memberDetail.getMemName());
        tvMemberPhone.setText(memberDetail.getMemPhone());
        tvMemberEmail.setText(memberDetail.getMemEmail());
        tvMemberJoinDate.setText(memberDetail.getMemJoindate());

        //設定蓋在Switch上的透明ImageView被按時,判斷Switch狀態並跳出對話框給使用這確認動作
        ivButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (memberDetail.getMemState() == 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將會員【" + memberDetail.getMemName() + "】的帳號 停用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是有效狀態時,對話框按確定的話要做的事情

                                        int mem_id = memberDetail.getMemId();
                                        int mem_state = 0;

                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/MemberServlet";
                                        account.Account(mem_id, mem_state);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("member", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textSuspendedMember);
                                            swMemberState.setChecked(false);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }

                                }
                            })

                            .show();
                } else if (memberDetail.getMemState() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("注意!!")
                            .setMessage("確定要將會員 【" + memberDetail.getMemName() + "】 的帳號 啟用 嗎？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按取消的話要做的事情
                                }
                            })
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //會員是停權狀態時,對話框按確定的話要做的事情
                                    int mem_id = memberDetail.getMemId();
                                    int mem_state = 1;
                                    account.Account(mem_id, mem_state);
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/MemberServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "saveAccount");
                                        jsonObject.addProperty("member", new Gson().toJson(account));

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
                                            Common.ShowToast(activity, R.string.textStartMember);
                                            swMemberState.setChecked(true);
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
