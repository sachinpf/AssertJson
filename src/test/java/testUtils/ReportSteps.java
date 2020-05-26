package testUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.testng.Reporter;

public class ReportSteps extends Reporter {

    public static void addINFO(String info) {
        Reporter.log("<BR>" + info);
    }

    public static void addJsonToReport(String filePath, String message) {
        Reporter.log("<a href=" + "\\AssertJson/" + filePath + ">" + message + "</a>");
    }

    public static void addJsonToReport(JsonObject obj, String message) {
        String f = FileUtils.writeToFile(obj, null);
        Reporter.log("<a href=" + "\\AssertJson/" + f + ">" + message + "</a>");
    }

    public static void addJsonToReport(JsonArray obj, String message) {
        String f = FileUtils.writeToFile(obj, null);
        Reporter.log("<a href=" + "\\AssertJson/" + f + ">" + message + "</a>");
    }

}
