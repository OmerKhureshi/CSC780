package com.drawsome.drawing;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a singleton class that handles marshaling and unmarshaling of drawing
 * details sent via bluetooth.
 * Created by pooja on 10/15/2015.
 */
public class MarshalHandler {

    private static MarshalHandler marshalHandler = new MarshalHandler();
    private static ByteBuffer tempBuffer = ByteBuffer.allocate(20000);
    private static int lengthDataStoredTempBuffer;
    private static int lengthObject;
    private static boolean incompleteDataFlag = false;
    private MarshalHandler() {

    }
 /*
   static method, returns singleton instance.
  */
    public static MarshalHandler getMarshalHandlerInstance() {
        return marshalHandler;
    }

    /*
    *   The method handles marshaling of data. It converts object DrawingDetailsBean to byte array.
    *   @param drawingDetailsBean object to be marshaled.
    */
    public byte[] marshal(DrawingDetailsBean drawingDetailsBean) {

        int lengthBean = drawingDetailsBean.getPointList().size();
      //  byte[] streamToSend = new byte[ lengthBean * 8 + 3 *4];
        Log.d("marshal ","lenthBean " + lengthBean);
        int lengthOfData = lengthBean * 8 + 5 * 4 + 1;
        Log.d("marshal ","lengthOfData " + lengthOfData);

        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthOfData + 4);
        byteBuffer.putInt(lengthOfData);
        byteBuffer.putInt(drawingDetailsBean.getHeight());
        byteBuffer.putInt(drawingDetailsBean.getWidth());
        byteBuffer.putInt(drawingDetailsBean.getPaint());
        byteBuffer.putFloat(drawingDetailsBean.getStrokewidth());

        if(drawingDetailsBean.isEraserFlag())
             byteBuffer.put((byte)1);
        else
            byteBuffer.put((byte)0);
        byteBuffer.putInt(lengthBean);
         for(Point point : drawingDetailsBean.getPointList()) {
             byteBuffer.putFloat(point.getX());
             byteBuffer.putFloat(point.getY());
         }
        return byteBuffer.array();
    }

    /*
    * This method handles unmarshaling of data. It converts given byte array into DrawingDetailsBean
    * object. If the object is sent partially, it stores it into temporary buffer. Once complete object
     * is received in nect chunk of data, the object is read.
    * @param bytebuffer : bytebuffer array containing data
    * @param length : length of data to be read
    * @return List containing drawingDetailsBean objects.
     */
    public static ArrayList<DrawingDetailsBean> unmarshal(byte[] byteBuffer,int length) {
        final int intSize = 4;
        int count =0;
        ArrayList<DrawingDetailsBean> drawingList = new ArrayList<DrawingDetailsBean>();
        Log.d("unmarshaler " ," bytebuffer length " + length);

        // if the partial data is already in the previous cycle, then merge it with the data sent
        // in this cycle.
        if(incompleteDataFlag == true) {
            if (tempBuffer != null) {
                int bytesToRead;

                // this cycle has the complete object. Merge it with previous data and unmarshal
                if(lengthObject <= length + lengthDataStoredTempBuffer) {
                    bytesToRead = lengthObject - lengthDataStoredTempBuffer;
                    incompleteDataFlag = false;
                    count =  bytesToRead;
                    Log.d("unmarshaler", "Complete Object received");
                    Log.d("unmarshaler", "lengthObject " + lengthObject + " length "
                            + length + " lengthDataStoredTempBuffer"  + lengthDataStoredTempBuffer
                            + " count " + count + " bytesToRead " + bytesToRead + "capacity "
                            + tempBuffer.capacity() + " remaining " + tempBuffer.remaining());
                    /*if(lengthObject > tempBuffer.remaining()){
                        ByteBuffer newBuffer = ByteBuffer.allocate(tempBuffer.capacity() + lengthObject - tempBuffer.remaining());
                        newBuffer.put(tempBuffer);
                        Log.d("New Buffer capacity " ,"" + newBuffer.capacity());
                        tempBuffer = newBuffer;
                    }*/
                    tempBuffer.put(byteBuffer, 0,bytesToRead);
                    Log.d("Unmasrshaler","remaining " + tempBuffer.remaining());
                    tempBuffer.position(0);
                    DrawingDetailsBean bean = readObject(tempBuffer);
                    if(bean != null)
                       drawingList.add(bean);
                    tempBuffer.clear();

                } else {
                    // this cycle does not contain complete data. Store it for the next cycle.
                    Log.d("incomplete data received", "storing for next cycle ");
                    /*if(length > tempBuffer.remaining()){
                        ByteBuffer newBuffer = ByteBuffer.allocate(tempBuffer.capacity() + length
                                            - tempBuffer.remaining());
                        Log.d("New Buffer capacity " ,"" + newBuffer.capacity());
                        newBuffer.put(tempBuffer);
                        tempBuffer = newBuffer;
                    }*/
                    tempBuffer. put(byteBuffer, 0, length);

                    lengthDataStoredTempBuffer = lengthDataStoredTempBuffer + length;
                    return null;
                }
            }
        }

    // while buffer has data, unmarshal it.
     while(count < length) {

         lengthObject = ByteBuffer.wrap(byteBuffer, count, intSize).getInt();
         count = count + intSize;
          Log.d("unmasrshaler ", " lengthOfObject " + lengthObject);

         // the complete object is received.
         if (lengthObject > length - count) {
             Log.d("incomplete data received", "storing for next cycle ");
             tempBuffer.clear();
             tempBuffer.put(byteBuffer, count, length - count);
            // tempBuffer = ByteBuffer.wrap(byteBuffer, count, length - count);
             lengthDataStoredTempBuffer = length - count;
             incompleteDataFlag = true;
             return null;

         } else {
           //incomplete object received. Store it in temporary buffer for next cycle and mark the flag.
             ByteBuffer buffer = ByteBuffer.wrap(byteBuffer, count, lengthObject);
             DrawingDetailsBean bean = readObject(buffer);
             if(bean != null)
                drawingList.add(bean);
             count = count + lengthObject;
             Log.d("unmarshaler " , " count increased to " + count);
         }
     }
         return drawingList;
    }

    /*
    * method to read byetebuffer and stores data in DrawingDetailsBean object.
     */
    private static DrawingDetailsBean  readObject (ByteBuffer buffer) {
        Log.d("unmarshaler "," remaining in readObject " + buffer.remaining() + " " + buffer.position());
     //   buffer.position(0);
        DrawingDetailsBean drawingDetailsBean = new DrawingDetailsBean();
        drawingDetailsBean.setHeight(buffer.getInt());
        drawingDetailsBean.setWidth(buffer.getInt());
        drawingDetailsBean.setPaint(buffer.getInt());
        drawingDetailsBean.setStrokewidth(buffer.getFloat());
        byte eraserMode = buffer.get();
        if(eraserMode == 1)
            drawingDetailsBean.setEraserFlag(true);
        else
            drawingDetailsBean.setEraserFlag(false);
        int lengthOfList = buffer.getInt();
        List<Point> pointList = new ArrayList<Point>();

        for (int index = 0; index < lengthOfList; index++) {
            Point point = new Point();
            point.setX(buffer.getFloat());
            point.setY(buffer.getFloat());
            Log.d(" Reading points " , point.getX() + "  " + point.getY());
            pointList.add(point);
        }
        drawingDetailsBean.setPointList(pointList);
        Log.d("reading object ",drawingDetailsBean.getPaint() + " " + drawingDetailsBean.getPointList().size());

        return drawingDetailsBean;
    }

}