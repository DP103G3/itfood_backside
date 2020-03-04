package tw.dp103g3.itfood_backside.delivery;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood_backside.main.Common;
import tw.dp103g3.itfood_backside.R;
import tw.dp103g3.itfood_backside.main.Url;
import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.ImageTask;


public class DeliveryManagementFragment extends Fragment {
    private static final String TAG = "TAG_DeliveryManagementFragment";
    private Activity activity;
    private RecyclerView rvDelivery;
    private CommonTask deliveryGetAllTask;
    private CommonTask deliveryDeleteTask;
    private ImageTask deliveryImageTask;
    private List<Delivery> deliverys;
    private TextView tvLapse,tvEffective,tvShowAll,tvListTitle,tvLeave;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_delivery_management, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SearchView searchView = view.findViewById(R.id.searchView);
        rvDelivery = view.findViewById(R.id.rvDelivery);
        rvDelivery.setLayoutManager(new LinearLayoutManager(activity));
        tvShowAll = view.findViewById(R.id.tvShowAll);
        tvLapse = view.findViewById(R.id.tvLapse);
        tvEffective = view.findViewById(R.id.tvEffective);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvLeave = view.findViewById(R.id.tvLeave);
        tvShowAll.setTextColor(Color.RED);
        deliverys = getDeliverys();
        showDeliverys(deliverys);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showDeliverys(deliverys);
                    tvListTitle.setText("所有會員");
                    tvShowAll.setTextColor(Color.RED);
                    tvEffective.setTextColor(Color.BLACK);
                    tvLapse.setTextColor(Color.BLACK);
                } else {
                    List<Delivery> searchDeliverys = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Delivery delivery : deliverys) {
                        if (delivery.getDelName().toUpperCase().contains(newText.toUpperCase())) {
                            searchDeliverys.add(delivery);
                        }
                    }

                    showDeliverys(searchDeliverys);
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
                tvLeave.setTextColor(Color.BLACK);
                searchView.setQuery("",false);
                showDeliverys(deliverys);
                tvListTitle.setText("所有外送員");
            }
        });


        tvLapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLapse.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);
                tvLeave.setTextColor(Color.BLACK);

                List<Delivery> lapseDeliverys = new ArrayList<>();
                for (Delivery delivery : deliverys) {
                    if (delivery.getDelState() == 1) {
                        lapseDeliverys.add(delivery);
                    }
                }
                showDeliverys(lapseDeliverys);
                tvListTitle.setText("有效外送員");
            }
        });

        tvEffective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEffective.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);
                tvLeave.setTextColor(Color.BLACK);

                List<Delivery> lapseDeliverys = new ArrayList<>();
                for (Delivery delivery : deliverys) {
                    if (delivery.getDelState() == 2) {
                        lapseDeliverys.add(delivery);
                    }
                }
                showDeliverys(lapseDeliverys);
                tvListTitle.setText("停權外送員");
            }
        });

        tvLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLeave.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);
                tvEffective.setTextColor(Color.BLACK);

                List<Delivery> lapseDeliverys = new ArrayList<>();
                for (Delivery delivery : deliverys) {
                    if (delivery.getDelState() == 0) {
                        lapseDeliverys.add(delivery);
                    }
                }
                showDeliverys(lapseDeliverys);
                tvListTitle.setText("離職外送員");
            }
        });

    }

    @SuppressLint("LongLogTag")
    private List<Delivery> getDeliverys() {
        List<Delivery> deliverys = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/DeliveryServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            deliveryGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = deliveryGetAllTask.execute().get();
                Type listType = new TypeToken<List<Delivery>>() {
                }.getType();
                deliverys = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.ShowToast(activity, R.string.textNoNetwork);
        }
        return deliverys;
    }

    private void showDeliverys(List<Delivery> deliverys) {
        if (deliverys == null || deliverys.isEmpty()) {
            Common.ShowToast(activity, R.string.textNoDeliverysFound);

        }
        DeliveryManagementFragment.DeliveryAdapter deliveryAdapter = (DeliveryManagementFragment.DeliveryAdapter) rvDelivery.getAdapter();
        // 如果memberAdapter不存在就建立新的，否則續用舊有的
        if (deliveryAdapter == null) {
            rvDelivery.setAdapter(new DeliveryManagementFragment.DeliveryAdapter(activity, deliverys));
        } else {
            deliveryAdapter.setDeliverys(deliverys);
            deliveryAdapter.notifyDataSetChanged();
        }
    }

    private class DeliveryAdapter extends RecyclerView.Adapter<DeliveryManagementFragment.DeliveryAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Delivery> deliverys;


        DeliveryAdapter(Context context, List<Delivery> deliverys) {
            layoutInflater = LayoutInflater.from(context);
            this.deliverys = deliverys;

        }

        void setDeliverys(List<Delivery> deliveryrs) {
            this.deliverys = deliveryrs;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDelivery;
            TextView tvDeliveryName, tvDeliveryPhone, tvDeliveryEmail, tvDeliveryState;

            MyViewHolder(View itemView) {
                super(itemView);
                ivDelivery = itemView.findViewById(R.id.ivDelivery);
                tvDeliveryName = itemView.findViewById(R.id.tvDeliveryName);
                tvDeliveryPhone = itemView.findViewById(R.id.tvDeliveryPhone);
                tvDeliveryEmail = itemView.findViewById(R.id.tvDeliveryEmail);
                tvDeliveryState = itemView.findViewById(R.id.tvDeliveryState);
            }
        }

        @Override
        public int getItemCount() {
            return deliverys.size();
        }

        @NonNull
        @Override
        public DeliveryManagementFragment.DeliveryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_delivery, parent, false);
            return new DeliveryManagementFragment.DeliveryAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DeliveryManagementFragment.DeliveryAdapter.MyViewHolder myViewHolder, int position) {
            final Delivery delivery = deliverys.get(position);

            if(delivery.getDelState() == 0){
                myViewHolder.tvDeliveryState.setText("離職");
                myViewHolder.tvDeliveryState.setTextColor(Color.RED);
            }else if(delivery.getDelState() == 1){
                myViewHolder.tvDeliveryState.setText("有效");
                myViewHolder.tvDeliveryState.setTextColor(Color.BLACK);
            }else if(delivery.getDelState() == 2){
                myViewHolder.tvDeliveryState.setText("停權");
                myViewHolder.tvDeliveryState.setTextColor(Color.RED);
            }
                myViewHolder.tvDeliveryName.setText(delivery.getDelName());
            myViewHolder.tvDeliveryPhone.setText(delivery.getDelPhone());
            myViewHolder.tvDeliveryEmail.setText(delivery.getDelEmail());
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("delivery", delivery);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_deliveryManagementFragment_to_deliveryDetailFragment, bundle);
                }
            });

        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (deliveryGetAllTask != null) {
            deliveryGetAllTask.cancel(true);
            deliveryGetAllTask = null;
        }

        if (deliveryImageTask != null) {
            deliveryImageTask.cancel(true);
            deliveryImageTask = null;
        }

        if (deliveryDeleteTask != null) {
            deliveryDeleteTask.cancel(true);
            deliveryDeleteTask = null;
        }
    }



}
