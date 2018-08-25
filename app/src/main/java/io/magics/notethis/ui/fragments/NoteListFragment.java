package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
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
import io.magics.notethis.R;
import io.magics.notethis.ui.NoteWidget;
import io.magics.notethis.viewmodels.NoteTitleViewModel;
import io.magics.notethis.utils.models.NoteTitle;

import static io.magics.notethis.utils.FragmentHelper.getTransition;


public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    public static final int SCROLL_UP = 145;
    public static final int SCROLL_DOWN = 261;

    public static final int ACTION_EDIT = 657;
    public static final int ACTION_VIEW = 794;

    @BindView(R.id.no_notes_layout)
    ConstraintLayout noNotesLayout;
    @BindView(R.id.note_list_recycler)
    RecyclerView noteListRecycler;
    @BindView(R.id.new_note_button)
    Button newNoteButton;

    Unbinder unbinder;

    private NoteListFragListener listener;
    private FabListener fabListener;

    private NoteTitleViewModel noteViewModel;

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
        setEnterTransition(getTransition(Gravity.END));
        setExitTransition(getTransition(Gravity.START));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_note_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        adapter = new NoteTitleAdapter();
        //noinspection ConstantConditions
        noteViewModel = ViewModelProviders.of(getActivity()).get(NoteTitleViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteViewModel.getNoteTitles().observe(getActivity(), noteTitles -> {
                    adapter.insertAllNoteTitles(noteTitles);
                    if (noteTitles != null && !noteTitles.isEmpty() && getContext() != null){
                        NoteWidget.updateWidget(getContext(),
                                noteTitles.get(noteTitles.size() - 1));
                    }
                });

        noteListRecycler.setAdapter(adapter);

        noteListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) listener.onNoteListScroll(SCROLL_DOWN);
                if (dy <= 0) listener.onNoteListScroll(SCROLL_UP);
            }
        });

        ItemTouchHelper.SimpleCallback touchHelper = new NoteListItemTouchHelper(0,
                ItemTouchHelper.START, (holder, direction, pos) -> {
            final NoteTitle noteTitle = adapter.noteTitles.get(pos);
            final int adapterPos = pos;
            adapter.deleteTitle(noteTitle);
            //noinspection ConstantConditions
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.main_root),
                    noteTitle.getTitle() + " deleted!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> adapter.restoreTitle(noteTitle, adapterPos));
            snackbar.setActionTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.secondaryColor, null));
            snackbar.setDuration(7000);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    snackbar.removeCallback(this);
                    if (event != DISMISS_EVENT_ACTION) {
                        noteViewModel.deletePermanent();
                    }
                    super.onDismissed(transientBottomBar, event);
                }
            });
            snackbar.show();
        });

        new ItemTouchHelper(touchHelper).attachToRecyclerView(noteListRecycler);

        newNoteButton.setOnClickListener(v -> listener.onNewNotePress());
        switchLayouts(adapter.noteTitles);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NoteListFragListener) listener = (NoteListFragListener) context;
        if (context instanceof FabListener) fabListener = (FabListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        fabListener = null;
    }

    void switchLayouts(List<NoteTitle> noteTitles) {
        startPostponedEnterTransition();
        noteListRecycler.setVisibility(noteTitles.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        noNotesLayout.setVisibility(noteTitles.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        if (fabListener != null) {
            if (noteListRecycler.getVisibility() == View.VISIBLE) fabListener.showFab();
            else fabListener.hideFab();
        }
    }

    public interface NoteListFragListener {
        void onNewNotePress();

        void onNoteListScroll(int state);

        void onNoteItemClicked(int id, int type);
    }

    public interface FabListener {
        void hideFab();
        void showFab();
    }

    public interface NoteItemTouchListener {
        void onSwiped(NoteTitleViewHolder holder, int direction, int pos);
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
            holder.titleWrapper.setOnClickListener(v ->
                    listener.onNoteItemClicked(noteTitle.getId(), ACTION_EDIT));
            holder.viewNoteIcon.setOnClickListener(v ->
                    listener.onNoteItemClicked(noteTitle.getId(), ACTION_VIEW));
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
            notifyItemRangeChanged(pos, noteTitles.size());
            noteViewModel.deleteTitle(noteTitle);
            switchLayouts(noteTitles);
        }

        void restoreTitle(NoteTitle title, int pos) {
            noteViewModel.restoreTitle();
            noteTitles.add(pos, title);
            notifyItemInserted(pos);
            switchLayouts(noteTitles);
        }
    }

    class NoteTitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_title)
        TextView noteTitle;
        @BindView(R.id.note_subtitle)
        TextView noteSubtitle;
        @BindView(R.id.view_note_icon)
        ImageView viewNoteIcon;
        @BindView(R.id.title_wrapper)
        ConstraintLayout titleWrapper;
        @BindView(R.id.note_vh_foreground)
        ConstraintLayout foreground;
        @BindView(R.id.note_vh_background)
        ConstraintLayout background;

        NoteTitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //From https://www.androidhive.info/2017/09/android-recyclerview-swipe-delete-undo-using-itemtouchhelper/
    class NoteListItemTouchHelper extends ItemTouchHelper.SimpleCallback {
        private NoteItemTouchListener listener;

        NoteListItemTouchHelper(int dragDirs, int swipeDirs,
                                NoteItemTouchListener listener) {
            super(dragDirs, swipeDirs);
            this.listener = listener;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            listener.onSwiped(((NoteTitleViewHolder) viewHolder), direction,
                    viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                final View foreground = ((NoteTitleViewHolder) viewHolder).foreground;
                getDefaultUIUtil().onSelected(foreground);
            }
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
            final View foreground = ((NoteTitleViewHolder) viewHolder).foreground;

            getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final View foreground = ((NoteTitleViewHolder) viewHolder).foreground;
            getDefaultUIUtil().clearView(foreground);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            final View foreground = ((NoteTitleViewHolder) viewHolder).foreground;
            getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState,
                    isCurrentlyActive);
        }
    }
}
