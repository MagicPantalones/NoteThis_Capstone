package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ViewTarget;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.GlideApp;

public class ImageSheetDialog extends BottomSheetDialogFragment {

    private static final String KEY_URL = "URL";


    @BindView(R.id.sheet_image)
    ImageView dialogImage;

    private String url;

    public ImageSheetDialog() {
        //Required constructor
    }

    public static ImageSheetDialog newInstance(String url) {
        ImageSheetDialog dialog = new ImageSheetDialog();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(getContext(), R.layout.image_bottom_sheet, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);

        BottomSheetCallback behaviorCb = new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN ||
                        newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Required override
            }
        };

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)
                ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = lp.getBehavior();


        if(behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setSkipCollapsed(true);
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(behaviorCb);
            view.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ((BottomSheetBehavior) behavior).setPeekHeight(view.getMeasuredHeight());
                }
            });
        }

        if (getArguments() != null) {
            url = getArguments().getString(KEY_URL);
        }

        GlideApp.with(view)
                .load(url)
                .override(ViewTarget.SIZE_ORIGINAL)
                .into(dialogImage);

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            bottomSheet.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.dark_transparent, null));
        }

    }
}
