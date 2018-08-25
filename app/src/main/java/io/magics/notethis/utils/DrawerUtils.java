package io.magics.notethis.utils;

import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import io.magics.notethis.R;
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

        View headerRoot = View.inflate(activity, R.layout.nav_header, null);

        return new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withTranslucentStatusBar(true)
                .withDisplayBelowStatusBar(true)
                .withSelectedItem(currentFrag)
                .addDrawerItems(
                        drawerItemNoteList,
                        drawerItemImgurList,
                        drawerItemHelp,
                        drawerItemLogOut
                )
                .withHeader(headerRoot);

    }

    public static void setEmail(Drawer drawer, String userEmail) {
        View header = drawer.getHeader();
        TextView emailField = header.findViewById(R.id.nav_user_name);
        emailField.setText(userEmail);
    }
}
