package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

    public interface SheetCallbacks {
        void onReturnTemplate(String template);
    }

    public TemplatesBottomSheet() {
        //Required public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.templates_bottom_sheet, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        behavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_sheet_fragment));
        setup();
    }


    private void setup() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        NoteViewModel connectedObs = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);

        List<Fragment> sheets = new ArrayList<>();
        imageSheet = ImageSheet.newInstance();
        sheets.add(ListSheet.newInstance());
        sheets.add(HeadersSheet.newInstance());
        sheets.add(LinkSheet.newInstance());
        sheets.add(imageSheet);

        SheetsAdapter adapter = new SheetsAdapter(getChildFragmentManager(), sheets);
        sheetsPager.setAdapter(adapter);

        connectedObs.getConnectionStatus().observe(this,
                connected -> imageSheet.connectionState(connected));

        for (ImageView icon : peekIcons) {
            switch (icon.getId()) {
                case R.id.peek_lists:
                    icon.setOnClickListener(v -> sheetsPager.setCurrentItem(0, true));
                    break;
                case R.id.peek_headers:
                    icon.setOnClickListener(v -> sheetsPager.setCurrentItem(1, true));
                    break;
                case R.id.peek_links:
                    icon.setOnClickListener(v -> sheetsPager.setCurrentItem(2, true));
                    break;
                case R.id.peek_image:
                    icon.setOnClickListener(v -> sheetsPager.setCurrentItem(3, true));
                    break;
                default:
                    break;
            }
        }

    }

    public void setSheetExpanded() {
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void setSheetCollapsed() {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void hide() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
