package org.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.planet.earth.jsonUtils.AssertJson;
import org.planet.earth.jsonUtils.JsonObjectAssertion;
import org.planet.earth.jsonUtils.exceptions.ExceedsLimitOfObjectSize;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.FileUtils;
import testUtils.ReportSteps;
import testUtils.testDataCreator.*;

import java.util.Arrays;

public class TestAssertJson {

    private JsonArray firstObject;
    private JsonArray secondObject;
    private String testFileName1 = "TestJsonArray_1.json";
    private String testFileName2 = "TestJsonArray_2.json";

    //creating test data
    @Test(testName = "Creating Test Data")
    public void createTestData() {
        ReportSteps.addINFO("-----------Test Data Creation-----------------");
        // Step 1: Creating test data
        ReportSteps.addINFO("Step 1: Creating test json array 1:");
        DataCreator testData1 = new DataCreator();
        testData1.createFirstTypeOfData(20, testFileName1);

        ReportSteps.addINFO("Step 2: Creating test json array 2: ");
        DataCreator testData2 = new DataCreator();
        testData2.createSecondTypeOfData(20, testFileName2);

        //reading and storing it to objects
        firstObject = FileUtils.readJsonArrayFile("/output/" + testFileName1);
        secondObject = FileUtils.readJsonArrayFile("/output/" + testFileName2);

        Assert.assertTrue(true);
    }

    //do not assert settings
   //@Test(testName = "Test doNotAssertKeys")
    public void assertDoNotAssertKey() {
        AssertJson aj = new AssertJson();
        aj.setFirstObjectName("SQL Result");
        aj.setSecondObjectName("Service Result");
        aj.setDoNotAssertKeys(Arrays.asList("Phone Number","test 1"));

        JsonObject firstObject = FileUtils.readJsonObjectFile("/sampleJsons/doNotAssertKeys1.json");
        JsonObject secondObject = FileUtils.readJsonObjectFile("/sampleJsons/doNotAssertKeys2.json");

        JsonObjectAssertion result;

        try{
            result = aj.assertJsonObject(firstObject, secondObject);
            String assertString = result.getMissingKeysInObject1() + result.getMissingKeysInObject2();
            Assert.assertTrue(assertString.equals("[.inventory info.out of stock]"));
            System.out.println("Test doNOtAssertKeys passed");
        }
        catch(ExceedsLimitOfObjectSize e){
            e.printStackTrace();
        }
    }

    @Test(testName = "Assert json object check", dependsOnMethods = {"createTestData"})
    public void assertJsonObjectCheck() {
        AssertJson aj = new AssertJson();
        aj.setFirstObjectName("SQL Result");
        aj.setSecondObjectName("Service Result");

        JsonObject o2 = secondObject.get(2).getAsJsonObject();
        JsonObject o1 = firstObject.get(2).getAsJsonObject();
        /*System.out.println(o1);
        System.out.println(o2);*/

        try {
            JsonObjectAssertion result;
            aj.setDoNotAssertKeys(Arrays.asList("whole sellername"));
            aj.setTransformBoolean(true);
            aj.setNormalizeDateFormat(true);
            result = aj.assertJsonObject(o1, o2);

            System.out.println(result.toString());
        } catch (ExceedsLimitOfObjectSize exceedsLimitOfObjectSize) {
            exceedsLimitOfObjectSize.printStackTrace();
        }

        //Assert.assertTrue(true);
    }

    /*@Test(testName = "Assert json array check", dependsOnMethods = {"assertJsonObjectCheck"})
    public void assertJsonArrayCheck() {
        System.out.println("\n\n ***** tbd");
    }*/

}
