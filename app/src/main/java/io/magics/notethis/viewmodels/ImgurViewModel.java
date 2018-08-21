package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.io.File;

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

    public ImgurViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(DatabaseReference userRef) {
        if (!isInitialized) {
            imgurRef = userRef.child("images");
            isInitialized = true;
            appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
        }
    }

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
            //TODO Write Image model to Room & FirebaseDB
            AppDbUtils.insertImgurRef(appDatabase, image);
            FirebaseUtils.insertImgurLink(imgurRef, image);
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

    public File getSelectedFile() { return selectedFile; }
}
