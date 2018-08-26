package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.List;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.data.network.ImgurUtils;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.models.Image;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImgurViewModel extends AndroidViewModel {

    private DatabaseReference imgurRef;
    private boolean isInitialized = false;
    private File selectedFile;
    private CompositeDisposable disposables = new CompositeDisposable();
    private AppDatabase appDatabase;
    private LiveData<List<Image>> images;
    private MutableLiveData<Boolean> initialized = new MutableLiveData<>();
    private MutableLiveData<Image> uploadedImage = new MutableLiveData<>();


    public ImgurViewModel(@NonNull Application application) { super(application); }

    public void init(String uid) {
        if (!isInitialized) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            imgurRef = FirebaseUtils.getImagePath(rootRef, uid);
            imgurRef.keepSynced(true);
            isInitialized = true;
            appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
            images = appDatabase.userImageModel().getImages();
            initialized.setValue(true);
        }
    }

    public boolean isInitialized() { return isInitialized; }

    public LiveData<List<Image>> getImages() { return images; }
    public LiveData<Image> getUploadedImage() { return uploadedImage; }

    private void uploadPhoto(File image, String title) {
        disposables.add(ImgurUtils.getRetrofitClient().create(ImgurUtils.ImgurService.class)
                .uploadImage(
                        ImgurUtils.AUTH_CLIENT_ID,
                        title,
                        null,
                        null,
                        null,
                        MultipartBody.Part.createFormData("image",
                                image.getName(),
                                RequestBody.create(MediaType.parse("image/**"), image)))
                .subscribeOn(Schedulers.io())
                .switchMap(imageResponse -> Observable.just(ImgurUtils.convertToImage(imageResponse)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUploadedImage)
        );
    }

    private void handleUploadedImage(Image image) {

        if (image.getStatus() == ImgurUtils.IMGUR_SUCCESS) {
            AppDbUtils.insertImgurRef(appDatabase, image);
            FirebaseUtils.insertImgurLink(imgurRef, image);
            uploadedImage.setValue(image);
            selectedFile = null;
        } else {
            if (ImgurUtils.IMGUR_ERRORS.containsKey(image.getStatus())) {
                Toast.makeText(getApplication(), ImgurUtils.IMGUR_ERRORS.get(image.getStatus()),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void prepareUpload(File image) {
        selectedFile = image;
    }

    public void upload(String title) {
        if (selectedFile != null) {
            uploadPhoto(selectedFile, title);
        }
    }

    public void deleteImage(Image image) {
        AppDbUtils.deleteImgurRef(appDatabase, image);
        FirebaseUtils.deleteImgurLink(imgurRef, image);
    }

    public void restoreImage(Image image) {
        AppDbUtils.insertImgurRef(appDatabase, image);
        FirebaseUtils.insertImgurLink(imgurRef, image);
    }
}
