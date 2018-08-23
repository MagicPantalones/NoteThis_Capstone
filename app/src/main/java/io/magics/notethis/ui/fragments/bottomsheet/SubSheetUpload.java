package io.magics.notethis.ui.fragments.bottomsheet;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.SheetUtils;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.ImgurViewModel;

public class SubSheetUpload extends BottomSheetDialogFragment {

    private static final String ARG_IMG = "img";

    @BindView(R.id.sub_upload_preview)
    ImageView previewView;
    @BindView(R.id.sub_upload_img_title)
    EditText imgTitleView;
    @BindView(R.id.sub_upload_ok_btn)
    Button okBtn;
    @BindView(R.id.sub_upload_cxl_btn)
    Button cxlBtn;
    @BindView(R.id.sub_upload_progress)
    ProgressBar uploadProgress;

    ImgurViewModel model;

    SheetCallbacks callbacks;

    public SubSheetUpload() {
        //Required
    }

    public static SubSheetUpload newInstance(File imgFile) {
        SubSheetUpload frag = new SubSheetUpload();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMG, imgFile);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.sub_sheet_upload, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        SheetUtils.setBehaviour(view, dialog);
        
        File img = (File) getArguments().getSerializable(ARG_IMG);

        GlideApp.with(this)
                .load(img)
                .error(R.drawable.owl_24dp_color)
                .into(previewView);

        okBtn.setOnClickListener(v -> {
            if (!Utils.isConnected(getContext())) {
                dismiss();
                return;
            }
            setLoading();
            String title = TextUtils.isEmpty(imgTitleView.getText()) ? "Uploaded Image" :
                    imgTitleView.getText().toString();
            model.upload(title);
            model.getUploadedImage().observe(this, image -> {
                model.getUploadedImage().removeObservers(this);
                if (image == null)return;
                if (callbacks != null) {
                    callbacks.onReturnTemplate(SheetUtils.getImgTemplate(getResources(),
                            image.getTitle(), image.getLink()));
                }
            });
        });

        cxlBtn.setOnClickListener(v -> dismiss());
    }

    private void setLoading(){
        uploadProgress.setVisibility(View.VISIBLE);
        okBtn.setVisibility(View.GONE);
        cxlBtn.setVisibility(View.GONE);
        imgTitleView.setEnabled(false);
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

