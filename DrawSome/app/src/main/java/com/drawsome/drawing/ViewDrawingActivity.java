package com.drawsome.drawing;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

public class ViewDrawingActivity extends Activity {

    DrawView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drawing);
        mView = (DrawView) findViewById(R.id.viewDraw);
        mView.setEnabled(false);
        mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmClientSocket());
        mView.startThread();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
