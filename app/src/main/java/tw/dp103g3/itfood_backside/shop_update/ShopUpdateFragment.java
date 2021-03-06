package tw.dp103g3.itfood_backside.shop_update;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import tw.dp103g3.itfood_backside.main.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.main.Url;
import tw.dp103g3.itfood_backside.shop.Shop;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.ShopImageTask;

import static android.app.Activity.RESULT_OK;


public class ShopUpdateFragment extends Fragment {
    private static final String TAG = "TAG_ShopUpdateFragment";
    private Activity activity;
    private Shop shopDetail, shop;
    private CommonTask shopGetAllTask;
    private CommonTask shopDeleteTask;
    private TextView tvShopId, tvShopState, tvShopJoinDate;
    private TextView tvNameWarning, tvPhoneWarning, tvEmailWarning, tvAddressWarning, tvTaxWarning, tvAreaWarning, tvInfoWarning;
    private ImageView ivShop;
    private Button btTakePicture, btPickPicture, btFinishUpdate, btCancel;
    private EditText etShopName, etShopPhone, etShopEmail, etShopAddress, etShopTax, etShopArea, etShopInfo;
    private Uri contentUri, croppedImageUri;
    private Toolbar toolbarShopUpdate;
    private byte[] image;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_PICTURE = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity.setTitle(R.string.textShopUpdateTitle);
        return inflater.inflate(R.layout.fragment_shop_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivShop = view.findViewById(R.id.ivShop);
        tvShopId = view.findViewById(R.id.tvShopId);
        tvShopState = view.findViewById(R.id.tvShopState);
        tvShopJoinDate = view.findViewById(R.id.tvShopJoinDate);
        btTakePicture = view.findViewById(R.id.btTakePicture);
        btPickPicture = view.findViewById(R.id.btPickPicture);
        btFinishUpdate = view.findViewById(R.id.btFinishUpdate);
        btCancel = view.findViewById(R.id.btCancel);
        etShopName = view.findViewById(R.id.etShopName);
        etShopPhone = view.findViewById(R.id.etShopPhone);
        //etShopEmail = view.findViewById(R.id.etShopEmail);
        etShopAddress = view.findViewById(R.id.etShopAddress);
        etShopTax = view.findViewById(R.id.etShopTax);
        etShopArea = view.findViewById(R.id.etShopArea);
        etShopInfo = view.findViewById(R.id.etShopInfo);
        tvNameWarning = view.findViewById(R.id.tvNameWarning);
        tvPhoneWarning = view.findViewById(R.id.tvPhoneWarning);
        //tvEmailWarning = view.findViewById(R.id.tvEmailWarning);
        tvAddressWarning = view.findViewById(R.id.tvAddressWarning);
        tvTaxWarning = view.findViewById(R.id.tvTaxWarning);
        tvAreaWarning = view.findViewById(R.id.tvAreaWarning);
        tvInfoWarning = view.findViewById(R.id.tvInfoWarning);
        toolbarShopUpdate = view.findViewById(R.id.toolbarShopUpdate);

        toolbarShopUpdate.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("shop") == null) {
            Common.ShowToast(activity, R.string.textNoShopsFound);
            navController.popBackStack();
            return;
        }
        shop = (Shop) bundle.getSerializable("shop");

        showShop();

        Button btTakePicture = view.findViewById(R.id.btTakePicture);
        btTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定存檔路徑
                File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = new File(file, "picture.jpg");
                contentUri = FileProvider.getUriForFile(
                        activity, activity.getPackageName() + ".provider", file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivityForResult(intent, REQ_TAKE_PICTURE);
                } else {
                    Common.ShowToast(activity, R.string.textNoCameraApp);
                }
            }
        });

        Button btPickPicture = view.findViewById(R.id.btPickPicture);
        btPickPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_IMAGE);
            }
        });

        Button btFinishUpdate = view.findViewById(R.id.btFinishUpdate);
        btFinishUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int  counts = 0;
                String shopName = etShopName.getText().toString();
                if (shopName.length() <= 0) {
                    tvNameWarning.setText(R.string.textNameIsInvalid);
                    Common.ShowToast(activity, R.string.textNameIsInvalid);
                    counts++;
                }
                String shopPhone = etShopPhone.getText().toString().trim();
                if (shopPhone.length() <= 0) {
                    tvPhoneWarning.setText(R.string.textShopPhoneIsInvalid);
                    Common.ShowToast(getActivity(), R.string.textShopPhoneIsInvalid);
                    counts++;
                }else  if(shopPhone.length() < 9){
                    tvPhoneWarning.setText(R.string.textPhoneError);
                    Common.ShowToast(activity, R.string.textPhoneError);
                    counts++;
                }else if (!shopPhone.startsWith("0")) {
                    tvPhoneWarning.setText(R.string.textPhoneError);
                    Common.ShowToast(activity, R.string.textPhoneError);
                    counts++;
                } else{
                    tvPhoneWarning.setText("");
                }

                /*String shopEmail = etShopEmail.getText().toString().trim();
                if (shopEmail.length() <= 0) {
                    tvEmailWarning.setText("請輸入信箱！");
                    Common.ShowToast(getActivity(), R.string.textShopEmailIsInvalid);
                    return;
                }else {
                    tvEmailWarning.setText("");
                }*/

                String shopAddress = etShopAddress.getText().toString().trim();
                if (shopAddress.length() <= 0) {
                    tvAddressWarning.setText("請輸入地址！");
                    Common.ShowToast(getActivity(), R.string.textShopAddressIsInvalid);
                    counts++;
                }else {
                    tvAddressWarning.setText("");
                }

                String shopTax = etShopTax.getText().toString().trim();
                if (shopTax.length() <= 0) {
                    tvTaxWarning.setText(R.string.textShopTaxIsInvalid);
                    Common.ShowToast(getActivity(), R.string.textShopTaxIsInvalid);
                    counts++;
                }else if(shopTax.length() < 8){
                    tvTaxWarning.setText(R.string.textTaxError);
                    Common.ShowToast(activity, R.string.textTaxError);
                    counts++;
                }else{
                    tvTaxWarning.setText("");
                }
                String shopArea = etShopArea.getText().toString().trim();
                if (shopArea.length() <= 0) {
                    tvAreaWarning.setText("請輸入區域編號！");
                    Common.ShowToast(getActivity(), R.string.textShopAreaIsInvalid);
                    counts++;
                }else {
                    tvAreaWarning.setText("");
                }

                String shopInfo = etShopInfo.getText().toString().trim();
                if (counts > 0){
                    counts = 0;
                    return;
                }
                List<Address> addressList;
                double latitude = -181.0;
                double longitude = -181.0;
                try {
                    addressList = new Geocoder(activity).getFromLocationName(shopAddress, 1);
                    if (addressList != null && addressList.size() > 0) {
                        latitude = addressList.get(0).getLatitude();
                        longitude = addressList.get(0).getLongitude();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }



                if (Common.networkConnected(activity)) {
                    String url = Url.URL + "/ShopServlet";
                    shop.setFields(shopName, shopPhone, shopAddress, latitude, longitude, shopTax, Integer.parseInt(shopArea), shopInfo);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "update");
                    jsonObject.addProperty("shop", new Gson().toJson(shop));
                    // 有圖才上傳
                    if (image != null) {
                        jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
                    }
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
                        Common.ShowToast(activity, R.string.textUpdateSuccess);
                        /* 回前一個Fragment */
                        navController.popBackStack();
                    }
                } else {
                    Common.ShowToast(activity, R.string.textNoNetwork);
                }

            }
        });

        Button btCancel = view.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 回前一個Fragment */
                navController.popBackStack();
            }
        });
    }


    private void showShop() {
        String url = Url.URL + "/ShopServlet";
        int id = shop.getId();
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
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

        tvShopId.setText(shopDetail.getEmail());
        if(shopDetail.getState() == 0){
            tvShopState.setText("未上架");
            tvShopState.setTextColor(Color.RED);

        }else if(shopDetail.getState() == 1){
            tvShopState.setText("已上架");
            tvShopState.setTextColor(Color.BLUE);
        }
        tvShopJoinDate.setText(shopDetail.getJointime());
        etShopName.setText(shopDetail.getName());
        etShopPhone.setText(shopDetail.getPhone());
        //etShopEmail.setText(shopDetail.getEmail());
        etShopAddress.setText(shopDetail.getAddress());
        etShopTax.setText(shopDetail.getTax());
        if(String.valueOf(shopDetail.getArea()) != null){
            etShopArea.setText(String.valueOf(shopDetail.getArea()));
        }else {
            etShopArea.setText("");
        }
        etShopInfo.setText(shopDetail.getInfo());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_IMAGE:
                    Uri uri = intent.getData();
                    crop(uri);
                    break;
                case REQ_CROP_PICTURE:
                    Log.d(TAG, "REQ_CROP_PICTURE: " + croppedImageUri.toString());
                    try {
                        Bitmap picture = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(croppedImageUri));
                        ivShop.setImageBitmap(picture);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        croppedImageUri = Uri.fromFile(file);
        // take care of exceptions
        try {
            // call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // the recipient of this Intent can read soruceImageUri's data
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // set image source Uri and type
            cropIntent.setDataAndType(sourceImageUri, "image/*");
            // send crop message
            cropIntent.putExtra("crop", "true");
            // aspect ratio of the cropped area, 0 means user define
            cropIntent.putExtra("aspectX", 0); // this sets the max width
            cropIntent.putExtra("aspectY", 0); // this sets the max height
            // output with and height, 0 keeps original size
            cropIntent.putExtra("outputX", 0);
            cropIntent.putExtra("outputY", 0);
            // whether keep original aspect ratio
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri);
            // whether return data by the intent
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQ_CROP_PICTURE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Common.ShowToast(activity, "This device doesn't support the crop action!");
        }
    }


}
