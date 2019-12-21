package tw.dp103g3.itfood_backside;

import android.app.Activity;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;


import tw.dp103g3.itfood_backside.task.MemberImageTask;


public class MemberDetailFragment extends Fragment {
    private Activity activity;
    private final static String TAG = "TAG_BookDetailsFragment";
    private ImageView ivMember;
    private TextView tvMemberId, tvMemberState, tvMemberName,tvMemberPhone, tvMemberEmail, tvMemberJoinDate;
    private Member member;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
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
        tvMemberId = view.findViewById(R.id.tvMemberId);
        tvMemberState = view.findViewById(R.id.tvMemberState);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberPhone = view.findViewById(R.id.tvMemberPhone);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        tvMemberJoinDate = view.findViewById(R.id.tvMemberJoinDate);

        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("member") == null) {
            Common.showToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        member = (Member) bundle.getSerializable("member");
        showMember();
    }
    private void showMember() {
        String url = Common.URL_SERVER + "BookServlet";
        int id = member.getMemId();
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        Bitmap bitmap = null;
        try {
            bitmap = new MemberImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivMember.setImageBitmap(bitmap);
        } else {
            ivMember.setImageResource(R.drawable.img_member);
        }
        tvMemberId.setText(String.valueOf(member.getMemId()));
        tvMemberState.setText(member.getMemState());
        tvMemberName.setText(member.getMemName());
        tvMemberPhone.setText(member.getMemPhone());
        tvMemberEmail.setText(member.getMemEmail());
        tvMemberJoinDate.setText(member.getMemJoindate());


    }

}
