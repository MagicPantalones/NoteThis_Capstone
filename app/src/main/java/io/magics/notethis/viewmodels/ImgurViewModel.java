package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import io.magics.notethis.data.network.ImgurUtils;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.ImgurUpload;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImgurViewModel extends AndroidViewModel {

    private DatabaseReference imgurRef;
    private ConnectionLiveData connected;
    private boolean isInitialized = false;
    private CompositeDisposable disposables = new CompositeDisposable();

    public ImgurViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(DatabaseReference userRef) {
        imgurRef = userRef.child("images");
        connected = new ConnectionLiveData(getApplication());
        isInitialized = true;
    }

    public void uploadPhoto(ImgurUpload upload) {
        disposables.add(ImgurUtils.getRetrofitClient().create(ImgurUtils.ImgurService.class)
                .uploadImage(
                        ImgurUtils.AUTH_CLIENT_ID,
                        upload.getTitle(),
                        upload.getDescription(),
                        upload.getAlbumId(),
                        null,
                        MultipartBody.Part.createFormData("image",
                                upload.getImage().getName(),
                                RequestBody.create(MediaType.parse("image/**"), upload.getImage())))
                .subscribeOn(Schedulers.io())
                .switchMap(imageResponse -> Observable.just(ImgurUtils.convertToImage(imageResponse)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUploadedImage)
        );
    }

    private void handleUploadedImage(Image image) {

    }

}
