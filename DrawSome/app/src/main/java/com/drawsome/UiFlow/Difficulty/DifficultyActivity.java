package com.drawsome.UiFlow.Difficulty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drawsome.R;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.SingletonBluetoothSocket;
import com.drawsome.drawing.DrawingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DifficultyActivity extends AppCompatActivity implements OnWordSelectListener{

    android.support.v4.view.ViewPager viewPager;
    TabLayout tabLayout;
    RelativeLayout layout;
    TextView titleTextView;
    String word;
    int level;
    private ConnectedThread connectedThread;
    private final String WORD_KEY = "wordToBeGuessed";
    private final String EXIT_KEY = "exitMessage";
    private final String GUESS_WORD_KEY = "wordToGuess";
    private final String SEPARATOR = ";";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.difficulty_activity);

        titleTextView = (TextView) findViewById(R.id.difficulty_activity_title_text);
        tabLayout = (TabLayout) findViewById(R.id.difficulty_activity_tab_layout);
        layout = (RelativeLayout) findViewById(R.id.difficulty_activity_rel_layout);
        layout.setBackgroundColor(Color.parseColor("#ef5350"));
        viewPager = (android.support.v4.view.ViewPager) findViewById(R.id.difficulty_activity_view_pager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    //tabLayout.setBackgroundColor(Color.parseColor("#ffff44"));
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ef5350"));
                    layout.setBackgroundColor(Color.parseColor("#ef5350"));
                    tabLayout.setTabTextColors(Color.parseColor("#E5E5E5"), Color.parseColor("#ef5350"));
                    titleTextView.setTextColor(Color.parseColor("#ef5350"));

                } else if (position == 1) {
                    //tabLayout.setBackgroundColor(Color.BLUE);
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#66bb6a"));
                    layout.setBackgroundColor(Color.parseColor("#66bb6a"));
                    tabLayout.setTabTextColors(Color.parseColor("#E5E5E5"), Color.parseColor("#66bb6a"));
                    titleTextView.setTextColor(Color.parseColor("#66bb6a"));

                } else if (position == 2) {
                    //tabLayout.setBackgroundColor(Color.MAGENTA);
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#9575cd"));
                    layout.setBackgroundColor(Color.parseColor("#9575cd"));
                    tabLayout.setTabTextColors(Color.parseColor("#E5E5E5"), Color.parseColor("#9575cd"));
                    titleTextView.setTextColor(Color.parseColor("#9575cd"));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.difficulty_activity_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
      //  ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);

    }

    @Override
    protected void onStart(){
        super.onStart();
        connectedThread = new ConnectedThread();
        Handler handler = new UIHandler();
        connectedThread.setHandler(handler);
        connectedThread.start();
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new EasyFragment(), "Easy");
        adapter.addFragment(new MediumFragment(), "Medium");
        adapter.addFragment(new HardFragment(), "Hard");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void startDrawingActivity(View view) {
        connectedThread.write(word+";"+level);
        try {
            Thread.sleep(500);
        }catch(InterruptedException ie){
             Log.d("DifficultyActivity","Interrupted exception " + ie.getMessage());
            ie.printStackTrace();
         }
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.putExtra(GUESS_WORD_KEY, word + SEPARATOR + level);
        startActivity(intent);
    }

    @Override
    public void onWordSelect(String wordSelected,int levelSelected) {
        word = wordSelected;
        level = levelSelected;
        //sendWord(word);
        Log.v(this.getClass().toString(), "word selected " + word);

    }

    @Override
    protected void onStop(){

        super.onStop();
        if(connectedThread != null)
            connectedThread.interrupt();
        Log.d("Thread interrupted ", "" + connectedThread.isInterrupted());
        connectedThread = null;
    }
//    public void sendWord(String wordSelected) {
//        ConnectedThread connectedThread = ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread();
//        connectedThread.write(word);
//    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setMessage("Are you sure you want to exit?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                connectedThread.write(EXIT_KEY);
                try{
                    Thread.sleep(500);
                }catch(InterruptedException ie){
                    Log.d("DifficlutyActivity ","Intrrupted excepion " + ie.getMessage());
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


   private void handleExit(){

        try {
            finish();

            connectedThread.join(500);
            if(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().isConnected() && connectedThread.isInterrupted()) {
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getInputStream().close();
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().getOutputStream().close();
                SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket().close();

                Log.d("DifficultyActivity","Closing bluetooth socket");
            }
        } catch(IOException e){
            e.printStackTrace();
            Log.d("DIFFICULTYACTIVITY ","IOEXCEPTION " + e.getMessage());
        } catch(InterruptedException e){
            e.printStackTrace();
            Log.d("DIFFICULTYACTIVITY ","INTERRUPTEDEXCEPTION " + e.getMessage());
        }

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
                }

            }
        }
    }
}
