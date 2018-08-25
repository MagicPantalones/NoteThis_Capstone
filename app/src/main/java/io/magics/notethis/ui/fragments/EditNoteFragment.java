package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.NoteWidget;
import io.magics.notethis.ui.dialogs.CloseDialog;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;

import static io.magics.notethis.utils.FragmentHelper.getTransition;

public class EditNoteFragment extends Fragment {

    public static final int ACTION_SAVE = 765;
    public static final int ACTION_CLOSE = 592;
    public static final String TAG_BOT_SHEET = "bottom_sheet";

    @BindView(R.id.edit_note_view)
    EditText editNoteView;

    Unbinder unbinder;
    private SheetVisibility sheetVisibilityHandler;

    private NoteViewModel viewModel;

    TemplatesBottomSheet bottomSheet;

    public interface SheetVisibility {
        void showSheet();
    }

    public EditNoteFragment() {
        // Required empty public constructor
    }

    public static EditNoteFragment newInstance() {
        return new EditNoteFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(getTransition(Gravity.END));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_note, container, false);
        unbinder = ButterKnife.bind(this, root);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        viewModel.getNote().observe(this, note -> {
            if (getContext() != null) {
                Utils.setToolbarTitle(getContext(), note.getTitle(), R.color.primaryTextColor);
                editNoteView.setText(note.getBody());
                editNoteView.setSelection(editNoteView.getText().length());
            }
        });


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheet = (TemplatesBottomSheet)
                getFragmentManager().findFragmentById(R.id.bottom_sheet_fragment);
        if (sheetVisibilityHandler != null) sheetVisibilityHandler.showSheet();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SheetVisibility) sheetVisibilityHandler = (SheetVisibility) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sheetVisibilityHandler = null;
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
                prepareClose();
                break;
            default:
                //MainActivity handles the other menu actions.
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasUnsavedChanges() {
        return viewModel.hasUnsavedChanges(editNoteView.getText().toString());
    }

    public void setTemplate(String template) {
        if (bottomSheet != null) bottomSheet.setSheetCollapsed();
        if (template != null) {
            editNoteView.append(template);
        }
        editNoteView.setSelection(editNoteView.getText().length());
    }

    @SuppressWarnings("ConstantConditions")
    public void prepareSave(int action) {
        if (action == ACTION_SAVE && hasUnsavedChanges()) {
            String title = Utils.getToolbarTitle(getContext());
            if (title.equals(NoteViewModel.NEW_NOTE_TITLE)) {
                SaveDialog.newInstance(ACTION_SAVE).show(getFragmentManager(), Utils.DIALOG_SAVE);
                editNoteView.clearFocus();
            } else {
                viewModel.saveChanges(title, title);
            }
        } else if (action == ACTION_CLOSE) {
            CloseDialog.newInstance().show(getFragmentManager(), Utils.DIALOG_CLOSE);
            editNoteView.clearFocus();
        }

    }

    private void prepareClose() {
        if (hasUnsavedChanges()) {
            CloseDialog.newInstance().show(getFragmentManager(), Utils.DIALOG_CLOSE);
            editNoteView.clearFocus();
        } else {
            Utils.backPressed(getContext());
        }
    }

}
