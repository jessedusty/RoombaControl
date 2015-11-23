package com.littlebencreations.roombcontrol;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


/**
 *
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //private Button startButton, sendButton, clearButton, stopButton;
    private Button connectButton, disconnectButton;
    private Button forwardButton, reverseButton, leftButton, rightButton, stopMoveButton;
    private Button enableTButton, disableTButton;
    private Button trigEnableButton, trigDisableButton;

    private TextView distanceText, popcornText;

    private PopServer mServer;
    private RoombaController roombaController;


    void setUiEnabled(boolean state) {
        //        stopButton.setEnabled(bool);
       /* if (serialPort == null) {
            //connectButton.setEnabled(false);
        } else {

        }*/
        connectButton.setEnabled(!state);
        if (connectButton != null) {
            disconnectButton.setEnabled(state);
            forwardButton.setEnabled(state);
            reverseButton.setEnabled(state);
            leftButton.setEnabled(state);
            rightButton.setEnabled(state);
            stopMoveButton.setEnabled(state);
            enableTButton.setEnabled(state);
            disableTButton.setEnabled(state);
            //trigEnableButton.setEnabled(state);
            //trigDisableButton.setEnabled(state);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button)findViewById(R.id.connectButton);
        disconnectButton = (Button)findViewById(R.id.disconnectButton);
        forwardButton = (Button)findViewById(R.id.forwardButton);
        reverseButton = (Button)findViewById(R.id.reverseButton);
        leftButton = (Button)findViewById(R.id.leftButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        stopMoveButton = (Button)findViewById(R.id.stopMoveButton);
        enableTButton = (Button)findViewById(R.id.startTrackingButton);
        disableTButton = (Button)findViewById(R.id.stopTrackingButton);
        /*
        trigEnableButton = (Button)findViewById(R.id.trigEnableButton);
        trigDisableButton = (Button)findViewById(R.id.trigDisableButton);
        */
        setUiEnabled(false);

        roombaController = new RoombaController(this);
        distanceText = (TextView) findViewById(R.id.distanceText);
        popcornText = (TextView) findViewById(R.id.popcornMessage);

        // Should just be calling and updating in the background
        ServerCaller serverCaller = new ServerCaller(this, roombaController, popcornText);
        serverCaller.execute();
    }

    private static class ServerCaller extends AsyncTask<Void, String, Void> {
        private static final String LOG_TAG = ServerCaller.class.getSimpleName();

        private final Context CONTEXT;
        private final RoombaController CONTROLLER;
        private final PopServer SERVER;
        private final TextView MESSAGE_VIEW;

        private final long UPDATE_INTERVOL = 500; // update every half second // MILLISECONDS
        private Timer callTimer;

        public ServerCaller(Context context, RoombaController controller, final TextView messageView) {
            CONTEXT = context;
            CONTROLLER = controller;
            MESSAGE_VIEW = messageView;

            callTimer = new Timer();

            SERVER = new PopServer(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // Get the command
                    // Set the message if there is one
                    String message = null, action = "";
                    try {
                        action = response.getString(PopServer.ACTION_KEY);
                        if (response.has(PopServer.MESSAGE_KEY)) {
                            message = response.getString(PopServer.MESSAGE_KEY);
                            // Could also post this as a progress update
                            MESSAGE_VIEW.setText(message);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Problem with pop server response", e);
                    }

                    switch (action) {
                        case RoombaController.ACTION_STAY:
                            CONTROLLER.stay();
                            break;
                        case RoombaController.ACTION_DELIVER:
                            CONTROLLER.deliver();
                            break;
                        case RoombaController.ACTION_COMEBACK:
                            CONTROLLER.comeback();
                            break;
                        default:
                            // Do nothing if we get a bad response
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Toast.makeText(CONTEXT, "Error with server: " + errorResponse.toString(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        

        @Override
        protected Void doInBackground(Void... params) {
            // Call the server every so often and update the action of the robot
            // Again, not necessarily the best way to do this
            // Will cause this thread to run infinitely
            callTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SERVER.getCommand();
                                   }
                               }, 0, UPDATE_INTERVOL);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public void directionButtonPressed(View view) {
        TextView origin = (TextView)view;
        switch ((String)origin.getText()) {
            case "Forward":
                roombaController.goForward();
                break;
            case "Backward":
                roombaController.goBackward();
                break;
            case "Left":
                roombaController.goLeft();
                break;
            case "Right":
                roombaController.goRight();
                break;
            case "Stop":
                roombaController.stopMoving();
                break;

        }

    }

}
