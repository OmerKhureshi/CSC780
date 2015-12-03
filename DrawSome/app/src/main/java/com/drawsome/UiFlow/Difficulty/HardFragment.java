package com.drawsome.UiFlow.Difficulty;

import android.content.Context;
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

import com.drawsome.R;
import com.drawsome.database.WordBean;
import com.drawsome.database.WordsDBHelper;

import java.util.List;

public class HardFragment extends Fragment{

    ListView listView;
    String[] hardWords = {"phone", "laptop", "flower", "house", "hill", "banana"};
    OnWordSelectListener onWordSelectListener;

    public HardFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WordsDBHelper wordsDBHelper = new WordsDBHelper(getContext());
        List<WordBean> listWords = wordsDBHelper.getDifficultWords();
        hardWords = new String[listWords.size()];
        int index =0;
        for (WordBean bean: listWords) {
            hardWords[index] = bean.getWord();
            index++;
        }
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
                onWordSelectListener.onWordSelect(((TextView)listView.getChildAt(position)).getText().toString());
            }
        });
        return rootView;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onWordSelectListener = (OnWordSelectListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnWordSelectListener interface");
        }
    }
}
