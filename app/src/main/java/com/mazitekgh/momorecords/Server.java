package com.mazitekgh.momorecords;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * MtnMomo
 * Created by Zakaria on 6/20/2019 at 7:18 PM.
 */
public class Server {
    public static final String URL_ADDRESS = "http://192.168.43.180/momo/api/login-auth.php";
    private static final String TAG = "Server";
    private Context context;
    private ServerActionComplete mlistener;

    public Server(Context context, ServerActionComplete onServerActionComplete) {
        this.context = context;
        mlistener = onServerActionComplete;
    }

    public void attemptLogin(final String username, final String password) {

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, URL_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //  Log.d(TAG, "onResponse: " +response);
                //  processResponse(response);
                // showProgress(false);

                mlistener.onActionComplete(ServerAction.ACTION_LOGIN, false, response);

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);

                mlistener.onActionComplete(ServerAction.ACTION_LOGIN, true, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hs = new HashMap<>(1);
                hs.put("username", username);
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
                    new SharedPref(context).saveCookie(cookie);
                }
                return super.parseNetworkResponse(response);

            }


        };


        queue.add(sr);
    }

    public void checkLogin() {

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, URL_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                JsonObject jso = new Gson().fromJson(response, JsonObject.class);
                boolean isSuccess = jso.get("status").getAsBoolean();
                String loginMessage = jso.get("loginMessage").getAsString();
                mlistener.onActionComplete(ServerAction.ACTION_CHECK_LOGIN, !isSuccess, loginMessage);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mlistener.onActionComplete(ServerAction.ACTION_CHECK_LOGIN, true, error.getMessage());
            }

        }) {

            /**
             * Returns a list of extra HTTP headers to go along with this request. Can
             * throw {@link AuthFailureError} as authentication may be required to
             * provide these values.
             *
             * @throws AuthFailureError In the event of auth failure
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mp = new HashMap<>(1);
                String loginCookie = new SharedPref(context).getCookie();
                mp.put("Cookie", loginCookie);
                return mp;
            }
        };
        sr.setShouldCache(true);


        queue.add(sr);

    }


    public enum ServerAction {
        ACTION_LOGIN,
        ACTION_CHECK_LOGIN,
        ACTION_REGISTER
    }


    public interface ServerActionComplete {
        void onActionComplete(ServerAction serverAction, boolean isError, String response);
    }
}
