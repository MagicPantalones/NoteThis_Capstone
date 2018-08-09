package io.magics.notethis.ui;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.utils.FirebaseUtils;
import io.magics.notethis.utils.TempVals;
import io.magics.notethis.utils.models.Note;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String FRAG_INTRO = "frag_intro";

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private FirebaseAuth auth;
    private FirebaseUser fireUser;
    private DatabaseReference fireDatabase;
    private DatabaseReference userRef;

    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.main_root)
    View mainRoot;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        fragManager = getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();
        fireDatabase = FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(mainToolbar);

        fireDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.w(TAG, "Wrote successfully to DB");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Firebase DB write error: " + databaseError.getCode(), databaseError.toException());
            }
        });

        if (showIntro) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );

            getSupportActionBar().hide();

            fragManager.beginTransaction()
                    .replace(R.id.container_main, IntroFragment.newInstance(), FRAG_INTRO)
                    .commit();

            if (auth.getCurrentUser() == null) {
                auth.signInWithEmailAndPassword(TempVals.USER, TempVals.CODE)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.w(TAG, "Success");
                                Snackbar.make(mainRoot, "Login Success",
                                        Snackbar.LENGTH_LONG).show();
                                exitIntro();
                            } else {
                                Log.w(TAG, "onComplete: ", task.getException());
                                Snackbar.make(mainRoot, "Not logged in!",
                                        Snackbar.LENGTH_INDEFINITE).show();
                            }
                        });
            }
        }


    }

    @Override
    protected void onDestroy() {
        auth.signOut();
        super.onDestroy();
    }

    private void exitIntro(){
        showIntro = false;
        fireUser = auth.getCurrentUser();
        getSupportActionBar().show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        checkUserExist();
    }

    private void checkUserExist() {
        String uid = auth.getUid();
        fireDatabase.child("users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Log.w(TAG, "User does not exist. Writing new user");
                    FirebaseUtils.writeNewUser(fireDatabase, fireUser);
                } else {
                    Log.w(TAG, "User exists");
                    Note note = new Note("1", "Hello World!", "Hello World! Nice to meet you!");
                    FirebaseUtils.writeNewNote(fireDatabase.child("users").child(uid), note);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Firebase DB user write error: ", databaseError.toException());
            }
        });

    }

}
