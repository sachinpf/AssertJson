package org.tests;

import com.google.gson.JsonObject;
import org.planet.earth.jsonUtils.support.Flatten;
import org.testng.Assert;
import org.testng.annotations.Test;
import testUtils.FileUtils;
import testUtils.ReportSteps;

public class TestJsonFlattening {
    @Test(testName = "Test Json Flattening")
    public void testFlattenJsonObject() {
        ReportSteps.addINFO("Test 1: Test Flatten JsonObject: ");
        JsonObject actualObject = FileUtils.readJsonObjectFile("/sampleJsons/testFlattened_before.json");
        ReportSteps.addJsonToReport(actualObject, "Before Flatten");
        try {
            Flatten toBeFlattenObject = new Flatten(25,
                    true,
                    true,
                    null);
            JsonObject flattenedObject = toBeFlattenObject.getFlattenedObject(actualObject);
            ReportSteps.addJsonToReport(flattenedObject, "After Flatten");
            JsonObject expectedObject = FileUtils.readJsonObjectFile("/sampleJsons/testFlattened_after.json");
            boolean status = flattenedObject.equals(expectedObject);
            ReportSteps.addINFO("Flattening json object passed? " + status);
            Assert.assertTrue(status, "Flattening json object passed?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
