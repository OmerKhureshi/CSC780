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
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.drawmate.drawmate.Draw.DrawActivity;
import com.drawmate.drawmate.NFC.NfcHelper;
import com.drawmate.drawmate.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionActivity extends Activity {

    private BluetoothAdapter BA;
    Button joinButton, initiateButton, sendButton;
    private EditText editText;
    private TextView receivedMessageText;
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    private static final int SERVER_CONNECTION = 2;
    private static final int CLIENT_CONNECTION = 3;
    NfcHelper nfcHelper;
    int counter = 0;
    BluetoothHelper bluetoothHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        Toast.makeText(this, "OnCreate.", Toast.LENGTH_SHORT).show();
        receivedMessageText = (TextView) findViewById(R.id.receivedMessage);

        initiateButton = (Button) findViewById(R.id.initiateButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        joinButton = (Button) findViewById(R.id.joinButton);

        bluetoothHelper = new BluetoothHelper(this);
        nfcHelper = bluetoothHelper.initiate();
    }


    public void initiate(View v) {
        bluetoothHelper.initiateServer();
    }

    public void join(View v) {
        bluetoothHelper.joinServer();
    }

    public void draw ( View view ) {
        Intent intent = new Intent(this, com.drawmate.drawmate.Draw.DrawActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcHelper.invokedViaNFC(getIntent())){
            Toast.makeText(this, "In process intent.", Toast.LENGTH_SHORT).show();
            processIntent(getIntent());
        }
        else {
            Log.v("omer is debugging", "Class: MainActivity; method: onResume; did not call processIntent.");
            Toast.makeText(this, "On Resume without process intent", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }


    public void processIntent(Intent intent) {
        String msg = intent.getAction();
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) rawMsgs[0];

        if (counter < 1){
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
            startActivityForResult(getVisible, CLIENT_CONNECTION);
            Toast.makeText(this, "In processIntent", Toast.LENGTH_SHORT).show();
        }
        counter = 1;

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
        if (resultCode == DISCOVER_DURATION && requestCode == SERVER_CONNECTION) {
            bluetoothHelper.handleServerConnection();
        } else if (resultCode == DISCOVER_DURATION && requestCode == CLIENT_CONNECTION) {
            bluetoothHelper.startBluetoothDiscovery();
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }


    public void sendText(View v) {
        EditText text = (EditText) findViewById(R.id.editText);
        bluetoothHelper.sendText(text.getText().toString());
    }

    public void newMsg(String msg) {
        receivedMessageText = (TextView) findViewById(R.id.receivedMessage);
        receivedMessageText.setText(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothHelper.unregisterBluetoothReceiver();
    }
}
