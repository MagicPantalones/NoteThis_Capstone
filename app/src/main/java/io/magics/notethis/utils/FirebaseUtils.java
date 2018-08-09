package io.magics.notethis.utils;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.User;

public class FirebaseUtils {

    private FirebaseUtils() {}


    public static void writeNewUser(DatabaseReference rootRef, FirebaseUser user) {
        final String uid = user.getUid();
        rootRef.child("users").child(uid).setValue(new User(user.getEmail()));
    }

    public static void writeNewNote(DatabaseReference userRef, Note note) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/notes/" + note.getId(), note.toMap());
        userRef.updateChildren(childUpdates);
    }

}
