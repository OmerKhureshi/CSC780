package com.drawsome.bluetooth;

/**
 * Created by pooja on 11/4/2015.
 */
public class ConnectedThreadSingleton {

    private static ConnectedThreadSingleton connectedThreadSingleton = new ConnectedThreadSingleton();

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }

    public void setConnectedThread(ConnectedThread connectedThread) {
        this.connectedThread = connectedThread;
    }

    private ConnectedThread connectedThread;
    private ConnectedThreadSingleton() {

    }
    public static ConnectedThreadSingleton getConnectedThreadInstance() {
        return connectedThreadSingleton;
    }

}