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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.utils.TempVals;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String FRAG_INTRO = "frag_intro";

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private FirebaseAuth auth;

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

        setSupportActionBar(mainToolbar);

        if (showIntro) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );

            getSupportActionBar().hide();

            fragManager.beginTransaction()
                    .replace(R.id.container_main, IntroFragment.newInstance(), FRAG_INTRO)
                    .commit();

            if (auth.getCurrentUser() == null) {
                auth.signInWithEmailAndPassword(TempVals.USER, TempVals.CODE)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.w(TAG, "Success");
                                    Snackbar.make(mainRoot, "Login Success", 
                                            Snackbar.LENGTH_INDEFINITE).show();
                                    exitIntro();
                                } else {
                                    Log.w(TAG, "onComplete: ", task.getException());
                                    Snackbar.make(mainRoot, "Not logged in!", 
                                            Snackbar.LENGTH_INDEFINITE).show();
                                }
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
        getSupportActionBar().show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

}
