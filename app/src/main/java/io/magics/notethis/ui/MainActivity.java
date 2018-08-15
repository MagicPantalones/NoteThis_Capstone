package io.magics.notethis.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.CountDownTimer;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.data.DataProvider;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.ui.fragments.PreviewFragment;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.viewmodels.NoteTitleViewModel;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;

public class MainActivity extends AppCompatActivity implements
        NoteListFragment.NoteListFragListener, NoteListFragment.FabListener {

    private static final String TAG = "MainActivity";

    private static final String FRAG_INTRO = "frag_intro";
    private static final String FRAG_NOTE_LIST = "frag_note_list";
    private static final String FRAG_EDIT_NOTE = "frag_edit_note";

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private DataProvider dataProvider;
    private NoteTitleViewModel noteTitleViewModel;
    private NoteViewModel noteViewModel;
    private boolean userSignedIn = false;
    private boolean connected = true;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_root)
    View mainRoot;
    @BindView(R.id.main_fab)
    FloatingActionButton mainFab;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        fragManager = getSupportFragmentManager();

        initViewModels();

        setSupportActionBar(toolbar);
        dataProvider = new DataProvider();

        mainFab.setOnClickListener(v -> onNewNotePress());

        List<Fragment> frags = fragManager.getFragments();
        if (frags != null) {
            for (Fragment frag : frags) {
                if (frag != null) {
                    fragManager.beginTransaction().remove(frag).commit();
                }
            }
        }

        if (showIntro) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            Utils.hideToolbar(this);
            fragManager.beginTransaction()
                    .replace(R.id.container_main, IntroFragment.newInstance(), FRAG_INTRO)
                    .commit();

            new CountDownTimer(3000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    //Required override
                }

                @Override
                public void onFinish() {
                    exitIntro();
                }

            }.start();
        }


    }

    @SuppressWarnings("ConstantConditions")
    private void initViewModels() {
        noteTitleViewModel = ViewModelProviders.of(this).get(NoteTitleViewModel.class);
        noteTitleViewModel.init();

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.init();
        noteViewModel.getSignInStatus().observe(this, status -> {

            connected = status;
            if (!status) {
                //TODO handle disconnect
            } else {
                //TODO handle reconnect
            }
        });

        noteViewModel.getSignInStatus().observe(this, signedIn -> {

            if (signedIn) {
                exitIntro();
            } else {
                //TODO Show login screen
            }
        });

    }

    @Override
    public void onBackPressed() {
        //TODO When note has title "New note" list bugs, Find out why.
        Fragment frag = fragManager.findFragmentById(R.id.container_main);

        if (frag instanceof EditNoteFragment && ((EditNoteFragment) frag).hasUnsavedChanges()) {
            ((EditNoteFragment) frag).prepareSave(EditNoteFragment.ACTION_BACK);
        } else {
            appBarLayout.setExpanded(true, true);
            Utils.setToolbarTitle(this, R.string.app_name, R.color.secondaryColor);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Utils.dispose(unbinder);
        noteViewModel.deleteNotes(noteTitleViewModel.getDeletedTitles());
        super.onDestroy();
    }

    private void exitIntro() {
        if (showIntro) {
            showIntro = false;

            Utils.showToolbar(this);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            fragManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container_main, NoteListFragment.newInstance(), FRAG_NOTE_LIST)
                    .commit();
        }
    }

    @Override
    public void onNewNotePress() {

        noteViewModel.newNote();

        Utils.setToolbarTitle(this, NoteViewModel.NEW_NOTE_TITLE, R.color.primaryTextColor);

        fragManager.beginTransaction()
                .replace(R.id.container_main, EditNoteFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNoteListScroll(int state) {
        if (state == NoteListFragment.SCROLL_UP && mainFab.getVisibility() != View.VISIBLE) {
            mainFab.show();
        } else if (state == NoteListFragment.SCROLL_DOWN
                && mainFab.getVisibility() == View.VISIBLE) {
            mainFab.hide();
        }
    }

    @Override
    public void onNoteItemClicked(int id, int action) {
        noteViewModel.editNote(id);
        if (action == NoteListFragment.ACTION_EDIT) {
            fragManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container_main, EditNoteFragment.newInstance(), FRAG_EDIT_NOTE)
                    .addToBackStack(FRAG_EDIT_NOTE)
                    .commit();
        } else {
            fragManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container_main, PreviewFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void hideFab() {
        if (mainFab != null) {
            mainFab.hide();
        }
    }

    @Override
    public void showFab() {
        if (mainFab != null) {
            mainFab.show();
        }
    }

    /* NO_INTERNET_LOG_IN
    TODO Check if user is signed in else show log-in screen and show snakckbar telling
    user to connect to the internet. Else just show Snackbar in loading screen
    */

    /* NO_INTERNET_IMGUR
    TODO Show snackbar telling user that Image can't be uploaded, but will be uploaded as
    soon as they are reconnected to the internet. SnackBar has abort button.
    */

    /* NO_INTERNET_FIRE_DB &&
    TODO Tell user that they are working offline and notes will uploaded to the cloud once
    they reconnect to the net. Turn on offline mode.
    */

}
