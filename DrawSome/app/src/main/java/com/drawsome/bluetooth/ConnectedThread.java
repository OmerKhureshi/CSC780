package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;

import com.drawsome.drawing.DrawingDetailsBean;
import com.drawsome.drawing.MarshalHandler;

/**
 * Created by pooja on 10/11/2015.
 */


    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler handler;

        public ConnectedThread(BluetoothSocket socket,Handler handler) {
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
            while (true) {
                try {
                    // Read from the InputStream
                   bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    if(bytes > 0) {
                       DrawingDetailsBean drawingDetailsBean = MarshalHandler.getMarshalHandlerInstance().unmarshal(buffer);
                    //    BitmapDataObject bitmapDataObject = BitmapDataObject.deserialize(mmInStream);
                       // BitmapDataObject bitmapDataObject = buffer.re
                       /* DrawingDetailsBean bean = DrawingDetailsBean.deserialize(buffer);
                        Log.d("Receieved message ", bean.getPath() + "   " + bean.getPaint());
                       */
                        Log.d("Received bean ",drawingDetailsBean.toString());
                        Message msg = handler.obtainMessage();

                        /**
                         * Class Message can store two integer values that can be
                         * passed as parameters. If the data that needs to be passed
                         * is more complex, use Message.setData()
                         */
                        Bundle b = new Bundle();
                //        b.putParcelable("DrawingBean", bean);
                        b.putParcelable("DrawingDetails",drawingDetailsBean);
                        Log.d("snt data for display from connectedthread ",drawingDetailsBean.toString());
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
//                    mmInStream.reset();

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
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /* Call this from the main activity to send data to the remote device */


    public  void sendDrawingDetails(DrawingDetailsBean drawingDetailsBean) {
        try {
            mmOutStream.write(MarshalHandler.getMarshalHandlerInstance().marshal(drawingDetailsBean));
            Log.d("sending drawingDeails  " , drawingDetailsBean.toString());
            mmOutStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
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
    }

