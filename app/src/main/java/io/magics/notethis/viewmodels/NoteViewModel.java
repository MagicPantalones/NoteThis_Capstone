package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;
import java.util.Observable;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteViewModel extends AndroidViewModel {

    private static final String TAG = "NoteViewModel";
    public static final String NEW_NOTE_TITLE = "New Note";

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference noteRef;
    private String uid;
    private MutableLiveData<Note> note = new MutableLiveData<>();
    private MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
    private MutableLiveData<Boolean> connected = new MutableLiveData<>();
    private MutableLiveData<List<Note>> serverNotes = new MutableLiveData<>();

    private AppDatabase appDatabase;


    public NoteViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
        note.setValue(new Note(NEW_NOTE_TITLE, "", ""));
        connected.setValue(true);
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
    public void deleteNotes(List<NoteTitle> titles) { FirebaseUtils.deleteNote(noteRef, titles); }

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
