package assignment;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 * TODO: Implement this!
 */
public class WebQueryEngine {
    WebIndex index;
    String stream;

    public WebQueryEngine(WebIndex i) {
        index = i;
        stream = null;
    }

    /**
     * Returns a WebQueryEngine that uses the given Index to construct answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */
    public static WebQueryEngine fromIndex(WebIndex index) {
        // TODO: Implement this!
        return new WebQueryEngine(index);
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        // TODO: Implement this!
        ArrayList<Page> pages = new ArrayList<Page>();
        stream = query.toLowerCase().trim();;
        System.out.println(stream);
        Node root = parseQuery();
        while(stream.length() > 0) {
            root = new Node("&", parseQuery(), root);
        }
        //add all the pages to collection
        Set<Page> urls = index.getIndices().keySet();
        for(Page page : urls) {
            if(hasQuery(root, page)) {
                pages.add(page);
                System.out.println(page.getURL());
            }
        }
        print2D(root);
        System.out.println(pages.size());
        return pages;
    }

    /*private void addExplicitAnds() {
        stream = stream.replaceAll("\\s+", " ");
        boolean inBracket = false;
        int andCount = 0;
        int quoteCount = 0;
        for(int i = 0; i < stream.length(); i++) {
            if(stream.charAt(i) == ' ' && !inBracket) {
                stream = stream.substring(0, i) + " & " + stream.substring(i+1);
                i += 2;
                andCount++;
            }
            
            if(stream.charAt(i) == '\"')
                quoteCount++;
            
            if(stream.charAt(i) == '(' || quoteCount % 2 == 1) {
                inBracket = true;
                System.out.println("true: " + stream);
                System.out.println("index: " + i);
            }

            if(stream.charAt(i) == ')' || (quoteCount != 0 && quoteCount % 2 == 0)) {
                inBracket = false;
                System.out.println("false: " + stream);
                System.out.println("index: " + i);
            }
        }

        //add before parentheses
        for(int i = 0; i < andCount; i++) {
            stream = "(" + stream;
        }

        System.out.println(stream);
        //add after parentheses
        int parenthesesIndex = nextSpace(stream, stream.indexOf("&") + 2);
        for(int i = 0; i < andCount; i++) {
            if(parenthesesIndex > 0) {
                stream = stream.substring(0, parenthesesIndex) + ")" + stream.substring(parenthesesIndex);
                parenthesesIndex = nextSpace(stream, parenthesesIndex + 4);
                System.out.println("stream " + stream);
            }
            else {
                stream += ")";
            }
        }
        System.out.println(stream);
    }

    private int nextSpace(String s, int index) {
        for(int i = index; i < s.length(); i++) {
            if(s.charAt(i) == ' ')
                return i;
        }
        return -1;
    }*/

    private void print2D(Node root)
    {
        print2DUtil(root, 0);
    }

    private void print2DUtil(Node root, int space)
    {
        if (root == null)
            return;
        space += 10;
        print2DUtil(root.right, space);
        System.out.print("\n");
        for (int i = 10; i < space; i++)
            System.out.print(" ");
        System.out.print(root.value + "\n");
        print2DUtil(root.left, space);
    }

    //assume nodes with one children cant exist
    //assume that a string can't have ! as a character
    private boolean hasQuery(Node root, Page page) {
        //only occurs with single word query
        HashMap<Page, HashMap<String, ArrayList<Integer>>> urlWords = index.getWords();
        HashMap<String, ArrayList<Integer>> words = urlWords.get(page);
        HashMap<Page, HashMap<Integer, String>> urlIndices = index.getIndices();
        HashMap<Integer, String> indices = urlIndices.get(page);
        if(root.left == null || root.right == null) {
            if(root.value.indexOf("!") == 0)
                return notQuery(root, words);
            else if(root.value.indexOf("\"") == 0)
                return phraseQuery(root, words, indices);
            else
                return singleQuery(root, words);
        }
        else if(hasNextGenLeaves(root)) {
            boolean hasFirstQuery;
            boolean hasSecondQuery;

            //process left child
            if(root.left.value.indexOf("!") == 0)
                hasFirstQuery = notQuery(root.left, words);
            else if(root.left.value.indexOf("\"") == 0)
                hasFirstQuery = phraseQuery(root.left, words, indices);
            else
                hasFirstQuery = singleQuery(root.left, words);

            //process right child
            if(root.right.value.indexOf("!") == 0) 
                hasSecondQuery = notQuery(root.right, words);
            else if(root.right.value.indexOf("\"") == 0)
                hasSecondQuery = phraseQuery(root.right, words, indices);
            else
                hasSecondQuery = singleQuery(root.right, words);
            
            if(root.value.equals("&"))
                return hasFirstQuery && hasSecondQuery;
            else
                return hasFirstQuery || hasSecondQuery;
        }
        else {
            if(root.value.equals("&"))
                return hasQuery(root.left, page) && hasQuery(root.right, page);
            else
                return hasQuery(root.left, page) || hasQuery(root.right, page);
        }
    }

    private boolean hasNextGenLeaves(Node root) {
        return root.left.left == null && root.left.right == null && root.right.left == null && root.right.right == null;
    }

    private boolean singleQuery(Node root, HashMap<String, ArrayList<Integer>> words) {
        boolean hasQuery = false;
        if(words.get(root.value) != null)
            hasQuery = true;
        return hasQuery;
    }

    private boolean notQuery(Node root, HashMap<String, ArrayList<Integer>> words) {
        boolean hasQuery = true;
        if(words.get(root.value.substring(1)) != null)
            hasQuery = false;
        return hasQuery;
    }

    private boolean phraseQuery(Node root, HashMap<String, ArrayList<Integer>> words, HashMap<Integer, String> indices) {
        boolean hasQuery = false;
        String[] phrases = root.value.substring(1,root.value.length()-1).split(" ");
        int phraseLocation = -1;
        if(words.get(phrases[0]) != null) {
            for(int i = 0; i < words.get(phrases[0]).size(); i++) {
                phraseLocation = words.get(phrases[0]).get(i);
                hasQuery = true;
                for(int j = 1; j < phrases.length; j++) {
                    if(!indices.get(phraseLocation+1).equals(phrases[j]))
                        hasQuery = false;
                    phraseLocation++;
                }
                if(hasQuery)
                    return hasQuery;
            }
        }

        return hasQuery;
    }
 

    //MAKE SURE ALL LOWERCASE
    private String getToken() {
        if(stream.length() != 0) {
            String c = stream.substring(0, 1);
            stream = stream.substring(1);
            while(c.equals(" ")) {
                c = stream.substring(0, 1);
                stream = stream.substring(1);
            }
            if (c.equals("&") || c.equals("|") || c.equals("(") || c.equals(")") || c.equals("!") || c.equals("\""))
                return c;
            else {
                String phrase = c;
                int i = 0;
                boolean reachedEnd = true;
                while(i < stream.length()) {
                    c = stream.substring(i, i+1);
                    if(c.equals(" ") || c.equals("&") || c.equals("|" ) || c.equals("(") || c.equals(")") || c.equals("!") || c.equals("\"")) {
                        stream = stream.substring(i);
                        reachedEnd = false;
                        break;
                    }
                    else {
                        phrase += c;
                        i++;
                    }
                }
  
                if(reachedEnd)
                    stream = "";

                return phrase;
            }
        }
        return null;
    }

    //assumption with grammar rules / gettoken must return operator
    private Node parseQuery() {
        String token = getToken();
        System.out.println(token);
        if(token.equals("(")) {
            Node left = parseQuery();
            String op = getToken();
            Node right = parseQuery();
            getToken(); //clear right parentheses
            return new Node(op, left, right);
        }
        else if(token.equals("!")) {
            String nextToken = getToken();
            return new Node(token+nextToken);
        }
        else if(token.equals("\"")) {
            String nextToken = getToken();
            String phrase = token;
            while(!nextToken.equals("\"")) {
                System.out.println(nextToken);
                phrase += nextToken + " ";
                nextToken = getToken();
            }
            return new Node(phrase.substring(0,phrase.length()-1) + nextToken);
        }
        else if(isWord(token) ) {
            return new Node(token);
        }
        else {
            System.err.println("Parse error ocurred with " + token);
            return null;
        }
    }

    private boolean isWord(String word) {
        return !word.equals("&") && !word.equals("|") && !word.equals("(") && !word.equals(")") && !word.equals("!") && !word.equals(" ") && !word.equals("\"");
    }

    private class Node {
        String value;
        Node left;
        Node right;
        
        public Node(String v) {
            value = v;
            left = null;
            right = null;
        }

        public Node(String v, Node l, Node r) {
            value = v;
            left = l;
            right = r;
        }
    }
}
