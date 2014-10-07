package test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created with IntelliJ IDEA.
 * User: Linus
 * Date: 2014-07-21
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */
public class MockWriter{

    PrintWriter writer = null;
    private static int uniqueCounter = 0;
    private String fileName;

    public MockWriter() {
        try {
            fileName = "temp"+uniqueCounter++ +".txt";
            writer = new PrintWriter(fileName);

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }

    public PrintWriter getWriter(){

        return writer;
    }

    public String getOutput(){

        try {

            File file = new File(fileName);

            writer.flush(); // it may not have been flushed yet...
            String output = FileUtils.readFileToString(file, "UTF-8");
            file.delete();
            return output;

        } catch (IOException e) {

            e.printStackTrace();
            return "Could not read outcome from request";
        }
    }
}
