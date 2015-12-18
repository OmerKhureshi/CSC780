package com.drawsome.UiFlow.Difficulty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.SingletonBluetoothSocket;
import com.drawsome.drawing.ViewDrawingActivity;

import java.io.IOException;


/**
 * This activity displays a message to secondary user that primary user is selecting difficults.
 * Author: Pooja Kanchan.
 */
public class DifficultySecondUserActivity extends AppCompatActivity {

    private UIHandler difficultyHandler = new UIHandler();
    private ConnectedThread connectedThread;
    String word;
    private final String WORD_KEY = "wordToBeGuessed";
    private final String EXIT_KEY = "exitMessage";
    private final String GUESS_WORD_KEY = "wordToGuess";
    private final String SEPARATOR = ";";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_second_user);

    }

    @Override
    protected void onStart() {
        super.onStart();
        connectedThread = new ConnectedThread();
        connectedThread.setHandler(difficultyHandler);
        // ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);
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

    @Override
    protected void onStop() {
        super.onStop();
        // connectedThread = ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread();
        //connectedThread.write("Ending thread");
        if (connectedThread != null)
            connectedThread.interrupt();

    }

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {

            word = msg.getData().getString(WORD_KEY);
            if (word != null) {
           /*     runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        guess_word.setText(word);
                    }
                });*/
                if(word.equalsIgnoreCase(EXIT_KEY)){
                    handleExit();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ViewDrawingActivity.class);
                intent.putExtra(GUESS_WORD_KEY, word);
                startActivity(intent);
            }
        }
    }

    private void handleExit(){

        try {
            finish();
            try {
                connectedThread.join(700);
            }catch(InterruptedException e){
            e.printStackTrace();
            Log.d("DIFFICULTYACTIVITY ","INTERRUPTEDEXCEPTION " + e.getMessage());
            }
        if(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().isConnected() && connectedThread.isInterrupted()) {
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getInputStream().close();
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getOutputStream().close();
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().close();
                SingletonBluetoothSocket.getBluetoothSocketInstance().setMmSocket(null);
                Log.d("DifficultyActivity","Closing bluetooth socket");
            }
        } catch(IOException e) {
            e.printStackTrace();
            Log.d("DIFFICULTYACTIVITY ", "IOEXCEPTION " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("DifficultySecondUserActivity","onbackpressed");
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setMessage("Are you sure you want to exit?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                connectedThread.write(EXIT_KEY);

                // wait to make sure other device receives message
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Log.d("DifficultyActivity ", "Interrupted exception " + ie.getMessage());
                    ie.printStackTrace();
                }
               handleExit();
            }
        });
        newDialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();

            }
        });
        newDialog.show();
    }
}