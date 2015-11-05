package com.drawsome.bluetooth;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pooja on 11/4/2015.
 */
public class ConnectedThread extends Thread{

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private List<String> data = new ArrayList<String>();
    private boolean flag = true;
    public ConnectedThread() {

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getInputStream();
            tmpOut = SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }


    @Override
    public void run() {

          byte[] buffer = new byte[200];  // buffer store for the stream
          int bytes; // bytes returned from read()
          // Keep listening to the InputStream until an exception occurs
          while (flag) {
              try {
                  // Read from the InputStream
                  if (mmInStream.available() > 0) {
                      bytes = mmInStream.read(buffer);
                      // Send the obtained bytes to the UI activity
                      synchronized (this) {
                          if (bytes > 0) {
                              Log.d("Received bytes ", "" + bytes);
                              data.add(new String(buffer));
//                    mmInStream.reset();
                          }
                      }
                  }
                  }catch(IOException e){
                      e.printStackTrace();
                      break;
                  }


          }

    }

    public synchronized List<String> getData(){
        return data;
    }


    /* Call this from the main activity to send data to the remote device */
    public void write(String message) {
        try {
            mmOutStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void interrupt(){
        try {
            flag = false;
            Log.d("Connected thread ","flag set false");

        }finally{
            super.interrupt();
        }
    }
}
