package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

public class ApiViewModel extends AndroidViewModel {

    private DatabaseReference userRef;
    private DatabaseReference noteRef;



    public ApiViewModel(@NonNull Application application) {
        super(application);
    }


}
