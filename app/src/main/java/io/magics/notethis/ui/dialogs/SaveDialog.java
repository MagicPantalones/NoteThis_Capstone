package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.EditNoteFragment;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;

public class SaveDialog extends DialogFragment{

    private static final String ARG_ACTION = "ACTION";
    
    @BindView(R.id.d_save_edit_text)
    EditText titleText;

    private int action;

    public SaveDialog() {
        // Empty constructor required for DialogFragment
    }

    public static SaveDialog newInstance(int action) {
        SaveDialog dialog = new SaveDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            action = getArguments().getInt(ARG_ACTION, -1);
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getContext(), R.layout.dialog_save, null);
        ButterKnife.bind(this, view);
        NoteViewModel viewModel =
                ViewModelProviders.of(getActivity()).get(NoteViewModel.class);

        String oldTitle = Utils.getToolbarTitle(getContext());

        alertBuilder.setTitle(getString(R.string.dialog_note_title_heading));
        alertBuilder.setView(view);
        alertBuilder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (dialog != null) {
                viewModel.saveChanges(titleText.getText().toString());
                dialog.dismiss();
                if (action == EditNoteFragment.ACTION_CLOSE) {
                    Utils.backPressed(getContext());
                }
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        Dialog dialog = alertBuilder.create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        titleText.setText(oldTitle);
        titleText.selectAll();
        titleText.requestFocus();

        return dialog;
    }

}
