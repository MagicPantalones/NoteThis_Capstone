package io.magics.notethis.data.network;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ImgurApi {


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
