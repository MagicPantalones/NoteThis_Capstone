package io.magics.notethis.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.ui.fragments.ImgurListFragment;
import io.magics.notethis.ui.fragments.PreviewFragment;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet;
import io.magics.notethis.ui.fragments.bottomsheet.SubSheetUpload;
import io.magics.notethis.utils.DocUtils;
import io.magics.notethis.utils.DrawerUtils;
import io.magics.notethis.utils.FragmentHelper;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.ImgurViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.viewmodels.NoteTitleViewModel;
import io.magics.notethis.ui.fragments.NoteListFragment;

import static io.magics.notethis.utils.Utils.DIALOG_CLOSE;
import static io.magics.notethis.utils.Utils.DIALOG_UPLOAD;

public class MainActivity extends AppCompatActivity implements
        NoteListFragment.NoteListFragListener, NoteListFragment.FabListener,
        SubSheetUpload.UploadDialogHandler, TemplatesBottomSheet.SheetCallbacks,
        FragmentHelper.InterfaceListener {

    private static final String TAG = "MainActivity";

    private static final int READ_WRITE_PERMISSION = 7682;

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private NoteViewModel noteViewModel;
    private ImgurViewModel imgurViewModel;

    private Snackbar disconnectSnack;
    Drawer navDrawer;

    private Uri fileUri;
    private boolean connected = true;
    private boolean userSignedIn = false;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_root)
    View mainRoot;
    @BindView(R.id.container_main)
    View fragContainer;
    @BindView(R.id.main_fab)
    FloatingActionButton mainFab;
    @BindView(R.id.upload_fab)
    FloatingActionButton uploadFab;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;

    TemplatesBottomSheet bottomSheet;

    FragmentHelper fragHelper;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        fragManager = getSupportFragmentManager();
        initViewModels();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        mainFab.setOnClickListener(v -> onNewNotePress());
        uploadFab.setOnClickListener(v -> onUploadImagePress());

        bottomSheet =
                (TemplatesBottomSheet) fragManager.findFragmentById(R.id.bottom_sheet_fragment);


        disconnectSnack = Snackbar.make(mainRoot, getString(R.string.disconnect_snack),
                Snackbar.LENGTH_INDEFINITE);

        DrawerBuilder drawerBuilder = DrawerUtils.initDrawer(this,
                toolbar, FragmentHelper.ID_NOTE_LIST);

        drawerBuilder.withOnDrawerItemClickListener((view, position, drawerItem) -> {
            if (drawerItem == null) return false;
            int id = (int) drawerItem.getIdentifier();
            if (id == FragmentHelper.ID_LOG_OUT) {
                navDrawer.deselect();
                noteViewModel.signOut();
            }

            fragHelper.changeFragFromDrawer(fragManager, id);
            return false;
        });

        navDrawer = drawerBuilder
                .withOnDrawerNavigationListener(clickedView -> {
                    onBackPressed();
                    return true;
                })
                .withSavedInstance(savedInstanceState)
                .build();

        fragHelper = new FragmentHelper(this, navDrawer, actionBar,
                savedInstanceState, this);

        if (getIntent().getIntExtra(NoteWidget.EXTRA_NOTE_ID, -1) != -1) {
            int id = getIntent().getIntExtra(NoteWidget.EXTRA_NOTE_ID, -1);
            noteViewModel.editNote(id);
            fragHelper.changeFragment(fragManager, FragmentHelper.ID_PREVIEW, false);
        } else if (showIntro) {
            fragHelper.startIntro(fragManager);
        }

    }


    @SuppressWarnings("ConstantConditions")
    private void initViewModels() {
        imgurViewModel = ViewModelProviders.of(this).get(ImgurViewModel.class);

        NoteTitleViewModel noteTitleViewModel = ViewModelProviders.of(this)
                .get(NoteTitleViewModel.class);
        noteTitleViewModel.init();
        noteTitleViewModel.getDeletedNote().observe(this, note ->
                noteViewModel.deleteNote(note));


        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.init();
        noteViewModel.getConnectionStatus().observe(this, status -> {
            connected = status;
            Utils.onConnectionStateChange(this, disconnectSnack, status);
        });

        noteViewModel.getSignInStatus().observe(this, signedIn -> {
            userSignedIn = signedIn;
            if (signedIn && fragHelper.getCurrentFragId() == FragmentHelper.ID_SIGN_IN) {
                fragHelper.introToListFrag(fragManager);
            }
        });

        noteViewModel.getFirebaseUser().observe(this, user -> {
            if (user == null || imgurViewModel.isInitialized()) return;
            imgurViewModel.init(user.getUid());
            DrawerUtils.setEmail(navDrawer,
                    user.getEmail());
        });
    }

    @Override
    public void onBackPressed() {

        if (fragHelper.handleBackPressed(fragManager)) {
            appBarLayout.setExpanded(true, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Utils.dispose(unbinder);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(FragmentHelper.FRAG_HELPER_STATE, fragHelper.saveState());
    }

    @Override
    public void onNewNotePress() {
        noteViewModel.newNote();
        Utils.setToolbarTitle(this, NoteViewModel.NEW_NOTE_TITLE, R.color.primaryTextColor);
        fragHelper.changeFragment(fragManager, FragmentHelper.ID_EDIT_NOTE, false);
    }


    @Override
    public void onNoteListScroll(int state) {
        if (state == NoteListFragment.SCROLL_UP && mainFab.getVisibility() != View.VISIBLE) {
            showFab();
        } else if (state == NoteListFragment.SCROLL_DOWN
                && mainFab.getVisibility() == View.VISIBLE) {
            hideFab();
        }
    }

    @Override
    public void onNoteItemClicked(int id, int action) {
        noteViewModel.editNote(id);
        if (action == NoteListFragment.ACTION_EDIT) {
            fragHelper.changeFragment(fragManager, FragmentHelper.ID_EDIT_NOTE, false);
        } else {
            fragHelper.changeFragment(fragManager, FragmentHelper.ID_PREVIEW, true);
        }
    }

    @Override
    public void hideFab() {
        if (mainFab != null) {
            mainFab.hide();
        }
        if (uploadFab != null) {
            uploadFab.hide();
        }
        if (fragHelper.getCurrentFragId() != FragmentHelper.ID_EDIT_NOTE) {
            appBarLayout.setExpanded(true, true);
            if (fragHelper.getCurrentFragId() != FragmentHelper.ID_PREVIEW) {
                Utils.setToolbarTitle(this, getString(R.string.app_name),
                        R.color.secondaryColor);
            }
        }
    }

    @Override
    public void showFab() {
        Utils.setToolbarTitle(this, R.string.app_name, R.color.secondaryColor);
        if (mainFab != null) {
            mainFab.show();
        }
    }

    @Override
    public void changeFab(int fabType) {
        Utils.setToolbarTitle(this, R.string.app_name, R.color.secondaryColor);
        if (mainFab == null || uploadFab == null) return;
        if (fabType == FragmentHelper.FAB_NEW_NOTE) {
            if (uploadFab.getVisibility() == View.VISIBLE) {
                uploadFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        mainFab.show();
                    }
                });
            } else {
                mainFab.show();
            }
        } else {
            if (mainFab.getVisibility() == View.VISIBLE) {
                mainFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        uploadFab.show();
                    }
                });
            } else {
                uploadFab.show();
            }
        }
    }

    @Override
    public void onIntroDone() {
        if (userSignedIn) {
            if (fragHelper.getCurrentFragId() == FragmentHelper.ID_PREVIEW) return;
            fragHelper.introToListFrag(fragManager);
        } else {
            fragHelper.introToSignInFrag(fragManager);
        }
    }

    @Override
    public void hideSheet() {
        if (bottomSheet != null) bottomSheet.hide();
    }

    private void onUploadImagePress() {
        startActivityForResult(DocUtils.getChoseFileIntent(), DocUtils.RC_PICK_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DocUtils.RC_PICK_IMG && resultCode == RESULT_OK) {
            fileUri = data.getData();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, READ_WRITE_PERMISSION);
            } else {
                createUploadDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_WRITE_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createUploadDialog();
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createUploadDialog() {
        if (!connected) return;
        String filePath = DocUtils.getPath(this, fileUri);
        if (filePath == null || filePath.isEmpty()) return;
        File img = new File(filePath);
        imgurViewModel.prepareUpload(img);
        SubSheetUpload.newInstance(img, SubSheetUpload.LIST_INS).show(fragManager, DIALOG_UPLOAD);
    }

    @Override
    public void onUpload(String title) {
        if (connected) imgurViewModel.upload(title);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_menu_help:
                fragHelper.changeFragment(fragManager, FragmentHelper.ID_HELP, false);
                return true;
            case R.id.edit_menu_preview:
                fragHelper.changeFragment(fragManager, FragmentHelper.ID_PREVIEW, false);
                return true;
            case android.R.id.home:
                if (navDrawer != null
                        && !navDrawer.getActionBarDrawerToggle().isDrawerIndicatorEnabled()) {
                    onBackPressed();
                }
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void showSheet() {
        bottomSheet.show();
    }

    @Override
    public void onReturnTemplate(String template) {
        Fragment frag = fragManager.findFragmentById(R.id.container_main);
        if (frag instanceof EditNoteFragment) {
            ((EditNoteFragment) frag).setTemplate(template);
        }
    }

    @Override
    public void hideAppBar() {
        appBarLayout.setExpanded(false, true);
    }

    //TODO Add support for RTL & D-PAD

    //TODO Handle lifecycles.

    //TODO Create release build.
}
