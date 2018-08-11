package io.magics.notethis.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.CountDownTimer;
import android.os.Handler;
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
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.viewmodels.EditNoteViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.utils.models.NoteTitle;

public class MainActivity extends AppCompatActivity implements DataProvider.DataProviderHandler,
        NoteListFragment.NoteListFragListener, EditNoteFragment.EditNoteFragListener,
        SaveDialog.SaveDialogListener {

    private static final String TAG = "MainActivity";

    private static final String FRAG_INTRO = "frag_intro";
    private static final String FRAG_NOTE_LIST = "frag_note_list";
    private static final String FRAG_EDIT_NOTE = "frag_edit_note";

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private DataProvider dataProvider;
    private NoteViewModel noteViewModel;
    private EditNoteViewModel editNoteViewModel;
    private boolean userSignedIn = false;

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
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        editNoteViewModel = ViewModelProviders.of(this).get(EditNoteViewModel.class);

        setSupportActionBar(toolbar);
        AppDatabase db = AppDatabase.getInMemoryDatabase(getApplication());
        dataProvider = new DataProvider(db, this);

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

        dataProvider.init();
    }

    @Override
    public void onBackPressed() {
        //TODO If there are unsaved changes, promt user with dialog asking if they want to save.
        //If they press save and title is "New Note" show save Note Dialog. Else save & exit.
        appBarLayout.setExpanded(true, true);
        if (Utils.getToolbarTitle(this).equals(getString(R.string.new_note_title))) {
            Utils.setToolbarTitle(this, toolbar, R.string.app_name, R.color.secondaryColor);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Utils.dispose(unbinder);
        dataProvider.dispose();
        super.onDestroy();
    }

    private void exitIntro() {
        showIntro = false;
        Utils.showToolbar(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        showNotesList();
    }


    @Override
    public void onNoteTitlesFetched(List<NoteTitle> noteTitles) {
        noteViewModel.setNoteTitles(noteTitles);
    }

    @Override
    public void onNoteInserted(int id) {
        editNoteViewModel.setNoteId(id);
    }

    @Override
    public void onNoteFetched(Note note) {
        editNoteViewModel.setNote(note);
        Utils.setToolbarTitle(this, toolbar, note.getTitle(), R.color.primaryTextColor);
        fragManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container_main, EditNoteFragment.newInstance(), FRAG_EDIT_NOTE)
                .addToBackStack(FRAG_EDIT_NOTE)
                .commit();
    }


    private void showNotesList() {
        if (!showIntro) {
            fragManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container_main, NoteListFragment.newInstance(), FRAG_NOTE_LIST)
                    .addToBackStack(FRAG_NOTE_LIST)
                    .commit();
            mainFab.show();
        }
    }

    @Override
    public void onNewNotePress() {

        editNoteViewModel.newNote();

        Utils.setToolbarTitle(this, toolbar, R.string.new_note_title, R.color.primaryTextColor);

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
    public void onNoteListChange(boolean showFab) {
        if (!showFab) {
            mainFab.hide();
            Utils.setToolbarTitle(this, toolbar, R.string.app_name, R.color.secondaryColor);
        } else {
            Fragment frag = fragManager.findFragmentById(R.id.container_main);
            if (!(frag instanceof EditNoteFragment) && frag.isVisible()) {
                mainFab.show();
                Utils.setToolbarTitle(this, toolbar, R.string.app_name, R.color.secondaryColor);
            }
        }
    }

    @Override
    public void onNoteItemClicked(int id) {
        dataProvider.getNoteById(id);
    }

    @Override
    public void onClose(boolean hasChanges) {
        /*
        TODO implement Dialog warning if user wants to save note.
        One dialog with yes or no and one dialog where user can write a title if the note does
        not have one already.
        */
        if (hasChanges) {
            String title = Utils.getToolbarTitle(this);
            dataProvider.insertNotes(editNoteViewModel.getNoteForSave(true, title));
        }
    }

    @Override
    public void hideFab() {
        mainFab.hide();
    }

    @Override
    public void onSave(String title) {
        String oldTitle = Utils.getToolbarTitle(this);
        if (oldTitle.equals(getString(R.string.new_note_title))) {
            dataProvider.insertNotes(editNoteViewModel.getNoteForSave(true, title));
        } else {
            Note note = editNoteViewModel.getNoteForSave(true, title);
            dataProvider.updateNote(note);
        }
        Utils.setToolbarTitle(this, toolbar, title, R.color.primaryTextColor);
    }

    @SuppressWarnings("unused")
    @Override
    public void onError(DataProvider.DataError dataError) {
        switch (dataError) {
            case NO_DATA_AVAILABLE:
                int look;
                //TODO Hide main fragment list layout, show no note layout.
                break;
            case NO_INTERNET_LOG_IN:
                String mom;
                /* TODO Check if user is signed in else show log-in screen and show snakckbar telling
                user to connect to the internet. Else just show Snackbar in loading screen */
                break;
            case NO_INTERNET_IMGUR:
                boolean im;
                /*
                TODO Show snackbar telling user that Image can't be uploaded, but will be uploaded as
                soon as they are reconnected to the internet. SnackBar has abort button.
                 */
                break;
            case NO_INTERNET_FIRE_DB:
                double fooling;
                /*
                TODO Tell user that they are working offline and notes will uploaded to the cloud once
                they reconnect to the net. Turn on offline mode.
                 */
                break;
            case FIREBASE_WRITE_ERROR:
                float the;
                /*
                TODO Write data to DB. Try to re-auth user.
                 */
                break;
            case ROOM_WRITE_ERROR:
                NoteTitle linter;
                /*
                TODO Write note to Firebase. If not possible show user Snackbar telling them to connect
                to internet and try again.
                 */
                break;
            case UNHANDLED_ERROR:
                /*
                TODO Try to re-authenticate user. Check local DB timestamp, FB timestamp and sync.
                Show Toast saying something went wrong. Write current note to FB/DB
                 */
                break;
            default:
                //Should not happen

        }
    }

}
