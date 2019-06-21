package com.mazitekgh.momorecords.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.Server;
import com.mazitekgh.momorecords.SharedPref;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements Server.ServerActionComplete {

    private static final String TAG = "LoginActivity";

    // UI references.
    private EditText usernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        usernameView = findViewById(R.id.index_no);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {

                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        hideKeyboard(getCurrentFocus());
        // Reset errors.
        usernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isIndexValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
            hideKeyboard(getCurrentFocus());
            new Server(this, this).attemptLogin(username, password);

        }
    }

    private boolean isIndexValid(String indexNo) {
        //TODO: Replace this with your own logic
        return indexNo.length() >= 2;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
    }


    private void attemptLogin(final String indexNo, final String password) {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr = new StringRequest(Request.Method.POST, Server.URL_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d(TAG, "onResponse: " + response);
                processResponse(response);
                showProgress(false);

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);
                Toast.makeText(LoginActivity.this, "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hs = new HashMap<>(1);
                hs.put("username", indexNo);
                hs.put("password", password);
                hs.put("rememberme", "1");
                return hs;
            }


            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> hs = response.headers;
                String cookie = hs.get("Set-Cookie");
                Log.d(TAG, "parseNetworkResponse: " + hs);
                Log.d(TAG, "parseNetworkResponse: cookies == " + cookie);
                if (cookie != null && !cookie.equals("")) {
                    new SharedPref(getApplicationContext()).saveCookie(cookie);
                }
                return super.parseNetworkResponse(response);

            }


        };


        queue.add(sr);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    private void processResponse(String response) {
        JsonObject jso = new Gson().fromJson(response, JsonObject.class);
        boolean isSuccess = jso.get("status").getAsBoolean();
        String loginMessage = jso.get("loginMessage").getAsString();
        if (isSuccess) {
            //call main activity
            Snackbar.make(this.mLoginFormView, "Successful", Snackbar.LENGTH_INDEFINITE).setAction("OK", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("fromServer", true);
                    //      intent.putExtra("studentInfo",studentInfoJson);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            })
                    .show();
        } else {
            Snackbar.make(getCurrentFocus(), "Error: " + loginMessage, Snackbar.LENGTH_SHORT);
        }


//            Intent intent =new Intent(LoginActivity.this,MainActivity.class);
//            intent.putExtra("data",dataJson );
//            intent.putExtra("studentInfo",studentInfoJson);
//            startActivity( intent);
    }


    @Override
    public void onActionComplete(Server.ServerAction serverAction, boolean isError, String response) {
        if (!isError) {
            Log.d(TAG, "onResponse: " + response);
            processResponse(response);
            showProgress(false);
        } else {
            Log.e(TAG, "onErrorResponse: " + response);
            Toast.makeText(LoginActivity.this, "Error occurred: " + response, Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }
} //end of login activity

