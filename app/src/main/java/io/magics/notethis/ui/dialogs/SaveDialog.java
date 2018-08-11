package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;

public class SaveDialog extends DialogFragment{

    @BindView(R.id.d_save_edit_text)
    EditText titleText;

    SaveDialogListener listener;

    public SaveDialog() {
        // Empty constructor required for DialogFragment
    }


    public static SaveDialog newInstance() {
        return new SaveDialog();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleText.requestFocus();
        getDialog().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getContext(), R.layout.dialog_save, null);
        ButterKnife.bind(this, view);

        alertBuilder.setTitle(getString(R.string.dialog_save_note_title));
        alertBuilder.setView(view);
        alertBuilder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (dialog != null) {
                listener.onSave(titleText.getText().toString());
                dialog.dismiss();
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (dialog != null) dialog.dismiss();
        });

        return alertBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SaveDialogListener) listener = (SaveDialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface SaveDialogListener {
        void onSave(String title);
    }
}
