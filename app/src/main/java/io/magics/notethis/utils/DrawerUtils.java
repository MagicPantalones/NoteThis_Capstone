package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import io.magics.notethis.R;
import io.magics.notethis.ui.UiUtils;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.viewmodels.ImgurViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;

public class DrawerUtils {

    public static final int ITEM_NOTE_LIST = 1;
    public static final int ITEM_IMGUR_LIST = 2;
    public static final int ITEM_HELP_SCREEN = 3;
    public static final int ITEM_LOG_OUT = 4;

    public static DrawerBuilder initDrawer(Activity activity, Toolbar toolbar, int currentFrag) {



        PrimaryDrawerItem drawerItemNoteList = new PrimaryDrawerItem()
                .withIdentifier(ITEM_NOTE_LIST)
                .withName(R.string.menu_nav_notes)
                .withIcon(R.drawable.outline_note_24);

        PrimaryDrawerItem drawerItemImgurList = new PrimaryDrawerItem()
                .withIdentifier(ITEM_IMGUR_LIST)
                .withName(R.string.menu_nav_imgur)
                .withIcon(R.drawable.outline_image_24);

        SecondaryDrawerItem drawerItemHelp = new SecondaryDrawerItem()
                .withIdentifier(ITEM_HELP_SCREEN)
                .withName(R.string.menu_nav_help)
                .withIcon(R.drawable.outline_help_outline_24);

        SecondaryDrawerItem drawerItemLogOut = new SecondaryDrawerItem()
                .withIdentifier(ITEM_LOG_OUT)
                .withName(R.string.menu_nav_sign_out)
                .withIcon(R.drawable.outline_remove_circle_outline_24)
                .withEnabled(false);

        return new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(currentFrag)
                .addDrawerItems(
                        drawerItemNoteList,
                        drawerItemImgurList,
                        drawerItemHelp,
                        drawerItemLogOut
                )
                .withShowDrawerOnFirstLaunch(false);

    }

    public static void setDrawerItem(Activity activity, FragmentManager manager,
                                     IDrawerItem drawerItem) {
        if (drawerItem == null) return;
        long id = drawerItem.getIdentifier();
        if (id == ITEM_NOTE_LIST) {
            UiUtils.showNoteListFrag(activity, manager);
        } else if (id == ITEM_IMGUR_LIST) {
            UiUtils.showImgurList(manager);
        } else if (id == ITEM_HELP_SCREEN) {
            UiUtils.showHelpFrag(manager);
        }
    }

    public static void signOut(Activity activity, NoteViewModel noteVm, FragmentManager manager) {
        noteVm.signOut();
        UiUtils.handleUserSignOut(activity, manager);
    }

    public static Drawer setProfileAndBuild(Activity activity, DrawerBuilder builder,
                                            String userEmail, String profileImage) {
        IProfile profile = new ProfileDrawerItem().withName(userEmail)
                .withIcon(profileImage);

        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(profile)
                .withOnAccountHeaderListener((view, profile1, current) -> false)
                .build();

        return builder
                .withAccountHeader(header)
                .build();
    }
}