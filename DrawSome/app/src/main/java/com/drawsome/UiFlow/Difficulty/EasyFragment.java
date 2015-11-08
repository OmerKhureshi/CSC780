package com.drawmate.drawmate.UiFlow.Difficulty;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.drawmate.drawmate.R;

public class EasyFragment extends Fragment {

    ListView listView;
    String[] easyWords = {"phone", "laptop", "flower", "house", "hill", "banana"};

    public EasyFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.easy_fragment, container, false);
        listView = (ListView) rootView.findViewById(R.id.easy_fragment_list_view);;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_text_view, easyWords);
        listView.setAdapter(arrayAdapter);

//        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                for (int i = 0; i < listView.getCount(); i++) {
//                    TextView item = (TextView) listView.getChildAt(i);
//                    item.setBackgroundColor(Color.parseColor("#ef5350"));
//                }
//                view.setBackgroundColor(Color.parseColor("#BF4240"));
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//
//        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < listView.getCount(); i++) {
                    TextView item = (TextView) listView.getChildAt(i);
                    item.setBackgroundColor(Color.parseColor("#ef5350"));
                }
                view.setBackgroundColor(Color.parseColor("#BF4240"));
            }
        });
        return rootView;
    }

}
