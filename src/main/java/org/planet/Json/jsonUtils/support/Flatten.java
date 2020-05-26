package org.planet.Json.jsonUtils.support;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.planet.Json.jsonUtils.exceptions.ExceedsLimitOfObjectSize;

import java.util.*;
import java.util.stream.Collectors;

final public class Flatten {
    private final JsonObject flattenedObject;
    private final Gson gson;

    //global items
    private JsonObject toBeFlattenObject;

    //config items
    private int maxSizeOfKeysInObject;
    private final boolean convertArrayWithSquaresInQuotes;
    private final boolean normalizeSpaces;
    private final List<String> doNotAssertKeys;

    public Flatten(int maxSizeOfKeysInObject,
                      boolean convertArrayWithSquaresInQuotes, boolean normalizeSpaces, List<String> doNotAssertKeys) {
        this.maxSizeOfKeysInObject = maxSizeOfKeysInObject;
        this.convertArrayWithSquaresInQuotes = convertArrayWithSquaresInQuotes;
        this.normalizeSpaces = normalizeSpaces;

        if (doNotAssertKeys == null)
            this.doNotAssertKeys = new ArrayList<>();
        else
            this.doNotAssertKeys = doNotAssertKeys;

        flattenedObject = new JsonObject();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public JsonObject getFlattenedObject(JsonObject toBeFlattenObject) throws ExceedsLimitOfObjectSize {
        this.toBeFlattenObject = toBeFlattenObject;
        //flattening now
        flattenJsonObject("", null);
        return flattenedObject;
    }

    //flattening starts here
    private void flattenJsonObject(String parentKey, JsonObject toBeFlattenObject)
            throws ExceedsLimitOfObjectSize {

        /*
         * Cases addressed:
         * 1. Lowering json keys
         * 2. Flattening inner json objects
         * 3. Flattening inner json arrays
         *  3.1 all basic type arrays like String, Double, Integer etc.
         *  3.2 Json objects array
         *  3.3 arrayWithSquaresInQuotes i.e. "[2,3,4]" treats this as JsonArray,
         *      [Note: this could be result from SQL execution when used group_concat]
         *  3.4 blank array - add's it as blank only
         * 4. Skip keys not needed to be asserted - doNotAssertKeys
         * */

        /*  Since this method gets called again from within this method,
         *   initially the values of toBeFlattenObject should be null
         *   however consecutive calls to this method will have values, and those shouldn't be overwritten
         * */
        if (toBeFlattenObject == null)
            toBeFlattenObject = this.toBeFlattenObject;

        /*
         * For performance reasons, checking the size of JsonObject KeySet.
         * This size is configurable.
         * */
        if (toBeFlattenObject.keySet().size() > maxSizeOfKeysInObject)//need configuration condition
            throw new ExceedsLimitOfObjectSize();


        /*
         * Flattening starts here
         * */
        for (String currentKey : toBeFlattenObject.keySet()) {

            if (!doNotAssertKeys.contains(currentKey.toLowerCase())) //ensures keys set as doNotAssertKeys as skipped
            {
                JsonElement currentElement = toBeFlattenObject.get(currentKey);
                //Step-1: Checking if it's inner JsonObject
                if (currentElement.isJsonObject()) {
                    String tKey = parentKey + "." + currentKey.toLowerCase();
                    flattenJsonObject(tKey, toBeFlattenObject.get(currentKey).getAsJsonObject());
                }
                //Step-2: Checking if it's inner JsonArray
                else if (currentElement.isJsonArray()) {
                    handleJsonArray(currentKey, parentKey, currentElement);
                }
                //Step-3: handle objects that looks like "[2,3,4]" aka. arrayWithSquaresInQuotes
                else if (looksToBeArray(currentElement) && convertArrayWithSquaresInQuotes) {
                    String tStr = currentElement.getAsString();
                    //excluding 1st and last square brackets [ ]
                    String[] tStrArray = tStr.substring(1, tStr.length() - 2).split(",");
                    JsonElement tElement = gson.toJsonTree(tStrArray);
                    handleJsonArray(currentKey, parentKey, tElement); //tElement should be passed, not currentElement
                }
                //Step-4: Finally add root keys with their values and adding flattened object
                else {
                    Object obj = gson.fromJson(toBeFlattenObject.get(currentKey), Object.class);

                    //addresses issues like " one    two three   " should be "one two three"
                    if (obj instanceof String && normalizeSpaces) {
                        obj = StringUtils.normalizeSpace(obj.toString());
                    }

                    //checking if it's number and converting to number
                    //numbers that are greater than 7 can be phone numbers, so skipping those
                    if (obj instanceof String && obj.toString().length() < 7 && NumberUtils.isParsable(obj.toString())) {
                        obj = Float.parseFloat(obj.toString());
                    }
                    flattenedObject.add(parentKey + "." + currentKey.toLowerCase(), gson.toJsonTree(obj));
                }
            }
        }
    }

    //used only in case of inner json array within json object
    private void handleJsonArray(String currentKey, String parentKey, JsonElement currentElement) throws ExceedsLimitOfObjectSize {
        JsonArray tArray = currentElement.getAsJsonArray();

        /*
         * For performance reasons, checking the size of JsonObject KeySet.
         * This size is configurable.
         * */
        if (tArray.size() > maxSizeOfKeysInObject) //need configuration condition
            throw new ExceedsLimitOfObjectSize();

        if (tArray.size() > 0) {//ensuring array is not blank
            //Step 1: JsonArray consists of JsonObjects
            if (tArray.get(0).isJsonObject()) {
                for (int i = 0; i < tArray.size(); i++) {
                    String tKey = parentKey + "." + currentKey.toLowerCase() + i;
                    flattenJsonObject(tKey, tArray.get(i).getAsJsonObject());
                }
            }
            //Step 2: JsonArray consists of String arrays or Number Arrays
            else {
                List<Object> objectList = gson.fromJson(tArray, ArrayList.class);

                List<Number> numberList = null;
                //checking if it's double quoted number
                if (objectList.get(0) instanceof String) {
                    numberList = getNumberListIfNumbers(objectList);
                }

                String tKey = parentKey + "." + currentKey.toLowerCase();
                flattenedObject.add(parentKey + "." + currentKey.toLowerCase(),
                        numberList == null ? gson.toJsonTree(objectList) : gson.toJsonTree(numberList));
            }
        } else {//handling blank array, adding it as is
            flattenedObject.add(parentKey + "." + currentKey.toLowerCase(), toBeFlattenObject.get(currentKey));
        }
    }

    //the method checks if a given string is parable to number and returns list of numbers
    private List<Number> getNumberListIfNumbers(List<Object> objectList) {
        if (areAllObjectsNumber(objectList)) {
            return objectList
                    .stream()
                    .parallel()
                    .map(o -> Double.parseDouble(o.toString())).collect(Collectors.toList());
        }

        return null;
    }

    //checks if all items in list are numbers that are in double quotes
    private boolean areAllObjectsNumber(List<Object> n) {
        List<Boolean> b = n.stream()
                .parallel()
                .map(o -> NumberUtils.isParsable(o.toString()))
                .collect(Collectors.toList());

        b.removeAll(Arrays.asList(true));

        if (b.size() > 0)
            return false;

        return true;
    }

    //method that recognizes  "[2,3,4]" as array
    private boolean looksToBeArray(JsonElement currentElement) {
        String tStr = currentElement.getAsString();
        char firstLetter = tStr.charAt(0);
        char lastLetter = tStr.charAt(tStr.length() - 1);
        if (firstLetter == '[' && lastLetter == ']' && tStr.contains(","))
            return true;
        return false;
    }

    public void setMaxSizeOfKeysInObject(int maxSizeOfKeysInObject) {
        this.maxSizeOfKeysInObject = maxSizeOfKeysInObject;
    }

}
