package com.drawsome.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.drawsome.drawing.DrawingDetailsBean;
import com.drawsome.drawing.MarshalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pooja on 10/21/2015.
 */
public class ConnectedDrawingWriteThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Integer lock =0;

    List<DrawingDetailsBean> listToSend = null;

    public ConnectedDrawingWriteThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        listToSend = new ArrayList<DrawingDetailsBean>();
    }
    public  void addToListToSend(DrawingDetailsBean drawingDetailsBean) {
        synchronized(this) {
            Log.d("Inside addToListToSend ", "Obtained lock, adding drawing details to list");
            listToSend.add(drawingDetailsBean);
            notify();
        }
    }
    public void run() {
        while (true) {
            synchronized (this) {
            try {
                Log.d("WritingThread:","Blocking call wait");
            wait();
        }catch(InterruptedException e){
            e.printStackTrace();
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
    public  void sendDrawingDetails(DrawingDetailsBean drawingDetailsBean) {
        try {
            mmOutStream.write(MarshalHandler.getMarshalHandlerInstance().marshal(drawingDetailsBean));
            Log.d("sending drawingDeails  ", drawingDetailsBean.toString());
            mmOutStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
