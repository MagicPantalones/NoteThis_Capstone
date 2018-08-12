package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.viewmodels.EditNoteViewModel;
import ru.noties.markwon.Markwon;


public class PreviewFragment extends Fragment {

    @BindView(R.id.markdown_text)
    TextView markdownTextView;

    Unbinder unbinder;
    EditNoteViewModel model;
    Observer<Note> observer;

    public PreviewFragment() {
        // Required empty public constructor
    }
    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_preview, container, false);
        unbinder = ButterKnife.bind(this, root);
        model = ViewModelProviders.of(getActivity()).get(EditNoteViewModel.class);

        observer = note -> Markwon.setMarkdown(markdownTextView, note.getBody());
        model.observeReadNote(getActivity(), observer);

        return root;
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder);
        model.removeReadObserver(observer);
        super.onDetach();
    }
}
