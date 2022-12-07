package assignment;


import java.net.*;
import java.util.*;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 * TODO: Implement this!
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;
    private HashMap<Page, HashMap<Integer, String>> indices;
    private HashMap<Page, HashMap<String, ArrayList<Integer>>> words;
    private HashMap<Integer, String> currentIndices;
    private HashMap<String, ArrayList<Integer>> currentWords;
    private int index;

    public WebIndex() {
        indices = new HashMap<Page, HashMap<Integer, String>>();
        words = new HashMap<Page, HashMap<String, ArrayList<Integer>>>();
        currentIndices = new HashMap<Integer, String>();
        currentWords = new HashMap<String, ArrayList<Integer>>();
        index = 0;
    }

    //add word to the current hashmaps and increment the current index
    public void addWord(String word) {
        currentIndices.put(index, word);
        if(currentWords.get(word) == null)
            currentWords.put(word, new ArrayList<Integer>());
        currentWords.get(word).add(index);
        index++;
    }

    //create page object and add it to the general hashmaps (sets up the environment for adding words)
    public void setURL(URL url) {
        Page p = new Page(url);
        index = 0;
        currentIndices = new HashMap<Integer, String>();
        currentWords = new HashMap<String, ArrayList<Integer>>();
        indices.put(p, currentIndices);
        words.put(p, currentWords);
    }

    public HashMap<Page, HashMap<Integer, String>> getIndices() {
        return indices;
    }

    public HashMap<Page, HashMap<String, ArrayList<Integer>>> getWords() {
        return words;
    }
}
