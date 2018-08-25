package io.magics.notethis.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import io.magics.notethis.R;
import io.magics.notethis.ui.UiUtils;
import io.magics.notethis.ui.fragments.HelpFragment;
import io.magics.notethis.ui.fragments.ImgurListFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.viewmodels.ImgurViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;

public class DrawerUtils {

    public static final int ITEM_NOTE_LIST = 111;
    public static final int ITEM_IMGUR_LIST = 331;
    public static final int ITEM_HELP_SCREEN = 551;
    public static final int ITEM_LOG_OUT = 4;

    public static DrawerBuilder initDrawer(Activity activity, Toolbar toolbar, int currentFrag) {

        int selectedColor = activity.getResources().getColor(R.color.secondaryColor);


        PrimaryDrawerItem drawerItemNoteList = new PrimaryDrawerItem()
                .withIdentifier(ITEM_NOTE_LIST)
                .withIconTintingEnabled(true)
                .withSelectedIconColor(selectedColor)
                .withName(R.string.menu_nav_notes)
                .withIcon(R.drawable.outline_note_24);

        PrimaryDrawerItem drawerItemImgurList = new PrimaryDrawerItem()
                .withIdentifier(ITEM_IMGUR_LIST)
                .withIconTintingEnabled(true)
                .withSelectedIconColor(selectedColor)
                .withName(R.string.menu_nav_imgur)
                .withIcon(R.drawable.outline_image_24);

        PrimaryDrawerItem drawerItemHelp = new PrimaryDrawerItem()
                .withIdentifier(ITEM_HELP_SCREEN)
                .withIconTintingEnabled(true)
                .withSelectedIconColor(selectedColor)
                .withName(R.string.menu_nav_help)
                .withIcon(R.drawable.outline_help_outline_24);

        PrimaryDrawerItem drawerItemLogOut = new PrimaryDrawerItem()
                .withIdentifier(ITEM_LOG_OUT)
                .withIconTintingEnabled(true)
                .withSelectedIconColor(selectedColor)
                .withName(R.string.menu_nav_sign_out)
                .withIcon(R.drawable.outline_remove_circle_outline_24);

        return new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withTranslucentStatusBar(false)
                .withDisplayBelowStatusBar(true)
                .withSelectedItem(currentFrag)
                .addDrawerItems(
                        drawerItemNoteList,
                        drawerItemImgurList,
                        drawerItemHelp,
                        drawerItemLogOut
                )
                .withShowDrawerOnFirstLaunch(false);

    }

    public static boolean setDrawerItem(FragmentManager manager,
                                     IDrawerItem drawerItem) {
        if (drawerItem == null) return true;
        long id = drawerItem.getIdentifier();
        if (id == ITEM_NOTE_LIST) {
            UiUtils.replaceFragment(manager, NoteListFragment.newInstance());
        } else if (id == ITEM_IMGUR_LIST) {
            UiUtils.replaceFragment(manager, ImgurListFragment.newInstance());
        } else if (id == ITEM_HELP_SCREEN) {
            UiUtils.replaceFragment(manager, HelpFragment.newInstance());
        }
        return false;
    }

    public static boolean signOut(Activity activity, NoteViewModel noteVm, FragmentManager manager) {
        noteVm.signOut();
        UiUtils.handleUserSignOut(activity, manager);
        return false;
    }

    public static Drawer setProfileAndBuild(Activity activity, DrawerBuilder builder,
                                            String userEmail) {

        View headerRoot = View.inflate(activity, R.layout.nav_header, null);
        TextView emailField = headerRoot.findViewById(R.id.nav_user_name);
        emailField.setText(userEmail);

        return builder
                .withHeader(headerRoot)
                .build();
    }
}
