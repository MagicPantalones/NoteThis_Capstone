package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.MainActivity;
import io.reactivex.disposables.Disposable;

public class Utils {

    private static final String TAG = "Utils";

    public static final String DIALOG_SAVE = "dialog_save";
    public static final String DIALOG_CLOSE = "dialog_close";
    public static final String DIALOG_UPLOAD = "dialog_upload";

    public static final int SDK_V = Build.VERSION.SDK_INT;


    public static void dispose(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Disposable && ((Disposable) object).isDisposed()) {
                ((Disposable) object).dispose();
            }
            if (object instanceof Unbinder) {
                ((Unbinder) object).unbind();
            }
        }
    }

    public static void setToolbarTitle(Context context, Object title, int colorResId) {
        Toolbar toolbar = ((MainActivity) context).findViewById(R.id.main_toolbar);
        if (toolbar != null) {
            int color = ResourcesCompat.getColor(context.getResources(), colorResId, null);

            if (title instanceof Integer) {
                toolbar.setTitle((int) title);
            } else if (title instanceof String) {
                toolbar.setTitle((String) title);
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

    public static void backPressed(Context context) {
        ((MainActivity) context).onBackPressed();
    }

    @SuppressWarnings("unchecked")
    public static <V extends View> V bindEither(View view, @IdRes int id1, @IdRes int id2) {
        View v1 = view.findViewById(id1);
        View v2 = view.findViewById(id2);

        if (v1 == null) return (V) v2;
        else return (V) v1;
    }

    //Solution from rafsanahmad007's answer here: https://stackoverflow.com/questions/41718633/bottomnavigationview-with-more-than-3-items-tab-title-is-hiding
    public static void removeShiftMode(BottomNavigationView botNav) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) botNav.getChildAt(0);
        try {
            Field shiftMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftMode.setAccessible(true);
            shiftMode.setBoolean(menuView, false);
            shiftMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(false);
                item.setCheckable(false);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.w(TAG, "removeShiftMode: ", e);
        }
    }

    public static Transition getSignInTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_shared);
    }

    public static Transition getSignInEnterTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_enter);
    }

    public static Transition getIntroToSignInTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_exit);
    }

    private Utils() {}
}
