package com.drawsome.drawing;

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
    private MarshalHandler() {

    }
    public static MarshalHandler getMarshalHandlerInstance() {
        return marshalHandler;
    }
    public byte[] marshal(DrawingDetailsBean drawingDetailsBean) {

        int lengthBean = drawingDetailsBean.getPointList().size();
      //  byte[] streamToSend = new byte[ lengthBean * 8 + 3 *4];

        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthBean * 8 + 3 * 4);
        byteBuffer.putInt(drawingDetailsBean.getPaint());
        byteBuffer.putFloat(drawingDetailsBean.getStrokewidth());
        byteBuffer.putInt(lengthBean);
         for(Point point : drawingDetailsBean.getPointList()) {
             byteBuffer.putFloat(point.getX());
             byteBuffer.putFloat(point.getY());
         }
        return byteBuffer.array();
    }

    public static DrawingDetailsBean unmarshal(byte[] byteBuffer) {
         int offset =0;
        final int intSize = 4;
        final int floatSize =4;
        DrawingDetailsBean drawingDetailsBean = new DrawingDetailsBean();
        drawingDetailsBean.setPaint(ByteBuffer.wrap(byteBuffer, offset, intSize).getInt());
        offset = offset + intSize;
        drawingDetailsBean.setStrokewidth(ByteBuffer.wrap(byteBuffer, offset, floatSize).getFloat());
        offset = offset + floatSize;
        int lengthOfList = ByteBuffer.wrap(byteBuffer,offset,intSize).getInt();
        offset = offset + intSize;
        List<Point> pointList = new ArrayList<Point>();
        for(int index =0; index < lengthOfList; index ++) {
            Point point = new Point();
            point.setX(ByteBuffer.wrap(byteBuffer,offset,floatSize).getFloat());
            offset = offset + floatSize;
            point.setY(ByteBuffer.wrap(byteBuffer,offset,floatSize).getFloat());
            offset = offset + floatSize;
            pointList.add(point);
        }
        drawingDetailsBean.setPointList(pointList);
        return drawingDetailsBean;
    }

}
