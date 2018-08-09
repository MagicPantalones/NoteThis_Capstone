package io.magics.notethis.data;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataProvider {

    private static final String BASE_URL = "https://api.imgur.com";
    private final Context context;

    public DataProvider(Context context) {
        this.context = context;
    }

    public void init(){

    }

    public void dispose(){

    }

    private void startUpQuery(){

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

    private class FirebaseHandler {

        FirebaseHandler() {

        }

    }

}
