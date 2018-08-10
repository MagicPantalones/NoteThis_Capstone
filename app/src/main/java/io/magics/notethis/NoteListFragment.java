package io.magics.notethis;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.utils.models.NoteTitle;


public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    @BindView(R.id.no_notes_layout)
    ConstraintLayout noNotesLayout;
    @BindView(R.id.note_list_layout)
    ConstraintLayout noteListLayout;
    @BindView(R.id.note_list_recycler)
    RecyclerView noteListRecycler;
    @BindView(R.id.new_note_fab)
    FloatingActionButton newNoteFab;
    @BindView(R.id.new_note_button)
    Button newNoteButton;

    Unbinder unbinder;

    private NoteListFragListener fragListener;

    public NoteListFragment() {
        // Required empty public constructor
    }


    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_note_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
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
    }

    void switchLayouts(int listVisibility) {
        noteListLayout.setVisibility(listVisibility);
        noNotesLayout.setVisibility(listVisibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }


    public interface NoteListFragListener {
        void onNewNotePress();
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

        }

        @Override
        public int getItemCount() {
            return noteTitles.size();
        }

        void insertAllNoteTitles(List<NoteTitle> noteTitles) {
            this.noteTitles = noteTitles;
            notifyDataSetChanged();
        }

        void deleteTitle(NoteTitle noteTitle) {
            if (noteTitles.isEmpty()) return;
            int pos = noteTitles.indexOf(noteTitle);
            noteTitles.remove(pos);
            notifyItemRemoved(pos);
            if (noteTitles.isEmpty()) switchLayouts(View.INVISIBLE);
        }
    }

    private class NoteTitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_title)
        TextView noteTitle;
        @BindView(R.id.note_subtitle)
        TextView noteSubtitle;
        @BindView(R.id.view_note_icon)
        ImageView viewNoteIcon;

        NoteTitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
