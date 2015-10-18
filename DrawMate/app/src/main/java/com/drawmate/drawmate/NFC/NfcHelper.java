package com.drawmate.drawmate.NFC;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drawmate.drawmate.R;
import com.drawmate.drawmate.bluetooth.BluetoothConnectionActivity;
import com.drawmate.drawmate.bluetooth.BluetoothHelper;

public class NfcHelper
        implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback{

    private  NfcAdapter nfcAdapter;
    private Context context;
    private BluetoothConnectionActivity callingActivity;
    private BluetoothHelper parent;

    public NfcHelper(Context context, BluetoothHelper parent) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        this.context = context;
        this.callingActivity = (BluetoothConnectionActivity) context;
        this.parent = parent;
    }

    public boolean initiate() {
        boolean success = false;
        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessageCallback(this, callingActivity);
            nfcAdapter.setOnNdefPushCompleteCallback(this, callingActivity);
            success = true;
        }
        return success;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String msg = "Drawmate is awesome!";
        NdefMessage ndefMessage = new NdefMessage(
                NdefRecord.createMime("application/com.example.android.drawsome", msg.getBytes())
        );
        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        parent.initiateOnPushComplete();
    }

    public boolean invokedViaNFC(Intent intent) {
        boolean res = false;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            res = true;
        }
        return res;
    }
}