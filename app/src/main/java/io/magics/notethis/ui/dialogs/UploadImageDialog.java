package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.viewmodels.ImgurViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;

public class UploadImageDialog extends DialogFragment {

    private static final String ARG_IMAGE = "image";

    @BindView(R.id.dialog_photo_preview)
    ImageView photoPreview;
    @BindView(R.id.dialog_upload_title)
    EditText imgTitle;
    @BindView(R.id.dialog_upload_button)
    Button uploadButton;
    @BindView(R.id.dialog_cancel_button)
    Button cancelButton;

    UploadDialogHandler handler;

    public UploadImageDialog() {
        //Required public constructor
    }

    public static UploadImageDialog newInstance(File image){
        UploadImageDialog dialog = new UploadImageDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE, image);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(), R.layout.dialog_upload, null);
        ButterKnife.bind(this, view);
        builder.setView(view);

        File file = (File) getArguments().getSerializable(ARG_IMAGE);

        GlideApp.with(view)
                .load(file)
                .placeholder(R.drawable.owl_24dp_stroke)
                .error(R.drawable.owl_24dp_stroke)
                .into(photoPreview);

        uploadButton.setOnClickListener(v -> upload(imgTitle.getText().toString()));
        cancelButton.setOnClickListener(v -> dismiss());


        return builder.create();
    }

    private void upload(String title) {
        if (handler != null) handler.onUpload(title);
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDialogHandler) handler = (UploadDialogHandler) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler = null;
    }

    public interface UploadDialogHandler {
        void onUpload(String title);
    }
}
