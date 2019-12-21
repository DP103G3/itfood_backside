package tw.dp103g3.itfood_backside;


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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g3.itfood_backside.task.CommonTask;
import tw.dp103g3.itfood_backside.task.MemberImageTask;


public class MemberManagementFragment extends Fragment {
    private static final String TAG = "TAG_MemberManagementFragment";
    private Activity activity;
    private RecyclerView rvMember;
    private CommonTask memberGetAllTask;
    private CommonTask memberDeleteTask;
    private MemberImageTask memberImageTask;
    private List<Member> members;
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
        activity.setTitle(R.string.textMemberPageTitle);

        return inflater.inflate(R.layout.fragment_member_management, container, false);
    }





    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = view.findViewById(R.id.searchView);
        rvMember = view.findViewById(R.id.rvMember);
        rvMember.setLayoutManager(new LinearLayoutManager(activity));
        tvShowAll = view.findViewById(R.id.tvShowAll);
        tvLapse = view.findViewById(R.id.tvLapse);
        tvEffective = view.findViewById(R.id.tvEffective);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvShowAll.setTextColor(Color.RED);
        members = getMembers();
        showMembers(members);


        tvShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvShowAll.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvLapse.setTextColor(Color.BLACK);
                showMembers(members);
                tvListTitle.setText("所有會員");
            }
        });


        tvLapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvLapse.setTextColor(Color.RED);
                tvEffective.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);

                String lapse = "有效";
                List<Member> lapseMembers = new ArrayList<>();
                for (Member member : members) {
                        if (member.getMemState().toUpperCase().contains(lapse.toUpperCase())) {
                            lapseMembers.add(member);
                        }
                }
                showMembers(lapseMembers);
                tvListTitle.setText(lapse+"會員");
            }
        });

        tvEffective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEffective.setTextColor(Color.RED);
                tvLapse.setTextColor(Color.BLACK);
                tvShowAll.setTextColor(Color.BLACK);
                String effective = "停權";
                List<Member> lapseMembers = new ArrayList<>();
                for (Member member : members) {
                    if (member.getMemState().toUpperCase().contains(effective.toUpperCase())) {
                        lapseMembers.add(member);
                    }
                }
                showMembers(lapseMembers);
                tvListTitle.setText(effective+"會員");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showMembers(members);
                    tvListTitle.setText("所有會員");
                    tvShowAll.setTextColor(Color.RED);
                } else {
                    List<Member> searchMembers = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Member member : members) {
                        if (member.getMemName().toUpperCase().contains(newText.toUpperCase())) {
                            searchMembers.add(member);
                        }
                    }
                    showMembers(searchMembers);
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
    }


    @SuppressLint("LongLogTag")
    private List<Member> getMembers() {
        List<Member> members = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "MemberServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            memberGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = memberGetAllTask.execute().get();
                Type listType = new TypeToken<List<Member>>() {
                }.getType();
                members = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return members;
    }

    private void showMembers(List<Member> members) {
        if (members == null || members.isEmpty()) {
            Common.showToast(activity, R.string.textNoMembersFound);

        }
       MemberAdapter memberAdapter = (MemberAdapter) rvMember.getAdapter();
        // 如果memberAdapter不存在就建立新的，否則續用舊有的
        if (memberAdapter == null) {
            rvMember.setAdapter(new MemberAdapter(activity, members));
        } else {
            memberAdapter.setMembers(members);
            memberAdapter.notifyDataSetChanged();
        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Member> members;
        private int imageSize;

        MemberAdapter(Context context, List<Member> members) {
            layoutInflater = LayoutInflater.from(context);
            this.members = members;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setMembers(List<Member> members) {
            this.members = members;
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
            return members.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
            final Member member = members.get(position);
            String url = Common.URL_SERVER + "MemberServlet";
            int id = member.getMemId();
            memberImageTask = new MemberImageTask(url, id, imageSize, myViewHolder.ivMember);
            memberImageTask.execute();
            myViewHolder.tvMemberState.setText(member.getMemState());
            myViewHolder.tvMemberName.setText(member.getMemName());
            myViewHolder.tvMemberPhone.setText(member.getMemPhone());
            myViewHolder.tvMemberEmail.setText(member.getMemEmail());
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("member", member);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_memberManagementFragment_to_memberDetailFragment, bundle);
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
                                    bundle.putSerializable("member", member);
                                    Navigation.findNavController(view)
                                            .navigate(R.id.action_memberManagementFragment_to_memberUpdateFragment, bundle);
                                    break;
                                case R.id.delete:
                                    if (Common.networkConnected(activity)) {
                                        String url = Common.URL_SERVER + "/MemberServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "memberDelete");
                                        jsonObject.addProperty("memberId", member.getMemId());
                                        int count = 0;
                                        try {
                                            memberDeleteTask = new CommonTask(url, jsonObject.toString());
                                            String result = memberDeleteTask.execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.showToast(activity, R.string.textDeleteFail);
                                        } else {
                                            members.remove(member);
                                            MemberAdapter.this.notifyDataSetChanged();
                                            // 外面spots也必須移除選取的spot
                                            MemberManagementFragment.this.members.remove(member);
                                            Common.showToast(activity, R.string.textDeleteSuccess);
                                        }
                                    } else {
                                        Common.showToast(activity, R.string.textNoNetwork);
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
        if (memberGetAllTask != null) {
            memberGetAllTask.cancel(true);
            memberGetAllTask = null;
        }

        if (memberImageTask != null) {
            memberImageTask.cancel(true);
            memberImageTask = null;
        }

        if (memberDeleteTask != null) {
            memberDeleteTask.cancel(true);
            memberDeleteTask = null;
        }
    }

}
