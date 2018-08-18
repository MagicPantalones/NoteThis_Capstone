package io.magics.notethis.ui;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.ui.fragments.HelpFragment;
import io.magics.notethis.ui.fragments.IntroFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.ui.fragments.PreviewFragment;
import io.magics.notethis.ui.fragments.SignInFragment;
import io.magics.notethis.utils.Utils;

public class UiUtils {

    private static final long SLIDE_DEFAULT_TIME = 300;

    private static final String FRAG_INTRO = "frag_intro";
    private static final String FRAG_SIGN_IN = "frag_sign_in";
    private static final String FRAG_NOTE_LIST = "frag_note_list";
    private static final String FRAG_EDIT_NOTE = "frag_edit_note";
    private static final String FRAG_PREVIEW = "frag_preview";
    private static final String FRAG_HELP = "frag_help";

    private static final int CONTAINER = R.id.container_main;

    public static void introToSignInFrag(Context context, FragmentManager manager) {
        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Required override
            }

            @Override
            public void onFinish() {
                Fragment oldFrag = manager.findFragmentById(R.id.container_main);
                if (!(oldFrag instanceof IntroFragment)) return;

                View oldFragView = oldFrag.getView();

                if (oldFragView == null) return;

                ImageView sharedLogo = oldFragView.findViewById(R.id.img_intro_logo);
                SignInFragment newFrag = SignInFragment.newInstance();

                oldFrag.setExitTransition(Utils.getIntroToSignInTransition(context));
                newFrag.setSharedElementEnterTransition(Utils.getSignInTransition(context));
                newFrag.setEnterTransition(Utils.getSignInEnterTransition(context));


                manager.beginTransaction()
                        .setReorderingAllowed(true)
                        .addSharedElement(sharedLogo, sharedLogo.getTransitionName())
                        .replace(CONTAINER, newFrag, FRAG_SIGN_IN)
                        .commit();
            }

        }.start();

    }

    public static void introToListFrag(Activity activity, FragmentManager manager) {
        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Required override
            }

            @Override
            public void onFinish() {
                showNoteListFrag(activity, manager);
            }

        }.start();
    }

    public static void showIntroFrag(Activity activity, FragmentManager manager) {
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        IntroFragment newFrag = IntroFragment.newInstance();
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.START));
            newFrag.setEnterTransition(getTransition(Gravity.START));
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        Utils.hideToolbar(activity);


        manager.beginTransaction()
                .replace(CONTAINER, newFrag, FRAG_INTRO)
                .commit();
    }

    public static void showNoteListFrag(Activity activity, FragmentManager manager) {
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.END));
        }

        if (activity != null && activity.getWindow() != null) {
            Utils.showToolbar(activity);
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        NoteListFragment newFrag = NoteListFragment.newInstance();
        newFrag.postponeEnterTransition();
        newFrag.setEnterTransition(getTransition(Gravity.END));
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(CONTAINER, newFrag, FRAG_NOTE_LIST)
                .commit();
    }

    public static void showEditNoteFrag(FragmentManager manager) {
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.START));
        }
        EditNoteFragment newFrag = EditNoteFragment.newInstance();
        newFrag.setEnterTransition(getTransition(Gravity.END));

        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(CONTAINER, newFrag, FRAG_EDIT_NOTE)
                .addToBackStack(FRAG_EDIT_NOTE)
                .commit();
    }

    public static void showPreviewFrag(FragmentManager manager) {
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.START));
        }
        PreviewFragment newFrag = PreviewFragment.newInstance();
        newFrag.setEnterTransition(getTransition(Gravity.END));
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(CONTAINER, newFrag, FRAG_PREVIEW)
                .addToBackStack(FRAG_PREVIEW)
                .commit();
    }

    public static void showHelpFrag(FragmentManager manager) {
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.START));
        }
        HelpFragment newFrag = HelpFragment.newInstance();
        newFrag.setEnterTransition(getTransition(Gravity.END));
        manager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(CONTAINER, newFrag, FRAG_HELP)
                .addToBackStack(FRAG_HELP)
                .commit();

    }

    public static void showImgurList(FragmentManager manager) {
        //TODO Implement after ImgurFragment has been written.
        Fragment oldFrag = manager.findFragmentById(CONTAINER);
        if (oldFrag != null) {
            oldFrag.setExitTransition(getTransition(Gravity.START));
        }
    }

    public static void handleUserSignOut(Activity activity, FragmentManager manager) {
        List<Fragment> fragList = manager.getFragments();
        if (fragList != null && !fragList.isEmpty()) {
            for (Fragment frag : fragList) {
                manager.beginTransaction().remove(frag).commit();
            }
        }
        showIntroFrag(activity, manager);
    }

    public static boolean isFragType(FragmentManager manager, Class<?> clazz) {
        Fragment frag = manager.findFragmentById(CONTAINER);
        return clazz.isAssignableFrom(frag.getClass());
    }


    private static Transition getTransition(int gravity) {
        Slide slide = new Slide();
        slide.setSlideEdge(gravity);
        slide.setDuration(SLIDE_DEFAULT_TIME);
        slide.setInterpolator(new FastOutSlowInInterpolator());
        return slide;
    }


    private UiUtils() {}

}
