package io.magics.notethis.ui.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import io.magics.notethis.viewmodels.ImgurViewModel;
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

    BottomSheetBehavior behavior;
    ImgurViewModel model;
    ImagePickDialogAdapter adapter;
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
        CoordinatorLayout.Behavior lb = lp.getBehavior();

        if (lb instanceof BottomSheetBehavior) {
            setup((BottomSheetBehavior) lb);
        } else {
            dismiss();
        }
    }

    private void setup(BottomSheetBehavior behavior) {
        this.behavior = behavior;
        this.behavior.setHideable(false);
        model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        adapter = new ImagePickDialogAdapter();
        model.getImages().observe(this, adapter::insertImages);
        for (ImageView icon : peekIcons) {
            switch (icon.getId()) {
                case R.id.peek_lists:
                    icon.setOnClickListener(v -> listPeekClicked());
                    break;
                case R.id.peek_headers:
                    icon.setOnClickListener(v -> headerPeekClicked());
                    break;
                case R.id.peek_links:
                    icon.setOnClickListener(v -> linkPeekClicked());
                    break;
                case R.id.peek_image:
                    icon.setOnClickListener(v -> imgPeekClicked());
                    break;
                default:
                    break;
            }
        }

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

        for (TextView h : headerPreviews) {
            switch (h.getId()) {
                case R.id.h1:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_1)));
                break;
                case R.id.h2:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_2)));
                    break;
                case R.id.h3:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_3)));
                    break;
                case R.id.h4:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_4)));
                    break;
                case R.id.h5:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_5)));
                    break;
                case R.id.h6:
                    h.setOnClickListener(v ->
                            returnTemplate(getString(R.string.template_headers_6)));
                    break;
                default:
                    break;
            }
            String text = h.getText().toString();
            Markwon.setMarkdown(h, text);
        }
        showViewHideRest(sheets.get(1), sheets.get(0), sheets.get(2), sheets.get(3));
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void linkPeekClicked() {
        View root = sheets.get(2);
        EditText urlTitle = root.findViewById(R.id.sheet_url_title);
        EditText url = root.findViewById(R.id.sheet_url);
        setPeekConfirmationButtons(getString(R.string.confirm), v -> {
            String retString = getString(R.string.template_link);
            String retUrl = TextUtils.isEmpty(url.getText()) ? "URL" : url.getText().toString();
            String retTitle = TextUtils.isEmpty(urlTitle.getText()) ? url.getText().toString()
                    : urlTitle.getText().toString();
            retString = retString.replace("alt-text", retTitle);
            urlTitle.setText("");
            url.setText("");
            returnTemplate(retString.replace("URL", retUrl));
        }, getString(R.string.cancel), v -> {
            urlTitle.setText("");
            urlTitle.setText("");
            setDefaultState();
        });
        switchRows(buttonRow, peekRow);
        showViewHideRest(sheets.get(2), sheets.get(0), sheets.get(1), sheets.get(3));
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void imgPeekClicked() {

        imageSheetHeaders.get(0).setOnClickListener(v -> {
            View root = imageSubSheets.get(0);
            RecyclerView recyclerView = root.findViewById(R.id.image_picker_recycler);
            recyclerView.setAdapter(adapter);
            showViewHideRest(imageSubSheets.get(0), imageSubSheets.get(1), imageSubSheets.get(2));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        imageSheetHeaders.get(1).setOnClickListener(v -> {
            @BindView(R.id.sheet_upload_preview)
            ImageView uploadPreview;
            @BindView(R.id.sub_upload_img_title)
            EditText uploadTitle;

        });
        imageSheetHeaders.get(2).setOnClickListener(v -> {
            @BindView(R.id.template_img_alt)
            EditText templateAltText;
            @BindView(R.id.template_img_url)
            EditText templateImgUrl;
        });
    }

    private void returnTemplate(String string) {
        if (callback != null) callback.onTemplateChosen(string);
        setDefaultState();
    }



    private void setDefaultState() {
        switchRows(peekRow, buttonRow);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setPeekConfirmationButtons(String positiveText, OnClickListener positiveListener,
                                            String negativeText, OnClickListener negativeListener) {
        positiveButton.setText(positiveText);
        positiveButton.setOnClickListener(positiveListener);
        negativeButton.setText(negativeText);
        negativeButton.setOnClickListener(negativeListener);
    }


    private void showViewHideRest(View show, View... hide) {
        show.setVisibility(View.VISIBLE);
        for (View v : hide) {
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void switchRows(View showRow, View hideRow) {
        if (showRow.getVisibility() == View.INVISIBLE) {
            showRow.setVisibility(View.VISIBLE);
        }
        if (hideRow.getVisibility() == View.VISIBLE) {
            hideRow.setVisibility(View.INVISIBLE);
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
