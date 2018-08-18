package io.magics.notethis.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.data.DataProvider;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.viewmodels.NoteTitleViewModel;
import io.magics.notethis.ui.fragments.NoteListFragment;

public class MainActivity extends AppCompatActivity implements
        NoteListFragment.NoteListFragListener, NoteListFragment.FabListener {

    private static final String TAG = "MainActivity";




    private boolean showIntro = true;
    private FragmentManager fragManager;
    private DataProvider dataProvider;
    private NoteTitleViewModel noteTitleViewModel;
    private NoteViewModel noteViewModel;
    private Snackbar disconnectSnack;
    private boolean connected = true;
    private ActionBarDrawerToggle drawerToggle;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_root)
    View mainRoot;
    @BindView(R.id.main_fab)
    FloatingActionButton mainFab;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.nav_view)
    NavigationView navDrawer;
    @BindView(R.id.main_drawer_layout)
    DrawerLayout drawerLayout;

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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        mainFab.setOnClickListener(v -> onNewNotePress());

        navDrawer.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.nav_drawer_notes:
                    if (item.isChecked()) break;
                    UiUtils.showNoteListFrag(this, fragManager);
                    break;
                case R.id.nav_drawer_imgur:
                    if (item.isChecked()) break;
                    Toast.makeText(this, "Show Imgur frag", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_drawer_help:
                    if (item.isChecked()) break;
                    Toast.makeText(this, "Show help frag", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_drawer_sign_out:
                    item.setChecked(false);
                    noteViewModel.signOut();
                    UiUtils.handleUserSignOut(this,fragManager);
                    break;
                default:
                    return true;
            }
            return true;
        });

        if (showIntro) {
            UiUtils.showIntroFrag(this, fragManager);
        }
    }



    @SuppressWarnings("ConstantConditions")
    private void initViewModels() {
        noteTitleViewModel = ViewModelProviders.of(this).get(NoteTitleViewModel.class);
        noteTitleViewModel.init();

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.init();
        noteViewModel.getConnectionStatus().observe(this, status -> {

            connected = status;
            if (!status) {
                disconnectSnack = Snackbar.make(mainRoot, getString(R.string.disconnect_snack),
                        Snackbar.LENGTH_INDEFINITE);
                disconnectSnack.show();
                disconnectSnack.setAction(R.string.hide_snack, v -> disconnectSnack.dismiss());
                int color = ResourcesCompat.getColor(getResources(), R.color.secondaryColor,
                        getTheme());
                disconnectSnack.setActionTextColor(color);
                disconnectSnack.show();
            } else {
                if (disconnectSnack != null && disconnectSnack.isShown()) {
                    disconnectSnack.dismiss();
                }
            }
        });

        noteViewModel.getSignInStatus().observe(this, signedIn -> {
            if (signedIn) {
                if (showIntro) {
                    showIntro = false;
                    UiUtils.introToListFrag(this, fragManager);
                } else {
                    UiUtils.showNoteListFrag(this, fragManager);
                }
            } else {
                UiUtils.introToSignInFrag(this, fragManager);
            }
        });
    }

    @Override
    public void onBackPressed() {
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


    @Override
    public void onNewNotePress() {

        noteViewModel.newNote();

        Utils.setToolbarTitle(this, NoteViewModel.NEW_NOTE_TITLE, R.color.primaryTextColor);

        UiUtils.showEditNoteFrag(fragManager);
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
            UiUtils.showEditNoteFrag(fragManager);
        } else {
            UiUtils.showPreviewFrag(fragManager);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

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
