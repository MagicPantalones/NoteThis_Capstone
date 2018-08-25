package io.magics.notethis.data.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

import io.magics.notethis.R;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";

    private static final String PATH_USERS = "users";
    private static final String PATH_NOTES = "notes";
    private static final String PATH_EMAIL = "email";
    private static final String PATH_IMAGES = "images";

    public interface FirebaseAuthCallback{
        void onSuccess(FirebaseUser user);
        void onFailed(Throwable e);
    }

    public interface FirebaseNoteCallback {
        void onComplete(List<Note> notes);
    }

    public interface FirebaseImgurCallback {
        void onComplete(List<Image> images);
    }

    public static void getUserFromAuth(FirebaseAuth auth, FirebaseAuthCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailed(null);
        } else {
            callback.onSuccess(auth.getCurrentUser());
        }
    }

    public static GoogleSignInClient getGsiClient(Context context) {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(context, gso);
    }

    public static void checkForUserEmail(DatabaseReference userRef, String email) {
        userRef.child(PATH_EMAIL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userRef.child(PATH_EMAIL).setValue(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Do nothing.
            }
        });
    }

    public static void insertNote(DatabaseReference noteRef, Note note) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.valueOf(note.getId()), note.toMap());
        noteRef.updateChildren(childUpdates);
    }

    public static void deleteNote(DatabaseReference noteRef, Note note) {
        if (note == null) return;
        noteRef.child(String.valueOf(note.getId())).removeValue();
    }

    public static void getAllNotes(DatabaseReference userRef, FirebaseNoteCallback callback) {
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
                handleFirebaseErrors(databaseError);
            }
        });
    }

    public static DatabaseReference getUserPath(DatabaseReference rootRef, String uid) {
        return rootRef.child(PATH_USERS).child(uid);
    }

    public static DatabaseReference getNotesPath(DatabaseReference rootRef, String uid) {
        return getUserPath(rootRef, uid).child(PATH_NOTES);
    }

    public static DatabaseReference getImagePath(DatabaseReference rootRef, String uid) {
        return getUserPath(rootRef, uid).child(PATH_IMAGES);
    }

    public static void insertImgurLink(DatabaseReference imgurRef, Image image) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.valueOf(image.getServerId()), image.toMap());
        imgurRef.updateChildren(childUpdates);
    }

    public static void deleteImgurLink(DatabaseReference imgurRef, Image image) {
        imgurRef.child(image.getServerId()).removeValue();
    }

    public static void getAllImgurLinks(DatabaseReference imgurRef, FirebaseImgurCallback cb) {
        imgurRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Image> images = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    images.add(snapshot.getValue(Image.class));
                }
                cb.onComplete(images);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleFirebaseErrors(databaseError);
            }
        });
    }

    private static void handleFirebaseErrors(DatabaseError databaseError) {
        Log.w(TAG, "Database error: ", databaseError.toException());
    }

}
