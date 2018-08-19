package io.magics.notethis.ui.fragments;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;

public class ImgurListFragment extends Fragment {

    @BindView(R.id.imgur_recycler)
    RecyclerView imgurRecycler;
    @BindView(R.id.imgur_list_progress)
    ProgressBar imgurProgress;

    Unbinder unbinder;

    public ImgurListFragment() {
        // Required empty public constructor
    }

    public static ImgurListFragment newInstance() {
        return new ImgurListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_imgur_list, container, false);
        unbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class ImgurAdapter {


    }

    class ImgurViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.)

        public ImgurViewHolder(View itemView) {
            super(itemView);
        }
    }

    //Item spacing decorator based on ianhanniballake's answer here:
    //https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
    private class ItemSpacingDecoration extends RecyclerView.ItemDecoration {

        private int spacing;

        ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            int column = pos % 2;

            if (column == 0) {
                outRect.left = spacing;
            } else {
                outRect.right = spacing;
            }
            outRect.bottom = spacing;
        }
    }
}
