package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.os.Handler;

import com.drawsome.drawing.DrawingDetailsBean;
import com.drawsome.drawing.MarshalHandler;

/**
 * The thread which reads incoming data. On receiving data, it is sent to marshalHandler for parsing.
 * If pasing is successful, the data is sent to UIHandler class for display.
 * Created by pooja on 10/11/2015.
 */


    public class ConnectedDrawingReadThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler handler;
        private boolean flag = true;
        public ConnectedDrawingReadThread(BluetoothSocket socket, Handler handler) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.handler = handler;
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
            byte[] buffer = new byte[20000];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (flag) {
                try {

                 if(mmInStream.available() > 0) {
                     // Read from the InputStream

                     bytes = mmInStream.read(buffer);
                     // Send the obtained bytes to the UI activity
                     if (bytes > 0) {
                         Log.d("Received bytes ", "" + bytes);
                         ArrayList<DrawingDetailsBean> drawingList = MarshalHandler.getMarshalHandlerInstance().unmarshal(buffer, bytes);
                         Log.d("Received bean ", "" + drawingList);
                         if (drawingList != null) {
                             Message msg = handler.obtainMessage();
                             Bundle b = new Bundle();
                             int lengthOFList = drawingList.size();
                             b.putString("type", "receive");
                             b.putInt("length", lengthOFList);
                             b.putParcelableArrayList("DrawingDetails", drawingList);
                             Log.d("snt data for display from connectedthread ", drawingList.toString());
                             msg.setData(b);
                             handler.sendMessage(msg);
                         }
//                    mmInStream.reset();
                     }
                 }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }



        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
                mmOutStream.close();
                mmInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public void setFlag(boolean flag){
        this.flag = flag;
    }
    }

