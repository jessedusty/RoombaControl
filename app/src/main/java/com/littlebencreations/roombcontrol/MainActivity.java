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
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    //private Button startButton, sendButton, clearButton, stopButton;
    private Button connectButton, disconnectButton, forwardButton, backwardButton;
    private TextView distanceText;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;

    private PopServer mServer;
    private RoombaController roombaController;



    TextView distanceText;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
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


    void setUiEnabled(boolean state) {
        //        stopButton.setEnabled(bool);
        if (serialPort == null) {
            //connectButton.setEnabled(false);
        } else {

        }
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
            trigEnableButton.setEnabled(state);
            trigDisableButton.setEnabled(state);
        }

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        connectButton.setEnabled(true);
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(19200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            //tvAppend(textView,"Serial Connection Opened!\n");

                            System.out.println("connection opened");
                            sendString("o");

                        } else {
                            System.out.println("SERIAL" + " PORT NOT OPEN");
                        }
                    } else {
                        connectButton.setEnabled(false);
                        System.out.println("SERIAL" + " PORT IS NULL");
                    }
                } else {
                    System.out.println("SERIAL" + " PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                System.out.println("connected");
                onClickStart(connectButton);

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                System.out.println("disconnected");
                onClickStop(disconnectButton);

            }
        }

        ;
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);


        connectButton = (Button)findViewById(R.id.connectButton);
        disconnectButton = (Button)findViewById(R.id.disconnectButton);
        forwardButton = (Button)findViewById(R.id.forwardButton);
        reverseButton = (Button)findViewById(R.id.reverseButton);
        leftButton = (Button)findViewById(R.id.leftButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        stopMoveButton = (Button)findViewById(R.id.stopMoveButton);
        enableTButton = (Button)findViewById(R.id.enableTButton);
        disableTButton = (Button)findViewById(R.id.disableTButton);
        trigEnableButton = (Button)findViewById(R.id.trigEnableButton);
        trigDisableButton = (Button)findViewById(R.id.trigDisableButton);
        setUiEnabled(false);

        roombaController = new RoombaController();
        distanceText = (TextView) findViewById(R.id.distanceText);
        popcornText = (TextView) findViewById(R.id.popcornMessage);

        // Should just be calling and updating in the background
        ServerCaller serverCaller = new ServerCaller(this, roombaController, popcornText);
        serverCaller.execute();
    }

    public void sendString(String string) {
        if (serialPort == null) {
            System.out.println("Device not attached");
            return;
        }
        serialPort.write(string.getBytes());
    }

    public void goForward(View view) {
        sendString("f");
    }

    public void goBackward(View view) {
        sendString("b");
    }

    public void goLeft(View view) {
        sendString("l");
    }

    public void goRight(View view) {
        sendString("r");
    }

    public void stopMoving(View view) {
        sendString("s");
    }
    
    public void sleepRoomba(View view) {
        sendString("x");
    }

    public void onClickStop(View view) {
        sendString("x");
        setUiEnabled(false);
        serialPort.close();
        //tvAppend(textView,"\nSerial Connection Closed! \n");
    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
        //sendString("o");
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
