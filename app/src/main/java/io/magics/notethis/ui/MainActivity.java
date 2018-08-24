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
        EditNoteFragment.SheetVisibility {

    private static final String TAG = "MainActivity";

    private static final int READ_WRITE_PERMISSION = 7682;

    private boolean showIntro = true;
    private FragmentManager fragManager;
    private NoteViewModel noteViewModel;
    private ImgurViewModel imgurViewModel;

    private Snackbar disconnectSnack;
    private ActionBarDrawerToggle drawerToggle;

    private Uri fileUri;
    private boolean connected = true;

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
    @BindView(R.id.nav_view)
    NavigationView navDrawer;
    @BindView(R.id.main_drawer_layout)
    DrawerLayout drawerLayout;

    TemplatesBottomSheet bottomSheet;

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
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        mainFab.setOnClickListener(v -> onNewNotePress());
        uploadFab.setOnClickListener(v -> onUploadImagePress());

        bottomSheet =
                (TemplatesBottomSheet) fragManager.findFragmentById(R.id.bottom_sheet_fragment);

        navDrawer.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.nav_drawer_notes:
                    if (item.isChecked()) break;
                    if (UiUtils.isFragType(fragManager, ImgurListFragment.class)) uploadFab.hide();
                    UiUtils.showNoteListFrag(this, fragManager);
                    break;
                case R.id.nav_drawer_imgur:
                    if (item.isChecked()) break;
                    UiUtils.showImgurList(fragManager);
                    break;
                case R.id.nav_drawer_help:
                    if (item.isChecked()) break;
                    UiUtils.showHelpFrag(fragManager);
                    break;
                case R.id.nav_drawer_sign_out:
                    item.setChecked(false);
                    noteViewModel.signOut();
                    imgurViewModel.signOut();
                    UiUtils.handleUserSignOut(this,fragManager);
                    break;
                default:
                    return true;
            }
            return true;
        });

        if (getIntent().getIntExtra(NoteWidget.EXTRA_NOTE_ID, -1) != -1) {
            int id = getIntent().getIntExtra(NoteWidget.EXTRA_NOTE_ID, -1);
            noteViewModel.editNote(id);
            UiUtils.showPreviewFrag(fragManager);
        } else if (showIntro) {
            UiUtils.showIntroFrag(this, fragManager);
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
                    if (!UiUtils.isFragType(fragManager, PreviewFragment.class)){
                        UiUtils.introToListFrag(this, fragManager);
                    }
                } else {
                    if (!UiUtils.isFragType(fragManager, PreviewFragment.class)) {
                        UiUtils.showNoteListFrag(this, fragManager);
                    }
                }
                imgurViewModel.init(noteViewModel.getUserRef());
            } else {
                UiUtils.introToSignInFrag(this, fragManager);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Fragment frag = fragManager.findFragmentById(R.id.container_main);
        Fragment dialogFrag = fragManager.findFragmentByTag(DIALOG_CLOSE);

        if (dialogFrag != null && dialogFrag.getActivity() == this) {
            appBarLayout.setExpanded(true, true);
            Utils.setToolbarTitle(this, R.string.app_name, R.color.secondaryColor);
            bottomSheet.hide();
            super.onBackPressed();
        } else if (frag instanceof EditNoteFragment
                && ((EditNoteFragment) frag).hasUnsavedChanges()) {
            ((EditNoteFragment) frag).prepareSave(EditNoteFragment.ACTION_CLOSE);
        } else {
            if (frag instanceof ImgurListFragment) {
                uploadFab.hide();
            }
            bottomSheet.hide();
            appBarLayout.setExpanded(true, true);
            Utils.setToolbarTitle(this, R.string.app_name, R.color.secondaryColor);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Utils.dispose(unbinder);
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
        if (uploadFab != null) {
            uploadFab.hide();
        }
    }

    @Override
    public void showFab() {
        if (mainFab != null && UiUtils.isFragType(fragManager, NoteListFragment.class)) {
            mainFab.show();
            bottomSheet.hide();
        }
    }

    @Override
    public void changeFab() {
        if (mainFab != null) {
            if (mainFab.getVisibility() != View.VISIBLE) {
                uploadFab.show();
                bottomSheet.hide();
            } else {
                mainFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        if (connected) {
                            uploadFab.show();
                            bottomSheet.hide();

                        }
                    }
                });
            }
        }
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
        if (drawerToggle.onOptionsItemSelected(item)) return true;

        switch (item.getItemId()) {
            case R.id.edit_menu_help:
                UiUtils.showHelpFrag(fragManager);
                break;
            case R.id.edit_menu_preview:
                UiUtils.showPreviewFrag(fragManager);
                break;
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

    //TODO Implement widget.

    //TODO Fix Imgur Bug where it does not populate list when getting Images from firebase.

    //TODO Add support for RTL & D-PAD

    //TODO Handle lifecycles.

    //TODO Create release build.
}
