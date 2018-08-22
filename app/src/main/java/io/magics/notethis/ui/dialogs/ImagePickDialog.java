package io.magics.notethis.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.viewmodels.ImgurViewModel;

public class ImagePickDialog extends BottomSheetDialogFragment {

    @BindView(R.id.image_picker)
    RecyclerView recyclerView;

    public ImagePickDialog() {
        //Required public constructor
    }

    public static ImagePickDialog newInstance() {
        return new ImagePickDialog();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(getContext(), R.layout.dialog_image_chooser, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        ImgurViewModel model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        ImagePickDialogAdapter adapter = new ImagePickDialogAdapter();

        recyclerView.setAdapter(adapter);
        model.getImages().observe(this, adapter::insertImages);

    }

    private class ImagePickDialogAdapter extends RecyclerView.Adapter<ImagePickerViewHolder> {

        List<Image> images = new ArrayList<>();

        ImagePickDialogAdapter(){}

        @NonNull
        @Override
        public ImagePickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.image_picker_vh, parent,
                    false);
            return new ImagePickerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagePickerViewHolder holder, int position) {
            Image image = images.get(position);
            GlideApp.with(holder.image)
                    .load(image.getLink())
                    .error(R.drawable.owl_24dp_stroke)
                    .placeholder(R.drawable.owl_24dp_stroke)
                    .fallback(R.drawable.owl_24dp_stroke)
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        void insertImages(List<Image> images) {
            this.images = images;
            notifyDataSetChanged();
        }
    }

    class ImagePickerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_picker_image)
        ImageView image;

        public ImagePickerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
