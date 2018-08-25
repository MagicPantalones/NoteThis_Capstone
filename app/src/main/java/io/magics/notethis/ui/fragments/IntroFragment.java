package io.magics.notethis.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.magics.notethis.R;
import io.magics.notethis.utils.FragmentHelper;

import static io.magics.notethis.utils.FragmentHelper.getTransition;

public class IntroFragment extends Fragment {

    public static IntroFragment newInstance() {
        return new IntroFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(getTransition(Gravity.END));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }
}
