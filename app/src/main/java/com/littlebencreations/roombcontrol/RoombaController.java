package com.littlebencreations.roombcontrol;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by austin on 11/22/15.
 *
 * Meant to be a simple controller for the Roomba
 */
public class RoombaController {
    private static final String LOG_TAG = RoombaController.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    private Context mContext;

    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;

    public static final String ACTION_DELIVER = "deliver";
    public static final String ACTION_STAY = "stay";
    public static final String ACTION_COMEBACK = "comeback";

    private String currentAction;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
//                        connectButton.setEnabled(true);
                        if (serialPort.open()) { //Set Serial Connection Parameters.
//                            setUiEnabled(true);
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
//                        connectButton.setEnabled(false);
                        System.out.println("SERIAL" + " PORT IS NULL");
                    }
                } else {
                    System.out.println("SERIAL" + " PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                System.out.println("connected");
                connect();

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                System.out.println("disconnected");
                connect();
            }
        }
    };

    private final UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] bytes) {
            String data = null;
            try {
                data = new String(bytes, "UTF-8");
                data.concat("/n");
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "Unsupported encoding received", e);
            }
            System.out.println(data);
        }
    };


    public RoombaController(Context context) {
        mContext = context;
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(broadcastReceiver, filter);

        currentAction = ACTION_STAY;
    }

    /* Serial Interfacing */
    private void sendString(String string) {
        if (serialPort == null) {
            Log.e(LOG_TAG, "Device not attached");
            return;
        }
        serialPort.write(string.getBytes());
    }

    public void goForward() {
        sendString("f");
    }

    public void goBackward() {
        sendString("b");
    }

    public void goLeft() {
        sendString("l");
    }

    public void goRight() {
        sendString("r");
    }

    public void stopMoving() {
        sendString("s");
    }

    public void sleepRoomba() {
        sendString("x");
    }

    public void stopConnection() {
        sendString("x");
        serialPort.close();
    }

    private void connect() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean foundArduino = false;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    foundArduino = true;
                } else {
                    connection = null;
                    device = null;
                }

                if (foundArduino)
                    break;
            }
        }
    }

    /* Popcorn control */
    private void setCurrentAction(String action) {
        this.currentAction = action;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    /**
     * Needs to interface with the arduino to launch the roomba's
     * clean movement. It will get to you. Eventually.
     */
    public void deliver() {
        if (currentAction.equals(ACTION_DELIVER)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_DELIVER);
    }

    /** Really don't need this, but would be nice if we shifted to a listen/respond model */
    public void stay() {
        if (currentAction.equals(ACTION_STAY)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_STAY);

    }

    /** */
    public void comeback() {
        if (currentAction.equals(ACTION_COMEBACK)) {
            // Don't do anything if we're already doing it
            return;
        }
        setCurrentAction(ACTION_COMEBACK);

    }
}
