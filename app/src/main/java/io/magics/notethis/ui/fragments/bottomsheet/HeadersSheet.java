package io.magics.notethis.ui.fragments.bottomsheet;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.SheetUtils;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;
import ru.noties.markwon.Markwon;

/**
 * A simple {@link Fragment} subclass.
 */
public class HeadersSheet extends Fragment {

    @BindViews({R.id.h1, R.id.h2, R.id.h3, R.id.h4, R.id.h5, R.id.h6})
    List<TextView> headers;

    private SheetCallbacks callbacks;

    public HeadersSheet() {
        // Required empty public constructor
    }

    public static HeadersSheet newInstance() {
        return new HeadersSheet();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sheet_headers, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (TextView h : headers) {
            h.setOnClickListener(v -> callback(SheetUtils.getHeader(h, getResources())));
            Markwon.setMarkdown(h, h.getText().toString());
        }
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
