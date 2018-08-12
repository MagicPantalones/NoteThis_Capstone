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
    private final AppDatabase appDatabase;
    private final Activity activity;
    private boolean connected;

    private Disposable roomTitleDisposable;
    private Disposable roomInsertNoteDisposable;
    private Disposable roomUpdateDisposable;
    private Disposable roomNoteDisposable;
    private Disposable delNotesTableDisposable;

    private DataProviderHandler providerHandler;

    private FirebaseInstance fireBaseInstance;


    public DataProvider(Activity activity, DataProviderHandler providerHandler) {
        this.activity = activity;
        this.providerHandler = providerHandler;
        this.appDatabase = AppDatabase.getInMemoryDatabase(activity.getApplication());
    }

    public void init() {
        startUpQuery();
        fireBaseInstance = new FirebaseInstance();
        fireBaseInstance.init();
    }

    public void dispose(List<NoteTitle> titles) {
        Utils.dispose(
                roomTitleDisposable,
                roomInsertNoteDisposable,
                roomUpdateDisposable,
                roomNoteDisposable
        );
        delNotesTableDisposable = deleteNotes(titles);
    }

    private void startUpQuery() {
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
                    if (longList != null && longList.size() == 1) {
                        providerHandler.onNoteInserted(longList.get(0).intValue());
                        notes[0].setId(longList.get(0).intValue());
                    }
                    insertNotesToFireBase(notes);
                }, this::handleRxErrors);
    }

    private void insertNotesToFireBase(Note... notes) {
        if (notes != null && notes.length > 0) {
            for (Note note : notes) {
                fireBaseInstance.writeNewNote(note);
            }
        }
    }

    public void updateNote(Note note) {
        roomUpdateDisposable = updateNoteRoom(note);
    }

    private Disposable deleteNotes(List<NoteTitle> noteTitles) {
        return Completable.fromAction(() -> {
            for (NoteTitle title : noteTitles) {
                appDatabase.userNoteModel().deleteNotes(title.getId());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> fireBaseInstance.deleteNote(noteTitles), this::handleRxErrors);
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
        fireBaseInstance.writeNewNote(note);
    }

    public void getNoteById(int id) {
        roomNoteDisposable = getOneNote(id);
    }

    private Disposable getOneNote(int id) {
        return Observable.fromCallable(() -> appDatabase.userNoteModel().getNote(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(note -> providerHandler.onNoteFetched(note));
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

    public interface DataProviderHandler {
        void onNoteTitlesFetched(List<NoteTitle> noteTitles);

        void onNoteInserted(int id);

        void onNoteFetched(Note note);

        void onFirebaseTitlesFetched(List<Note> notes);

        void onConnectionChange(Boolean connected);

        void onError(DataError dataError);
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

        private static final String PATH_USER = "users";
        private static final String PATH_NOTES = "notes";

        private FirebaseDatabase fireDb;
        private FirebaseAuth auth;
        private DatabaseReference fireDbRef;
        private DatabaseReference userPathRef;
        String userUid;
        String userEmail;

        FirebaseInstance() {
        }

        private void init() {

            auth = FirebaseAuth.getInstance();
            fireDb = FirebaseDatabase.getInstance();
            fireDbRef = fireDb.getReference();
            userPathRef = fireDbRef.child(PATH_USER);

            if (auth.getCurrentUser() == null) {
                auth.signInWithEmailAndPassword(TempVals.USER, TempVals.CODE)
                        .addOnCompleteListener(activity, task -> {
                            if (task.isSuccessful()) {
                                onSignIn(task.getResult().getUser());
                            } else {
                                Log.w(TAG, "Error signing in: ", task.getException());
                            }
                        });
            } else {
                onSignIn(auth.getCurrentUser());
            }
        }

        private void onSignIn(FirebaseUser user) {
            userUid = user.getUid();
            userEmail = user.getEmail();
            checkUserExist();

            fireDbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.w(TAG, "Wrote successfully to DB");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleFirebaseErrors(databaseError);
                }
            });

            fireDbRef.child(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    connected = dataSnapshot.getValue(Boolean.class);
                    providerHandler.onConnectionChange(connected);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleFirebaseErrors(databaseError);
                }
            });
        }

        private void checkUserExist() {
            fireDbRef.child(PATH_USER).child(userUid).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                fireDbRef.child(PATH_USER).child(userUid)
                                        .setValue(new User(userEmail)).addOnCompleteListener(task ->
                                        userPathRef = fireDbRef.child(PATH_USER).child(userUid));
                            } else {
                                userPathRef = fireDbRef.child(PATH_USER).child(userUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            handleFirebaseErrors(databaseError);
                        }
                    });
        }


        private void handleFirebaseErrors(DatabaseError databaseError) {
            switch (databaseError.getCode()) {
                case DatabaseError.NETWORK_ERROR:
                    providerHandler.onError(DataError.NO_INTERNET_FIRE_DB);
                    break;
                case DatabaseError.WRITE_CANCELED:
                    providerHandler.onError(DataError.FIREBASE_WRITE_ERROR);
                    break;
                default:
                    providerHandler.onError(DataError.UNHANDLED_ERROR);
                    break;
            }
        }

        private void writeNewNote(Note note) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(PATH_NOTES + note.getId(), note.toMap());
            userPathRef.updateChildren(childUpdates);
        }

        private void deleteNote(List<NoteTitle> noteTitles) {
            for (NoteTitle title : noteTitles) {
                userPathRef.child(PATH_NOTES).child(String.valueOf(title.getId())).removeValue();
            }
        }


    }

}