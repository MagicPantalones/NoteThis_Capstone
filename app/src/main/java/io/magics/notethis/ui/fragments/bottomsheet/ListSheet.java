package io.magics.notethis.ui.fragments.bottomsheet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.magics.notethis.R;


public class ListSheet extends Fragment {


    public ListSheet() {
        // Required empty public constructor
    }

    public static ListSheet newInstance() {
        return new ListSheet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sheet_list, container, false);
    }

}
