package io.magics.notethis.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.notethis.R;
import io.magics.notethis.data.network.FirebaseUtils;
import io.magics.notethis.utils.Utils;
import io.magics.notethis.viewmodels.NoteViewModel;

public class SignInFragment extends Fragment {

    private static final String TAG = "SignInFragment";

    private static final int RC_SIGN_IN = 557;

    @BindView(R.id.google_sign_in_btn)
    Button signInBtn;

    private Unbinder unbinder;
    private NoteViewModel model;


    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_in, container, false);
        unbinder = ButterKnife.bind(this, root);
        model = ViewModelProviders.of(getActivity()).get(NoteViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signInBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent signInIntent = FirebaseUtils.getGsiClient(getContext()).getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                model.onGoogleSignIn(task.getResult(ApiException.class));
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Sign in failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDetach() {
        Utils.dispose(unbinder);
        super.onDetach();
    }
}
