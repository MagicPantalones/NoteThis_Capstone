package io.magics.notethis.ui.widget;

import android.app.Application;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteWidgetIntentService extends IntentService {

    private static final String ACTION_UPDATE_NOTE_LIST =
            "io.magics.notethis.ui.widget.action.UPDATE_NOTE_LIST";


    public NoteWidgetIntentService() {
        super("NoteWidgetIntentService");
    }

    public static void startActionFoo(Context context) {
        Intent intent = new Intent(context, NoteWidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_NOTE_LIST);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_NOTE_LIST.equals(action)) {
                handleActionFoo();
            }
        }
    }

    private void handleActionFoo() {

    }

}
