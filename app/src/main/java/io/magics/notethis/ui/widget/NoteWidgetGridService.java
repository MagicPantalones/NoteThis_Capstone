package io.magics.notethis.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NoteWidgetGridService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NoteWidgetGridFactory(getApplicationContext());
    }
}
