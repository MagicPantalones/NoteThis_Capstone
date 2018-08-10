package io.magics.notethis.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.data.DataProvider;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.viewmodels.EditNoteViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.utils.models.NoteTitle;

public class MainActivity extends AppCompatActivity implements DataProvider.DataProviderHandler,
        NoteListFragment.NoteListFragListener, EditNoteFragment.EditNoteFragListener {

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
    Toolbar mainToolbar;
    @BindView(R.id.main_root)
    View mainRoot;
    @BindView(R.id.main_fab)
    FloatingActionButton mainFab;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        fragManager = getSupportFragmentManager();
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        editNoteViewModel = ViewModelProviders.of(this).get(EditNoteViewModel.class);

        setSupportActionBar(mainToolbar);
        AppDatabase db = AppDatabase.getInMemoryDatabase(getApplication());
        dataProvider = new DataProvider(db, this);

        String path = db.getOpenHelper().getReadableDatabase().getPath();
        Log.w(TAG, "DB PATH: " + path);

        //TODO Set a timer on show intro screen.
        if (showIntro) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

            fragManager.beginTransaction()
                    .replace(R.id.container_main, IntroFragment.newInstance(), FRAG_INTRO)
                    .commit();

        }

        dataProvider.init();
    }


    @Override
    protected void onDestroy() {
        dataProvider.dispose();
        super.onDestroy();
    }

    private void exitIntro() {
        showIntro = false;
        getSupportActionBar().show();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        showNotesList();
    }


    @Override
    public void onNoteTitlesFetched(List<NoteTitle> noteTitles) {
        noteViewModel.setNoteTitles(noteTitles);
        if (showIntro) {
            exitIntro();
        }
    }

    private void showNotesList() {
        /*
        TODO Do a test with empty DB to see if the empty list layout shows up since bug where it
        stuck is fixed.
        */
        if (!showIntro) {
            fragManager.beginTransaction()
                    .replace(R.id.main_root, NoteListFragment.newInstance(), FRAG_NOTE_LIST)
                    .commit();
        }
    }

    @Override
    public void onNewNotePress() {
        editNoteViewModel.newNote();
        fragManager.beginTransaction()
                .replace(R.id.main_root, EditNoteFragment.newInstance(), FRAG_EDIT_NOTE)
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
    public void onNoteListChange(int visibility) {
        if (visibility == View.INVISIBLE) {
            mainFab.hide();
        } else {
            mainFab.setVisibility(View.VISIBLE);
            mainFab.show();
        }
    }

    @Override
    public void onClose(boolean hasChanges) {
        /*
        TODO implement Dialog warning if user wants to save note.
        One dialog with yes or no and one dialog where user can write a title if the note does
        not have one already.
        */
        if (hasChanges) {
            String title = getSupportActionBar().getTitle().toString();
            dataProvider.insertNotes(editNoteViewModel.getNote(true, title));
        }
    }

    @Override
    public void saveNote(boolean hasChanged) {
        //TODO Implement dialog fragment on save. Where user can set new or change the title
        if (hasChanged) {
            String title = getSupportActionBar().getTitle().toString();
            dataProvider.insertNotes(editNoteViewModel.getNote(true, title));
        }
    }

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
