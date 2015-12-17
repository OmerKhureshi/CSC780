package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Singleton class which provides a central place to store bluetooth socket.
 * Created by pooja on 10/11/2015.
 */
public class SingletonBluetoothSocket {

    private static SingletonBluetoothSocket singletonBluetoothSocket = new SingletonBluetoothSocket();
    private BluetoothSocket mmSocket;

    private SingletonBluetoothSocket() {

    }
    public static SingletonBluetoothSocket getBluetoothSocketInstance() {
        return singletonBluetoothSocket;

    }


    public  BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    public  void setMmSocket(BluetoothSocket mmSocket) {
        this.mmSocket = mmSocket;
    }
}
