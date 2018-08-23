package io.magics.notethis.ui.fragments.bottomsheet;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.magics.notethis.R;


public class ImageSheet extends Fragment {


    public ImageSheet() {
        // Required empty public constructor
    }
    public static ImageSheet newInstance() {
        return new ImageSheet();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sheet_image, container, false);
    }

}
