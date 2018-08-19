package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.ImageView;

import com.labo.kaji.swipeawaydialog.support.v4.SwipeAwayDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;

public class ImageSheetDialog extends BottomSheetDialog {

    @BindView(R.id.dialog_image)
    ImageView dialogImage;

    private static final String ARG_URL = "url";

    private String url;

    public ImageSheetDialog(@NonNull Context context) {
        super(context);
    }
}
