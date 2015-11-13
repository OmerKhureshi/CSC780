package com.drawsome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to perform Database CRUD operations. On initialization, it reads words from words.txt file
 * and stores them in database.
 * It also Provides read words by difficulty level and returns random words of the provided difficulty level.
 * Created by pooja on 11/12/2015.

 example code:
 WordsDBHelper wordsDBHelper = new WordsDBHelper(this);
 List<WordBean> wordList = wordsDBHelper.getEasyWords();
 for(WordBean wordBean : wordList) {
 System.out.println("word " + wordBean.getWord());
 }

 */
public class WordsDBHelper extends SQLiteOpenHelper {

    private Context context;
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "pictionary_words.db";
    private static final String TABLE_NAME = "WORDS";
    private static final String COLUMN_WORD = "WORD";
    private static final String COLUMN_LEVEL = "LEVEL";
    private static final String COLUMN_HINT = "HINT";
    private static final String LIMIT = "5";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_WORD + " TEXT PRIMARY KEY, "
                    + COLUMN_LEVEL +" INTEGER, " + COLUMN_HINT + " TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public WordsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        this.context = context;
    }
    public void onCreate(SQLiteDatabase db) {
        Log.d("dbhelper ","oncreate called!");
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
        readFile();
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addWordsToDatabase(String word,int level,String hint) {
        SQLiteDatabase db = getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_LEVEL, level);
        values.put(COLUMN_HINT, hint);

         db.insert(TABLE_NAME, COLUMN_HINT, values);
    }

    /*
     * returns  5 random words with difficulty level: easy(1) from database.
     * @return list containing words.
     */
    public List<WordBean> getEasyWords() {
      return getWordsByDifficultyLevel(1);
    }

    /*
     * returns  5 random words with difficulty level: medium(2) from database.
     * @return list containing words.
     */
    public List<WordBean> getMediumWords() {
      return getWordsByDifficultyLevel(2);
    }

    /*
     * returns  5 random words with difficulty level: difficult(3) from database.
     * @return list containing words.
     */
    public List<WordBean> getDifficultWords() {
      return getWordsByDifficultyLevel(3);
    }

    /*
     * executes select query on database and return words with the difficulty level set by parameter
     * @param level difficulty level : 1 (easy) 2 (medium) 3 (difficult)
     */
    private List<WordBean> getWordsByDifficultyLevel(int level) {
        SQLiteDatabase db = getReadableDatabase();
        List<WordBean> wordList = new ArrayList<WordBean>();
        // ORDER BY RANDOM() LIMIT 1
        Cursor cursor =  db.query(TABLE_NAME, null, COLUMN_LEVEL + "=?",
                new String[]{String.valueOf(level)},null, null, "RANDOM()", LIMIT);

        Log.d("Retrieving ", " count " + cursor.getCount());
        if (cursor.moveToFirst()){
            do{
                String word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD));
                String hint = cursor.getString(cursor.getColumnIndex(COLUMN_HINT));
                Log.d("Retrieved word ", word + " " + hint);
                WordBean wordBean = new WordBean();
                wordBean.setWord(word);
                wordBean.setLevel(level);
                wordBean.setHint(hint);
                wordList.add(wordBean);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return wordList;
    }

    /*
     * Reads file words.txt and loads the words into database
     * Called only once by onCreate() on initialization.
     */
    private void readFile (){
        Log.d("dbhelper " , "reading file");


        String file = "words.txt";
        BufferedReader br = null;
        String line = "";
        String splitBy = ",";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("words.txt")));

            // do reading, usually loop until end of file reading

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                String[] list = line.split(splitBy);
                String word = list[0];
                int level = Integer.parseInt(list[1]);
                String hint = null;
                if(list.length == 3)
                    hint = list[2];
                Log.d("dbhelper ","word " + word + "level " + level + "hint " + hint);
                //       System.out.println();
                addWordsToDatabase(word,level,hint);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
    }

}
