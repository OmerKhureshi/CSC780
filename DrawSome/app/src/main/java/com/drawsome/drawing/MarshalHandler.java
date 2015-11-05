package com.drawsome.drawing;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pooja on 10/15/2015.
 */
public class MarshalHandler {

    private static MarshalHandler marshalHandler = new MarshalHandler();
    private static ByteBuffer tempBuffer;
    private MarshalHandler() {

    }
    public static MarshalHandler getMarshalHandlerInstance() {
        return marshalHandler;
    }
    public byte[] marshal(DrawingDetailsBean drawingDetailsBean) {

        int lengthBean = drawingDetailsBean.getPointList().size();
      //  byte[] streamToSend = new byte[ lengthBean * 8 + 3 *4];
        Log.d("marshal ","lenthBean " + lengthBean);
        int lengthOfData = lengthBean * 8 + 3 * 4;
        Log.d("marshal ","lengthOfData " + lengthOfData);

        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthOfData + 4);
        byteBuffer.putInt(lengthOfData);
        byteBuffer.putInt(drawingDetailsBean.getPaint());
        byteBuffer.putFloat(drawingDetailsBean.getStrokewidth());
        byteBuffer.putInt(lengthBean);
         for(Point point : drawingDetailsBean.getPointList()) {
             byteBuffer.putFloat(point.getX());
             byteBuffer.putFloat(point.getY());
         }
        return byteBuffer.array();
    }

    public static ArrayList<DrawingDetailsBean> unmarshal(byte[] byteBuffer,int length) {
        final int intSize = 4;
        final int floatSize =4;
        int count =0;
        ArrayList<DrawingDetailsBean> drawingList = new ArrayList<DrawingDetailsBean>();
     while(count < length) {
         DrawingDetailsBean drawingDetailsBean = new DrawingDetailsBean();
         int offset =4;
         int lengthData = ByteBuffer.wrap(byteBuffer, count, intSize).getInt();
         Log.d("unmarshaler " ," bytebuffer " + length);
        Log.d("in unmarshaler " , "length of data " + lengthData + " count " + count + " offset " + offset);
         if (lengthData > length - count) {
             Log.d("incomplete data received", "storing for next cycle ");
             tempBuffer = ByteBuffer.wrap(byteBuffer, count, byteBuffer.length - count);
             count = byteBuffer.length;
         }
         if(tempBuffer != null && tempBuffer.hasRemaining()) {
             lengthData = byteBuffer[count];
             int bytesToRead = lengthData - tempBuffer.capacity();
             tempBuffer.put(byteBuffer,count,bytesToRead);
             drawingDetailsBean.setPaint(tempBuffer.getInt());
             drawingDetailsBean.setStrokewidth(tempBuffer.getFloat());
             int lengthOfList = tempBuffer.getInt();
             List<Point> pointList = new ArrayList<Point>();

             for (int index = 0; index < lengthOfList; index++) {
                 Point point = new Point();
                  point.setX(tempBuffer.getInt());
                 point.setY(tempBuffer.getInt());
                pointList.add(point);
             }
             drawingDetailsBean.setPointList(pointList);
             offset = offset + bytesToRead;
            count = count + bytesToRead;
         } else {
             drawingDetailsBean.setPaint(ByteBuffer.wrap(byteBuffer, offset, intSize).getInt());
             offset = offset + intSize;
             drawingDetailsBean.setStrokewidth(ByteBuffer.wrap(byteBuffer, offset, floatSize).getFloat());
             offset = offset + floatSize;
             int lengthOfList = ByteBuffer.wrap(byteBuffer, offset, intSize).getInt();
             offset = offset + intSize;
             List<Point> pointList = new ArrayList<Point>();
             for (int index = 0; index < lengthOfList; index++) {
                 Point point = new Point();
                 point.setX(ByteBuffer.wrap(byteBuffer, offset, floatSize).getFloat());
                 offset = offset + floatSize;
                 point.setY(ByteBuffer.wrap(byteBuffer, offset, floatSize).getFloat());
                 offset = offset + floatSize;
                 pointList.add(point);
             }
             drawingDetailsBean.setPointList(pointList);
             drawingList.add(drawingDetailsBean);
             count = count + lengthData + 4;
             Log.d("unmarshaler " , " count increased to " + count);
         }
     }
         return drawingList;
    }

}
