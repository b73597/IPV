package com.farr.android.farrapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

public class SettingFragment extends Fragment {
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private ScrollView mUpdateForm;
    View mRootView;

    public SettingFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if(mRootView==null) {
            mRootView = inflater.inflate(R.layout.fragment_settings, container, false);

            mEmailView = (EditText) mRootView.findViewById(R.id.email);
            mPasswordView = (EditText) mRootView.findViewById(R.id.password);
            mProgressView = mRootView.findViewById(R.id.settings_progress);
            mUpdateForm = (ScrollView) mRootView.findViewById(R.id.update_form);

            mRootView.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attempUpdate();
                }
            });
        }

        mEmailView.setText(ParseUser.getCurrentUser().getEmail());

        return mRootView;
    }


    void attempUpdate(){
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String newEmail = mEmailView.getText().toString();
        String newPassword = mPasswordView.getText().toString();

        boolean noChange = true;
        if(!TextUtils.isEmpty(newEmail)){
            if(!LoginActivity.isEmailValid(newEmail)){
                mEmailView.setError(getString(R.string.error_invalid_email));
                mEmailView.requestFocus();
                return;
            } else noChange = false;
        }

        if(!TextUtils.isEmpty(newPassword)){
            if(!LoginActivity.isPasswordValid(newPassword)){
                mPasswordView.setError(getString(R.string.error_invalid_password));
                mPasswordView.requestFocus();
                return;
            } else noChange = false;
        }

        if(noChange) return;
        ParseUser user = ParseUser.getCurrentUser();
        if(!TextUtils.isEmpty(newEmail)){
            user.setEmail(newEmail);
            user.setUsername(newEmail);
        }
        if(!TextUtils.isEmpty(newPassword)){
            user.setPassword(newPassword);
        }
        showProgress(true);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                showProgress(false);
                if (e != null) {
                    Toast.makeText(getActivity(), "Invalid information provided!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mUpdateForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mUpdateForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUpdateForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUpdateForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
