package io.magics.notethis.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;

public class IntroFragment extends Fragment {

    private static final String TAG = "IntroFragment";

    Unbinder unbinder;

    public static IntroFragment newInstance() {
        return new IntroFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_intro, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

}
