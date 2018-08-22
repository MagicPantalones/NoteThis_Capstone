package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.utils.models.Image;
import ru.noties.markwon.Markwon;

public class TemplatesBottomSheet extends BottomSheetDialogFragment {

    @BindView(R.id.peek_row)
    View peekRow;
    @BindView(R.id.button_row)
    View buttonRow;
    @BindViews({R.id.peek_lists, R.id.peek_headers, R.id.peek_links, R.id.peek_image})
    List<ImageView> peekIcons;

    @BindView(R.id.peek_positive_button)
    Button positiveButton;
    @BindView(R.id.peek_negative_button)
    Button negativeButton;



    @BindViews({R.id.lists_sheet, R.id.headers_sheet, R.id.link_sheet, R.id.images_sheet})
    List<View> sheets;
    @BindViews({R.id.h1, R.id.h2, R.id.h3, R.id.h4, R.id.h5, R.id.h6})
    List<TextView> headerPreviews;
    @BindViews({R.id.pick_uploaded_image, R.id.upload_image, R.id.template_image})
    List<View> imageSheetHeaders;
    @BindViews({R.id.sub_sheet_pick, R.id.sub_sheet_upload, R.id.sub_sheet_template})
    List<View> imageSubSheets;

    @BindView(R.id.sheet_url_title)
    EditText urlTitle;
    @BindView(R.id.sheet_url)
    EditText url;

    @BindView(R.id.image_picker_recycler)
    RecyclerView recyclerView;

    @BindView(R.id.sheet_upload_preview)
    ImageView uploadPreview;
    @BindView(R.id.sub_upload_img_title)
    EditText uploadTitle;

    @BindView(R.id.template_img_alt)
    EditText templateAltText;
    @BindView(R.id.template_img_url)
    EditText templateImgUrl;

    BottomSheetBehavior behavior;

    TemplateSheetCallback callback;

    public interface TemplateSheetCallback {
        void onTemplateChosen(String template);
    }

    public TemplatesBottomSheet() {
        //Required public constructor
    }

    public static TemplatesBottomSheet newInstance() {
        return new TemplatesBottomSheet();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.templates_bottom_sheet, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)
                ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = lp.getBehavior();

        if (behavior instanceof BottomSheetBehavior) {
            setup((BottomSheetBehavior) behavior);
        } else {
            dismiss();
        }
    }

    private void setup(BottomSheetBehavior behavior) {
        this.behavior = behavior;


    }

    private void listPeekClicked() {
        View root = sheets.get(0);

        TextView orderedListPreview = root.findViewById(R.id.ordered_list);
        TextView unorderedListPreview = root.findViewById(R.id.unordered_list);

        String numText = orderedListPreview.getText().toString();
        String bulText = unorderedListPreview.getText().toString();

        Markwon.setMarkdown(orderedListPreview, numText);
        Markwon.setMarkdown(unorderedListPreview, bulText);

        orderedListPreview.setOnClickListener(v ->
                returnTemplate(getString(R.string.template_ordered_list)));
        unorderedListPreview.setOnClickListener(v ->
                returnTemplate(getString(R.string.template_unordered_list)));

        showViewHideRest(root, sheets.get(1), sheets.get(2), sheets.get(3));

        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void headerPeekClicked() {



    }

    private void linkPeekClicked() {

    }

    private void imgPeekClicked() {

    }

    private void returnTemplate(String string) {
        if (callback != null) callback.onTemplateChosen(string);
    }



    private void setDefaultState(int mode) {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    private void showViewHideRest(View show, View... hide) {
        show.setVisibility(View.VISIBLE);
        for (View v : hide) {
            v.setVisibility(View.GONE);
        }
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
