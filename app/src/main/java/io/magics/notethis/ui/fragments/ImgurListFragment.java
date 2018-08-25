package io.magics.notethis.ui.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.ui.MainActivity;
import io.magics.notethis.ui.dialogs.ImageSheetDialog;
import io.magics.notethis.ui.fragments.NoteListFragment.FabListener;
import io.magics.notethis.utils.GlideApp;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.viewmodels.ImgurViewModel;

import static io.magics.notethis.utils.FragmentHelper.getTransition;

public class ImgurListFragment extends Fragment {

    private static final String TAG_IMG_DIALOG = "image_dialog";

    @BindView(R.id.imgur_recycler)
    RecyclerView imgurRecycler;
    @BindView(R.id.imgur_list_progress)
    ProgressBar imgurProgress;
    ImgurViewModel model;

    Unbinder unbinder;

    FabListener mainFabListener;

    public ImgurListFragment() {
        // Required empty public constructor
    }

    public static ImgurListFragment newInstance() {
        return new ImgurListFragment();
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
        View root = inflater.inflate(R.layout.fragment_imgur_list, container, false);
        unbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(ImgurViewModel.class);
        ImgurAdapter adapter = new ImgurAdapter();

        model.getImages().observe(getActivity(), adapter::insertImages);


        imgurRecycler.addItemDecoration(new ItemSpacingDecoration(
                getResources().getDimensionPixelSize(R.dimen.margin_4dp)));
        imgurRecycler.setAdapter(adapter);


        if (mainFabListener != null) mainFabListener.changeFab();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FabListener) mainFabListener = (FabListener) context;
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder);
        mainFabListener = null;
        super.onDetach();
    }

    private void switchViews(List<Image> images) {
        if (imgurRecycler == null || imgurProgress == null) return;
        if (images != null && !images.isEmpty()) {
            imgurRecycler.setVisibility(View.VISIBLE);
            imgurProgress.setVisibility(View.GONE);
        }
    }

    private class ImgurAdapter extends RecyclerView.Adapter<ImgurViewHolder> {

        List<Image> images = new ArrayList<>();

        ImgurAdapter() { }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }

        @NonNull
        @Override
        public ImgurViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            Context context = parent.getContext();
            if (viewType == 0) {
                v = LayoutInflater.from(context)
                        .inflate(R.layout.imgur_list_vh_bar_bottom, parent, false);
            } else {
                v = LayoutInflater.from(context)
                        .inflate(R.layout.imgur_list_vh_bar_top, parent, false);
            }
            return new ImgurViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ImgurViewHolder holder, int position) {
            Image image = images.get(position);
            GlideApp.with(ImgurListFragment.this)
                    .load(image.getLink())
                    .into(holder.imgurView);
            holder.titleView.setText(image.getTitle());

            holder.menuButton.setOnClickListener(v ->
                    handlePopupClick(holder.menuButton, image, position));
            holder.imgurView.setOnClickListener(v -> handleImageClick(image.getLink()));

        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        private void handlePopupClick(View menuButton, Image image, int position) {
            int gravity = position % 2 == 0 ? Gravity.START : Gravity.END;
            PopupMenu popupMenu = new PopupMenu(getContext(), menuButton, gravity);
            popupMenu.inflate(R.menu.imgur_popup_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_menu_copy) {
                    ClipboardManager clipMan = (ClipboardManager) getActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = ClipData.newPlainText("Imgur url", image.getLink());
                    clipMan.setPrimaryClip(data);
                } else {
                    handleDeleteClick(position);
                    model.deleteImage(image);
                    Snackbar undoSnack = Snackbar.make(
                            ((MainActivity) getContext()).findViewById(R.id.main_root),
                            "Image deleted!", Snackbar.LENGTH_LONG);
                    undoSnack.setAction("UNDO", v -> restore(image, position));
                    undoSnack.setActionTextColor(ResourcesCompat.getColor(getResources(),
                            R.color.secondaryColor, null));
                    undoSnack.show();
                }
                return true;
            });
            popupMenu.show();
        }

        private void insertImages(List<Image> images) {
            this.images = images;
            notifyDataSetChanged();
            switchViews(this.images);
        }

        private void handleDeleteClick(int pos) {
            images.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, images.size());
            switchViews(images);
        }

        private void restore(Image image, int position) {
            model.restoreImage(image);
            images.add(position, image);
            notifyItemInserted(position);
            switchViews(images);
        }

        private void handleImageClick(String url) {
            ImageSheetDialog dialog = ImageSheetDialog.newInstance(url);
            dialog.show(getFragmentManager(), TAG_IMG_DIALOG);
        }
    }

    class ImgurViewHolder extends RecyclerView.ViewHolder {

        ImageView imgurView;
        TextView titleView;
        ImageView menuButton;

        ImgurViewHolder(View itemView) {
            super(itemView);
            imgurView = Utils.bindEither(itemView, R.id.vh_imgur_img_bot, R.id.vh_imgur_img_top);
            titleView = Utils.bindEither(itemView, R.id.vh_imgur_title_bot, R.id.vh_imgur_title_top);
            menuButton = Utils.bindEither(itemView, R.id.vh_imgur_menu_bot, R.id.vh_imgur_menu_top);
        }
    }

    //Item spacing decorator based on ianhanniballake's answer here:
    //https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
    private class ItemSpacingDecoration extends RecyclerView.ItemDecoration {

        private int spacing;

        ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            int column = pos % 2;

            if (column == 0) {
                outRect.right = spacing / 2;
            } else {
                outRect.left = spacing / 2;
            }
            outRect.bottom = spacing;
        }
    }
}
