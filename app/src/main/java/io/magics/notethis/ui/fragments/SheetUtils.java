package io.magics.notethis.ui.fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;


import io.magics.notethis.R;

public class SheetUtils {

    public static String getHeader(TextView h, Resources res) {
        switch (h.getId()) {
            case R.id.h1:
                return res.getString(R.string.template_headers_1);
            case R.id.h2:
                return res.getString(R.string.template_headers_2);
            case R.id.h3:
                return res.getString(R.string.template_headers_3);
            case R.id.h4:
                return res.getString(R.string.template_headers_4);
            case R.id.h5:
                return res.getString(R.string.template_headers_5);
            case R.id.h6:
                return res.getString(R.string.template_headers_6);
            default:
                return null;
        }
    }

    public static String getUrlTemplate(Resources res, CharSequence title, CharSequence url) {
        String retString = res.getString(R.string.template_link);
        String retUrl = TextUtils.isEmpty(url) ? "URL" : url.toString();
        String retTitle = TextUtils.isEmpty(title) ? url.toString() : title.toString();

        retString = retString.replace("link-text", retTitle);
        return retString.replace("URL", retUrl);
    }

    public static String getImgTemplate(Resources res, CharSequence alt, CharSequence url) {
        String retString = res.getString(R.string.template_image);
        String retUrl = TextUtils.isEmpty(url) ? "IMG-URL" : url.toString();
        String retTitle = TextUtils.isEmpty(alt) ? url.toString() : alt.toString();

        retString = retString.replace("alt-text", retTitle);
        return retString.replace("IMG-URL", retUrl);
    }

    public static void setBehaviour(View view, Dialog dialog) {
        BottomSheetCallback behaviorCb = new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dialog.dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Required override
            }
        };

        View coordinator;
        if (view.getParent() == null){
            coordinator = view;
        } else {
            coordinator = (View) view.getParent();
        }

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)
                coordinator.getLayoutParams();
        CoordinatorLayout.Behavior behavior = lp.getBehavior();
        if(behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(behaviorCb);
            ((BottomSheetBehavior) behavior).setSkipCollapsed(true);
            dialog.setOnShowListener(dialog1 ->
                    ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED));
        }

    }


    private SheetUtils() {
    }
}
