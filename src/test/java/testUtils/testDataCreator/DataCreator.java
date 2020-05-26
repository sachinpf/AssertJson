package testUtils.testDataCreator;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import testUtils.ReportSteps;
import testUtils.FileUtils;
import testUtils.testDataCreator.pojo.BookInfo;
import testUtils.testDataCreator.pojo.InventoryInfo;

public class DataCreator {

    private static Gson gson;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public void createFirstTypeOfData(long numOfRecords, String toBeCreatedFileName) {
        String sampleFileName = "/sampleJsons/SampleJsonArray_Small_1.json";//used as source to create data
        createData(sampleFileName, numOfRecords, true, toBeCreatedFileName);
    }

    public void createSecondTypeOfData(long numOfRecords, String toBeCreatedFileName) {
        String sampleFileName = "/sampleJsons/SampleJsonArray_Small_2.json"; //used as source to create data
        createData(sampleFileName, numOfRecords, false, toBeCreatedFileName);
    }

    private void createData(String sampleFileName, long numOfRecords, boolean first, String toBeCreatedFileName) {
        BookInfo bookInfo;
        InventoryInfo inventoryInfo;
        long i = 4;

        JsonArray testArray = FileUtils.readJsonArrayFile(sampleFileName);

        while (i <= numOfRecords) {
            JsonArray jArray = FileUtils.readJsonArrayFile(sampleFileName);
            JsonObject jsonObject1 = jArray.get(0).getAsJsonObject();
            JsonObject jsonObject2 = jArray.get(1).getAsJsonObject();

            //adding 3rd Object
            {
                bookInfo = new BookInfo(first);
                if (first) {
                    jsonObject1.addProperty("ID", i);
                    jsonObject1.add("BookInfo", bookInfo.toJsonObject());

                } else {
                    //                    jsonObject1.addProperty("Book ID", i);
                    jsonObject1.addProperty("ID", i);
                    jsonObject1.add("Book Info", bookInfo.toJsonObject());

                }

                inventoryInfo = new InventoryInfo(true);
                jsonObject1.add("InventoryInfo", inventoryInfo.toJsonObject());
                testArray.add(jsonObject1);
                i++;
            }

            //adding 4th Object
            {
                bookInfo = new BookInfo(first);
                if (first) {
                    jsonObject2.addProperty("ID", i);
                    jsonObject2.add("BookInfo", bookInfo.toJsonObject());

                } else {
//                    jsonObject2.addProperty("Book ID", i);
                    jsonObject2.addProperty("ID", i);
                    jsonObject2.add("Book Info", bookInfo.toJsonObject());
                }

                inventoryInfo = new InventoryInfo(true);
                jsonObject2.add("InventoryInfo", inventoryInfo.toJsonObject());
                testArray.add(jsonObject2);
                i++;
            }

            JsonArray jArray2 = FileUtils.readJsonArrayFile(sampleFileName);
            JsonObject jsonObject3 = jArray2.get(0).getAsJsonObject();
            JsonObject jsonObject4 = jArray2.get(1).getAsJsonObject();

            //adding 5th Object
            {
                if (first) {
                    jsonObject3.addProperty("ID", i);
                } else {
//                    jsonObject3.addProperty("Book ID", i);
                    jsonObject3.addProperty("ID", i);
                }
                testArray.add(jsonObject3);
                i++;
            }

            //adding 6th Object
            {
                if (first)
                    jsonObject4.addProperty("ID", i);
                else {
                  //  jsonObject4.addProperty("Book ID", i);
                    jsonObject4.addProperty("ID", i);

                }
                testArray.add(jsonObject4);
                i++;
            }
        }

        ReportSteps.log("\n Size of test data: " + String.valueOf(testArray.size()));
        //writing to JsonArrayFile in target
        String testFileName = FileUtils.writeToFile(testArray, toBeCreatedFileName);
        ReportSteps.addJsonToReport(testFileName, testFileName);
    }

}
