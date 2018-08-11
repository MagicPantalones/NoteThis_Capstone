package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.MainActivity;
import io.reactivex.disposables.Disposable;

public class Utils {

    public static final String DIALOG_SAVE = "dialog_save";

    public static void dispose(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Disposable && ((Disposable) object).isDisposed()){
                ((Disposable) object).dispose();
            }
            if (object instanceof Unbinder) {
                ((Unbinder) object).unbind();
            }
        }
    }

    public static void setToolbarTitle(Context context, Toolbar toolbar, Object title,
                                       int colorResId){
        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        int color = ResourcesCompat.getColor(context.getResources(), colorResId, context.getTheme());
        if (ab != null) {
            if (title instanceof Integer) {
                ab.setTitle((int) title);
            } else if (title instanceof String) {
                ab.setTitle((String) title);
            }
            toolbar.setTitleTextColor(color);
        }

    }

    public static String getToolbarTitle(Context context) {
        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        if (ab != null && ab.getTitle() != null) {
            return ab.getTitle().toString();
        }
        return context.getString(R.string.app_name);
    }

    public static void hideToolbar(Context context) {
        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
    }

    public static void showToolbar(Context context) {
        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        if (ab != null) {
            ab.show();
        }
    }
}
