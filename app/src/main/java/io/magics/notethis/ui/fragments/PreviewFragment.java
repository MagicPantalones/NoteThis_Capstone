package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.NoteWidget;
import io.magics.notethis.ui.fragments.NoteListFragment.FabListener;
import io.magics.notethis.utils.MarkdownUtils;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;

import static io.magics.notethis.utils.FragmentHelper.getTransition;


public class PreviewFragment extends Fragment {

    @BindView(R.id.markdown_text)
    TextView markdownTextView;

    Unbinder unbinder;
    NoteViewModel model;

    FabListener fabListener;

    public PreviewFragment() {
        // Required empty public constructor
    }

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(getTransition(Gravity.END));
        setExitTransition(getTransition(Gravity.START));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_preview, container, false);
        unbinder = ButterKnife.bind(this, root);
        model = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (fabListener != null) {
            fabListener.hideFab();
        }

        model.getNote().observe(getActivity(), note -> {
            if (getContext() != null) {
                Utils.setToolbarTitle(getContext(), note.getTitle(), R.color.primaryTextColor);

                final SpannableConfiguration config = MarkdownUtils.getMarkdownConfig(getContext());
                CharSequence formattedText = Markwon.markdown(config, note.getBody());

                markdownTextView.setMovementMethod(BetterLinkMovementMethod.newInstance());

                Markwon.unscheduleDrawables(markdownTextView);
                Markwon.unscheduleTableRows(markdownTextView);

                markdownTextView.setText(formattedText);

                Markwon.scheduleDrawables(markdownTextView);
                Markwon.scheduleTableRows(markdownTextView);

            }
        });
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder);
        fabListener = null;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FabListener) fabListener = (FabListener) context;
    }
}
