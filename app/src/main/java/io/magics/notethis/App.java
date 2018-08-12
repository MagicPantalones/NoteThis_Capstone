package io.magics.notethis;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
