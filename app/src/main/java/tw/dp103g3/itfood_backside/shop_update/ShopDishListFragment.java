package tw.dp103g3.itfood_backside.shop_update;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood_backside.main.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.main.Url;
import tw.dp103g3.itfood_backside.shop.Shop;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.ShopImageTask;

public class ShopDishListFragment extends Fragment {
    private static final String TAG = "TAG_ShopDishListFragment";
    private Activity activity;
    private RecyclerView rvShopDish;
    private CommonTask dishGetAllTask;
    private CommonTask dishDeleteTask;
    private ShopImageTask ImageTask;
    private List<Dish> dishes;
    private Shop shop;
    private Toolbar toolbarShopDishList;
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
        activity.setTitle(R.string.textShopDishLishTitle);
        return inflater.inflate(R.layout.fragment_shop_dish_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SearchView searchView = view.findViewById(R.id.searchView);
        rvShopDish = view.findViewById(R.id.rvShopDish);
        rvShopDish.setLayoutManager(new LinearLayoutManager(activity));
        tvShowAll = view.findViewById(R.id.tvShowAll);
        tvLapse = view.findViewById(R.id.tvLapse);
        tvEffective = view.findViewById(R.id.tvEffective);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvShowAll.setTextColor(Color.RED);
        toolbarShopDishList = view.findViewById(R.id.toolbarShopDishList);

        toolbarShopDishList.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        final NavController navController = Navigation.findNavController(view);
        Bundle bundleDish = getArguments();
        if (bundleDish == null || bundleDish.getSerializable("shop") == null) {
            Common.ShowToast(activity, R.string.textNoShopsFound);
            navController.popBackStack();
            return;
        }
        shop = (Shop) bundleDish.getSerializable("shop");

        dishes = getDishes();
        showDishs(dishes);
        FloatingActionButton btAdd = view.findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                Navigation.findNavController(view).navigate(R.id.action_shopDishListFragment_to_shopAddDishFragment, bundle);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showDishs(dishes);
                    tvListTitle.setText("所有餐點");
                    tvShowAll.setTextColor(Color.RED);
                    tvEffective.setTextColor(Color.BLACK);
                    tvLapse.setTextColor(Color.BLACK);
                } else {
                    List<Dish> searchDishs = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Dish dish : dishes) {
                        if (dish.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchDishs.add(dish);
                        }
                    }
                    showDishs(searchDishs);
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
                showDishs(dishes);
                tvListTitle.setText("所有餐點");
            }
        });


        tvLapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLapse.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                List<Dish> lapseDishs = new ArrayList<>();
                for (Dish dish : dishes) {
                    if (dish.getState() == 1) {
                        lapseDishs.add(dish);
                    }
                }
                showDishs(lapseDishs);
                tvListTitle.setText("已上架");
            }
        });

        tvEffective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEffective.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                List<Dish> lapseDishs = new ArrayList<>();
                for (Dish dish : dishes) {
                    if (dish.getState() == 0) {
                        lapseDishs.add(dish);
                    }
                }
                showDishs(lapseDishs);
                tvListTitle.setText("未上架");
            }
        });

    }

    @SuppressLint("LongLogTag")
    private List<Dish> getDishes() {
        int shopId = shop.getId();
        List<Dish> dishes = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DishServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getDishByShopId");
            jsonObject.addProperty("shop_id", shopId);
            String jsonOut = jsonObject.toString();
            dishGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = dishGetAllTask.execute().get();
                Type listType = new TypeToken<List<Dish>>() {
                }.getType();
                dishes = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }
        return dishes;
    }

    private void showDishs(List<Dish> dishs) {
        if (dishs == null || dishs.isEmpty()) {
            Common.ShowToast(activity, R.string.textNoDishsFound);

        }
        ShopDishListFragment.DishAdapter dishAdapter = (ShopDishListFragment.DishAdapter) rvShopDish.getAdapter();

        if (dishAdapter == null) {
            rvShopDish.setAdapter(new ShopDishListFragment.DishAdapter(activity, dishs));
        } else {
            dishAdapter.setDishs(dishs);
            dishAdapter.notifyDataSetChanged();
        }
    }

    private class DishAdapter extends RecyclerView.Adapter<ShopDishListFragment.DishAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Dish> dishs;
        private int imageSize;

        DishAdapter(Context context, List<Dish> dishs) {
            layoutInflater = LayoutInflater.from(context);
            this.dishs = dishs;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setDishs(List<Dish> dishs) {
            this.dishs = dishs;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDish;
            TextView tvDishName, tvInfo, tvPrice, tvDishState;

            MyViewHolder(View itemView) {
                super(itemView);
                ivDish = itemView.findViewById(R.id.ivDish);
                tvDishName = itemView.findViewById(R.id.tvDishName);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvDishState = itemView.findViewById(R.id.tvDishState);

            }
        }

        @Override
        public int getItemCount() {
            return dishs.size();
        }

        @NonNull
        @Override
        public ShopDishListFragment.DishAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_dish, parent, false);
            return new ShopDishListFragment.DishAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopDishListFragment.DishAdapter.MyViewHolder myViewHolder, int position) {
            final Dish dish = dishs.get(position);
            String url = Url.URL + "/DishServlet";
            int id = dish.getId();
            ImageTask = new ShopImageTask(url, id, imageSize, myViewHolder.ivDish);
            ImageTask.execute();
            if (dish.getState() == 0){
                myViewHolder.tvDishState.setText("未上架");
                myViewHolder.tvDishState.setTextColor(Color.RED);
            }else {
                myViewHolder.tvDishState.setText("已上架");
                myViewHolder.tvDishState.setTextColor(Color.BLACK);
            }
            myViewHolder.tvDishName.setText(dish.getName());
            myViewHolder.tvInfo.setText(dish.getInfo());
            myViewHolder.tvPrice.setText(String.valueOf(dish.getPrice()));
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("dish", dish);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_shopDishListFragment_to_shopDishDetailFragment, bundle);
                }
            });
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (dishGetAllTask != null) {
            dishGetAllTask.cancel(true);
            dishGetAllTask = null;
        }

        if (ImageTask != null) {
            ImageTask.cancel(true);
            ImageTask = null;
        }

        if (dishDeleteTask != null) {
            dishDeleteTask.cancel(true);
            dishDeleteTask = null;
        }
    }


}
