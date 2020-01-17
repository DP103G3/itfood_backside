package tw.dp103g3.itfood_backside.member_update;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.icu.util.Output;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g3.itfood_backside.Address;
import tw.dp103g3.itfood_backside.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.Url;
import tw.dp103g3.itfood_backside.member.Member;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.MemberImageTask;


public class MemberUpdateFragment extends Fragment {
    private static final String TAG = "TAG_MemberUpdateFragment";
    private Activity activity;
    private Member memberId, memberInfo;
    private Address memberAddress;
    private CommonTask memberGetAllTask;
    private CommonTask memberDeleteTask;
    private TextView tvMemberName ,tvMemberPhone ,tvMemberEmail ,tvMemberAddress ,tvMemberCard;
    private ImageView ivMemberName ,ivMemberPhone ,ivMemberEmail ,ivMemberAddress ,ivMemberCard;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity.setTitle(R.string.textMemberUpdateTitle);
        return inflater.inflate(R.layout.fragment_member_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberPhone = view.findViewById(R.id.tvMemberPhone);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        tvMemberAddress = view.findViewById(R.id.tvMemberAddress);
        tvMemberCard = view.findViewById(R.id.tvMemberCard);
        ivMemberName = view.findViewById(R.id.ivMemberName);
        ivMemberPhone = view.findViewById(R.id.ivMemberPhone);
        ivMemberEmail = view.findViewById(R.id.ivMemberEmail);
        ivMemberAddress = view.findViewById(R.id.ivMemberAddress);
        ivMemberCard = view.findViewById(R.id.ivMemberCard);

        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.ShowToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        memberId = (Member) bundle.getSerializable("member");

        showMember();
    }



    @SuppressLint("LongLogTag")
    private void showMember() {
        int mem_id = memberId.getMemId();
        memberInfo = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id" , mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                memberInfo = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        memberAddress = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/AddressServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gsonAddress = new GsonBuilder().create();
            jsonObject.addProperty("action", "getAll");
            jsonObject.addProperty("mem_id" , mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getAddressTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAddressTask.execute().get();
                memberAddress = gsonAddress.fromJson(jsonIn, Address.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }

        tvMemberName.setText(memberInfo.getMemName());
        tvMemberPhone.setText(memberInfo.getMemPhone());
        tvMemberEmail.setText(memberInfo.getMemEmail());
        if (memberAddress == null){

        }else {
            tvMemberAddress.setText(memberAddress.getInfo());
        }

        //tvMemberCard.setText(memberInfo.getMemEmail());
        ivMemberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_memberUpdateFragment_to_memberNameUpdateFragment, bundle);

            }
        });
        ivMemberPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_memberUpdateFragment_to_memberPhoneUpdateFragment, bundle);

            }
        });
        ivMemberEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", memberInfo);
                Navigation.findNavController(view)
                        .navigate(R.id.action_memberUpdateFragment_to_memberEmailUpdateFragment, bundle);

            }
        });
        ivMemberAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


            }
        });
        ivMemberCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


            }
        });

    }


}
