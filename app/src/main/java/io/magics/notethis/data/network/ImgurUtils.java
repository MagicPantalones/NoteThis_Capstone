package io.magics.notethis.data.network;

import android.annotation.SuppressLint;

import com.google.firebase.database.collection.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.haha.guava.collect.Collections2;
import com.squareup.haha.guava.collect.Maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.magics.notethis.BuildConfig;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Image;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class ImgurUtils {

    private static final String BASE_URL = "https://api.imgur.com";
    public static final String AUTH_CLIENT_ID = "Client-ID " + BuildConfig.ImgurClientId;

    public static final Map<Integer, String> IMGUR_ERRORS;
    static {
        @SuppressLint("UseSparseArrays")
        Map<Integer, String> map = new HashMap<>();
        map.put(-1, "App error. Try uploading file again.");
        map.put(400, "Img corrupted/does not meet specifications.");
        map.put(401, "Invalid user credentials.");
        map.put(403, "Forbidden access, app tokens expired.");
        map.put(404, "Image does not exist.");
        map.put(429, "To many requests.");
        map.put(500, "Imgur service broke.");
        IMGUR_ERRORS = Collections.unmodifiableMap(map);
    }

    public static final int IMGUR_SUCCESS = 200;

    public interface ImgurService {
        @Multipart
        @POST("/3/image")
        Observable<ImageResponse> uploadImage(
                @Header("Authorization") String auth,
                @Query("title") String title,
                @Query("description") String description,
                @Query("album") String albumId,
                @Query("account_url") String accountUrl,
                @Part MultipartBody.Part file
        );
    }

    public static Retrofit getRetrofitClient() {
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

    public static Image convertToImage(ImageResponse imageResponse) {
        Image image = new Image();
        if (imageResponse != null) {
            if (imageResponse.success) {
                image.setLink(imageResponse.data.link);
                image.setTitle(imageResponse.data.title);
                image.setServerId(imageResponse.data.id);
            }
            image.setStatus(imageResponse.status);
        }

        return image;
    }
}
