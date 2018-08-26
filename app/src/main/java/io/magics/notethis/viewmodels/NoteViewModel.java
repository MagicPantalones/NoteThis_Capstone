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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Note;

public class NoteViewModel extends AndroidViewModel {

    private static final String TAG = "NoteViewModel";
    public static final String NEW_NOTE_TITLE = "New Note";

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference noteRef;
    private MutableLiveData<Note> note = new MutableLiveData<>();
    private MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
    private MutableLiveData<FirebaseUser> firebaseUser = new MutableLiveData<>();
    private ConnectionLiveData connected;
    private Note oldNote;

    private AppDatabase appDatabase;


    public NoteViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<FirebaseUser> getFirebaseUser() { return firebaseUser; }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
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

    public void fetchNote(int id) {
        AppDbUtils.fetchNote(appDatabase, id, new RoomNoteCallback<Note>() {
            @Override
            public void onComplete(Note data) {
                if (note.getValue() == null || note.getValue().getId() != id) {
                    note.setValue(data);
                } else {
                    note.setValue(note.getValue());
                }
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
                    Note newOldNote = new Note(title, saveNote.getBody(), saveNote.getPreview());
                    newOldNote.setId(saveNote.getId());
                    oldNote = newOldNote;
                }

                @Override
                public void onFail(Throwable e) {
                    AppDbUtils.handleDbErrors(TAG, e);
                }
            });
        } else {
            Note newOldNote = new Note(title, saveNote.getBody(), saveNote.getPreview());
            newOldNote.setId(saveNote.getId());
            oldNote = newOldNote;
            AppDbUtils.updateNote(appDatabase, saveNote);
            FirebaseUtils.insertNote(noteRef, saveNote);
        }
    }

    public boolean hasUnsavedChanges(String text) {
        if (TextUtils.isEmpty(text) || note.getValue() == null) return false;
        boolean hasChanged = !text.equals(oldNote != null ? oldNote.getBody()
                : note.getValue().getBody());

        if (hasChanged) {
            Note newNote = note.getValue();
            newNote.setBody(text);
            newNote.setBodyPreview();
            note.setValue(newNote);
        }

        return hasChanged;
    }

    public void setOldData(Note note) {
        if (oldNote == null || oldNote.getId() != note.getId()) {
            oldNote = note;
        }
    }

    public void clearNote() {
        note.setValue(null);
        oldNote = null;
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
        userRef.keepSynced(true);
        noteRef = FirebaseUtils.getNotesPath(rootRef, uid);
        noteRef.keepSynced(true);
        FirebaseUtils.checkForUserEmail(userRef, user.getEmail());
        firebaseUser.setValue(user);
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
