package org.planet.Json.jsonUtils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.planet.Json.jsonUtils.exceptions.ExceedsLimitOfObjectSize;
import org.planet.Json.jsonUtils.exceptions.JsonArraySizeExceeds;
import org.planet.Json.jsonUtils.exceptions.NoJsonObjectArray;
import org.planet.Json.jsonUtils.exceptions.ZeroOrNullSizeJsonArray;
import org.planet.Json.jsonUtils.support.DateObject;
import org.planet.Json.jsonUtils.support.Flatten;
import org.planet.Json.jsonUtils.support.KeyIndexCallable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class AssertJson {
    /*
     * Use Cases to be implemented:
     * 1. Deep Assert Two Json Objects
     * 2. Deep Assert Two Json Arrays by passed in ID
     * 3. Ability to configure items / transformation
     * 4. Ability to exclude certain keys during assertions
     *
     * Config Items takes place only in case of values don't match:
     * 1. Normalize Date Format (normalizeDateFormat)
     *      //formats like 2019 April 10 and 2019/04/10 will be treated equal, only if two objects are unequal
     * 2. Max Json Object Key Limit (maxSizeOfKeysInObject)
     *      // If the keys in object exceed 50, it will throw an error.
     * 4. Max Assertion Limit (maxFailureCount) -
     *      //applicable only for JsonArray assertion
     * 5. Sort Arrays inside JsonObjects (sortArraysBeforeAssertion)
     *      //Sorts String/Number arrays inside json object, not applicable to JsonArray
     * 6. Convert String Objects with square brackets in double quotes to Array -
     *      //(arrayWithSquaresInQuotes) i.e. "[2,3,4]"
     * 7. String normalize spaces e.g. " one    two three   " should be "one two three" (normalizeSpaces)
     *     // helps in case of legacy databases
     * 8. Ignore certain keys (doNotAssertKeys)
     *      // excludes keys from assertion
     * 9. String to Boolean -  (transformBoolean)
     *      //"true" or "false" to true/false
     * 10. Consider String values like Y/N,1,0  as True/False given other object has it as true/false (transformBoolean)
     * 11. Case sensitive; //caseSensitive
     * */

    private int maxSizeOfKeysInObject;
    private boolean convertArrayWithSquaresInQuotes; //#6
    private boolean normalizeSpaces;

    private List<String> doNotAssertKeys;

    private int assertFirstXRows; //applicable for Json Array, iterates only first X numbers from firstArray
    private boolean normalizeDateFormat;
    private boolean transformBoolean; //#9 and #10
    private boolean caseSensitive;//#11

    private String firstObjectName;
    private String secondObjectName;
    private boolean ignoreSeconds;

    private Gson gson;

    public AssertJson() {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        //flattening default
        maxSizeOfKeysInObject = 50;
        convertArrayWithSquaresInQuotes = false;
        normalizeSpaces = false;
        doNotAssertKeys = null;

        //JsonObject assertion
        caseSensitive = false;

        //JsonArray assertion
        assertFirstXRows = 1000;

        //depending on other object
        transformBoolean = false;
        normalizeDateFormat = false;

        //
        firstObjectName = "firstObject";
        secondObjectName = "secondObject";
    }

    //method to assert Two Json Arrays by given ID
    //just pass in the IdKeyName
    public JsonArrayAssertion assertJsonArray(JsonArray firstArray,
                                              JsonArray secondArray,
                                              String IdKeyName)
            throws ZeroOrNullSizeJsonArray, NoJsonObjectArray, JsonArraySizeExceeds {

        //Step 1: ensuring none of the arrays are null or size zero
        if (firstArray == null || secondArray == null || firstArray.size() <= 0 || secondArray.size() <= 0)
            throw new ZeroOrNullSizeJsonArray();

        //Step 2: ensuring it's JsonObject array
        if (!firstArray.get(0).isJsonObject() || !secondArray.get(0).isJsonObject())
            throw new NoJsonObjectArray();

        //Step 3: if the size difference is greater than 500
        if (firstArray.size() - secondArray.size() >= 500)
            throw new JsonArraySizeExceeds();

        //Step 4: creating ID for each element in SecondArray
        Map<Object, Integer> indexForSecondArray = getKeyIndices(IdKeyName, secondArray, IdKeyName);

        //Step 5: asserting two json objects within JsonArray
        //List<JsonObjectAssertion> arrayAssertions = new ArrayList<>();

        JsonArrayAssertion arrayAssertions = new JsonArrayAssertion();

        //asserting now
        int maxSize = Math.min(firstArray.size(), assertFirstXRows);
        //Note: by default asserts only first 1000 rows from first Array.
        //this limit can be changed using setAssertFirstXRows
        StreamSupport.stream(firstArray.spliterator(), true)
                .parallel()
                .limit(maxSize)
                .forEach(o ->
                {
                    JsonObject obj1 = o.getAsJsonObject();
                    Object id = gson.fromJson(obj1.get(IdKeyName), Object.class);
                    JsonObject obj2;
                    try {
                        if (!indexForSecondArray.containsKey(id)) {
                            arrayAssertions.addMissingIDs(id);
                        } else {
                            obj2 = secondArray.get(
                                    indexForSecondArray.get(id)).getAsJsonObject();
                            arrayAssertions.addJsonObjectAssertions(assertJsonObject(obj1, obj2, id));
                        }
                    } catch (ExceedsLimitOfObjectSize exceedsLimitOfObjectSize) {
                        exceedsLimitOfObjectSize.printStackTrace();
                    }
                });

        //setting message & other attributes
        arrayAssertions.setFirstArraySize(firstArray.size());
        arrayAssertions.setSecondArraySize(secondArray.size());
        arrayAssertions.setStatusMessage("Asserted first " + maxSize + " objects from first json array with only matching objects by ID in second json array.");
        return arrayAssertions.getFailedOnly();
    }

    //All private methods for JsonArray assertion
    //method to locate object by ID
    private Map<Object, Integer> getKeyIndices(Object id, JsonArray secondArray, String IdKeyName) {

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        //Step 1 - Finding splitSize based on available available processors
        int arraySize = secondArray.size();
        int eachSplitMax;
        boolean oddSize = arraySize % 2 == 0;
        if (oddSize)
            eachSplitMax = 1 + (arraySize / availableProcessors);
        else
            eachSplitMax = (arraySize / availableProcessors);

        //Step 2 - Starting Threads and creating indices for each given key
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
        Map<Object, Integer> returnMap = new HashMap<>();

        int startNumber = 0;
        int endNumber = eachSplitMax;
        for (int i = 0; i < availableProcessors; i++) {
            try {
                //System.out.println("now i: " + i);
                Callable<Map<Object, Integer>> callable =
                        new KeyIndexCallable(startNumber, endNumber, secondArray, IdKeyName);
                Future<Map<Object, Integer>> future = executorService.submit(callable);
                startNumber = endNumber + 1;
                endNumber += eachSplitMax + 1;
                returnMap.putAll(future.get());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return returnMap;
    }


    //method to assert Two Json Objects
    public JsonObjectAssertion assertJsonObject(JsonObject firstObject,
                                                JsonObject secondObject) throws ExceedsLimitOfObjectSize {
        return assertJsonObject(firstObject, secondObject, null);
    }


    //All private methods for JsonObject assertion
    private JsonObjectAssertion assertJsonObject(JsonObject firstObject,
                                                 JsonObject secondObject, Object ID) throws ExceedsLimitOfObjectSize {
        //Object that stores mismatches
        JsonObjectAssertion assertion = new JsonObjectAssertion();

        try {
            //Object that stores assertion results
            assertion.setObject1Name(firstObjectName);
            assertion.setObject2Name(secondObjectName);
            assertion.setID(ID);

            if (!firstObject.equals(secondObject)) { //asserting now based on configuration
                JsonObject tbaObject1;
                JsonObject tbaObject2;
                List<String> keySet1;
                List<String> keySet2;

                //Step 1 - flattening the objects
                {
                    tbaObject1 = new Flatten(maxSizeOfKeysInObject,
                            convertArrayWithSquaresInQuotes,
                            normalizeSpaces,
                            doNotAssertKeys)
                            .getFlattenedObject(firstObject);

                    tbaObject2 = new Flatten(maxSizeOfKeysInObject,
                            convertArrayWithSquaresInQuotes,
                            normalizeSpaces,
                            doNotAssertKeys)
                            .getFlattenedObject(secondObject);
                }//step 1 ends

                //Step 2 - Finding missing keys
                {
                    keySet1 = new ArrayList<>(tbaObject1.keySet());
                    keySet2 = new ArrayList<>(tbaObject2.keySet());
                    List<String> keySetAnother1 = new ArrayList<>(keySet1);
                    keySet1.removeAll(keySet2);
                    keySet2.removeAll(keySetAnother1);
                    //adding missing keys from other objects, ensure keyset1 goes to object 2 and keyset2 gotes to object1
                    if (keySet1.size() > 0)
                        assertion.addMissingKeysInObject2(keySet1.toString());
                    if (keySet2.size() > 0)
                        assertion.addMissingKeysInObject1(keySet2.toString());
                }//step 2 ends

                //Step 3 - Finding unequal values for common keys between two objects
                {
                    List<String> commonKeySet = new ArrayList<>(tbaObject1.keySet());
                    commonKeySet.retainAll(tbaObject2.keySet());
                    //System.out.println("commonKeySet: " + commonKeySet);

                    List<JsonObject> differences =
                            commonKeySet.stream().parallel()
                                    .map(currentKey ->
                                            especialAssertEqual(currentKey,
                                                    tbaObject1.get(currentKey),
                                                    tbaObject2.get(currentKey)))
                                    .collect(Collectors.toList());

                    //if any differences found, adding it to assertion object
                    for (JsonObject obj : differences) {
                        if (obj.size() > 0) {
                            assertion.setStatus(false);
                            assertion.addNonEqualKeys(obj);
                        }
                    }

                }//step 3 ends

                return assertion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assertion;
    }

    private JsonObject especialAssertEqual(String currentKey,
                                           JsonElement jsonElement1, JsonElement jsonElement2) {
        JsonObject returnObj = new JsonObject();
        boolean status = true;

        if (!jsonElement1.equals(jsonElement2)) {
            status = false;
            /*
             * USE CASES:
             *  A. Any one object is
             *   1. instance of String (upper case), while the other one is not but expecting values to match
             *       use setCaseSensitive
             *   2. instance of number, while the other one is not but expecting values should match
             *       done this already in flattening Json
             *   3. instance of boolean, while the other one is not but expecting values should match
             *       use setTransformBoolean
             *   4. date/time/datetime, and format of date time doesn't matter, so expecting values to match
             *       use setNormalizeDateFormat
             *   5. if unsorted array (basic types like string, integer etc.
             * */
            Object firstObjectValue = gson.fromJson(jsonElement1, Object.class);
            Object secondObjectValue = gson.fromJson(jsonElement2, Object.class);

            if (firstObjectValue instanceof String || secondObjectValue instanceof String) {
                if (caseSensitive //ensuring caseSensitive is set to True
                        && firstObjectValue.toString().equalsIgnoreCase(secondObjectValue.toString())) //ignoring case
                {
                    status = true;
                } else if (transformBoolean && firstObjectValue instanceof Boolean)  //ensuring transformBoolean is set to be True
                {
                    Object obj = getBooleanValue(secondObjectValue);
                    if (obj == firstObjectValue)
                        status = true;
                } else if (transformBoolean && secondObjectValue instanceof Boolean) //ensuring transformBoolean is set to be True
                {
                    Object obj = getBooleanValue(firstObjectValue);
                    if (obj == secondObjectValue)
                        status = true;
                } else if (normalizeDateFormat)  //finally normalizing date
                {
                    DateObject dateObject = new DateObject(ignoreSeconds);
                    LocalDateTime obj1 = (LocalDateTime) dateObject.getDateIfDate(firstObjectValue.toString());
                    LocalDateTime obj2 = (LocalDateTime) dateObject.getDateIfDate(secondObjectValue.toString());

                    if (obj1 != null && obj2 != null && obj1.isEqual(obj2)) {
                        status = true;
                    }
                }
            } // instance of string ends
            else if (firstObjectValue instanceof ArrayList && secondObjectValue instanceof ArrayList) {
                //handling arrays here
                List<Object> obj1 = (List<Object>) firstObjectValue;
                List<Object> obj2 = (List<Object>) secondObjectValue;
                List<Object> obj3 = new ArrayList<>(obj1);

                if (obj1.size() == obj2.size()) { //this ensures duplicate check
                    obj1.removeAll(obj2);
                    obj2.removeAll(obj3);
                    if (obj1.size() == 0 && obj2.size() == 0) { //checking obj1 size is enough, not need to check obj2
                        status = true;
                    }
                }
            }//instance of array ends
        }

        if (!status) {
            returnObj.addProperty("keyName", currentKey);
            returnObj.add((firstObjectName + " value"), jsonElement1);
            returnObj.add((secondObjectName + " value"), jsonElement2);
        }

        return returnObj;
    }

    private Boolean getBooleanValue(Object transformToBoolean) {
        String t = transformToBoolean.toString();
        final boolean b = t.equalsIgnoreCase("true")
                || t.equals("1")
                || t.equalsIgnoreCase("y");
        return
                b;
        //add all your transform values as needed
    }


    //setters and getters
    public void setMaxSizeOfKeysInObject(int maxSizeOfKeysInObject) {
        this.maxSizeOfKeysInObject = maxSizeOfKeysInObject;
    }

    public void setConvertArrayWithSquaresInQuotes(boolean convertArrayWithSquaresInQuotes) {
        this.convertArrayWithSquaresInQuotes = convertArrayWithSquaresInQuotes;
    }

    public void setNormalizeSpaces(boolean normalizeSpaces) {
        this.normalizeSpaces = normalizeSpaces;
    }

    public void setDoNotAssertKeys(List<String> doNotAssertKeys) {
        this.doNotAssertKeys = doNotAssertKeys.stream()
                .parallel()
                .map(o -> o.toLowerCase())
                .collect(Collectors.toList());
    }


    public void setNormalizeDateFormat(boolean normalizeDateFormat) {
        this.normalizeDateFormat = normalizeDateFormat;
    }

    public void setTransformBoolean(boolean transformBoolean) {
        this.transformBoolean = transformBoolean;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setFirstObjectName(String firstObjectName) {
        this.firstObjectName = firstObjectName;
    }

    public void setSecondObjectName(String secondObjectName) {
        this.secondObjectName = secondObjectName;
    }

    public void setIgnoreSeconds(boolean ignoreSeconds) {
        this.ignoreSeconds = ignoreSeconds;
    }

    public void setAssertFirstXRows(int assertFirstXRows) {
        this.assertFirstXRows = assertFirstXRows;
    }
}

