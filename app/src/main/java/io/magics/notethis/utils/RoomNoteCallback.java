package io.magics.notethis.utils;


import java.util.List;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public interface RoomNoteCallback<T> {

    void onComplete(T data);
    void onFail(Throwable e);

    enum DataError {
        NO_DATA_AVAILABLE,
        NO_INTERNET_LOG_IN,
        NO_INTERNET_IMGUR,
        NO_INTERNET_FIRE_DB,
        ROOM_WRITE_ERROR,
        FIREBASE_WRITE_ERROR,
        UNHANDLED_ERROR
    }

}
