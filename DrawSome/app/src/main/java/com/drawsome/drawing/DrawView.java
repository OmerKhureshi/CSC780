
package com.drawsome.drawing;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedDrawingReadThread;
import com.drawsome.bluetooth.ConnectedDrawingWriteThread;

import java.util.ArrayList;
import java.util.List;
/*
 * The class provides drawing canvas and implements drawing actions like change color, eraser etc.
 *Created by pooja on 09/15/2015.
 */

public class DrawView extends View {
    private Path path;
    private Paint mPaint,canvasPaint;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    ConnectedDrawingReadThread connectedDrawingReadThread;
    ConnectedDrawingWriteThread connectedDrawingWriteThread;
    DrawingDetailsBean drawingDetailsBean = null;
    List<Point> pointList = null;
    private float brushSize, lastBrushSize;
    BluetoothSocket mmSocket;
    //erase flag
    private boolean erase=false;
    private int canvasHeight,canvasWidth;

    final private Handler handler = new UIHandler();


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

    /*
    *   Sets initial attributes of tools such as paint,brush. Called by the constructor of this object
     */
    private void init() {
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
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

    /*
    *  starts read and write threads for listening incoming data and sending data via bluetooth.
     */
    public void startThread() {
        connectedDrawingReadThread = new ConnectedDrawingReadThread(mmSocket,handler);
        connectedDrawingReadThread.start();
        connectedDrawingWriteThread = new ConnectedDrawingWriteThread(mmSocket);
        connectedDrawingWriteThread.start();
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

    /*
    * The method to handle actual drawing. The data is also sent to other device on ACTION_UP event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // start drawing on action down event.
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawingDetailsBean = new DrawingDetailsBean();
            pointList = new ArrayList<Point>();
            Point point = new Point();
            point.setX(event.getX());
            point.setY(event.getY());
            path.moveTo(event.getX(), event.getY());
            pointList.add(point);
            // Create new object of drawingdetailsBean
            drawingDetailsBean.setHeight(canvasHeight);
            drawingDetailsBean.setWidth(canvasWidth);
            drawingDetailsBean.setPointList(pointList);
            drawingDetailsBean.setPaint(mPaint.getColor());
            drawingDetailsBean.setStrokewidth(mPaint.getStrokeWidth());
            drawingDetailsBean.setEraserFlag(erase);
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

        } else if(event.getAction() == MotionEvent.ACTION_UP) {

            path.reset();
            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putParcelable("DrawingDetails",drawingDetailsBean);
            b.putString("type","send");
            msg.setData(b);
            handler.sendMessage(msg);

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
        Log.d("received height", "   " + h + "   " + w);
        canvasHeight = h;
        canvasWidth = w;
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    /*
    * Private class which handles incoming data form other device and displays on canvas.
     */
    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            /**
             * Retrieve the contents of the message and then update the UI
             */
            System.out.println("in ui handler");

            String type = msg.getData().getString("type");
            if (type!= null && type.equalsIgnoreCase("send")) {
                connectedDrawingReadThread.sendDrawingDetails(drawingDetailsBean);

            } else {

                List<DrawingDetailsBean> drawingList = msg.getData().getParcelableArrayList("DrawingDetails");


                if (drawingList != null) {
                    System.out.println("drawing list not null");
                    for (DrawingDetailsBean bean : drawingList) {
                    // calculate aspect ratio of the two screens.
                        float aspectHeight = ((float)canvasHeight)/bean.getHeight();
                        float aspectWidth = ((float)canvasWidth)/bean.getWidth();

                        Log.d("draw view aspect  " , aspectHeight + "   " + aspectWidth) ;
                        //canvasBitmap = bitmapDataObject.getCurrentImage();

                        //initialie paint attributes.
                        List<Point> pointList = bean.getPointList();
                        Paint tempPaint = new Paint();
                        tempPaint.setStyle(Paint.Style.STROKE);
                        tempPaint.setStrokeJoin(Paint.Join.ROUND);
                        tempPaint.setStrokeCap(Paint.Cap.ROUND);
                        tempPaint.setDither(true);
                        tempPaint.setAntiAlias(true);
                        tempPaint.setColor(bean.getPaint());
                        if (bean.isEraserFlag())
                            tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        else
                            tempPaint.setXfermode(null);

                        tempPaint.setStrokeWidth(bean.getStrokewidth());

                        // draw points on canvas.
                        Point originalPoint = pointList.get(0);
                        pointList.remove(0);
                        Path tempPath = new Path();
                        tempPath.moveTo(originalPoint.getX() * aspectWidth, originalPoint.getY() * aspectHeight);
                        for (Point point1 : pointList) {
                            tempPath.lineTo(point1.getX() * aspectWidth, point1.getY() * aspectHeight);

                            Log.d("Point ", point1.getX() + "  " + point1.getY());
                        }
                        drawCanvas.drawPath(tempPath, tempPaint);
                        invalidate();


                    }
                }
            }
        }

    }


    //set brush size
    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        mPaint.setStrokeWidth(brushSize);
    }

    //get and set last brush size
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    //set erase true or false
    public void setErase(boolean isErase){
        erase=isErase;
        if(erase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mPaint.setXfermode(null);
    }

    //start new drawing
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }


}