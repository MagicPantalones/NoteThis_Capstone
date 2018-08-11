package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.SharedListeners;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.EditNoteViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditNoteFragListener} interface
 * to handle interaction events.
 */
public class EditNoteFragment extends Fragment {

    @BindView(R.id.edit_note_view)
    EditText editNoteView;

    Unbinder unbinder;
    private EditNoteFragListener fragListener;

    private EditNoteViewModel viewModel;

    public EditNoteFragment() {
        // Required empty public constructor
    }

    public static EditNoteFragment newInstance() {
        return new EditNoteFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_note, container, false);
        unbinder = ButterKnife.bind(this, root);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(getActivity()).get(EditNoteViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (viewModel.getNote() != null) {
            editNoteView.setText(viewModel.getNote().getBody());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditNoteFragListener) {
            fragListener = (EditNoteFragListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_note_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String text = editNoteView.getText().toString();
        switch (item.getItemId()) {
            case R.id.edit_menu_save:
                if (viewModel.hasUnsavedChanges(text)){
                    SaveDialog.newInstance().show(getFragmentManager(), Utils.DIALOG_SAVE);
                }
                break;
            case R.id.edit_menu_close:
                prepareExit();
                break;
            default:
                //MainActivity handles the other menu actions.
        }

        return super.onOptionsItemSelected(item);
    }

    public void prepareExit() {
        String text = editNoteView.getText().toString();
        fragListener.onClose(viewModel.hasUnsavedChanges(text));
    }

    public interface EditNoteFragListener {
        void onClose(boolean hasChanges);
    }
}
