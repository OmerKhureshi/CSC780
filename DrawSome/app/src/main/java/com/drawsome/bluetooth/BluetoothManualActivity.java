package com.drawsome.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.UiFlow.Difficulty.DifficultyActivity;
import com.drawsome.UiFlow.Difficulty.DifficultySecondUserActivity;
import com.drawsome.drawing.ViewDrawingActivity;

import java.io.IOException;
import java.util.UUID;

/*
 * The class handles bluetooth connection through server and client. The users are required
 * start the bluetooth in server or client mode. It has methods to turn bluetooth ON,
 * make device visible to other devices, initialize connection and join the incoming connection.
 * The connection is initialized by one device. When the other device accepts the connection, bluetooth
 * channel is established between  them for exchange of data.
 * Authors: Pooja Kanchan and Syed Omer Salar Khureshi
 */
public class BluetoothManualActivity extends Activity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private BluetoothAdapter BA;
    Button joinButton,initiateButton, sendButton;
    // ConnectedThread connectedThread;
    private BroadcastReceiver mReceiver;
    private BluetoothDevice pairedDevice;
    private static final int DISCOVER_DURATION = 300;
    private static final int SERVER_CONNECTION = 2;
    private static final int CLIENT_CONNECTION = 3;
    private static final String UUID_STRING = "2511000-80cf0-11bd-b23e-10b96e4ef00d";
    NfcAdapter nfcAdapter;
    Boolean firstTime = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        initiateButton = (Button) findViewById(R.id.initiateButton);
        joinButton = (Button) findViewById(R.id.joinButton);
        BA = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                System.out.println("received device info!!");
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    pairedDevice =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Log.v(this.getClass().toString(), "NFC is not available.");
        } else {
            nfcAdapter.setNdefPushMessageCallback(this,this);
        }
    }
/*
 * The method to handle initialization. It checks if the bluetooth is supported by the device first.
 * If yes, then makes the device visible to other devices and puts it in listening mode to listen to
 * incoming request. This is a blocking mode.
  */

    public void initiate(View v) {
        TextView showProgress = (TextView) findViewById(R.id.textView_progress);
        showProgress.setText(R.string.connection_progress);
        initiateButton.setEnabled(false);
        joinButton.setEnabled(false);
        if(BA == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth not supported by device",Toast.LENGTH_LONG);
        }
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(getVisible, SERVER_CONNECTION);
    }

    /*
    * The method makes the device visible to other devices first.
     * It then starts discovering server sockets in listening mode. If it finds one,
     *  then it sends connection request.
     * to server socket
     */
    public void join(View v) {
        TextView showProgress = (TextView) findViewById(R.id.textView_progress);
        showProgress.setText(R.string.connection_progress);
        initiateButton.setEnabled(false);
        joinButton.setEnabled(false);
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
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Bluetooth Request");
            newDialog.setMessage("The application can not run without bluetooth. Are you sure do you want to quit?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    finish();
                }
            });
            newDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    if (!BA.isEnabled()) {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, 0);
                    }
                }
            });
            newDialog.show();
        }

    }
    /*
    * The method is called when connection initialization request is made by the device.
    * Puts the device in listening mode. For incoming connection requests, it checks for UUID string.
    * If the string matches, it accepts the connection. The socket which can be used for communication is
    * stored in SingletonBluetoothSocket singleton class for reference.
    */
    private void handleServerConnection() {


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
                SingletonBluetoothSocket.getBluetoothSocketInstance().setMmSocket(socket);
              /*  ConnectedThread connectedThread = new ConnectedThread();
                ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);
                connectedThread.start();*/

                break;

            }
        }

     /*   Intent callDrawingActivity = new Intent(this,DrawingActivity.class);
        startActivity(callDrawingActivity);*/
        Intent callDifficultyActivity = new Intent(this,DifficultyActivity.class);
        startActivity(callDifficultyActivity);

    }

    /*
    * The method which handles client connection discovery process.
     */
    private void clientConnect(View v) {

        Boolean res = BA.startDiscovery();
        System.out.println("** Started discovery!!!! " + res);

    }

    /*
     * The method handles client connection. It sends connection request to server with UUID.
     * It blocks till connection is successful or exception is thrown. On successful connection,
     * it stores socket in singletonBluetoothSocket for reference.
     */
    private void handleClientConnection(BluetoothDevice device) {

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
            SingletonBluetoothSocket.getBluetoothSocketInstance().setMmSocket(mmSocket);
          /*  ConnectedThread connectedThread = new ConnectedThread();
            ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);
            connectedThread.start();*/
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

        /*Intent callViewActivityIntent = new Intent(this,ViewDrawingActivity.class);
        startActivity(callViewActivityIntent);*/

        Intent callDifficultySecondIntent = new Intent(this,DifficultySecondUserActivity.class);
        startActivity(callDifficultySecondIntent);
    }


/*    public void sendText(View v) {

        EditText text = (EditText)findViewById(R.id.editText);
        connectedThread.write(text.getText().toString());

    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        BA.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

    public void callDrawingActivity(View view) {
        Intent callDifficultyActivity = new Intent(this,ViewDrawingActivity.class);
        startActivity(callDifficultyActivity);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String msg = "DrawSome is Awesome!";
        NdefMessage ndefMessage = new NdefMessage(
                NdefRecord.createMime("application/com.drawsome.bluetooth", msg.getBytes())
        );
        Log.v(this.getClass().toString(), "push started!");
        initiate(null);
        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.v(this.getClass().toString(), "push complete!");
        Toast.makeText(BluetoothManualActivity.this, "push complete", Toast.LENGTH_SHORT).show();
        initiate(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) && firstTime) {
            firstTime = false;
            processIntent(getIntent());
        }
    }

    /**
     * Called when app is invoked from an nfc calL.
     */
    public void processIntent(Intent intent) {
        join(null);
    }
}
