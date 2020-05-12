package org.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.testng.Reporter;
import org.utils.FileUtils;

public class ReportSteps extends Reporter {

    public static void addINFO(String info) {
        Reporter.log("<BR>" + info);
    }

    public static void addJsonToReport(String filePath, String message) {
        Reporter.log("<a href=" + "\\JsonDeepEqual/" + filePath + ">" + message + "</a>");
    }

    public static void addJsonToReport(JsonObject obj, String message) {
        String f = FileUtils.writeToFile(obj,message);
        Reporter.log("<a href=" + "\\JsonDeepEqual/" + f + ">" + message + "</a>");
    }

    public static void addJsonToReport(JsonArray obj, String message) {
        String f = FileUtils.writeToFile(obj, message);
        Reporter.log("<a href=" + "\\JsonDeepEqual/" + f + ">" + message + "</a>");
    }


}
