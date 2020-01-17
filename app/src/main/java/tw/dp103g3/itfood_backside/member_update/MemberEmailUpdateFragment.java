package tw.dp103g3.itfood_backside.member_update;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood_backside.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.Url;
import tw.dp103g3.itfood_backside.member.Member;
import tw.dp103g3.itfood_backside.task.CommonTask;


public class MemberEmailUpdateFragment extends Fragment {
    private static final String TAG = "TAG_MemberNameUpdateFragment";
    private Activity activity;
    private Member member;
    private EditText etMemberEmail;
    private Button btOk;
    private TextView tvWarning;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity.setTitle(R.string.textMemberEmailUpdateTitle);
        return inflater.inflate(R.layout.fragment_member_email_update, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etMemberEmail = view.findViewById(R.id.etMemberEmail);
        btOk = view.findViewById(R.id.btOk);
        tvWarning = view.findViewById(R.id.tvWarning);

        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.ShowToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        member = (Member) bundle.getSerializable("member");
        btOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(final View view) {
                int id = member.getMemId();
                String password = member.getMemPassword();
                String email = etMemberEmail.getText().toString();
                String phone = member.getMemPhone();
                int state = member.getMemState();
                String name = member.getMemName();
                if (name.length() <= 0) {
                    Common.ShowToast(activity, R.string.textNameIsInvalid);
                    tvWarning.setText("請輸入信箱！");
                    return;
                }else {
                    tvWarning.setText("");
                }

                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/MemberServlet";
                    member.Update(id, name, password, email, phone, state);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "update");
                    jsonObject.addProperty("member", new Gson().toJson(member));
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        //Common.ShowToast(activity, R.string.textUpdateFail);
                    } else {
                        Common.ShowToast(activity, R.string.textUpdateSuccess);
                        navController.popBackStack();
                    }
                } else {
                    Common.ShowToast(activity, R.string.textNoNetwork);
                }


            }

        });


        showMember();
    }

    private void showMember(){
        etMemberEmail.setText(member.getMemEmail());
    }

}