package io.magics.notethis.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.utils.MarkdownUtils;
import io.magics.notethis.utils.Utils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;

import static io.magics.notethis.utils.FragmentHelper.getTransition;


public class HelpFragment extends Fragment {

    private static final String TAG = "HelpFragment";

    @BindView(R.id.help_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.help_parse_progress)
    ProgressBar helpParseProgress;

    private Unbinder unbinder;
    private Disposable disposable;

    public HelpFragment() {
        //Required public constructor
    }

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(getTransition(Gravity.END));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);
        unbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getContext() != null) {
            disposable = Single.just(MarkdownUtils.loadHelpFile(getContext()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleParsedList, e -> Log.w(TAG, "Parse Error", e));
        }
    }

    private void handleParsedList(List<String> md) {
        recyclerView.setAdapter(new HelpFragmentAdapter(md));
        recyclerView.clearAnimation();
        recyclerView.setVisibility(View.VISIBLE);
        helpParseProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder, disposable);
        super.onDetach();
    }

    class HelpFragmentAdapter extends RecyclerView.Adapter<HelpViewHolder> {

        private List<String> paraGraphs;
        private final SpannableConfiguration configuration;

        HelpFragmentAdapter(List<String> mdParagraphs) {
            this.paraGraphs = mdParagraphs;
            configuration = MarkdownUtils.getMarkdownConfig(getContext());
        }

        @NonNull
        @Override
        public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.help_view_holder, parent, false);
            return new HelpViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HelpViewHolder holder, int position) {
            CharSequence renderedText = Markwon.markdown(configuration, paraGraphs.get(position));
            holder.mdView.setMovementMethod(BetterLinkMovementMethod.newInstance());
            Markwon.unscheduleDrawables(holder.mdView);
            Markwon.unscheduleTableRows(holder.mdView);
            holder.mdView.setText(renderedText);
            Markwon.scheduleDrawables(holder.mdView);
            Markwon.scheduleTableRows(holder.mdView);

        }

        @Override
        public int getItemCount() {
            return paraGraphs.size();
        }
    }

    class HelpViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.md_help_tv)
        TextView mdView;

        HelpViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
