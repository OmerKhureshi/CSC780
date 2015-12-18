
package com.drawsome.drawing;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.UiFlow.Difficulty.DifficultySecondUserActivity;
import com.drawsome.bluetooth.ConnectedDrawingReadThread;
import com.drawsome.bluetooth.ConnectedDrawingWriteThread;

import java.util.ArrayList;
import java.util.List;
/*
 * This class extends View and draws the custom view consisting of the drawing area.
 * It also support sending strokes to secondary device.
 * Authors: Pooja Kanchan and Syed Omer Salar Khureshi
 *
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
    private float brushSize, lastBrushSize;
    BluetoothSocket mmSocket;
    //erase flag
    private boolean erase=false;
    private int canvasHeight,canvasWidth;

    private boolean touchable = true;
    final private Handler handler = new UIHandler();
    final private static  int SEND_LIMIT =10;

    final private int NEW = -1;
    final private int SUCCESS = -2;
    final private int GIVE_UP = -3;

    final private String DRAWING_DETAILS_KEY = "DrawingDetails";
    final private String TYPE_KEY = "type";
    final private String TYPE_SEND = "send";

    final float density = getContext().getResources().getDisplayMetrics().density;
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
        mPaint.setColor(Color.BLACK);
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

        if(!touchable){
                return super.onTouchEvent(event);
        }
        // start drawing on action down event.
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawingDetailsBean = getDrawingObject(event.getX(),event.getY());
            path.moveTo(event.getX(), event.getY());
            //  path.lineTo(event.getX(), event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            path.lineTo(event.getX(), event.getY());
            Point point = new Point();
            //convert into device independent pixel
            point.setX(event.getX() * 160/density);
            point.setY(event.getY() * 160/density);
            path.moveTo(event.getX(), event.getY());
            drawingDetailsBean.getPointList().add(point);
            System.out.println("Point list " + drawingDetailsBean.getPointList().size());

            //send drawing to second device
            if(drawingDetailsBean.getPointList().size() > SEND_LIMIT){
                setHandlerMessage(drawingDetailsBean);
                drawingDetailsBean = getDrawingObject(event.getX(),event.getY());
            }
            drawCanvas.drawPath(path, mPaint);
            invalidate();

        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            path.reset();

            //send drawing to second device
            setHandlerMessage(drawingDetailsBean);
        }
        return true;
    }

   /*
    * send message to handler
    */
    private void setHandlerMessage(DrawingDetailsBean bean) {
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putParcelable(DRAWING_DETAILS_KEY,bean);
        b.putString(TYPE_KEY,TYPE_SEND);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    /*
    *  create a new drawing object
    */
    private DrawingDetailsBean getDrawingObject(float x , float y){
        DrawingDetailsBean bean = new DrawingDetailsBean();
        List<Point> pointList = new ArrayList<Point>();
        bean.setPointList(pointList);
        Point point = new Point();
        point.setX(160 * x/density);
        point.setY(160 * y/density);

        pointList.add(point);
        // Create new object of drawingdetailsBean
        bean.setHeight(canvasHeight);
        bean.setWidth(canvasWidth);
        bean.setPointList(pointList);
        bean.setPaint(mPaint.getColor());
        bean.setStrokewidth(mPaint.getStrokeWidth());
        bean.setEraserFlag(erase);
        return bean;
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
        canvasHeight = h ;
        canvasWidth = w;
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    public void stopThreads(){

        connectedDrawingWriteThread.setFlag(false);
        synchronized (connectedDrawingWriteThread) {
            connectedDrawingWriteThread.notify();
        }
        connectedDrawingReadThread.setFlag(false);
        Log.d("status of threads read ", connectedDrawingReadThread.isAlive() + " write " + connectedDrawingWriteThread.isAlive());
    }


    private void populateSuccessMessage(){
        stopThreads();
        Toast.makeText(getContext(), "Your drawing was guessed!!", Toast.LENGTH_LONG);
        Log.d("drawview ","Your drawing was guessed!!");
        Log.d("status of threads read ", connectedDrawingReadThread.isAlive() + " write " + connectedDrawingWriteThread.isAlive());
        try {
            Thread.sleep(2000);
        }  catch(InterruptedException ie){
            Log.d("interruptedException", ie.getMessage());
            ie.printStackTrace();
        }

        //display success message and start new activity
        ((Activity)getContext()).setContentView(R.layout.change_user_first);
             new CountDownTimer(4000,1000){
                    @Override
                    public void onTick(long millisUntilFinished){

                    }

                    @Override
                    public void onFinish(){
                        //set the new Content of your activity

                      System.out.println("loading activity");
                        ((Activity)getContext()).finish();
                        Intent intent = new Intent(getContext(), DifficultySecondUserActivity.class);
                        getContext().startActivity(intent);
                     //   YourActivity.this.setContentView(R.layout.main);

                    }
                }.start();
    }

    /*
     * send the word is guessed to second device
     */

    public void sendWordGuessedMessage(){
        Log.d("DrawView ","sending success message");
        DrawingDetailsBean bean = new DrawingDetailsBean();
        bean.setHeight(SUCCESS);
        bean.setWidth(SUCCESS);
        bean.setPaint(SUCCESS);
        bean.setStrokewidth(SUCCESS);
        bean.setEraserFlag(false);
        setHandlerMessage(bean);
    }

    /*
     * send the give up message to other device
     */
    public void sendGiveUpMessage(){
        Log.d("DrawView ","sending give up message");
        DrawingDetailsBean bean = new DrawingDetailsBean();
        bean.setHeight(GIVE_UP);
        bean.setWidth(GIVE_UP);
        bean.setPaint(GIVE_UP);
        bean.setStrokewidth(GIVE_UP);
        bean.setEraserFlag(false);
        setHandlerMessage(bean);
    }

    /*
     * set give up screen and play audio clip
     */
    private void populateGiveUpMessage(){
        Log.d("Drawview","Second user exit");
        stopThreads();
        Toast.makeText(getContext(),"Your buddy gave up!",Toast.LENGTH_LONG);
        new CountDownTimer(4000,1000){
            boolean flag = true;
            @Override
            public void onTick(long millisUntilFinished){
                if(flag) {
                    ((Activity) getContext()).setContentView(R.layout.give_up);
                    MediaPlayer player = MediaPlayer.create(getContext(), R.raw.give_up);
                    player.start();
                    flag = false;
                }
            }

            @Override
            public void onFinish(){
                ((Activity)getContext()).finish();

            }
        }.start();

    }
    /*
    * Private class which handles incoming data form other device and displays on canvas.
     */
    private final class UIHandler extends Handler {

        private Paint setNewPaint(int color, float strokeWidth,boolean eraserFlag ){
            Paint tempPaint = new Paint();
            tempPaint.setStyle(Paint.Style.STROKE);
            tempPaint.setStrokeJoin(Paint.Join.ROUND);
            tempPaint.setStrokeCap(Paint.Cap.ROUND);
            tempPaint.setDither(true);
            tempPaint.setAntiAlias(true);
            tempPaint.setColor(color);
            if (eraserFlag)
                tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            else
                tempPaint.setXfermode(null);

            tempPaint.setStrokeWidth(strokeWidth);
            return tempPaint;
        }

        public void handleMessage(Message msg) {
            /**
             * Retrieve the contents of the message and then update the UI
             */
            System.out.println("in ui handler");

            String type = msg.getData().getString(TYPE_KEY);
            if (type!= null && type.equalsIgnoreCase(TYPE_SEND)) {
                DrawingDetailsBean bean = msg.getData().getParcelable(DRAWING_DETAILS_KEY);
                if (bean != null) {
                    synchronized (connectedDrawingWriteThread){
                        connectedDrawingWriteThread.addToListToSend(bean);
                    }
                  //  connectedDrawingWriteThread.sendDrawingDetails(bean);
                }

            } else {
                List<DrawingDetailsBean> drawingList = msg.getData().getParcelableArrayList("DrawingDetails");
                if (drawingList != null) {
                    System.out.println("drawing list not null");
                    for (DrawingDetailsBean bean : drawingList) {
                    // calculate aspect ratio of the two screens.
                        if(bean.getHeight() == NEW || bean.getWidth() == NEW) {
                            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                            invalidate();
                            return;
                        } else if(bean.getWidth() == SUCCESS || bean.getHeight() == SUCCESS){
                            populateSuccessMessage();
                            return;
                        } else if(bean.getWidth() == GIVE_UP || bean.getHeight() == GIVE_UP){
                            populateGiveUpMessage();
                            return;
                        }

                        float aspectHeight = ((float)canvasHeight)/bean.getHeight();
                        float aspectWidth = ((float)canvasWidth)/bean.getWidth();

                        Log.d("draw view aspect  " , aspectHeight + "   " + aspectWidth) ;
                        //canvasBitmap = bitmapDataObject.getCurrentImage();

                        //initialie paint attributes.
                        List<Point> pointList = bean.getPointList();

                        Paint tempPaint = setNewPaint(bean.getPaint(),bean.getStrokewidth(),bean.isEraserFlag());

                        // draw points on canvas.
                        Point originalPoint = pointList.get(0);
                        pointList.remove(0);
                        Path tempPath = new Path();
                        tempPath.moveTo(originalPoint.getX() *  density/160, (originalPoint.getY() + 20) * density/160);
                        for (Point point1 : pointList) {
                            tempPath.lineTo(point1.getX() * density/160, point1.getY() * density/160);

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
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
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

    public void setAlpha(int val) {
        mPaint.setAlpha(val);
    }

    //start new drawing
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        DrawingDetailsBean bean = new DrawingDetailsBean();
        bean.setHeight(-1);
        bean.setWidth(-1);
        bean.setPaint(-1);
        bean.setStrokewidth(-1);
        bean.setEraserFlag(false);
        setHandlerMessage(bean);
        invalidate();
    }

    public void setTouchable(boolean val){
        this.touchable = val;
    }

}