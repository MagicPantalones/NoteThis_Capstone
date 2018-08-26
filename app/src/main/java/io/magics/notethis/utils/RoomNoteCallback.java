package io.magics.notethis.utils;


public interface RoomNoteCallback<T> {

    void onComplete(T data);
    void onFail(Throwable e);

}
