package io.magics.notethis.ui.fragments.bottomsheet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.magics.notethis.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LinkSheet extends Fragment {


    public LinkSheet() {
        // Required empty public constructor
    }

    public static LinkSheet newInstance() {
        return new LinkSheet();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sheet_link, container, false);
    }

}
