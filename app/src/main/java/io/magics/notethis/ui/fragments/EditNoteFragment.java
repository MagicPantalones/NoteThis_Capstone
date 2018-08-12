package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import io.magics.notethis.ui.MainActivity;
import io.magics.notethis.ui.SharedListeners;
import io.magics.notethis.ui.dialogs.CloseDialog;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.viewmodels.EditNoteViewModel;
import io.magics.notethis.viewmodels.NoteViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditNoteFragListener} interface
 * to handle interaction events.
 */
public class EditNoteFragment extends Fragment {

    public static final int ACTION_SAVE = 765;
    public static final int ACTION_CLOSE = 592;

    @BindView(R.id.edit_note_view)
    EditText editNoteView;

    Unbinder unbinder;
    private EditNoteFragListener fragListener;
    Observer<Note> observer;

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

        if (!Utils.getToolbarTitle(getContext()).equals(EditNoteViewModel.NEW_NOTE_TITLE)) {
            observer = note -> editNoteView.setText(note.getBody());
            viewModel.observeReadNote(getActivity(), observer);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragListener.hideFab();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        if (observer != null) viewModel.removeReadObserver(observer);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_note_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_menu_save:
                prepareSave(ACTION_SAVE);
                break;
            case R.id.edit_menu_close:
                prepareSave(ACTION_CLOSE);
                break;
            default:
                //MainActivity handles the other menu actions.
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasUnsavedChanges() {
        return viewModel.hasUnsavedChanges(editNoteView.getText().toString());
    }

    public void prepareSave(int action) {
        String text = editNoteView.getText().toString();
        if (action == ACTION_SAVE && viewModel.hasUnsavedChanges(text)) {
            String title = Utils.getToolbarTitle(getContext());
            if (title.equals(EditNoteViewModel.NEW_NOTE_TITLE)) {
                SaveDialog.newInstance(ACTION_SAVE).show(getFragmentManager(), Utils.DIALOG_SAVE);
            } else {
                viewModel.saveChanges(title);
            }
        } else if (action == ACTION_CLOSE) {
            if (viewModel.hasUnsavedChanges(text)) {
                CloseDialog.newInstance().show(getFragmentManager(), Utils.DIALOG_CLOSE);
            } else {
                Utils.backPressed(getContext());
            }
        }

    }

    public interface EditNoteFragListener {
        void hideFab();
    }
}
