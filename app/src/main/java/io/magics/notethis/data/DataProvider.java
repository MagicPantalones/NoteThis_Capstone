package io.magics.notethis.data;

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

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.utils.FirebaseUtils;
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
    private final AppDatabase appDatabase;

    private Disposable roomTitleDisposable;
    private Disposable roomInsertNoteDisposable;
    private Disposable roomUpdateDisposable;
    private Disposable delNotesTableDisposable;

    private DataProviderHandler providerHandler;

    private FirebaseInstance fireBaseInstance;

    public interface DataProviderHandler {
        void onNoteTitlesFetched(List<NoteTitle> noteTitles);
        void onNoteInserted(int id);
        void onError(DataError dataError);
    }

    public DataProvider(AppDatabase appDatabase,
                        DataProviderHandler providerHandler) {
        this.appDatabase = appDatabase;
        this.providerHandler = providerHandler;
    }

    public void init(){
        startUpQuery();
        fireBaseInstance = new FirebaseInstance();
        fireBaseInstance.init();
    }

    public void dispose(){
        Utils.dispose(
                roomTitleDisposable,
                roomInsertNoteDisposable,
                roomUpdateDisposable
        );
    }

    private void startUpQuery(){
        roomTitleDisposable = noteTitleRoomQuery();
    }

    private Disposable noteTitleRoomQuery() {
        return appDatabase.userNoteModel().getNoteTitles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(noteTitles -> providerHandler.onNoteTitlesFetched(noteTitles),
                        this::handleRxErrors);
    }

    private void handleRxErrors(Throwable e) {
        Log.e(TAG, "handleRxErrors: ", e);
        if (e instanceof RoomInsertException) {
            providerHandler.onError(DataError.ROOM_WRITE_ERROR);
        } else {
            providerHandler.onError(DataError.UNHANDLED_ERROR);
        }
    }

    public void insertNotes(Note... notes) {
        roomInsertNoteDisposable = insertNotesToRoom(notes);
    }

    private Disposable insertNotesToRoom(Note... notes) {
        return Observable.fromCallable(() -> appDatabase.userNoteModel().insertAll(notes))
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(Observable.error(new RoomInsertException()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(longList -> {
                    if (longList != null && longList.size() == 1){
                        providerHandler.onNoteInserted(longList.get(0).intValue());
                        notes[0].setId(longList.get(0).intValue());
                    }
                    insertNotesToFireBase(notes);
                }, this::handleRxErrors);
    }

    private void insertNotesToFireBase(Note... notes) {
        if (notes != null && notes.length > 0){
            for (Note note : notes) {
                fireBaseInstance.writeNewNote(note);
            }
        }
    }

    public void updateNote(Note note) {
        roomUpdateDisposable = updateNoteRoom(note);
    }

    private Disposable dropNoteTable() {
        return Completable.fromAction(() -> appDatabase.userNoteModel().deleteAll())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private Disposable updateNoteRoom(Note note) {
        return Completable.fromAction(() -> appDatabase.userNoteModel().updateNote(note))
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(e -> Completable.error(new RoomInsertException("Update failed")))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> updateNoteFirebase(note), this::handleRxErrors);
    }

    private void updateNoteFirebase(Note note) {
        Log.w(TAG, "updateNoteFirebase: Not implemented yet");
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

    public enum DataError {
        NO_DATA_AVAILABLE,
        NO_INTERNET_LOG_IN,
        NO_INTERNET_IMGUR,
        NO_INTERNET_FIRE_DB,
        ROOM_WRITE_ERROR,
        FIREBASE_WRITE_ERROR,
        UNHANDLED_ERROR
    }

    private class FirebaseInstance {

        private static final String PATH_USER = "path_user";

        private final FirebaseAuth auth;
        private final DatabaseReference fireDatabase;
        String userUid;
        String userEmail;

        FirebaseInstance() {
            auth = FirebaseAuth.getInstance();
            fireDatabase = FirebaseDatabase.getInstance().getReference();
        }

        private void init() {

            fireDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.w(TAG, "Wrote successfully to DB");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    
                }
            });

            if (auth.getCurrentUser() == null) {
                auth.signInWithEmailAndPassword(TempVals.USER, TempVals.CODE)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.w(TAG, "Success");
                                final FirebaseUser user = auth.getCurrentUser();
                                userUid = user != null ? user.getUid() : auth.getUid();
                                userEmail = user != null ? user.getEmail() : null;
                                checkUserExist();
                            } else {
                                Log.w(TAG, "Error signing in: ", task.getException());
                            }
                        });
            } else {
                final FirebaseUser user = auth.getCurrentUser();
                userUid = user.getUid();
                userEmail = user.getEmail();
            }
        }

        private void checkUserExist() {
            fireDatabase.child(PATH_USER).child(userUid).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()){
                                Log.w(TAG, "User does not exist. Writing new user");
                                writeNewUser();
                            } else {
                                Log.w(TAG, "User exists");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "Firebase DB user write error: ", databaseError.toException());
                        }
                    });
        }

        private void writeNewUser() {
            fireDatabase.child(PATH_USER).child(userUid).setValue(new User(userEmail));
        }

        private void writeNewNote(Note note) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/notes/" + note.getId(), note.toMap());
            fireDatabase.child(PATH_USER).child(userUid).updateChildren(childUpdates);
        }


    }

}
