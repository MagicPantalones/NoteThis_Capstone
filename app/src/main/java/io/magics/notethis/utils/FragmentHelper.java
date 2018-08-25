package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBarDrawerToggle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;

import java.util.List;

import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.ui.fragments.HelpFragment;
import io.magics.notethis.ui.fragments.ImgurListFragment;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.ui.fragments.PreviewFragment;
import io.magics.notethis.ui.fragments.SignInFragment;

public class FragmentHelper {

    private static final long SLIDE_DEFAULT_TIME = 300;

    private static final long INTRO_TIME = 3000;

    private static final int CONTAINER = R.id.container_main;

    public static final int ID_NOTE_LIST = 111;
    public static final int ID_EDIT_NOTE = 221;
    public static final int ID_IMGUR_LIST = 331;
    public static final int ID_PREVIEW = 441;
    public static final int ID_HELP = 551;
    public static final int ID_LOG_OUT = 4;


    private static final int ID_INTRO_SIGN_IN = 991;

    public static final int FAB_NEW_NOTE = 7878;
    public static final int FAB_UPLOAD = 3451;

    public static final String FRAG_HELPER_STATE = "frag_helper_state";

    private int previousFragId;
    private int currentFragId;

    private IntroFragment introFrag;
    private SignInFragment signInFrag;
    private NoteListFragment noteListFrag;
    private EditNoteFragment editNoteFrag;
    private ImgurListFragment imgurListFrag;
    private HelpFragment helpFrag;
    private PreviewFragment previewFrag;

    private final Activity activity;
    private Drawer drawer;

    InterfaceListener listener;

    public interface InterfaceListener {
        void hideFab();
        void showFab();
        void changeFab(int fabType);
        void onIntroDone();
    }

    public FragmentHelper(Activity activity, InterfaceListener listener, Bundle savedState) {
        this.activity = activity;
        this.listener = listener;
        if (savedState != null) {
            int[] restored = savedState.getIntArray(FRAG_HELPER_STATE);
            if (restored != null) {
                previousFragId = restored[0];
                currentFragId = restored[1];
            }
        }
        init();
    }

    private void init() {
        introFrag = IntroFragment.newInstance();
        signInFrag = SignInFragment.newInstance();
        noteListFrag = NoteListFragment.newInstance();
        editNoteFrag = EditNoteFragment.newInstance();
        imgurListFrag = ImgurListFragment.newInstance();
        helpFrag = HelpFragment.newInstance();
        previewFrag = PreviewFragment.newInstance();
    }

    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public void dispose() {
        drawer = null;
        listener = null;
    }

    public int[] saveState() {
        return new int[] {previousFragId, currentFragId};
    }

    public void startIntro(FragmentManager manager) {
        List<Fragment> fragList = manager.getFragments();
        if (fragList != null && !fragList.isEmpty()) {
            for (Fragment frag : fragList) {
                manager.beginTransaction().remove(frag).commit();
            }
            previousFragId = -1;
            currentFragId = -1;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        Utils.hideToolbar(activity);

        currentFragId = ID_INTRO_SIGN_IN;

        manager.beginTransaction()
                .replace(CONTAINER, introFrag)
                .commitAllowingStateLoss();

        new CountDownTimer(INTRO_TIME, 0) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Do nothing
            }

            @Override
            public void onFinish() {
                if (listener != null) listener.onIntroDone();
            }

        }.start();
    }

    public void changeFragment(FragmentManager manager, int fragmentId, boolean fromDrawer) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setReorderingAllowed(true);

        switch (fragmentId) {
            case ID_NOTE_LIST:
                transaction.replace(CONTAINER, noteListFrag).commitAllowingStateLoss();
                if (currentFragId == ID_IMGUR_LIST) changeFab(FAB_NEW_NOTE);
                else showFab();
                showDrawerIcon();
                break;
            case ID_EDIT_NOTE:
                transaction.replace(CONTAINER, editNoteFrag).commit();
                hideFab();
                hideDrawerIcon();
                break;
            case ID_PREVIEW:
                transaction.replace(CONTAINER, previewFrag).commit();
                hideFab();
                if (!fromDrawer) hideDrawerIcon();
                break;
            case ID_HELP:
                transaction.replace(CONTAINER, helpFrag).commit();
                hideFab();
                if (!fromDrawer) hideDrawerIcon();
                break;
            case ID_IMGUR_LIST:
                transaction.replace(CONTAINER, imgurListFrag).commit();
                changeFab(FAB_UPLOAD);
                showDrawerIcon();
                break;
            default:
                //Should not happen
        }
        previousFragId = fromDrawer ? ID_NOTE_LIST : fragmentId;
        currentFragId = fragmentId;
    }

    public void changeFragFromDrawer(FragmentManager manager, int itemId) {
        if (itemId == ID_LOG_OUT) {
            startIntro(manager);
            drawer.setSelectionAtPosition(ID_NOTE_LIST, false);
            return;
        }
        if (currentFragId == itemId) return;

        changeFragment(manager, itemId, true);
    }

    public boolean handleBackPressed(FragmentManager manager) {
        if (currentFragId == ID_NOTE_LIST) return false;
        boolean toNoteList = (previousFragId != ID_EDIT_NOTE
                && (currentFragId != ID_PREVIEW && currentFragId != ID_HELP));
        if (toNoteList) {
            changeFragment(manager, ID_NOTE_LIST, false);
            setDrawerSelection(ID_NOTE_LIST);
            return true;
        } else {
            changeFragment(manager, ID_EDIT_NOTE, false);
            setDrawerSelection(-1);
            return true;
        }
    }

    private void setDrawerSelection(int id) {
        if (drawer != null) {
            if (id != -1) {
                drawer.deselect();
            } else {
                drawer.setSelection(id);
            }
        }
    }

    private void hideDrawerIcon() {
        if (drawer != null) {
            if (drawer.isDrawerOpen()) drawer.closeDrawer();
            ActionBarDrawerToggle toggle = drawer.getActionBarDrawerToggle();
            if (toggle.isDrawerIndicatorEnabled()) {
                toggle.setDrawerIndicatorEnabled(false);
            }
        }
    }

    private void showDrawerIcon() {
        if (drawer != null) {
            ActionBarDrawerToggle toggle = drawer.getActionBarDrawerToggle();
            if (!toggle.isDrawerIndicatorEnabled()) {
                toggle.setDrawerIndicatorEnabled(true);
            }
        }
    }

    private void showFab() {
        if (listener != null) listener.showFab();
    }

    private void hideFab() {
        if (listener != null) listener.hideFab();
    }

    private void changeFab(int fabType){
        if (listener != null) listener.changeFab(fabType);
    }

    public static Transition getTransition(int gravity) {
        Slide slide = new Slide();
        slide.setSlideEdge(gravity);
        slide.setDuration(SLIDE_DEFAULT_TIME);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }


    private Transition getSignInTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_shared);
    }

    private Transition getSignInEnterTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_enter);
    }

    private Transition getIntroToSignInTransition(Context context) {
        return TransitionInflater.from(context).inflateTransition(R.transition.intro_sign_in_exit);
    }

}
