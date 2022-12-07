package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 *
 * TODO: Implement this!
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {
    List<URL> urls;
    WebIndex index;
    URL currentURL;
    boolean shouldIgnore;

    public CrawlingMarkupHandler() {
        urls = new ArrayList<URL>();
        index = new WebIndex();
        currentURL = null;
        shouldIgnore = false;
    }

    /**
    * This method returns the complete index that has been crawled thus far when called.
    */
    public Index getIndex() {
        return index;
    }

    /**
    * This method returns any new URLs found to the Crawler; upon being called, the set of new URLs
    * should be cleared.
    */
    public List<URL> newURLs() {
        List<URL> tempUrls = new ArrayList<URL>();
        for(int i = 0; i < urls.size(); i++) {
            tempUrls.add(urls.get(i));
        }

        urls = new ArrayList<URL>();
        return tempUrls;
    }

    //set the current url
    public void setURL(URL url) {
        currentURL = url;
        index.setURL(url);
    }

    /**
    * These are some of the methods from AbstractSimpleMarkupHandler.
    * All of its method implementations are NoOps, so we've added some things
    * to do; please remove all the extra printing before you turn in your code.
    *
    * Note: each of these methods defines a line and col param, but you probably
    * don't need those values. You can look at the documentation for the
    * superclass to see all of the handler methods.
    */

    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {}

    /**
    * Called when the parser finishes reading a document.
    * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
    * @param totalTimeNanos  the difference between current times at the start
    *                        and end of parsing
    * @param line            the line of the document where parsing ends
    * @param col             the column of the document where the parsing ends
    */
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {}

    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this element appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        if(elementName.equals("style") || elementName.equals("script"))
            shouldIgnore = true;
        if(elementName.equals("a") && attributes.containsKey("href")) {
            try {
                urls.add(new URL(currentURL, attributes.get("href")));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**s
    * Called at the end of any tag.
    * @param elementName the element name (such as "div").
    * @param line        the line in the document where this element appears.
    * @param col         the column in the document where this element appears.
    */
    public void handleCloseElement(String elementName, int line, int col) {
        if(elementName.equals("style") || elementName.equals("script"))
            shouldIgnore = false;
    }

    /**
    * Called whenever characters are found inside a tag. Note that the parser is not
    * required to return all characters in the tag in a single chunk. Whitespace is
    * also returned as characters.
    * @param ch      buffer containing characters; do not modify this buffer
    * @param start   location of 1st character in ch
    * @param length  number of characters in ch
    */
    public void handleText(char[] ch, int start, int length, int line, int col) {
        if(!shouldIgnore) {
            String word = "";
            for(int i = start; i < start + length; i++) {
                if(Character.isLetter(ch[i]) || Character.isDigit(ch[i]))
                    word += ch[i];
                else {
                    if(word.length() > 0) {
                        index.addWord(word.toLowerCase());
                        word = "";
                    }
                }
            }
            if(word.length() > 0)
                index.addWord(word.toLowerCase());
        }
    }
}
