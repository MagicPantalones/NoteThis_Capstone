package io.magics.notethis.data.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
            }
            image.setStatus(imageResponse.status);
        }
        
        return image;
    }
}
