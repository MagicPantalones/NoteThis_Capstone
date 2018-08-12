package io.magics.notethis.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.haha.perflib.Main;

import io.magics.notethis.R;
import io.magics.notethis.ui.MainActivity;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.ui.fragments.NoteListFragment;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.EditNoteViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;

public class CloseDialog extends DialogFragment{


    public static CloseDialog newInstance() {
        return new CloseDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        EditNoteViewModel model = ViewModelProviders.of(getActivity()).get(EditNoteViewModel.class);

        dialogBuilder.setTitle(R.string.unsaved_close_title);
        dialogBuilder.setMessage(R.string.unsaved_close_text);

        dialogBuilder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (dialog != null) {
                String oldTitle = Utils.getToolbarTitle(getContext());
                if (oldTitle.equals(EditNoteViewModel.NEW_NOTE_TITLE)){
                    SaveDialog.newInstance(EditNoteFragment.ACTION_CLOSE).show(getFragmentManager(),
                            Utils.DIALOG_SAVE);
                } else {
                    model.saveChanges(oldTitle);
                    dialog.dismiss();
                    Utils.backPressed(getContext());
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.exit, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
                Utils.backPressed(getContext());
            }
        });

        return dialogBuilder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }
}
