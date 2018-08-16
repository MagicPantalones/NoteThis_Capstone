package io.magics.notethis.data.network;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.magics.notethis.utils.TempVals;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.magics.notethis.utils.models.User;

public class FirebaseUtils {

    private static final String PATH_USERS = "users";
    private static final String PATH_NOTES = "notes";

    public interface FirebaseAuthCallback{
        void onSuccess(FirebaseUser user);
        void onFailed(Throwable e);
    }

    public interface FirebaseDbCallback {
        void onComplete(List<Note> notes);
        void onError(DatabaseError error);
    }

    public static void getUserFromAuth(FirebaseAuth auth, DatabaseReference rootRef,
                                       FirebaseAuthCallback callback) {
        if (auth.getCurrentUser() == null) {
            auth.signInWithEmailAndPassword(TempVals.USER, TempVals.CODE)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            callback.onSuccess(user);
                            initUserInDb(rootRef, user.getUid(), user.getEmail());
                        } else {
                            callback.onFailed(task.getException());
                        }
                    });
        } else {
            callback.onSuccess(auth.getCurrentUser());
        }
    }

    //Creates the "/users/$uid/notes/" path in Firebase if it's the users first sign-in.
    private static void initUserInDb(DatabaseReference rootRef, String uid, String email) {
        rootRef.child(PATH_USERS).child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    getUserPath(rootRef, uid).setValue(new User(email));
                    getNotesPath(rootRef, uid).setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //ignore
            }
        });
    }

    public static void insertNote(DatabaseReference noteRef, Note note) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.valueOf(note.getId()), note.toMap());
        noteRef.updateChildren(childUpdates);
    }

    public static void deleteNote(DatabaseReference noteRef, List<NoteTitle> noteTitles) {
        for (NoteTitle title : noteTitles) {
            noteRef.child(String.valueOf(title.getId())).removeValue();
        }
    }

    public static void getAllNotes(DatabaseReference userRef, FirebaseDbCallback callback) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Note> notes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    notes.add(snapshot.getValue(Note.class));
                }
                callback.onComplete(notes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        });
    }

    public static DatabaseReference getUserPath(DatabaseReference rootRef, String uid) {
        return rootRef.child(PATH_USERS).child(uid);
    }

    public static DatabaseReference getNotesPath(DatabaseReference rootRef, String uid) {
        return getUserPath(rootRef, uid).child(PATH_NOTES);
    }

    public static DatabaseReference getConnectionStatusRef(DatabaseReference rootRef) {
        return rootRef.child(".info/connected");
    }



}