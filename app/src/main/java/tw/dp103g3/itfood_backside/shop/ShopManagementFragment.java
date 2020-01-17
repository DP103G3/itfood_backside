package tw.dp103g3.itfood_backside.shop;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.v4.media.RatingCompat;
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
    private MenuItem testmenu;
    private RecyclerView rvShop;
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
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setLayoutManager(new LinearLayoutManager(activity));
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
                    tvListTitle.setText("所有店家");
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
                tvListTitle.setText("所有店家");
            }
        });


        tvLapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLapse.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                List<Shop> lapseShops = new ArrayList<>();
                for (Shop shop : shops) {
                    if (shop.getState() == 1) {
                        lapseShops.add(shop);
                    }
                }
                showShops(lapseShops);
                tvListTitle.setText("已上架店家");
            }
        });

        tvEffective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEffective.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                List<Shop> lapseShops = new ArrayList<>();
                for (Shop shop : shops) {
                    if (shop.getState() == 0) {
                        lapseShops.add(shop);
                    }
                }
                showShops(lapseShops);
                tvListTitle.setText("未上架/下架店家");
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
        ShopManagementFragment.ShopAdapter shopAdapter = (ShopManagementFragment.ShopAdapter) rvShop.getAdapter();
        // 如果memberAdapter不存在就建立新的，否則續用舊有的
        if (shopAdapter == null) {
            rvShop.setAdapter(new ShopManagementFragment.ShopAdapter(activity, shops));
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
            ImageView ivShop;
            TextView tvShopName, tvShopPhone, tvShopEmail, tvShopState;

            MyViewHolder(View itemView) {
                super(itemView);
                ivShop = itemView.findViewById(R.id.ivShop);
                tvShopName = itemView.findViewById(R.id.tvShopName);
                tvShopPhone = itemView.findViewById(R.id.tvShopPhone);
                tvShopEmail = itemView.findViewById(R.id.tvShopEmail);
                tvShopState = itemView.findViewById(R.id.tvShopState);
            }
        }

        @Override
        public int getItemCount() {
            return shops.size();
        }

        @NonNull
        @Override
        public ShopManagementFragment.ShopAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_shop, parent, false);
            return new ShopManagementFragment.ShopAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopManagementFragment.ShopAdapter.MyViewHolder myViewHolder, int position) {
            final Shop shop = shops.get(position);
            String url = Url.URL + "/ShopServlet";
            int id = shop.getId();
            shopImageTask = new ShopImageTask(url, id, imageSize, myViewHolder.ivShop);
            shopImageTask.execute();
            if (shop.getState() == 0){
                myViewHolder.tvShopState.setText("未上架/下架");
                myViewHolder.tvShopState.setTextColor(Color.RED);
            }else {
                myViewHolder.tvShopState.setText("已上架");
                myViewHolder.tvShopState.setTextColor(Color.BLACK);
            }
            myViewHolder.tvShopName.setText(shop.getName());
            myViewHolder.tvShopPhone.setText(shop.getPhone());
            myViewHolder.tvShopEmail.setText(shop.getEmail());
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
                                            .navigate(R.id.action_shopManagementFragment_to_shopUpdateFragment, bundle);
                                    break;
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
