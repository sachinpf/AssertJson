package org.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.testng.Reporter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    private static String outputDirectory = "output";
    private static int counter = 0;

    static{
        //creating directory
        File f = new File("target/test-classes", outputDirectory);
        f.mkdir();
        //updating for later use
        outputDirectory = "target/test-classes/" + outputDirectory;
    }

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static JsonObject readJsonObjectFile(String filePath) {
        URL url = FileUtils.class.getResource(filePath);
        try (FileReader fileReader = new FileReader(url.getPath())) {
            return gson.fromJson(fileReader, JsonObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return new JsonObject();
    }

    public static String readFile(String filePath) {
        URL url = FileUtils.class.getResource(filePath);
        try  {
            return Files.readString(Paths.get(url.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return "Unable to read a file.";
    }

    public static JsonArray readJsonArrayFile(String filePath) {
        URL url = FileUtils.class.getResource(filePath);
        try (FileReader fileReader = new FileReader(url.getPath())) {
            return gson.fromJson(fileReader, JsonArray.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return new JsonArray();
    }

    public static String writeToFile(String obj, String fileName) {
        if(fileName==null)
            fileName = outputDirectory + "/myFile_" + (++counter) + ".json";
        else
            fileName = outputDirectory + "/" + fileName;

        File myFile = new File(fileName);
        try {
            FileWriter fileWriter = new FileWriter(myFile);
            fileWriter.write(obj.toString());
            fileWriter.flush();
            fileWriter.close();
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return null;
    }


    public static String writeToFile(JsonObject obj, String fileName) {
        if(fileName==null)
            fileName = outputDirectory + "/jObjectFile_" + (++counter) + ".json";
        else
            fileName = outputDirectory + "/" + fileName;

        File myFile = new File(fileName);
        try {
            FileWriter fileWriter = new FileWriter(myFile);
            fileWriter.write(obj.toString());
            fileWriter.flush();
            fileWriter.close();
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return null;
    }


    public static String writeToFile(JsonArray obj, String fileName) {
        if(fileName==null)
            fileName = outputDirectory + "/jObjectFile_" + (++counter) + ".json";
        else
            fileName = outputDirectory + "/" + fileName;

        File myFile = new File(fileName);
        try {
            FileWriter fileWriter = new FileWriter(myFile);
            fileWriter.write(obj.toString());
            fileWriter.flush();
            fileWriter.close();
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
        return null;
    }


}
