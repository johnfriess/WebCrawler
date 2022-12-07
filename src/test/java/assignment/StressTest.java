package assignment;

import java.io.BufferedWriter;
import java.io.*;

public class StressTest {
    public static void main(String[] args) throws Exception {
        generateHTML();
        String[] wcArgs = new String[1];
        wcArgs[0] = "./internet/0.html"; //path to 0.html
        WebCrawler.main(wcArgs);
        WebIndex index = (WebIndex) (Index.load("index.db"));
        WebQueryEngine wqe = WebQueryEngine.fromIndex(index);
        System.out.println(wqe.query("Blah").size());
    }

    public static void generateHTML() throws Exception {
        for(int i = 0; i < 10; i++) {
            System.out.println(i);
            File f = new File("src/test/java/assignment/internet/" + i + ".html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("<html><body><h1><a href=\""+ (i+1) + ".html\">Blah,</a> Blah!</h1>");
            bw.write("<textarea cols=75 rows=10>");
            for (int ii=0; ii<1; ii++) {
                bw.write("Blah blah..");
            }
            bw.write("</textarea>");
            bw.write("</body></html>");
            bw.close();
        }
    }
}
