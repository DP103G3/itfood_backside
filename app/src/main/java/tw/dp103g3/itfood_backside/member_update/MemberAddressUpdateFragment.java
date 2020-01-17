package tw.dp103g3.itfood_backside.member_update;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.dp103g3.itfood_backside.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberAddressUpdateFragment extends Fragment {


    public MemberAddressUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_address_update, container, false);
    }

}
