package io.magics.notethis.ui.fragments.bottomsheet;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.utils.DocUtils;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.ImgurViewModel;

import static android.app.Activity.RESULT_OK;


public class ImageSheet extends Fragment {

    private static final String TAG_PICK = "pick";
    private static final String TAG_UPLOAD = "upload";
    private static final String TAG_TEMPLATE = "template";

    private static final int FRAG_READ_WRITE_RQ = 1245;

    @BindViews(R.id.sheet_image_pick)
    TextView imgPick;
    @BindViews(R.id.sheet_image_upload)
    TextView imgUpload;
    @BindViews(R.id.sheet_image_template)
    TextView imgTemplate;

    private Uri fileUri;

    public ImageSheet() {
        // Required empty public constructor
    }

    public static ImageSheet newInstance() {
        return new ImageSheet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sheet_image, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgPick.setOnClickListener(v -> SubSheetPick.newInstance()
                .show(getFragmentManager(), TAG_PICK));

        imgTemplate.setOnClickListener(v -> SubSheetTemplate.newInstance()
                .show(getFragmentManager(), TAG_TEMPLATE));

        imgUpload.setOnClickListener(v -> {
            if (getContext() == null) return;
            if (Utils.isConnected(getContext())) {
                startActivityForResult(DocUtils.getChoseFileIntent(), DocUtils.RC_FRAG_PICK_IMG);
            }
        });
    }

    private void showSubSheetUpload() {
        if (!Utils.isConnected(getContext()) || fileUri == null) return;
        String filepath = DocUtils.getPath(getContext(), fileUri);
        if (filepath == null || filepath.isEmpty()) return;
        File img = new File(filepath);

        ImgurViewModel model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        model.prepareUpload(img);
        SubSheetUpload subSheetUpload = SubSheetUpload.newInstance(img);
        subSheetUpload.show(getFragmentManager(), TAG_UPLOAD);
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
                showSubSheetUpload();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == FRAG_READ_WRITE_RQ && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showSubSheetUpload();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void connectionState(Boolean connected) {
        imgUpload.setEnabled(connected);
    }
}
