package io.magics.notethis.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import io.magics.notethis.utils.models.Note;

public class EditNoteViewModel extends ViewModel {

    private String tempText;
    private Note note;

    public Note getNoteForSave(boolean saveChanges, String newTitle) {
        if (saveChanges) {
            note.setBody(tempText);
            note.setBodyPreview();
        }

        if (!TextUtils.isEmpty(newTitle)) {
            note.setTitle(newTitle);
        }

        if (TextUtils.isEmpty(note.getTitle())) {
            note.setTitle("New Note");
        }

        return note;
    }

    public void setNoteId(int id) {
        if (note != null) note.setId(id);
    }

    public Note getNote() {
        return note;
    }

    public void newNote() {
        tempText = "";
        note = new Note("", "", "");
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public boolean hasUnsavedChanges(String text) {
        if (TextUtils.isEmpty(text)) return false;
        boolean hasChanged = !note.getBody().equals(text);
        if (hasChanged) {
            tempText = text;
        }

        return hasChanged;
    }
}
