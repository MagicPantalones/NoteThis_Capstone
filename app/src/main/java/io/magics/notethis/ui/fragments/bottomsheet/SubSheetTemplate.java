package io.magics.notethis.ui.fragments.bottomsheet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.SheetUtils;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;

public class SubSheetTemplate extends BottomSheetDialogFragment {

    @BindView(R.id.sub_template_alt)
    EditText altEtView;
    @BindView(R.id.sub_template_url)
    EditText imgUrlView;
    @BindView(R.id.sub_template_ok_btn)
    Button okBtn;
    @BindView(R.id.sub_template_cxl_btn)
    Button cxlBtn;

    SheetCallbacks callbacks;

    public SubSheetTemplate() {
        //Required
    }

    public static SubSheetTemplate newInstance() {
        return new SubSheetTemplate();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.sub_sheet_template, null);
        ButterKnife.bind(this, view);
        SheetUtils.setBehaviour(view, dialog);



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
}
