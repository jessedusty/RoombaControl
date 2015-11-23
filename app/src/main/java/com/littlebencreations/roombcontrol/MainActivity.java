package com.littlebencreations.roombcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

import cz.msebera.android.httpclient.Header;


/**
 *
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    //private Button startButton, sendButton, clearButton, stopButton;
    private Button connectButton, disconnectButton, forwardButton, backwardButton;
    private TextView distanceText, popcornText;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;

    private PopServer mServer;
    private RoombaController roombaController;

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] bytes) {
            String data = null;
            try {
                data = new String(bytes, "UTF-8");
                data.concat("/n");
                //tvAppend(textView, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(data);
        }
    };

    /* This should be an intent filter in the manifest methinks? */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(19200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            //tvAppend(textView,"Serial Connection Opened!\n");

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                //onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //onClickStop(stopButton);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roombaController = new RoombaController();
        distanceText = (TextView) findViewById(R.id.distanceText);
        popcornText = (TextView) findViewById(R.id.popcornMessage);

        // Should just be calling and updating in the background
        ServerCaller serverCaller = new ServerCaller(this, roombaController, popcornText);
        serverCaller.execute();
    }

    /**
     * What does this do?
     * @param state
     */
    public void setUiEnabled(boolean state) {

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
                            messageView.setText(message);
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

}
