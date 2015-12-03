package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.drawsome.drawing.DrawingDetailsBean;
import com.drawsome.drawing.MarshalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The thread which sends data via established bluetooth channel.
 * Created by pooja on 10/21/2015.
 */
public class ConnectedDrawingWriteThread extends Thread {
    private final OutputStream mmOutStream;

    private boolean flag = true;
    List<DrawingDetailsBean> listToSend = null;

    public ConnectedDrawingWriteThread(BluetoothSocket socket) {

        OutputStream tmpOut = null;
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmOutStream = tmpOut;

        listToSend = new ArrayList<DrawingDetailsBean>();
    }

    /*
    * Method that allows to add data to be sent in the list.
      * The data must be an object of DrawingDetailsBean class.
     */
    public  void addToListToSend(DrawingDetailsBean drawingDetailsBean) {
        synchronized(this) {
            Log.d("Inside addToListToSend ", "Obtained lock, adding drawing details to list");
            listToSend.add(drawingDetailsBean);
            notify();
        }
    }
    public void run() {
        while (flag) {
            synchronized (this) {
            try {
                Log.d("WritingThread:","Blocking call wait");
            wait();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
                if(!flag){
                    break;
                }
                Log.d("WritingThread:","Notified, ready to send data");
            for (DrawingDetailsBean bean : listToSend) {
                sendDrawingDetails(bean);
            }
                Log.d("WritingThread:","Data sent");
                listToSend.clear();
        }
    }
    }

    /*
    * The method sends object of DrawingDetailsBean to marshalHandler to marshal data in form of byte array.
    * The byte array then is sent to the other device.
     */
    private  void sendDrawingDetails(DrawingDetailsBean drawingDetailsBean) {
        try {

            mmOutStream.write(MarshalHandler.getMarshalHandlerInstance().marshal(drawingDetailsBean));
            Log.d("sending drawingDeails  ", drawingDetailsBean.toString());
            mmOutStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

}
