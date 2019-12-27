package tw.dp103g3.itfood_backside;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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

import tw.dp103g3.itfood_backside.task.ShopImageTask;


public class ShopDetailFragment extends Fragment {
    private Activity activity;
    private final static String TAG = "TAG_ShopDetailsFragment";
    private ImageView ivMember;
    private TextView tvMemberId, tvMemberState, tvMemberName,tvMemberPhone, tvMemberEmail, tvMemberJoinDate;
    private Shop shop;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
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
        ivMember = view.findViewById(R.id.ivShop);
        tvMemberId = view.findViewById(R.id.tvShopId);
        tvMemberState = view.findViewById(R.id.tvShopState);
        tvMemberName = view.findViewById(R.id.tvShopName);
        tvMemberPhone = view.findViewById(R.id.tvShopPhone);
        tvMemberEmail = view.findViewById(R.id.tvShopEmail);
        tvMemberJoinDate = view.findViewById(R.id.tvShopJoinDate);

        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("shop") == null) {
            Common.ShowToast(activity, R.string.textNoMembersFound);
            navController.popBackStack();
            return;
        }
        shop = (Shop) bundle.getSerializable("shop");
        showShop();
    }
    private void showShop() {
        String url = Common.URL + "ShopServlet";
        int id = shop.getId();
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        Bitmap bitmap = null;
        try {
            bitmap = new ShopImageTask(url, id, imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivMember.setImageBitmap(bitmap);
        } else {
            ivMember.setImageResource(R.drawable.no_image);
        }
        tvMemberId.setText(String.valueOf(shop.getId()));
        tvMemberState.setText(shop.getState());
        tvMemberName.setText(shop.getName());
        tvMemberPhone.setText(shop.getTax());
        tvMemberEmail.setText(shop.getEmail());
        tvMemberJoinDate.setText(shop.getJointime());


    }

}
