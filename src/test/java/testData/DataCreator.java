package testData;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.utils.FileUtils;
import testData.pojo.BookInfo;

import java.net.URL;

public class DataCreator {

    private BookInfo bookInfo;
    private URL testFileURL;
    private static Gson gson;
    private String testFileName;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public DataCreator(long numOfRecords, String fileName) {
        /*JsonArray testArray = JsonFileUtils.readJsonArrayFile("/sampleJsons/SampleJsonArray_Small.json");
        System.out.println(testArray);*/

        String text = FileUtils.readFile("/sampleJsons/SampleJsonArray_Small.json");
        //System.out.println(text);
        FileUtils.writeToFile(text,"myfile.html");
        String text2 = FileUtils.readFile("/output/myfile.html");
        System.out.println(text2);

        /*BookInfo bookInfo;
        InventoryInfo inventoryInfo;
        long target = (numOfRecords / 4) + 2;
        long i = 3;

        while (i <= target) {
            JsonArray jArray = JsonFileUtils.readJsonArrayFile("/sampleJsons/SampleJsonArray_Small.json");
            JsonObject jsonObject1 = jArray.get(0).getAsJsonObject();
            JsonObject jsonObject2 = jArray.get(1).getAsJsonObject();

            //adding 3rd Object
            {
                jsonObject1.addProperty("ID", i);
                bookInfo = new BookInfo(true);
                jsonObject1.add("BookInfo", bookInfo.toJsonObject());
                inventoryInfo = new InventoryInfo(true);
                jsonObject1.add("InventoryInfo", inventoryInfo.toJsonObject());
                testArray.add(jsonObject1);
                i++;
            }

            //adding 4th Object
            {
                jsonObject2.addProperty("ID", i);
                bookInfo = new BookInfo(true);
                jsonObject2.add("BookInfo", bookInfo.toJsonObject());
                inventoryInfo = new InventoryInfo(true);
                jsonObject2.add("InventoryInfo", inventoryInfo.toJsonObject());
                testArray.add(jsonObject2);
                i++;
            }

            JsonArray jArray2 = JsonFileUtils.readJsonArrayFile("/sampleJsons/SampleJsonArray_Small.json");
            JsonObject jsonObject3 = jArray2.get(0).getAsJsonObject();
            JsonObject jsonObject4 = jArray2.get(1).getAsJsonObject();

            //adding 5th Object
            {
                jsonObject3.addProperty("ID", i);
                testArray.add(jsonObject3);
                i++;
            }

            //adding 6th Object
            {
                jsonObject4.addProperty("ID", i);
                testArray.add(jsonObject4);
                i++;
            }
        }

        ReportSteps.log("\n Size of test data: " + String.valueOf(testArray.size()));
        //writing to JsonArrayFile in target
        testFileName = JsonFileUtils.writeToFile(testArray, fileName);
        ReportSteps.addJsonToReport(testFileName, testFileName);*/
    }

    public String getTestFileName() {
        return testFileName;
    }
}
