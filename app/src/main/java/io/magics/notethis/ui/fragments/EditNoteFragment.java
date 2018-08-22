package io.magics.notethis.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.dialogs.CloseDialog;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.ui.fragments.NoteListFragment.FabListener;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;

public class EditNoteFragment extends Fragment {

    public static final int ACTION_SAVE = 765;
    public static final int ACTION_CLOSE = 592;
    public static final int ACTION_BACK = 388;

    @BindView(R.id.edit_note_view)
    EditText editNoteView;

    Unbinder unbinder;
    private FabListener fabListener;

    private NoteViewModel viewModel;

    private PopupMenu popupHeaders;
    private PopupMenu popupImages;

    public interface EditNoteHandler {
        void onMenuListenerReady(BottomNavigationView.OnNavigationItemSelectedListener listener);

        void onMenuClick();
    }

    public EditNoteFragment() {
        // Required empty public constructor
    }

    public static EditNoteFragment newInstance() {
        return new EditNoteFragment();
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
        if (fabListener != null) {
            fabListener.hideFab();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FabListener) {
            fabListener = (FabListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fabListener = null;
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
        } else if (action == ACTION_CLOSE && hasUnsavedChanges()) {
            CloseDialog.newInstance().show(getFragmentManager(), Utils.DIALOG_CLOSE);
            editNoteView.clearFocus();
        } else if (action == ACTION_BACK) {
            editNoteView.clearFocus();
        }

    }

    public void prepareMenus(BottomNavigationView menu, EditNoteHandler handler) {
        handler.onMenuListenerReady(item -> {
            switch (item.getItemId()) {
                case R.id.action_md_list_num:
                    editNoteView.append(getString(R.string.template_ordered_list));
                    break;
                case R.id.action_md_list_bullet:
                    editNoteView.append(getString(R.string.template_unordered_list));
                    break;
                case R.id.action_md_headers:
                    prepareHeaderMenu(menu.findViewById(R.id.action_md_headers));
                    break;
                case R.id.action_md_link:
                    editNoteView.clearFocus();
                    prepareUrlInsertDialog();
                    break;
                case R.id.action_md_image:
                    prepareImagesMenu(menu.findViewById(R.id.action_md_image));
                    break;
                default:
                    break;
            }
            item.setChecked(false);

            handler.onMenuClick();

            return true;
        });
    }

    private void prepareHeaderMenu(View view) {
        PopupMenu menu = new PopupMenu(getContext(), view, Gravity.TOP);
        menu.inflate(R.menu.templates_sub_headers);
        menu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.sub_header_1:
                    editNoteView.append(getString(R.string.template_headers_1));
                    break;
                case R.id.sub_header_2:
                    editNoteView.append(getString(R.string.template_headers_2));
                    break;
                case R.id.sub_header_3:
                    editNoteView.append(getString(R.string.template_headers_3));
                    break;
                case R.id.sub_header_4:
                    editNoteView.append(getString(R.string.template_headers_4));
                    break;
                case R.id.sub_header_5:
                    editNoteView.append(getString(R.string.template_headers_5));
                    break;
                case R.id.sub_header_6:
                    editNoteView.append(getString(R.string.template_headers_6));
                    break;
            }

            return true;
        });

        menu.show();
    }

    private void prepareImagesMenu(View view) {
        PopupMenu menu = new PopupMenu(getContext(), view, Gravity.TOP);
        menu.inflate(R.menu.templates_sub_headers);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sub_images_imgur) {
                //TODO Launch ImgurList dialog
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View etRoot = View.inflate(getContext(), R.layout.dialog_double_et, null);
                builder.setView(etRoot);
                EditText urlTitleEt = etRoot.findViewById(R.id.dialog_url_text_et);
                EditText urlEt = etRoot.findViewById(R.id.dialog_url_et);
                urlEt.setHint("Set Alt-Text");
                builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (dialog != null) {
                        String urlTitle = urlTitleEt.getText().toString();
                        String url = urlEt.getText().toString();
                        String template = getString(R.string.template_image);

                        if (!TextUtils.isEmpty(urlTitle)) {
                            template = template.replace("alt-text", urlTitle);
                        }

                        if (!TextUtils.isEmpty(url)) {
                            template = template.replace("URL", url);
                        }
                        dialog.dismiss();
                        editNoteView.append(template);
                    }
                });
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                        editNoteView.setSelection(editNoteView.getText().length());
                        editNoteView.requestFocus();
                    }
                });
                urlTitleEt.requestFocus();

                Dialog dialog = builder.create();
                dialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
            return true;
        });
    }

    private void prepareUrlInsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View etRoot = View.inflate(getContext(), R.layout.dialog_double_et, null);
        builder.setView(etRoot);
        EditText urlTitleEt = etRoot.findViewById(R.id.dialog_url_text_et);
        EditText urlEt = etRoot.findViewById(R.id.dialog_url_et);
        urlEt.setHint("Set url text");
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            if (dialog != null) {
                String urlTitle = urlTitleEt.getText().toString();
                String url = urlEt.getText().toString();
                String template = getString(R.string.template_link);

                if (!TextUtils.isEmpty(urlTitle)) {
                    template = template.replace("alt-text", urlTitle);
                }

                if (!TextUtils.isEmpty(url)) {
                    template = template.replace("URL", url);
                }
                dialog.dismiss();
                editNoteView.append(template);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
                editNoteView.setSelection(editNoteView.getText().length());
                editNoteView.requestFocus();
            }
        });
        urlTitleEt.requestFocus();

        Dialog dialog = builder.create();
        dialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

    }

}
