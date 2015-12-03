package com.drawsome.UiFlow.Difficulty;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.drawsome.bluetooth.ConnectedThreadSingleton;
import com.drawsome.drawing.DrawingActivity;

import java.util.ArrayList;
import java.util.List;

public class DifficultyActivity extends AppCompatActivity implements OnWordSelectListener{

    android.support.v4.view.ViewPager viewPager;
    TabLayout tabLayout;
    RelativeLayout layout;
    TextView titleTextView;
    String word;

    private ConnectedThread connectedThread = new ConnectedThread();


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
        ConnectedThreadSingleton.getConnectedThreadInstance().setConnectedThread(connectedThread);
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
        connectedThread.write(word);
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    public void onWordSelect(String wordSelected) {
        word = wordSelected;
        //sendWord(word);
        Log.v(this.getClass().toString(), "word selected " + word);

    }

//    public void sendWord(String wordSelected) {
//        ConnectedThread connectedThread = ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread();
//        connectedThread.write(word);
//    }
}
