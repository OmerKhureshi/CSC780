package com.drawmate.drawmate.UiFlow.Difficulty;

//import android.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.drawmate.drawmate.R;

public class MediumFragment extends Fragment{


    ListView listView;
    String[] mediumWords = {"phone", "laptop", "flower", "house", "hill", "banana"};

    public MediumFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.medium_fragment, container, false);
        listView = (ListView) rootView.findViewById(R.id.medium_fragment_list_view);;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_text_view, mediumWords);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < listView.getCount(); i++) {

                    TextView item = (TextView) listView.getChildAt(i);
                    item.setBackgroundColor(Color.parseColor("#66bb6a"));
                }
                view.setBackgroundColor(Color.parseColor("#47824A"));
            }
        });
        return rootView;
    }
}
