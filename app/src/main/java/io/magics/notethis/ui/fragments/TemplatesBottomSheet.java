package io.magics.notethis.ui.fragments;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.DocUtils;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.viewmodels.ImgurViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;
import ru.noties.markwon.Markwon;

import static android.app.Activity.RESULT_OK;

public class TemplatesBottomSheet extends Fragment {

    private static final int FRAG_READ_WRITE_RQ = 1245;

    private static final int MODE_LIST = 11;
    private static final int MODE_HEADER = 22;
    private static final int MODE_LINK = 33;
    private static final int MODE_IMG = 44;

    private static final int SUB_MODE_PICK = 55;
    private static final int SUB_MODE_UPLOAD = 66;
    private static final int SUB_MODE_TEMPLATE = 77;

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

    private BottomSheetBehavior behavior;
    private ImgurViewModel model;
    private ImagePickDialogAdapter adapter;
    private TemplateSheetCallback callback;
    private Uri fileUri;
    private boolean connected;
    private int defaultPeekHeight;

    public interface TemplateSheetCallback {
        void onTemplateChosen(String template);
    }

    public interface SheetCallbacks {
        void onReturnTemplate(String template);
    }

    public TemplatesBottomSheet() {
        //Required public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.templates_bottom_sheet, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        behavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_sheet_fragment));
        setup();
    }


    private void setup() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        NoteViewModel connectedObs = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);
        adapter = new ImagePickDialogAdapter();
        model.getStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean initialized) {
                if (initialized != null && initialized) {
                    model.getStatus().removeObserver(this);
                    model.getImages().observe(TemplatesBottomSheet.this, adapter::insertImages);
                }
            }
        });
        defaultPeekHeight = getResources().getDimensionPixelSize(R.dimen.button_area_size);
        connectedObs.getConnectionStatus().observe(this, aBoolean -> {
            connected = aBoolean;
            imageSubSheets.get(1).setEnabled(connected);
        });

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

    private void setDefaultState() {
        switchRows(peekRow, buttonRow);
        behavior.setPeekHeight(defaultPeekHeight);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setImageSheetState() {
        switchRows(peekRow, buttonRow);
        showViewHideRest(sheets.get(3), sheets.get(0), sheets.get(1), sheets.get(2));
        showAll(imageSheetHeaders);
        hideAll(imageSubSheets);
        // getResources().getDimensionPixelSize(R.dimen.list_text_area_size) * 4
        int listHeight = sheets.get(3).getHeight();
        int peekHeight = listHeight + defaultPeekHeight;
        behavior.setPeekHeight(peekHeight);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setSheetMode(int mode) {
        if (mode == MODE_IMG) {
            setImageSheetState();
            return;
        } else if (mode == MODE_LIST) {
            showViewHideRest(sheets.get(0), sheets.get(1), sheets.get(2), sheets.get(3));
        } else if (mode == MODE_HEADER) {
            showViewHideRest(sheets.get(1), sheets.get(0), sheets.get(2), sheets.get(3));
        } else if (mode == MODE_LINK) {
            showViewHideRest(sheets.get(2), sheets.get(0), sheets.get(1), sheets.get(3));
            switchRows(buttonRow, peekRow);
        }
        if (behavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            setSheetExpanded();
        }
    }

    private void setSheetSubMode(int mode) {
        hideAll(imageSheetHeaders);
        if (mode == SUB_MODE_PICK) {
            showViewHideRest(imageSubSheets.get(0), imageSubSheets.get(1), imageSubSheets.get(2));
        } else if (mode == SUB_MODE_UPLOAD) {
            showViewHideRest(imageSubSheets.get(1), imageSubSheets.get(0), imageSubSheets.get(2));
        } else if (mode == SUB_MODE_TEMPLATE) {
            showViewHideRest(imageSubSheets.get(2), imageSubSheets.get(0), imageSubSheets.get(1));
        }
        switchRows(buttonRow, peekRow);

        setSheetExpanded();
    }

    private void setSheetExpanded() {
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        setSheetMode(MODE_LIST);
    }

    private void headerPeekClicked() {
        for (TextView h : headerPreviews) {
            h.setOnClickListener(v -> returnTemplate(SheetUtils.getHeader(h, getResources())));
            String text = h.getText().toString();
            Markwon.setMarkdown(h, text);
        }
        setSheetMode(MODE_HEADER);
    }

    private void linkPeekClicked() {
        View root = sheets.get(2);
        EditText title = root.findViewById(R.id.sheet_url_title);
        EditText url = root.findViewById(R.id.sheet_url);
        title.setText("");
        url.setText("");
        setPeekConfirmationButtons(
                getString(R.string.confirm),
                v -> returnTemplate(SheetUtils.getUrlTemplate(getResources(), title.getText(),
                        url.getText())),
                getString(R.string.cancel),
                v -> setDefaultState());
        setSheetMode(MODE_LINK);
    }

    private void imgPeekClicked() {
        imageSheetHeaders.get(0).setOnClickListener(v -> {
            View root = imageSubSheets.get(0);
            RecyclerView recyclerView = root.findViewById(R.id.image_picker_recycler);
            recyclerView.setAdapter(adapter);
            setPeekConfirmationButtons(getString(R.string.cancel), v1 -> setImageSheetState(),
                    null, null);
            setSheetSubMode(SUB_MODE_PICK);
        });
        imageSheetHeaders.get(1).setOnClickListener(v -> {
            if (!connected) return;
            startActivityForResult(DocUtils.getChoseFileIntent(), DocUtils.RC_FRAG_PICK_IMG);
        });
        imageSheetHeaders.get(2).setOnClickListener(v -> {
            View root = imageSubSheets.get(2);
            EditText imgTitle = root.findViewById(R.id.sub_template_alt);
            EditText imgUrl = root.findViewById(R.id.sub_template_url);
            imgTitle.setText("");
            imgUrl.setText("");

            setPeekConfirmationButtons(getString(R.string.confirm),
                    v1 -> returnTemplate(SheetUtils.getImgTemplate(getResources(),
                            imgTitle.getText(), imgUrl.getText())),
                    getString(R.string.cancel),
                    v1 -> setImageSheetState());

            setSheetSubMode(SUB_MODE_TEMPLATE);
        });
        setSheetMode(MODE_IMG);
    }

    private void showImageSubSheetUpload() {
        if (!connected || fileUri == null) return;
        String filepath = DocUtils.getPath(getContext(), fileUri);
        if (filepath == null || filepath.isEmpty()) return;
        File img = new File(filepath);
        model.prepareUpload(img);

        View root = imageSubSheets.get(1);
        ImageView imgPreview = root.findViewById(R.id.sheet_upload_preview);
        EditText imgTitle = root.findViewById(R.id.sub_upload_img_title);

        GlideApp.with(getContext())
                .load(img)
                .fallback(R.drawable.owl_24dp_color)
                .error(R.drawable.owl_24dp_color)
                .placeholder(R.drawable.owl_24dp_color)
                .into(imgPreview);

        setPeekConfirmationButtons(getString(R.string.dialog_upload), v -> {
            String title = TextUtils.isEmpty(imgTitle.getText()) ? "Uploaded Image" :
                    imgTitle.getText().toString();
            if (!connected){
                setImageSheetState();
                return;
            }
            model.upload(title);

            model.getUploadedImage().observe(this, image -> {

                model.getUploadedImage().removeObservers(this);
                if (image == null) {
                    setImageSheetState();
                    return;
                }

                returnTemplate(SheetUtils.getImgTemplate(getResources(), image.getTitle(),
                        image.getLink()));
            });
        }, getString(R.string.cancel), v -> setImageSheetState());
        setSheetSubMode(SUB_MODE_UPLOAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DocUtils.RC_FRAG_PICK_IMG && resultCode == RESULT_OK) {
            fileUri = data.getData();

            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, FRAG_READ_WRITE_RQ);
            } else {
                showImageSubSheetUpload();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FRAG_READ_WRITE_RQ && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImageSubSheetUpload();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void returnTemplate(String string) {
        if (callback != null) callback.onTemplateChosen(string);
        setDefaultState();
    }

    public void setCallback(TemplateSheetCallback cb) {
        callback = cb;
    }

    public void clearCallback() {
        callback = null;
    }

    private void setPeekConfirmationButtons(String positiveText, OnClickListener positiveListener,
                                            String negativeText, OnClickListener negativeListener) {
        positiveButton.setText(positiveText);
        positiveButton.setOnClickListener(positiveListener);
        if (TextUtils.isEmpty(negativeText)) {
            negativeButton.setVisibility(View.GONE);
        } else {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(negativeText);
            negativeButton.setOnClickListener(negativeListener);
        }
    }

    private void showViewHideRest(View show, View... hide) {
        show.setVisibility(View.VISIBLE);
        for (View v : hide) {
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void hideAll(List<View> hide) {
        for (View v : hide) {
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void showAll(List<View> show) {
        for (View v : show) {
            if (v.getVisibility() == View.GONE) {
                v.setVisibility(View.VISIBLE);
            }
        }
    }

    private void switchRows(View showRow, View hideRow) {
        if (showRow.getVisibility() == View.GONE) {
            showRow.setVisibility(View.VISIBLE);
        }
        if (hideRow.getVisibility() == View.VISIBLE) {
            hideRow.setVisibility(View.GONE);
        }
    }


}
