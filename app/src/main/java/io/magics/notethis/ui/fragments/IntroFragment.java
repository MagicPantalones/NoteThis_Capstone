package io.magics.notethis.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import io.magics.notethis.ui.fragments.NoteListFragment.FabListener;

public class IntroFragment extends Fragment {

    FabListener fabListener;

    public static IntroFragment newInstance() {
        return new IntroFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (fabListener != null) fabListener.hideFab();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FabListener) fabListener = (FabListener) context;
    }

    @Override
    public void onDetach() {
        fabListener = null;
        super.onDetach();
    }
}
