package com.drawsome.database;

/**
 * Class to store information about the word such as word, difficulty level and hint.
 * Created by pooja on 11/12/2015.
 */
public class WordBean {
    private String word;
    private int level;
    private String hint;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
