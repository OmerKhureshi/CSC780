package com.drawsome.drawing;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThreadSingleton;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

public class ViewDrawingActivity extends Activity {

    DrawView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drawing);
        ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread().interrupt();
        Log.d("Thread interrupted ","" +ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread().isInterrupted());
        mView = (DrawView) findViewById(R.id.viewDraw);
        mView.setEnabled(false);
        mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket());
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
