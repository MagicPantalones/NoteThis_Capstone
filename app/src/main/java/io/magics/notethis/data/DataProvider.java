package io.magics.notethis.data;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAccumulator;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.utils.RoomInsertException;
import io.magics.notethis.utils.TempVals;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.magics.notethis.utils.models.User;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataProvider {

    private static final String TAG = "DataProvider";

    private static final String BASE_URL = "https://api.imgur.com";
    private boolean connected;

    public DataProvider() {
    }

    public void init() {

    }

    public void dispose(List<NoteTitle> titles) {
    }

    private Retrofit getRetrofitClient() {
        Gson gson = new GsonBuilder().create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

}