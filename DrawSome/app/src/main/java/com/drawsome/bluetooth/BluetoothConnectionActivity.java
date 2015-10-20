package com.drawsome.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.drawing.DrawingActivity;
import com.drawsome.drawing.ViewDrawingActivity;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionActivity extends Activity {

    private BluetoothAdapter BA;
    Button joinButton,initiateButton, sendButton;
   // ConnectedThread connectedThread;
    private EditText editText;
    private TextView receivedMessageText;
    private BroadcastReceiver mReceiver;
    private BluetoothDevice pairedDevice;
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    private static final int SERVER_CONNECTION = 2;
    private static final int CLIENT_CONNECTION = 3;
    private static final String UUID_STRING = "2511000-80cf0-11bd-b23e-10b96e4ef00d";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        editText = (EditText) findViewById(R.id.editText);
        receivedMessageText = (TextView)findViewById(R.id.receivedMessage);

        initiateButton = (Button) findViewById(R.id.initiateButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        joinButton = (Button) findViewById(R.id.joinButton);
        BA = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                System.out.println("received device info!!");
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    pairedDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    handleClientConnection(pairedDevice);
                }
            }
        };

        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
            IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // Register the BroadcastReceiver
        registerReceiver(mReceiver, intentFilter); // Don't forget to unregister during onDestroy

    }


    public void initiate(View v) {
        if(BA == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth not supported by device",Toast.LENGTH_LONG);
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(getVisible, SERVER_CONNECTION);
    }

    public void join(View v) {
        if(BA == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth not supported by device",Toast.LENGTH_LONG);
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(getVisible, CLIENT_CONNECTION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println("RECEIVED result code " + resultCode + " request code " + requestCode );
        if(resultCode == DISCOVER_DURATION && requestCode == SERVER_CONNECTION) {
            handleServerConnection();
        } else if(resultCode == DISCOVER_DURATION && requestCode == CLIENT_CONNECTION) {
            Boolean res = BA.startDiscovery();
            System.out.println("** Started discovery!!!! " + res);
        } else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(),"BLUETOOTH REQUEST CANCELLED",Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void handleServerConnection() {


        /*AcceptThread acceptThread = new AcceptThread(this,BA);
        acceptThread.start();*/

        BluetoothServerSocket mmServerSocket = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            mmServerSocket = BA.listenUsingRfcommWithServiceRecord("Drawsome", UUID.fromString(UUID_STRING) );
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
                Toast.makeText(this,"Server:error occurred",Toast.LENGTH_LONG).show();
                e.printStackTrace();
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                //manageConnectedSocket(socket);
                Toast.makeText(this,"Connection accepted",Toast.LENGTH_LONG).show();
                try {
                    mmServerSocket.close();

                } catch(IOException io) {
                    System.out.println("IOException " + io.getMessage());
                }
                SingletonBluetoothSocket.getBluetoothSocketInstance().setMmServerSocket(socket);
                break;

            }
        }
        Intent callDrawingActivity = new Intent(this,DrawingActivity.class);
        startActivity(callDrawingActivity);

    }

    public void clientConnect(View v) {

        Boolean res = BA.startDiscovery();
        System.out.println("** Started discovery!!!! " + res);

    }
    public void handleClientConnection(BluetoothDevice device) {

        BluetoothSocket mmSocket = null;
        Log.d("Log :","Calling handleConnection");
        //final BluetoothDevice device = BA.getRemoteDevice(BA.getAddress()); //Getting the Verifier Bluetooth
           /* ConnectThread connectThread = new ConnectThread(device,this,BA);
            connectThread.start();
           */ try {
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
            SingletonBluetoothSocket.getBluetoothSocketInstance().setMmClientSocket(mmSocket);
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            connectException.printStackTrace();

            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();}
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
        Toast.makeText(this,"Connection successful",Toast.LENGTH_LONG).show();

        Intent callViewActivityIntent = new Intent(this,ViewDrawingActivity.class);
        startActivity(callViewActivityIntent);



    }


    /*public void sendText(View v) {

        EditText text = (EditText)findViewById(R.id.editText);
        connectedThread.write(text.getText().toString());

    }
*/

    @Override
    public void onDestroy() {
        BA.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

}
