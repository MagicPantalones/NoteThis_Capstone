package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.bottomsheet.HeadersSheet;
import io.magics.notethis.ui.fragments.bottomsheet.ImageSheet;
import io.magics.notethis.ui.fragments.bottomsheet.LinkSheet;
import io.magics.notethis.ui.fragments.bottomsheet.ListSheet;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;

public class TemplatesBottomSheet extends Fragment {


    @BindView(R.id.peek_row)
    View peekRow;
    @BindViews({R.id.peek_lists, R.id.peek_headers, R.id.peek_links, R.id.peek_image})
    List<ImageView> peekIcons;
    @BindView(R.id.sheets_pager)
    ViewPager sheetsPager;

    private BottomSheetBehavior behavior;
    private ImageSheet imageSheet;

    private SheetCallbacks callbacks;

    public interface SheetCallbacks {
        void onReturnTemplate(String template);
        void hideAppBar();
    }

    public TemplatesBottomSheet() {
        //Required public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.templates_bottom_sheet, container, false);
        ButterKnife.bind(this, root);
        setRetainInstance(false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        behavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_sheet_fragment));
        setup();
        hide();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SheetCallbacks) callbacks = (SheetCallbacks) context;
    }

    @Override
    public void onDetach() {
        callbacks = null;
        super.onDetach();
    }

    private void setup() {
        NoteViewModel connectedObs = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);
        List<Fragment> sheets = new ArrayList<>();
        ListSheet listSheet = ListSheet.newInstance();
        HeadersSheet headersSheet = HeadersSheet.newInstance();
        LinkSheet linkSheet = LinkSheet.newInstance();
        imageSheet = ImageSheet.newInstance();
        sheets.add(listSheet);
        sheets.add(headersSheet);
        sheets.add(linkSheet);
        sheets.add(imageSheet);

        SheetsAdapter adapter = new SheetsAdapter(getChildFragmentManager(), sheets);
        sheetsPager.setOffscreenPageLimit(4);
        sheetsPager.setAdapter(adapter);
        connectedObs.getConnectionStatus().observe(this,
                connected -> imageSheet.connectionState(connected));

        for (ImageView icon : peekIcons) {
            switch (icon.getId()) {
                case R.id.peek_lists:
                    icon.setOnClickListener(v -> setPage(0));
                    break;
                case R.id.peek_headers:
                    icon.setOnClickListener(v -> setPage(1));
                    break;
                case R.id.peek_links:
                    icon.setOnClickListener(v -> setPage(2));
                    break;
                case R.id.peek_image:
                    icon.setOnClickListener(v -> setPage(3));
                    break;
                default:
                    break;
            }
        }

    }

    private void setPage(int pos) {
        if (behavior == null || behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) return;
        if (callbacks != null) callbacks.hideAppBar();
        sheetsPager.setCurrentItem(pos, true);
        setSheetExpanded();
    }

    public void setSheetExpanded() {
        if (behavior == null) return;
        if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void setSheetCollapsed() {
        if (behavior == null) return;
        if (behavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            setFocusable(true);
        }
    }

    public void hide() {
        if (behavior == null) return;
        if (behavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            setFocusable(false);
        }
    }

    public boolean isExpanded() {
        return behavior != null
                && behavior.getState() != BottomSheetBehavior.STATE_HIDDEN
                && behavior.getState() != BottomSheetBehavior.STATE_COLLAPSED;
    }

    private void setFocusable(boolean focusable) {
        if (getView() != null) {
            getView().setFocusable(focusable);
            getView().setFocusableInTouchMode(focusable);
        }
    }

    public void show() {
        setSheetCollapsed();
    }

    private class SheetsAdapter extends FragmentPagerAdapter {

        List<Fragment> sheets;

        public SheetsAdapter(FragmentManager fm, List<Fragment> sheets) {
            super(fm);
            this.sheets = sheets;
        }

        @Override
        public Fragment getItem(int position) {
            return sheets.get(position);
        }

        @Override
        public int getCount() {
            return sheets.size();
        }
    }

}
