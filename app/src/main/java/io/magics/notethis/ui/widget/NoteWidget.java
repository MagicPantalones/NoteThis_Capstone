package io.magics.notethis.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.squareup.haha.perflib.Main;

import io.magics.notethis.R;
import io.magics.notethis.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class NoteWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Intent appIntent = new Intent(context, MainActivity.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget);



        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

    }

    @Override
    public void onEnabled(Context context) {
        NoteWidgetIntentService.startActionFoo(context);
    }


}

