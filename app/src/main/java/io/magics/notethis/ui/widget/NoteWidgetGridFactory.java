package io.magics.notethis.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import io.magics.notethis.R;
import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.ui.MainActivity;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteWidgetGridFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String EXTRA_NOTE_ID = "note_id";

    private Context context;
    private AppDatabase appDb;
    private List<NoteTitle> noteTitles;

    public NoteWidgetGridFactory(Context appContext) {
        appDb = AppDatabase.getInMemoryDatabase(appContext);
        context = appContext;
    }

    @Override
    public void onCreate() {
        //Not needed.
    }

    @Override
    public void onDataSetChanged() {
        if (noteTitles == null) {
            new GetNoteList().execute();
        }
    }

    @Override
    public void onDestroy() {
        //Not needed.
    }

    @Override
    public int getCount() {
        return noteTitles.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (noteTitles == null || noteTitles.isEmpty()) return null;
        RemoteViews root = new RemoteViews(context.getPackageName(),
                R.layout.note_widget_grid_item);
        NoteTitle title = noteTitles.get(position);

        root.setTextViewText(R.id.note_widget_title, title.getTitle());
        root.setTextViewText(R.id.note_widget_subtitle, title.getPreview());

        Intent fillIntent = new Intent();
        fillIntent.putExtra(EXTRA_NOTE_ID, title.getId());

        root.setOnClickFillInIntent(R.id.grid_item_wrapper, fillIntent);
        return root;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    private class GetNoteList extends AsyncTask<Void, Void, List<NoteTitle>> {

        @Override
        protected List<NoteTitle> doInBackground(Void... voids) {
            return appDb.userNoteModel().getNoteTitlesList();
        }

        @Override
        protected void onPostExecute(List<NoteTitle> titles) {
            super.onPostExecute(titles);
            noteTitles = titles != null ? titles : new ArrayList<>();
        }
    }

}
