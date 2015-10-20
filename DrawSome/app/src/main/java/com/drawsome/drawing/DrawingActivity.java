package com.drawsome.drawing;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

public class DrawingActivity extends Activity {

    DrawView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        mView = (DrawView) findViewById(R.id.draw);
        mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmServerSocket());
        mView.startThread();

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


}