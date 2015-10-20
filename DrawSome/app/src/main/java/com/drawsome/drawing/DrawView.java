
package com.drawsome.drawing;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.drawsome.bluetooth.ConnectedThread;

import java.util.ArrayList;
import java.util.List;


public class DrawView extends View {
    private Path path;
    private Paint mPaint,canvasPaint;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    ConnectedThread connectedThread;
    DrawingDetailsBean drawingDetailsBean = null;
    List<Point> pointList = null;

    BluetoothSocket mmSocket;

    public DrawView(Context context) {
        super(context);
        init();
        this.setBackgroundColor(Color.WHITE);
    }
    public DrawView(Context context,AttributeSet attributeSet) {
        super(context,attributeSet);
        init();
        this.setBackgroundColor(Color.WHITE);
    }
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        this.setBackgroundColor(Color.WHITE);
    }

    private void init() {

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xFFFFFF00);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        canvasPaint = new Paint();
             canvasPaint.setFlags(Paint.DITHER_FLAG);
        path = new Path();
    }

    public void startThread() {
        connectedThread = new ConnectedThread(mmSocket,handler);
        connectedThread.start();
    }
    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    public void setMmSocket(BluetoothSocket mmSocket) {
        this.mmSocket = mmSocket;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void setStrokeWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawingDetailsBean = new DrawingDetailsBean();
            pointList = new ArrayList<Point>();
            Point point = new Point();
            point.setX(event.getX());
            point.setY(event.getY());
            path.moveTo(event.getX(), event.getY());
            pointList.add(point);
            drawingDetailsBean.setPointList(pointList);
            drawingDetailsBean.setPaint(mPaint.getColor());
            drawingDetailsBean.setStrokewidth(mPaint.getStrokeWidth());
            //  path.lineTo(event.getX(), event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            path.lineTo(event.getX(), event.getY());
            Point point = new Point();
            point.setX(event.getX());
            point.setY(event.getY());
            path.moveTo(event.getX(), event.getY());
            pointList.add(point);
            drawCanvas.drawPath(path, mPaint);
            invalidate();

            /*DrawingDetailsBean bean = new DrawingDetailsBean();
            bean.setPath(path);
            bean.setPaint(mPaint);
            Log.d("DrawingActivity ","sending from touchevent " + bean.getPath() + " " + bean.getPaint());
            connectedThread.sendDrawing(bean);
*/
  /*          BitmapDataObject bitmapDataObject = new BitmapDataObject();
            bitmapDataObject.setCurrentHeight(canvasBitmap.getHeight());
            bitmapDataObject.setCurrentWidth(canvasBitmap.getWidth());
            bitmapDataObject.setCurrentImage(canvasBitmap);
            Log.d("Sending bitmap from drawView " , bitmapDataObject.toString());
            connectedThread.sendBitmap(bitmapDataObject);*/

        } else if(event.getAction() == MotionEvent.ACTION_UP) {

            path.reset();
            connectedThread.sendDrawingDetails(drawingDetailsBean);
           // drawingDetailsBean.sendData();
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("received height" ,"   " + h + "   " + w);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            /**
             * Retrieve the contents of the message and then update the UI
             */
            DrawingDetailsBean bean = msg.getData().getParcelable("DrawingDetails");
            if(bean != null) {

                //canvasBitmap = bitmapDataObject.getCurrentImage();
                List<Point> pointList = bean.getPointList();
                Paint tempPaint = new Paint();
                tempPaint.setStyle(Paint.Style.STROKE);
                tempPaint.setStrokeJoin(Paint.Join.ROUND);
                tempPaint.setStrokeCap(Paint.Cap.ROUND);
                tempPaint.setDither(true);
                tempPaint.setAntiAlias(true);

                tempPaint.setColor(bean.getPaint());
                tempPaint.setStrokeWidth(bean.getStrokewidth());
                Point originalPoint = pointList.get(0);
                pointList.remove(0);
                Path tempPath = new Path();
                tempPath.moveTo(originalPoint.getX(),originalPoint.getY());
                for(Point point1 : pointList){
                    tempPath.lineTo(point1.getX(),point1.getY());

                    Log.d("Point " , point1.getX() + "  " + point1.getY());
                }
                drawCanvas.drawPath(tempPath,tempPaint);
                invalidate();

                //drawCanvas.setBitmap(canvasBitmap);

            }
/*
            DrawingDetailsBean bean = msg.getData().getParcelable("DrawingBean");
            Log.d(" in handler " , "received " + bean.getPath() + "  " + bean.getPaint());
            drawOnCanvas(bean);
*/
        }

    }
    final private Handler handler = new UIHandler();

}