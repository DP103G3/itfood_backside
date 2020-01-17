package tw.dp103g3.itfood_backside.shop;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.Switch;
import android.widget.TextView;

import tw.dp103g3.itfood_backside.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.Url;
import tw.dp103g3.itfood_backside.task.ShopImageTask;


public class ShopDetailFragment extends Fragment {
    private Activity activity;
    private final static String TAG = "TAG_ShopDetailsFragment";
    private ImageView ivShop;
    private TextView tvShopId, tvShopState, tvShopName,tvShopPhone, tvShopEmail, tvShopJoinDate
            , tvShopAddress, tvShopTax, tvShopArea, tvShopInfo;
    private Shop shop;
    private Switch swShopState;


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
        String url = Url.URL + "/ShopServlet";
        int id = shop.getId();
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
        tvShopId.setText(String.valueOf(shop.getId()));
        if(shop.getState() == 0){
            tvShopState.setText("未上架/下架店家");
            tvShopState.setTextColor(Color.RED);
            swShopState.setChecked(false);
        }else if(shop.getState() == 1){
            tvShopState.setText("已上架店家");
            swShopState.setChecked(true);
        }
        tvShopName.setText(shop.getName());
        tvShopPhone.setText(shop.getPhone());
        tvShopEmail.setText(shop.getEmail());
        tvShopJoinDate.setText(shop.getJointime());
        tvShopAddress.setText(shop.getAddress());
        tvShopTax.setText(shop.getTax());
        tvShopArea.setText(String.valueOf(shop.getArea()));
        tvShopInfo.setText(shop.getInfo());

    }

}
