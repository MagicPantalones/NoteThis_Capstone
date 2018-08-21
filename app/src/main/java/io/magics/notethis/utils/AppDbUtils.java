package io.magics.notethis.utils;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
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

    public static void insertNote(AppDatabase db, Note note,
                                  @Nullable RoomNoteCallback<Note> listeners) {
        Single.fromCallable(() -> db.userNoteModel().insertAll(note))
                .subscribeOn(Schedulers.io())
                .flatMap(l -> {
                    if (l != null && !l.isEmpty()) {
                        note.setId(l.get(0).intValue());
                        return Single.fromCallable(() -> note);
                    }
                    return Single.error(new RoomInsertException("Insert failed"));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(note1 -> {
                    if (listeners != null) listeners.onComplete(note1);
                }, throwable -> {
                    if (listeners != null) listeners.onFail(throwable);
                });
    }

    public static void insertNotes(AppDatabase db, List<Note> notes) {
        Observable.fromIterable(notes)
                .subscribeOn(Schedulers.io())
                .map(note -> db.userNoteModel().insertAll(note))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void deleteNote(AppDatabase db, NoteTitle title) {
        Completable.fromAction(() -> db.userNoteModel().deleteNotes(title.getId()))
                .subscribeOn(Schedulers.io())
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

    public static class RoomInsertException extends RuntimeException {

        public RoomInsertException() {
            super();
        }

        public RoomInsertException(String message) {
            super(message);
        }

    }
}
