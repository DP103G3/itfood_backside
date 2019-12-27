package tw.dp103g3.itfood_backside.shop;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood_backside.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.Url;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.ShopImageTask;



public class ShopManagementFragment extends Fragment {
    private static final String TAG = "TAG_ShopManagementFragment";
    private Activity activity;
    private RecyclerView rvMember;
    private CommonTask shopGetAllTask;
    private CommonTask shopDeleteTask;
    private ShopImageTask shopImageTask;
    private List<Shop> shops;
    private TextView tvLapse,tvEffective,tvShowAll,tvListTitle;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity.setTitle(R.string.textShopPageTitle);
        return inflater.inflate(R.layout.fragment_shop_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SearchView searchView = view.findViewById(R.id.searchView);
        rvMember = view.findViewById(R.id.rvMember);
        rvMember.setLayoutManager(new LinearLayoutManager(activity));
        tvShowAll = view.findViewById(R.id.tvShowAll);
        tvLapse = view.findViewById(R.id.tvLapse);
        tvEffective = view.findViewById(R.id.tvEffective);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvShowAll.setTextColor(Color.RED);
        shops = getShops();
        showShops(shops);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showShops(shops);
                    tvListTitle.setText("所有商家");
                    tvShowAll.setTextColor(Color.RED);
                    tvEffective.setTextColor(Color.BLACK);
                    tvLapse.setTextColor(Color.BLACK);
                } else {
                    List<Shop> searchShops = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Shop shop : shops) {
                        if (shop.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchShops.add(shop);
                        }
                    }
                    showShops(searchShops);
                    tvListTitle.setText(null);
                    tvShowAll.setTextColor(Color.BLACK);
                    tvEffective.setTextColor(Color.BLACK);
                    tvLapse.setTextColor(Color.BLACK);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        tvShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvShowAll.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvLapse.setTextColor(Color.BLACK);
                searchView.setQuery("",false);
                showShops(shops);
                tvListTitle.setText("所有商家");
            }
        });


        tvLapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLapse.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                String lapse = "已上架";
                List<Shop> lapseShops = new ArrayList<>();
                for (Shop shop : shops) {
                    if (shop.getState().toUpperCase().contains(lapse.toUpperCase())) {
                        lapseShops.add(shop);
                    }
                }
                showShops(lapseShops);
                tvListTitle.setText(lapse+"商家");
            }
        });

        tvEffective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEffective.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);
                String effective = "未上架/下架";
                List<Shop> lapseShops = new ArrayList<>();
                for (Shop shop : shops) {
                    if (shop.getState().toUpperCase().contains(effective.toUpperCase())) {
                        lapseShops.add(shop);
                    }
                }
                showShops(lapseShops);
                tvListTitle.setText(effective+"商家");
            }
        });

    }


    @SuppressLint("LongLogTag")
    private List<Shop> getShops() {
        List<Shop> shops = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/ShopServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            shopGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = shopGetAllTask.execute().get();
                Type listType = new TypeToken<List<Shop>>() {
                }.getType();
                shops = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }
        return shops;
    }

    private void showShops(List<Shop> shops) {
        if (shops == null || shops.isEmpty()) {
            Common.ShowToast(activity, R.string.textNoMembersFound);

        }
        ShopManagementFragment.ShopAdapter shopAdapter = (ShopManagementFragment.ShopAdapter) rvMember.getAdapter();
        // 如果memberAdapter不存在就建立新的，否則續用舊有的
        if (shopAdapter == null) {
            rvMember.setAdapter(new ShopManagementFragment.ShopAdapter(activity, shops));
        } else {
            shopAdapter.setShops(shops);
            shopAdapter.notifyDataSetChanged();
        }
    }

    private class ShopAdapter extends RecyclerView.Adapter<ShopManagementFragment.ShopAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Shop> shops;
        private int imageSize;

        ShopAdapter(Context context, List<Shop> shops) {
            layoutInflater = LayoutInflater.from(context);
            this.shops = shops;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setShops(List<Shop> shops) {
            this.shops = shops;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMember;
            TextView tvMemberName, tvMemberPhone, tvMemberEmail, tvMemberState;

            MyViewHolder(View itemView) {
                super(itemView);
                ivMember = itemView.findViewById(R.id.ivMember);
                tvMemberName = itemView.findViewById(R.id.tvMemberName);
                tvMemberPhone = itemView.findViewById(R.id.tvMemberPhone);
                tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
                tvMemberState = itemView.findViewById(R.id.tvMemberState);
            }
        }

        @Override
        public int getItemCount() {
            return shops.size();
        }

        @NonNull
        @Override
        public ShopManagementFragment.ShopAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member, parent, false);
            return new ShopManagementFragment.ShopAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopManagementFragment.ShopAdapter.MyViewHolder myViewHolder, int position) {
            final Shop shop = shops.get(position);
            String url = Url.URL + "ShopServlet";
            int id = shop.getId();
            shopImageTask = new ShopImageTask(url, id, imageSize, myViewHolder.ivMember);
            shopImageTask.execute();
            myViewHolder.tvMemberState.setText(shop.getState());
            myViewHolder.tvMemberName.setText(shop.getName());
            myViewHolder.tvMemberPhone.setText(shop.getTax());
            myViewHolder.tvMemberEmail.setText(shop.getEmail());
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("shop",shop);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_shopManagementFragment_to_shopDetailFragment, bundle);
                }
            });
            myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.update:
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("shop", shop);
                                    Navigation.findNavController(view)
                                            .navigate(R.id.action_memberManagementFragment_to_memberUpdateFragment, bundle);
                                    break;
                                case R.id.delete:
                                    if (Common.networkConnected(activity)) {
                                        String url = Url.URL + "/ShopServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "shopDelete");
                                        jsonObject.addProperty("shopId", shop.getId());
                                        int count = 0;
                                        try {
                                            shopDeleteTask = new CommonTask(url, jsonObject.toString());
                                            String result = shopDeleteTask.execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.ShowToast(activity, R.string.textDeleteFail);
                                        } else {
                                            shops.remove(shop);
                                            ShopManagementFragment.ShopAdapter.this.notifyDataSetChanged();
                                            // 外面spots也必須移除選取的spot
                                            ShopManagementFragment.this.shops.remove(shop);
                                            Common.ShowToast(activity, R.string.textDeleteSuccess);
                                        }
                                    } else {
                                        Common.ShowToast(activity, R.string.textNoNetwork);
                                    }
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            });
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (shopGetAllTask != null) {
            shopGetAllTask.cancel(true);
            shopGetAllTask = null;
        }

        if (shopImageTask != null) {
            shopImageTask.cancel(true);
            shopImageTask = null;
        }

        if (shopDeleteTask != null) {
            shopDeleteTask.cancel(true);
            shopDeleteTask = null;
        }
    }

}
