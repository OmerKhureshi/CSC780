package com.drawsome;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Activity  {
    Button b1,b2,b3,b4;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    ListView lv;
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    ConnectedThread connectedThread;
    private EditText editText;
    private TextView receivedMessageText;
    private BroadcastReceiver mReceiver;
    private BluetoothDevice pairedDevice;

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            /**
             * Retrieve the contents of the message and then update the UI
             */

            String message  =  msg.getData().getString("ReceivedMessage");

            Toast.makeText(getApplicationContext(),"message " + message , Toast.LENGTH_LONG);
            receivedMessageText.setText(message);
        }

    }

    final private Handler handler = new UIHandler();

    private final class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket ) {
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
                    if(bytes > 0) {

                        String message = new String(buffer);
                        Log.d("Receieved message ", message);
                        Message msg = handler.obtainMessage();

                        /**
                         * Class Message can store two integer values that can be
                         * passed as parameters. If the data that needs to be passed
                         * is more complex, use Message.setData()
                         */
                        Bundle b = new Bundle();
                        b.putString("ReceivedMessage",message);
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        b1 = (Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);

        editText = (EditText) findViewById(R.id.editText);
        receivedMessageText = (TextView)findViewById(R.id.receivedMessage);
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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // Register the BroadcastReceiver
        registerReceiver(mReceiver, intentFilter); // Don't forget to unregister during onDestroy

    }

    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,Toast.LENGTH_LONG).show();
    }

    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void list(View v){
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
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
    public void sendViaBluetooth(View v) {

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
        } else {
            enableBluetooth();
        }
    }

    public void enableBluetooth() {

        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);

        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {


        Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            File f = new File(Environment.getExternalStorageDirectory(), "md5sum.txt");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

            if(appsList.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for(ResolveInfo info : appsList) {
                    packageName = info.activityInfo.packageName;
                    if(packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Bluetooth havn't been found",
                            Toast.LENGTH_LONG).show();
                } else {
                    intent.setClassName(packageName, className);
                    startActivity(intent);
                }
            }
        } else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Operation is cancelled", Toast.LENGTH_LONG)
                    .show();
        }
    }


    public void serverConnect(View v) {

        handleServerConnection();
    }
        public void handleServerConnection() {


        /*AcceptThread acceptThread = new AcceptThread(this,BA);
        acceptThread.start();*/

        BluetoothServerSocket mmServerSocket = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            mmServerSocket = BA.listenUsingRfcommWithServiceRecord("Drawsome",UUID.fromString("2511000-80cf0-11bd-b23e-10b96e4ef00d") );
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
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
                break;

            }
        }
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
                mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("2511000-80cf0-11bd-b23e-10b96e4ef00d"));
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
                    closeException.printStackTrace();}
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
            Toast.makeText(this,"Connection successful",Toast.LENGTH_LONG).show();

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();


    }

public void sendText(View v) {

    EditText text = (EditText)findViewById(R.id.editText);
    connectedThread.write(text.getText().toString());

}
    @Override
    public void onDestroy() {
        BA.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }
}



 /*
 class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    Context context;
    BluetoothAdapter BA;
    public AcceptThread(Context context,BluetoothAdapter BA) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        this.context = context;
        BluetoothServerSocket tmp = null;
        this.BA = BA;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = BA.listenUsingRfcommWithServiceRecord("Drawsome",UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d") );
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void run() {
    }

    // Will cancel the listening socket, and cause the thread to finish
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     class ConnectThread extends Thread {
         private final BluetoothSocket mmSocket;
         private final BluetoothDevice mmDevice;
         private final BluetoothAdapter BA;
         private Context context;

         public ConnectThread(BluetoothDevice device,Context context,BluetoothAdapter BA) {
             // Use a temporary object that is later assigned to mmSocket,
             // because mmSocket is final
             BluetoothSocket tmp = null;
             mmDevice = device;
             this.context = context;
             this.BA = BA;
             // Get a BluetoothSocket to connect with the given BluetoothDevice
             try {
                 // MY_UUID is the app's UUID string, also used by the server code
                 tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
             } catch (IOException e) {
                e.printStackTrace();
             }
             mmSocket = tmp;
         }

         public void run() {
             System.out.println("************** CLIENT *******************");
             // Cancel discovery because it will slow down the connection

         }

         // Will cancel an in-progress connection, and close the socket
         public void cancel() {
             try {
                 mmSocket.close();
             } catch (IOException e) {
                e.printStackTrace();
             }
         }
     }


 }

*/