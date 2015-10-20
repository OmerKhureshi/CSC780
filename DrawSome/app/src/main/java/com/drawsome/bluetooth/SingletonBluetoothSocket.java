package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Created by pooja on 10/11/2015.
 */
public class SingletonBluetoothSocket {

    private static SingletonBluetoothSocket singletonBluetoothSocket = new SingletonBluetoothSocket();
    private BluetoothSocket mmServerSocket;
    private BluetoothSocket mmClientSocket;


    private SingletonBluetoothSocket() {

    }
    public static SingletonBluetoothSocket getBluetoothSocketInstance() {
        return singletonBluetoothSocket;

    }

    public  BluetoothSocket getMmServerSocket() {
        return mmServerSocket;
    }

    public  void setMmServerSocket(BluetoothSocket mmServerSocket) {
        this.mmServerSocket = mmServerSocket;
    }

    public  BluetoothSocket getMmClientSocket() {
        return mmClientSocket;
    }

    public  void setMmClientSocket(BluetoothSocket mmClientSocket) {
        this.mmClientSocket = mmClientSocket;
    }






}
