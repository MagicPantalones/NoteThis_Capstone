package io.magics.notethis.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.utils.MarkdownUtils;
import io.magics.notethis.utils.Utils;
import ru.noties.markwon.Markwon;


public class HelpFragment extends Fragment {

    @BindView(R.id.help_recycler)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public HelpFragment() {
        //Required public constructor
    }

    public static HelpFragment newInstance() {
        return new HelpFragment();
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
            List<String> md = MarkdownUtils.loadHelpFile(getContext());
            recyclerView.setAdapter(new HelpFragmentAdapter(md));
        }
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder);
        super.onDetach();
    }

    class HelpFragmentAdapter extends RecyclerView.Adapter<HelpViewHolder> {

        private List<String> paraGraphs;

        HelpFragmentAdapter(List<String> mdParagraphs) {
            this.paraGraphs = mdParagraphs;
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
            String text = paraGraphs.get(position);
            if (position > 0 && paraGraphs.get(position - 1).contains("## Code")) {
                holder.mdView.setText(Html.fromHtml(text));
            } else {
                Markwon.setMarkdown(holder.mdView, text);
            }
        }

        @Override
        public int getItemCount() {
            return paraGraphs.size();
        }
    }

    class HelpViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.md_help_tv)
        TextView mdView;

        public HelpViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
