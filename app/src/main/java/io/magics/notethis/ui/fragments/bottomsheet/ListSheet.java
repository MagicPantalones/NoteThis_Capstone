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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.notethis.R;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet;
import io.magics.notethis.ui.fragments.TemplatesBottomSheet.SheetCallbacks;
import ru.noties.markwon.Markwon;


public class ListSheet extends Fragment {

    @BindView(R.id.sheet_list_ordered)
    TextView orderedListView;
    @BindView(R.id.sheet_list_unordered)
    TextView unorderedListView;

    SheetCallbacks callbacks;

    public ListSheet() {
        // Required empty public constructor
    }

    public static ListSheet newInstance() {
        return new ListSheet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sheet_list, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Markwon.setMarkdown(orderedListView, orderedListView.getText().toString());
        Markwon.setMarkdown(unorderedListView, unorderedListView.getText().toString());

        orderedListView.setOnClickListener(v ->
                callback(getString(R.string.template_ordered_list)));

        unorderedListView.setOnClickListener(v ->
                callback(getString(R.string.template_unordered_list)));
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
