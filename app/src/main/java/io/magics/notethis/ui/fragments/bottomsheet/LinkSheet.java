package io.magics.notethis.ui.fragments.bottomsheet;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.SheetUtils;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;

import static io.magics.notethis.ui.fragments.SheetUtils.getUrlTemplate;

/**
 * A simple {@link Fragment} subclass.
 */
public class LinkSheet extends Fragment {

    @BindView(R.id.sheet_url_title)
    EditText title;
    @BindView(R.id.sheet_url_url)
    EditText url;
    @BindView(R.id.sheet_url_ok_btn)
    Button okBtn;
    @BindView(R.id.sheet_url_cxl_btn)
    Button cxlBtn;

    SheetCallbacks callbacks;

    public LinkSheet() {
        // Required empty public constructor
    }

    public static LinkSheet newInstance() {
        return new LinkSheet();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sheet_link, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        okBtn.setOnClickListener(v ->
                callback(getUrlTemplate(getResources(), title.getText(), url.getText())));

        cxlBtn.setOnClickListener(v -> callback(null));

    }

    private void callback(String string) {
        if (callbacks != null) {
            callbacks.onReturnTemplate(string);
        }
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



}
