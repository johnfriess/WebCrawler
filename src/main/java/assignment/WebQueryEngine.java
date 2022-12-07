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

    //private constructor - use fromIndex()
    private WebQueryEngine(WebIndex i) {
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
        return new WebQueryEngine(index);
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the qery expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        try {
            stream = query.toLowerCase().trim();;
            Node root = parseQuery();
            //for implicit ands, keep processing until empty
            while(stream.length() > 0) {
                root = new Node("&", parseQuery(), root);
            }
            //see which pages fulfill the query
            ArrayList<Page> pages = new ArrayList<Page>();
            Set<Page> urls = index.getIndices().keySet();
            for(Page page : urls) {
                if(hasQuery(root, page))
                    pages.add(page);
            }
            return pages;
        } catch(Exception e) {
            System.err.println("Error parsing the query");
        }

        return new ArrayList<Page>();
    }

    //assume nodes with one children cant exist
    //assume that a string can't have ! as a character
    private boolean hasQuery(Node root, Page page) {
        if(root == null)
            return false;
        HashMap<Page, HashMap<String, ArrayList<Integer>>> urlWords = index.getWords();
        HashMap<String, ArrayList<Integer>> words = urlWords.get(page);
        HashMap<Page, HashMap<Integer, String>> urlIndices = index.getIndices();
        HashMap<Integer, String> indices = urlIndices.get(page);

        //only occurs when there is one query
        if(root.left == null || root.right == null) {
            if(root.value.indexOf("!") == 0)
                return notQuery(root, words);
            else if(root.value.indexOf("\"") == 0)
                return phraseQuery(root, words, indices);
            else
                return singleQuery(root, words);
        }
        //only occurs when there is an operator and its two children are queries
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
        //recursively call based on current node operator
        else {
            if(root.value.equals("&"))
                return hasQuery(root.left, page) && hasQuery(root.right, page);
            else
                return hasQuery(root.left, page) || hasQuery(root.right, page);
        }
    }

    //determisn if the next generation of a node are the leaves on the tree
    private boolean hasNextGenLeaves(Node root) {
        return root.left.left == null && root.left.right == null && root.right.left == null && root.right.right == null;
    }

    //determine if a single query exists by seeing if the value of the query is non-null
    private boolean singleQuery(Node root, HashMap<String, ArrayList<Integer>> words) {
        return words.get(root.value) != null;
    }

    //determine if a negation query exists by seeing if the value of the query is null
    private boolean notQuery(Node root, HashMap<String, ArrayList<Integer>> words) {
        return words.get(root.value.substring(1)) == null;
    }

    //determine if phrase query exists by starting at the first index of the phrase and checking if each subsequent word in the phrase is at the next index.
    private boolean phraseQuery(Node root, HashMap<String, ArrayList<Integer>> words, HashMap<Integer, String> indices) {
        boolean hasQuery = false;
        String[] phrases = root.value.substring(1,root.value.length()-1).split(" ");
        int phraseLocation = -1;
        if(words.get(phrases[0]) != null) {
            for(int i = 0; i < words.get(phrases[0]).size(); i++) {
                phraseLocation = words.get(phrases[0]).get(i);
                hasQuery = true;
                for(int j = 1; j < phrases.length; j++) {
                    if(indices.get(phraseLocation+1) != null && !indices.get(phraseLocation+1).equals(phrases[j]))
                        hasQuery = false;
                    phraseLocation++;
                }
                if(hasQuery)
                    return hasQuery;
            }
        }

        return hasQuery;
    }
 

    //identify next string token, removing any leading whitespace
    private String getToken() {
        if(stream.length() > 0) {
            //find first non-whitespace character
            String c = stream.substring(0, 1);
            stream = stream.substring(1);
            while(c.equals(" ")) {
                if(stream.length() > 0) {
                    c = stream.substring(0, 1);
                    stream = stream.substring(1);
                }
                else
                    return null;
            }

            if(isOperator(c))
                return c;
            else {
                //append characters to the phrase until it reaches whitespace or operator character
                String phrase = c;
                int i = 0;
                boolean reachedEnd = true;
                while(i < stream.length()) {
                    c = stream.substring(i, i+1);
                    if(c.equals(" ") || isOperator(c)) {
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

    //parse the tree according to the assignment handout
    private Node parseQuery() {
        String token = getToken();
        if(token != null) {
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
                    phrase += nextToken + " ";
                    nextToken = getToken();
                }
                return new Node(phrase.substring(0,phrase.length()-1) + nextToken);
            }
            else if(isWord(token)) {
                return new Node(token);
            }
            else {
                System.err.println("Parse error ocurred with " + token);
                return null;
            }
        }
        return null;
    }

    //determines if a given string is a word
    private boolean isWord(String word) {
        return !word.equals("&") && !word.equals("|") && !word.equals("(") && !word.equals(")") && !word.equals("!") && !word.equals(" ") && !word.equals("\"");
    }

    //determines if a given string is a operator
    private boolean isOperator(String operator) {
        return operator.equals("&") || operator.equals("|") || operator.equals("(") || operator.equals(")") || operator.equals("!") || operator.equals("\"");
    }

    //Node auxiliary class to store value and left and right subtrees
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
