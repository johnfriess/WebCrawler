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

    // TODO: Implement all of this! You may choose your own data structures an internal APIs.
    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).
    public WebIndex() {
        indices = new HashMap<Page, HashMap<Integer, String>>();
        words = new HashMap<Page, HashMap<String, ArrayList<Integer>>>();
        currentIndices = new HashMap<Integer, String>();
        currentWords = new HashMap<String, ArrayList<Integer>>();
        index = 0;
    }

    //make sure to add as lowercase
    public void addWord(String word) {
        currentIndices.put(index, word);
        if(currentWords.get(word) == null)
            currentWords.put(word, new ArrayList<Integer>());
        currentWords.get(word).add(index);
        index++;
    }

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

    public String toString() {
        Set<Page> keyset = words.keySet();
        String output = "";
        for(Page page : keyset) {
            output += words.get(page).get("scholar");
            output += "\n";
        }
        return output;
    }
    
}
