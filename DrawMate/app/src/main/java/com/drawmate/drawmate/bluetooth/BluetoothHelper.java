package com.drawmate.drawmate.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.drawmate.drawmate.NFC.NfcHelper;
import com.drawmate.drawmate.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHelper{

    private BluetoothAdapter BA;
    Button joinButton, initiateButton, sendButton;
    ConnectedThread connectedThread;
    private EditText editText;
    private TextView receivedMessageText;
    private BroadcastReceiver mReceiver;
    private BluetoothDevice pairedDevice;
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    private static final int SERVER_CONNECTION = 2;
    private static final int CLIENT_CONNECTION = 3;
    private static final String UUID_STRING = "2511000-80cf0-11bd-b23e-10b96e4ef00d";
    NfcHelper nfcHelper;
    int counter = 0;
    BluetoothConnectionActivity callingActivity;
    Context context;

    public BluetoothHelper(Context context) {
        this.context = context;
        callingActivity = (BluetoothConnectionActivity) context;

    }

    protected BluetoothHelper(Parcel in) {
        pairedDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        counter = in.readInt();
    }

    public NfcHelper initiate() {
        BA = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    pairedDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    handleClientConnection(pairedDevice);
                }
            }
        };

        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            callingActivity.startActivityForResult(turnOn, 0);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // Register the BroadcastReceiver
        context.registerReceiver(mReceiver, intentFilter); // Don't forget to unregister during onDestroy

        nfcHelper = new NfcHelper(context, this);
        boolean status = nfcHelper.initiate();
        if (!status) {
            //Toast.makeText(this,"Sorry no nfc on your device.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return nfcHelper;
    }
    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("ReceivedMessage");
            callingActivity.newMsg(message);
            //receivedMessageText.setText(message);

        }
    }

    //This handler is associated with main thread.
    final private Handler handler = new UIHandler();

    //Creates new thread.
    private final class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    if (bytes > 0) {

                        String message = new String(buffer);
                        Log.d("Receieved message ", message);
                        Message msg = handler.obtainMessage();

                        /**
                         * Class Message can store two integer values that can be
                         * passed as parameters. If the data that needs to be passed
                         * is more complex, use Message.setData()
                         */
                        Bundle b = new Bundle();
                        b.putString("ReceivedMessage", message);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            try {
                mmOutStream.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void initiateServer() {
        if (BA == null) {
            //Toast.makeText(getApplicationContext(), "Bluetooth not supported by device", Toast.LENGTH_LONG).show();
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        callingActivity.startActivityForResult(getVisible, SERVER_CONNECTION);
    }

    public void initiateOnPushComplete() {
        if (BA == null) {
            Toast.makeText(context, "Bluetooth not supported by device", Toast.LENGTH_LONG).show();
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        callingActivity.startActivityForResult(getVisible, SERVER_CONNECTION);
    }

    public void joinServer() {
        if (BA == null) {
            Toast.makeText(context, "Bluetooth not supported by device", Toast.LENGTH_LONG).show();
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        callingActivity.startActivityForResult(getVisible, CLIENT_CONNECTION);
    }

    public void handleServerConnection() {
        /*AcceptThread acceptThread = new AcceptThread(this,BA);
        acceptThread.start();*/
        BluetoothServerSocket mmServerSocket = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            mmServerSocket = BA.listenUsingRfcommWithServiceRecord("Drawsome", UUID.fromString(UUID_STRING));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                System.out.println("*****************server: accept;");

            } catch (IOException e) {
                //Toast.makeText(this, "Server:error occurred", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                //manageConnectedSocket(socket);
                ///Toast.makeText(this, "Connection accepted", Toast.LENGTH_LONG).show();
                try {
                    mmServerSocket.close();
                } catch (IOException io) {
                    System.out.println("IOException " + io.getMessage());
                }
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
                break;

            }
        }
    }

    public void startBluetoothDiscovery() {
        BA.startDiscovery();
    }

    public void clientConnect(View v) {

        Boolean res = BA.startDiscovery();
        System.out.println("** Started discovery!!!! " + res);

    }

    public void handleClientConnection(BluetoothDevice device) {
        BluetoothSocket mmSocket = null;
        Log.d("Log :", "Calling handleConnection");
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BA.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            connectException.printStackTrace();

            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
        //Toast.makeText(this, "Connection successful", Toast.LENGTH_LONG).show();

        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    public void sendText(String text) {
        connectedThread.write(text);
    }

    public void unregisterBluetoothReceiver() {
        BA.cancelDiscovery();
        context.unregisterReceiver(mReceiver);
    }
}
