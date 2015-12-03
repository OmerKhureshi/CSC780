package com.drawsome.drawing;



import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.ConnectedThreadSingleton;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

import java.util.UUID;

/*
 * The activity which handles UI events of drawing canvas and drawing tools.
 * Created by pooja on 10/15/2015.
 */
public class DrawingActivity extends Activity implements View.OnClickListener{

    DrawView mView;
    private ImageView currPaint, eraseBtn, newBtn, saveBtn, drawBtn, brushBtn;
    private Spinner brush;
    private int brushDefSize;
    private int eraserSize;
    private float smallBrush, mediumBrush, largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_drawing);
        ConnectedThread connectedThread = ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread();
//        connectedThread.write("Ending thread");
        if(connectedThread != null)
        connectedThread.interrupt();
        Log.d("Thread interrupted ", "" + ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread().isInterrupted());

        mView = (DrawView) findViewById(R.id.draw);
        mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket());
        mView.startThread();
        mView.setTouchable(true);
        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        brushDefSize = R.integer.medium_size;


        //brush button
       // brush = (Spinner) findViewById(R.id.brushes_spinner);
       // MyAdapterBrushSize myAdapterBrushSize = new MyAdapterBrushSize(getApplicationContext(), R.layout.spinner_view_brush_size, new String[] {"50"});
       // brush.setAdapter(myAdapterBrushSize);

        //eraser button
      //  brush = (Spinner) findViewById(R.id.eraser_spinner);
      //  MyAdapterEraserSize myAdapterEraserSize = new MyAdapterEraserSize(getApplicationContext(), R.layout.spinner_view_brush_size, new String[] {"50"});
      //  brush.setAdapter(myAdapterEraserSize);

        //draw button
        drawBtn = (ImageView)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //brush button
        brushBtn = (ImageView)findViewById(R.id.draw_brush);
        brushBtn.setOnClickListener(this);

        //set initial size
        mView.setBrushSize(mediumBrush);

        //erase button
        eraseBtn = (ImageView)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageView)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = (ImageView)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

    }
    public void onLargeBrushClick(View v) {
        mView.setStrokeWidth(30);
    }
    public void onMedBrushClick(View v) {
        mView.setStrokeWidth(20);
    }
    public void onSmallBrushClick(View v) {
        mView.setStrokeWidth(10);
    }

    public void setColor(View v) {
     if(v instanceof ImageButton) {
         ImageButton img = (ImageButton) v;
         ColorDrawable colorDrawable = (ColorDrawable)img.getBackground();
         System.out.println("****************** color " + colorDrawable.getColor());

         mView.setColor(colorDrawable.getColor());
     }

    }

  /*
  * Class handles UI events related to brush size.
   */
    public class MyAdapterBrushSize extends ArrayAdapter<String> {

        String[] sObj;

        public MyAdapterBrushSize(Context context, int resource, String[] objs) {
            super(context, resource, objs);
            sObj = objs;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view= getLayoutInflater().inflate(R.layout.spinner_view_brush_size, parent,false);
            return view;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row=inflater.inflate(R.layout.spinner_item_brush_size, parent, false);
            final TextView label= (TextView) row.findViewById(R.id.brushes_spinnerItem_textView);
            label.setText(String.valueOf(brushDefSize));

            SeekBar seekBar = (SeekBar) row.findViewById(R.id.brushes_spinnerItem_seekBar);
            seekBar.setProgress(brushDefSize);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    label.setText(String.valueOf(progress));
                    mView.setErase(false);
                    mView.setBrushSize(progress);
                    brushDefSize = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            return row;
        }
    }

    /*
    * Class which handles UI events of eraser.
     */
    public class MyAdapterEraserSize extends ArrayAdapter<String> {

        String[] sObj;

        public MyAdapterEraserSize(Context context, int resource, String[] objs) {
            super(context, resource, objs);
            sObj = objs;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view= getLayoutInflater().inflate(R.layout.spinner_view_eraser, parent,false);
            return view;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row=inflater.inflate(R.layout.spinner_item_eraser, parent, false);
            final TextView label= (TextView) row.findViewById(R.id.eraser_spinnerItem_textView);
            label.setText(String.valueOf(brushDefSize));

            SeekBar seekBar = (SeekBar) row.findViewById(R.id.eraser_spinnerItem_seekBar);
            seekBar.setProgress(brushDefSize);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    label.setText(String.valueOf(progress));
                    mView.setErase(true);
                    mView.setBrushSize(progress);
                    brushDefSize = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            return row;
        }
    }


    /*
    * Method which handles UI event of color selection.
     */
    public void pickColor (View view) {
        HSVColorPickerDialog cpd = new HSVColorPickerDialog( this, 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                // Do something with the selected color
                float[] colorHsv = { 0f, 0f, 1f };
                int test = 1;
                //drawView.setColor(test, colorHsv);
                Toast.makeText(getApplicationContext(), "Color: " + color, Toast.LENGTH_SHORT).show();
                mView.setColor(color);
            }
        });
        cpd.setTitle( "Pick a color" );
        cpd.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    //user clicked paint
    public void paintClicked(View view){
        //use chosen color

        //set erase false
        mView.setErase(false);
        mView.setBrushSize(mView.getLastBrushSize());

        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            mView.setColor(Integer.parseInt(color));
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed, getTheme()));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint, getTheme()));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public void onClick(View view){

        if(view.getId()==R.id.draw_btn){
            //draw button clicked


            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(smallBrush);
                    mView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(mediumBrush);
                    mView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(largeBrush);
                    mView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            //show and wait for user interaction
            brushDialog.show();
        }
        else if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        }
        else if(view.getId()==R.id.new_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    mView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }
        else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    mView.setDrawingCacheEnabled(true);
                    //attempt to save
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), mView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");
                    //feedback
                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    mView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }
    }



}