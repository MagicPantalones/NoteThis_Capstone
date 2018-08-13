package io.magics.notethis.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.List;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("CheckResult")
public class AppDbUtils {

    public static void fetchNote(AppDatabase db, int id, RoomNoteCallback<Note> callback) {
        Single.fromCallable(() -> db.userNoteModel().getNote(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback::onComplete, callback::onFail);
    }

    public static void insertNote(AppDatabase db, Note note, RoomNoteCallback<Note> listeners) {
        Single.fromCallable(() -> db.userNoteModel().insertAll(note))
                .subscribeOn(Schedulers.io())
                .flatMap(l -> {
                    if (l != null) {
                        note.setId(l.intValue());
                        return Single.fromCallable(() -> note);
                    }
                    return Single.error(new RoomInsertException("Insert failed"));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listeners::onComplete, listeners::onFail);
    }

    public static void insertNotes(AppDatabase db, List<Note> notes) {
        Observable.fromIterable(notes)
                .subscribeOn(Schedulers.io())
                .map(note -> db.userNoteModel().insertAll(note))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void deleteNotes(AppDatabase db, List<NoteTitle> titles) {
        Observable.fromIterable(titles)
                .subscribeOn(Schedulers.io())
                .doOnNext(noteTitle -> db.userNoteModel().deleteNotes(noteTitle.getId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void updateNote(AppDatabase db, Note note) {
        Completable.fromAction(() -> db.userNoteModel().updateNote(note))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void insertImgurRef(AppDatabase db, Image image) {
        Completable.fromAction(() -> db.userImageModel().insertImages(image))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void insertImgurRefs(AppDatabase db, List<Image> images) {
        Observable.fromIterable(images)
                .subscribeOn(Schedulers.io())
                .doOnNext(image -> db.userImageModel().insertImages(image))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void deleteImgurRef(AppDatabase db, Image image) {
        Completable.fromAction(() -> db.userImageModel().deleteImage(image))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void handleDbErrors(String tag, Throwable e) {
        Log.w(tag, "handleDbErrors: ", e);
    }

    private AppDbUtils() {}

}
