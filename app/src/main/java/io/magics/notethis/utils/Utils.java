package io.magics.notethis.utils;

import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

public class Utils {

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
}
