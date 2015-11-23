package com.littlebencreations.roombcontrol;


import android.content.Context;
import android.net.Uri;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by austin on 11/22/15.
 * Built to interface with the simple server for popcorn control
 * Meant on the client side just to query for commands
 */
public class PopServer {
    private static final String LOG_TAG = PopServer.class.getSimpleName();
    private static final String SERVER_URL = "http://155.246.204.55:8000";
    private static final String COMMAND_EXT = "command";
    public static final String ACTION_KEY = "command";
    public static final String MESSAGE_KEY = "command";


    private JsonHttpResponseHandler mListener;

    public PopServer(JsonHttpResponseHandler listener) {
        mListener = listener;
    }

    public void getCommand() {
        Uri builtUri = Uri.parse(SERVER_URL).buildUpon()
                .appendPath(COMMAND_EXT).build();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(builtUri.toString(), mListener);
    }
}
