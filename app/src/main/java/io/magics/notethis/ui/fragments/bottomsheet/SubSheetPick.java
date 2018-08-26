package io.magics.notethis.ui.fragments.bottomsheet;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.SheetUtils;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.viewmodels.ImgurViewModel;

import static io.magics.notethis.ui.fragments.SheetUtils.getImgTemplate;

public class SubSheetPick extends BottomSheetDialogFragment {

    @BindView(R.id.sub_pick_recycler)
    RecyclerView recyclerView;

    private SheetCallbacks callbacks;

    public SubSheetPick() {
        //Required
    }

    public static SubSheetPick newInstance(){
        return new SubSheetPick();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.sub_sheet_pick, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ImagePickDialogAdapter adapter = new ImagePickDialogAdapter();


        recyclerView.setAdapter(adapter);

        ImgurViewModel model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        model.getImages().observe(this, adapter::insertImages);
        SheetUtils.setBehaviour(view, dialog);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SheetCallbacks) callbacks = (SheetCallbacks) context;
    }

    @Override
    public void onDetach() {
        callbacks = null;
        super.onDetach();
    }

    private class ImagePickDialogAdapter extends RecyclerView.Adapter<ImagePickerViewHolder> {

        List<Image> images = new ArrayList<>();

        ImagePickDialogAdapter() {
        }

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
                    holder.image.setOnClickListener(v -> {
                        if (callbacks != null) {
                            callbacks.onReturnTemplate(getImgTemplate(getResources(),
                                    image.getTitle(), image.getLink()));
                            dismiss();
                        }
            });
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

        ImagePickerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
