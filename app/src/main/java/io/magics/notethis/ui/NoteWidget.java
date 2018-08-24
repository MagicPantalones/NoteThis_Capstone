package io.magics.notethis.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import io.magics.notethis.R;
import io.magics.notethis.utils.MarkdownUtils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import ru.noties.markwon.Markwon;

/**
 * Implementation of App Widget functionality.
 */
public class NoteWidget extends AppWidgetProvider {
    public static final String EXTRA_NOTE_ID = "note_id";
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, NoteTitle noteTitle) {
        Intent appIntent = new Intent(context, MainActivity.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        if (TextUtils.isEmpty(noteTitle.getTitle())) {
            views.setViewVisibility(R.id.widget_title, View.GONE);
            views.setViewVisibility(R.id.widget_preview, View.GONE);
            views.setViewVisibility(R.id.widget_open_app, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.widget_open_app,
                    PendingIntent.getActivity(context, 0, appIntent, 0));
        } else {
            views.setTextViewText(R.id.widget_title, noteTitle.getTitle());
            views.setTextViewText(R.id.widget_preview, noteTitle.getPreview());
            views.setViewVisibility(R.id.widget_title, View.VISIBLE);
            views.setViewVisibility(R.id.widget_preview, View.VISIBLE);
            views.setViewVisibility(R.id.widget_open_app, View.GONE);
            appIntent.putExtra(EXTRA_NOTE_ID, noteTitle.getId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateWidget(Context context, NoteTitle noteTitle) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] awmIds = awm.getAppWidgetIds(new ComponentName(context, NoteWidget.class));
        for (int appWidgetId : awmIds) {
            updateAppWidget(context, awm, appWidgetId, noteTitle);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateWidget(context, new NoteTitle());
    }
}

