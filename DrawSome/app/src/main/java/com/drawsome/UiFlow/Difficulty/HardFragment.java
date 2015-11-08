package com.drawmate.drawmate.UiFlow.Difficulty;

//import android.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.drawmate.drawmate.R;

public class HardFragment extends Fragment{

    ListView listView;
    String[] hardWords = {"phone", "laptop", "flower", "house", "hill", "banana"};

    public HardFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.hard_fragment, container, false);
        listView = (ListView) rootView.findViewById(R.id.hard_fragment_list_view);;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_text_view, hardWords);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < listView.getCount(); i++) {

                    TextView item = (TextView) listView.getChildAt(i);
                    item.setBackgroundColor(Color.parseColor("#9575cd"));
                }
                view.setBackgroundColor(Color.parseColor("#68518F"));
            }
        });
        return rootView;
    }
}
