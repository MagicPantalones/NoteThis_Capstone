package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.SharedListeners;
import io.magics.notethis.viewmodels.NoteViewModel;
import io.magics.notethis.utils.models.NoteTitle;


public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    public static final int SCROLL_UP = 145;
    public static final int SCROLL_DOWN = 261;

    @BindView(R.id.no_notes_layout)
    ConstraintLayout noNotesLayout;
    @BindView(R.id.note_list_recycler)
    RecyclerView noteListRecycler;
    @BindView(R.id.new_note_button)
    Button newNoteButton;

    Unbinder unbinder;

    private NoteListFragListener fragListener;

    private Observer<List<NoteTitle>> titleObserver;
    private NoteViewModel noteViewModel;

    private NoteTitleAdapter adapter;

    public NoteListFragment() {
        // Required empty public constructor
    }


    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Slide(Gravity.START));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_note_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        adapter = new NoteTitleAdapter();
        noteViewModel = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteListRecycler.setAdapter(adapter);

        noteListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) fragListener.onNoteListScroll(SCROLL_DOWN);
                if (dy <= 0) fragListener.onNoteListScroll(SCROLL_UP);
            }
        });

        newNoteButton.setOnClickListener(this::onClick);

        titleObserver = noteTitles -> adapter.insertAllNoteTitles(noteTitles);

        noteViewModel.observeNoteTitles(getActivity(), titleObserver);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NoteListFragListener) {
            fragListener = (NoteListFragListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragListener = null;
        noteViewModel.unObserveNoteTitle(titleObserver);
    }

    void switchLayouts(List<NoteTitle> noteTitles) {
        noteListRecycler.setVisibility(noteTitles.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        noNotesLayout.setVisibility(noteTitles.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        fragListener.onNoteListChange(noteListRecycler.getVisibility() == View.VISIBLE);
    }

    private void onClick(View v) {
        fragListener.onNewNotePress();
    }


    public interface NoteListFragListener {
        void onNewNotePress();
        void onNoteListScroll(int state);
        void onNoteListChange(boolean showFab);
    }

    private class NoteTitleAdapter extends RecyclerView.Adapter<NoteTitleViewHolder> {

        List<NoteTitle> noteTitles;

        NoteTitleAdapter() {
            noteTitles = new ArrayList<>();
        }

        @NonNull
        @Override
        public NoteTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context).inflate(R.layout.note_title_view_holder,
                    parent, false);
            return new NoteTitleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteTitleViewHolder holder, int position) {
            NoteTitle noteTitle = noteTitles.get(position);
            holder.noteTitle.setText(noteTitle.getTitle());
            holder.noteSubtitle.setText(noteTitle.getPreview());
        }

        @Override
        public int getItemCount() {
            return noteTitles.size();
        }

        void insertAllNoteTitles(List<NoteTitle> noteTitles) {
            this.noteTitles = noteTitles;
            notifyDataSetChanged();
            switchLayouts(this.noteTitles);
        }

        void deleteTitle(NoteTitle noteTitle) {
            if (noteTitles.isEmpty()) return;
            int pos = noteTitles.indexOf(noteTitle);
            noteTitles.remove(pos);
            notifyItemRemoved(pos);
            switchLayouts(noteTitles);
        }
    }

    class NoteTitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_title)
        TextView noteTitle;
        @BindView(R.id.note_subtitle)
        TextView noteSubtitle;

        NoteTitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
