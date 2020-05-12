package testCases;

import org.helpers.ReportSteps;
import org.testng.Reporter;
import org.testng.annotations.Test;
import testData.DataCreator;

public class TestSuite1 {

    @Test
    public void compareJsonArray() {
        Reporter.log("**Starting test");
        // Step 1: Creating test data
        ReportSteps.addINFO("Step 1: Creating test json array 1:");
        DataCreator testData1, testData2;
        testData1 = new DataCreator(20, "TestJsonArray_1.json");

        /*ReportSteps.addINFO("Step 2: Creating test json array 2: ");
        testData2 = new DataCreator(20, "TestJsonArray_2.json");
*/
    }
}
