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

    private DataProviderHandler providerHandler;

    private FirebaseInstance fireBaseInstance;


    public DataProvider(DataProviderHandler providerHandler) {
        this.providerHandler = providerHandler;
    }

    public void init() {
        fireBaseInstance = new FirebaseInstance();
        fireBaseInstance.init();
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