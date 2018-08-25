package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import java.util.List;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteViewModel extends AndroidViewModel {

    private static final String TAG = "NoteViewModel";
    public static final String NEW_NOTE_TITLE = "New Note";

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference noteRef;
    private MutableLiveData<Note> note = new MutableLiveData<>();
    private MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
    private ConnectionLiveData connected;

    private AppDatabase appDatabase;


    public NoteViewModel(@NonNull Application application) {
        super(application);
    }

    public DatabaseReference getUserRef() { return userRef; }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
        note.setValue(new Note(NEW_NOTE_TITLE, "", ""));
        connected = new ConnectionLiveData(getApplication());
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        signIn();
    }

    public void newNote() {
        note.setValue(new Note(NEW_NOTE_TITLE, "", ""));
    }

    public LiveData<Note> getNote() { return note; }
    public LiveData<Boolean> getSignInStatus() { return signedIn; }
    public LiveData<Boolean> getConnectionStatus() { return connected; }
    public void deleteNote(Note note) { FirebaseUtils.deleteNote(noteRef, note); }

    public void editNote(int id) {
        AppDbUtils.fetchNote(appDatabase, id, new RoomNoteCallback<Note>() {
            @Override
            public void onComplete(Note data) {
                note.setValue(data);
            }

            @Override
            public void onFail(Throwable e) {
                AppDbUtils.handleDbErrors(TAG, e);
            }
        });
    }

    public void saveChanges(String title, String oldTitle) {
        Note saveNote = note.getValue();
        saveNote.setTitle(title);

        note.setValue(saveNote);

        if (oldTitle.equals(NEW_NOTE_TITLE)) {
            AppDbUtils.insertNote(appDatabase, saveNote, new RoomNoteCallback<Note>() {
                @Override
                public void onComplete(Note data) {
                    note.setValue(data);
                    FirebaseUtils.insertNote(noteRef, data);
                }

                @Override
                public void onFail(Throwable e) {
                    AppDbUtils.handleDbErrors(TAG, e);
                }
            });
        } else {
            AppDbUtils.updateNote(appDatabase, saveNote);
            FirebaseUtils.insertNote(noteRef, saveNote);
        }
    }

    public boolean hasUnsavedChanges(String text) {
        if (TextUtils.isEmpty(text)) return false;

        boolean hasChanged = !text.equals(note.getValue().getBody());

        if (hasChanged) {
            Note tempNote = note.getValue();
            tempNote.setBody(text);
            tempNote.setBodyPreview();
            note.setValue(tempNote);
        }

        return hasChanged;
    }

    private void signIn(){
        FirebaseUtils.getUserFromAuth(auth, new FirebaseUtils.FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                handleSignIn(user);
            }

            @Override
            public void onFailed(Throwable e) {
                signedIn.setValue(false);
            }
        });
    }

    public void signOut(){
        auth.signOut();
        signedIn.setValue(false);
        AppDbUtils.deleteNoteTable(appDatabase);
        AppDbUtils.deleteImgurTable(appDatabase);
    }

    public void onGoogleSignIn(GoogleSignInAccount account) {
        Log.d(TAG, "Firebase Auth with Google: " + account.getId());
        auth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handleSignIn(task.getResult().getUser());
                    }
                });
    }

    private void handleSignIn(FirebaseUser user) {
        signedIn.setValue(true);
        String uid = user.getUid();
        userRef = FirebaseUtils.getUserPath(rootRef, uid);
        noteRef = FirebaseUtils.getNotesPath(rootRef, uid);
        FirebaseUtils.checkForUserEmail(userRef, user.getEmail());
        checkDatabase();
        checkImageDatabase();
    }

    private void checkDatabase() {
        AppDbUtils.lookForNoteData(appDatabase, rows -> {
            if (rows <= 0) {
                FirebaseUtils.getAllNotes(noteRef, notes -> {
                    if (notes != null && !notes.isEmpty()) {
                        AppDbUtils.insertNotes(appDatabase, notes);
                    }
                });
            }
        });
    }

    private void checkImageDatabase() {
        AppDbUtils.lookForImgurData(appDatabase, rows -> {
            if (rows <= 0) {
                FirebaseUtils.getAllImgurLinks(userRef.child("images"), images1 -> {
                    if (images1 != null && !images1.isEmpty()) {
                        AppDbUtils.insertImgurRefs(appDatabase, images1);
                    }
                });
            }
        });
    }
}
