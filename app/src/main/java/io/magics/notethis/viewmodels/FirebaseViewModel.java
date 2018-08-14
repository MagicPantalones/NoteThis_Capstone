package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class FirebaseViewModel extends AndroidViewModel {

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference noteRef;
    private String uid;
    private MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
    private MutableLiveData<Boolean> connected = new MutableLiveData<>();
    private MutableLiveData<List<Note>> serverNotes = new MutableLiveData<>();

    public FirebaseViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        connected.setValue(true);
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        signIn();
    }

    public LiveData<Boolean> getSignInStatus() {
        return signedIn;
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connected;
    }

    public void insertNote(Note note) {
        FirebaseUtils.insertNote(noteRef, note);
    }
    //I even though insert and update does the same thing. I have created two methods for usability.
    public void updateNote(Note note) {
        FirebaseUtils.insertNote(noteRef, note);
    }

    public void deleteNotes(List<NoteTitle> titles) {
        FirebaseUtils.deleteNote(noteRef, titles);
    }

    public LiveData<List<Note>> fetchAllNotesFromServer() {
        FirebaseUtils.getAllNotes(userRef, new FirebaseUtils.FirebaseDbCallback() {
            @Override
            public void onComplete(List<Note> notes) {
                serverNotes.setValue(notes);
            }

            @Override
            public void onError(DatabaseError error) {
                serverNotes.setValue(null);
            }
        });
        return serverNotes;
    }

    private void signIn(){
        FirebaseUtils.getUserFromAuth(auth, rootRef, new FirebaseUtils.FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                signedIn.setValue(true);
                uid = user.getUid();
                userRef = FirebaseUtils.getUserPath(rootRef, uid);
                noteRef = FirebaseUtils.getNotesPath(rootRef, uid);
                observeConnection();
            }

            @Override
            public void onFailed(Throwable e) {
                signedIn.setValue(false);
            }
        });
    }

    private void observeConnection() {
        FirebaseUtils.getConnectionStatusRef(rootRef).addValueEventListener(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                connected.setValue(dataSnapshot.getValue(Boolean.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Required override
            }
        });
    }



}
