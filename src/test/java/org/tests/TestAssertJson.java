package org.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sachinpf.json.utils.AssertJson;
import io.github.sachinpf.json.utils.JsonArrayAssertion;
import io.github.sachinpf.json.utils.JsonObjectAssertion;
import io.github.sachinpf.json.exceptions.ExceedsLimitOfObjectSize;
import io.github.sachinpf.json.exceptions.JsonArraySizeExceeds;
import io.github.sachinpf.json.exceptions.NoJsonObjectArray;
import io.github.sachinpf.json.exceptions.ZeroOrNullSizeJsonArray;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.FileUtils;
import testUtils.ReportSteps;
import testUtils.testDataCreator.*;

import java.util.Arrays;

public class TestAssertJson {

    private JsonArray firstArray;
    private JsonArray secondArray;
    private String testFileName1 = "TestJsonArray_1.json";
    private String testFileName2 = "TestJsonArray_2.json";

    //creating test data
    @Test(testName = "Creating Test Data")
    public void createTestData() {
        ReportSteps.addINFO("-----------Test Data Creation-----------------");
        // Step 1: Creating test data
        ReportSteps.addINFO("Step 1: Creating test json array 1:");
        DataCreator testData1 = new DataCreator();
        testData1.createFirstTypeOfData(13, testFileName1);
        ReportSteps.addINFO("Step 2: Creating test json array 2: ");
        DataCreator testData2 = new DataCreator();
        testData2.createSecondTypeOfData(10, testFileName2);
        //testData2.createSecondTypeOfData(20, testFileName2);

        //reading and storing it to objects
        firstArray = FileUtils.readJsonArrayFile("/output/" + testFileName1);
        secondArray = FileUtils.readJsonArrayFile("/output/" + testFileName2);

        Assert.assertTrue(true);
    }

    //do not assert settings
    //@Test(testName = "Test doNotAssertKeys")
    public void assertDoNotAssertKey() {
        AssertJson aj = new AssertJson();
        aj.setFirstObjectName("SQL Result");
        aj.setSecondObjectName("Service Result");
        aj.setDoNotAssertKeys(Arrays.asList("Phone Number", "test 1"));

        JsonObject firstObject = FileUtils.readJsonObjectFile("/sampleJsons/doNotAssertKeys1.json");
        JsonObject secondObject = FileUtils.readJsonObjectFile("/sampleJsons/doNotAssertKeys2.json");

        JsonObjectAssertion result;

        try {
            result = aj.assertJsonObject(firstObject, secondObject);
            String assertString = result.getMissingKeysInObject1() + result.getMissingKeysInObject2();
            Assert.assertTrue(assertString.equals("[.inventory info.out of stock]"));
            System.out.println("Test doNOtAssertKeys passed");
        } catch (ExceedsLimitOfObjectSize e) {
            e.printStackTrace();
        }
    }

    //@Test(testName = "Assert json object check", dependsOnMethods = {"createTestData"})
    public void assertJsonObjectCheck() {
        AssertJson aj = new AssertJson();
        aj.setFirstObjectName("SQL Result");
        aj.setSecondObjectName("Service Result");

        JsonObject o2 = secondArray.get(2).getAsJsonObject();
        JsonObject o1 = firstArray.get(2).getAsJsonObject();

        try {
            JsonObjectAssertion result;
            aj.setDoNotAssertKeys(Arrays.asList("whole sellername"));
            aj.setTransformBoolean(true);
            aj.setNormalizeDateFormat(true);
            aj.setNormalizeSpaces(true);
            aj.setIgnoreSeconds(true);
            result = aj.assertJsonObject(o1, o2);

            System.out.println(result.toString());
        } catch (ExceedsLimitOfObjectSize exceedsLimitOfObjectSize) {
            exceedsLimitOfObjectSize.printStackTrace();
        }

        //Assert.assertTrue(true);
    }

    @Test(testName = "Assert json array check", dependsOnMethods = {"createTestData"})
    public void assertJsonArrayCheck()
            throws ZeroOrNullSizeJsonArray, NoJsonObjectArray, JsonArraySizeExceeds, ExceedsLimitOfObjectSize {

        AssertJson aj = new AssertJson();
        aj.setFirstObjectName("SQL Result");
        aj.setSecondObjectName("Service Result");
        // aj.setAssertFirstXRows(5000);

        JsonArrayAssertion arrayAssertion = aj.assertJsonArray(firstArray, secondArray, "ID");

        ReportSteps.addINFO("assertJsonArrayCheck results: ");
        ReportSteps.addJsonToReport(arrayAssertion.getAsJsonObject(), "Array assertion results");
    }

}
