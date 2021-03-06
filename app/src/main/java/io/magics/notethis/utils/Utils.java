package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    interface OnKeyboardHiddenListener {
        void onHidden();
    }

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

    public static boolean isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();

    }

    public static void onConnectionStateChange(Context context, final Snackbar snackbar,
                                               boolean status) {
        if (!status) {
            snackbar.show();
            snackbar.setAction(R.string.hide_snack, v -> snackbar.dismiss());

            int color = ResourcesCompat.getColor(context.getResources(), R.color.secondaryColor,
                    null);

            snackbar.setActionTextColor(color);
            snackbar.show();

        } else {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
        }
    }

    public static void hideKeyboard(Activity activity, @Nullable OnKeyboardHiddenListener listener) {
        try {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (listener != null) {
                new Handler().postDelayed(listener::onHidden, 50);
            }
        } catch (Exception e) {
            Log.e(TAG, "hideKeyboard: ", e);
        }
    }

    public static void hideKeyboard(Activity activity) {
        hideKeyboard(activity, (OnKeyboardHiddenListener) null);
    }


    public static void hideKeyboard(Context context, View view,
                                    @Nullable OnKeyboardHiddenListener listener) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
            if (listener != null) {
                new Handler().postDelayed(listener::onHidden, 50);
            }
        } catch (Exception e) {
            Log.e(TAG, "hideKeyboard with View: ", e);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        hideKeyboard(context, view, null);
    }



    private Utils() {}
}
