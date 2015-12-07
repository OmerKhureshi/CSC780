package com.drawsome.UiFlow.Difficulty;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.ConnectedThreadSingleton;
import com.drawsome.drawing.DrawingActivity;
import com.drawsome.drawing.ViewDrawingActivity;

public class DifficultySecondUserActivity extends AppCompatActivity {

    private UIHandler difficultyHandler = new UIHandler();
    private ConnectedThread connectedThread;
    String word;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_second_user);
        connectedThread = new ConnectedThread();
        connectedThread.setHandler(difficultyHandler);
        ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);
        connectedThread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_difficulty_second_user, menu);
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

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
           word =  msg.getData().getString("wordToBeGuessed");
            if(word != null) {
           /*     runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        guess_word.setText(word);
                    }
                });*/
                Intent intent = new Intent(getApplicationContext(), ViewDrawingActivity.class);
                intent.putExtra("wordToGuess",word);
                startActivity(intent);
            }
        }
    }
}