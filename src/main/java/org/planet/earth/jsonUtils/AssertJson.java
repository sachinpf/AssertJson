package org.planet.earth.jsonUtils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.planet.earth.jsonUtils.exceptions.ExceedsLimitOfObjectSize;
import org.planet.earth.jsonUtils.support.Flatten;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private int maxFailureCount; //applicable for JsonArray (having many jsonObjects) to be compared.

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
        maxFailureCount = 50;

        //depending on other object
        transformBoolean = false;
        normalizeDateFormat = false;

        //
        firstObjectName = "firstObject";
        secondObjectName = "secondObject";
    }


    public JsonObjectAssertion assertJsonObject(JsonObject firstObject,
                                                JsonObject secondObject) throws ExceedsLimitOfObjectSize {
        return assertJsonObject(firstObject, secondObject, null);
    }


    //All private methods
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
                            assertion.setStatusMessage("assertion failed.");
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
                    LocalDateTime obj2 = (LocalDateTime) getDateIfDate(secondObjectValue.toString());
                    LocalDateTime obj1 = (LocalDateTime) getDateIfDate(firstObjectValue.toString());

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
            else {
                System.out.println("**** Unidentified object classes: ");
                System.out.println(firstObjectValue.getClass());
                System.out.println(secondObjectValue.getClass());
            }//all other instances, if identified will be printed on console
        }

        if (!status) {
            returnObj.addProperty("keyName: ", currentKey);
            returnObj.add((firstObjectName + " value: "), jsonElement1);
            returnObj.add((secondObjectName + " value: "), jsonElement2);
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

    //matches given string with date format
    private Object getDateIfDate(String dateTime) {
        //Pattern 1 - Matching format: "2018-08-30T06:28:13Z"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            String re5 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);

            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 2 - Matching format: "2019-02-16T06:38:49.349Z"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            // String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re5 = "(\\.)";    // Any Single Character 1
            String re6 = "(\\d)";    // Any Single Digit 1
            String re7 = "(\\d)";    // Any Single Digit 2
            String re8 = "(\\d)";    // Any Single Digit 3
            String re9 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 3 - Matching format: "Fri Feb 15 22:38:49 PST 2019"
        {
            String re1 = "((?:Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Tues|Thur|Thurs|Sun|Mon|Tue|Wed|Thu|Fri|Sat))";    // Day Of Week 1
            String re2 = "(\\s+)";    // White Space 1
            String re3 = "((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";    // Month 1
            String re4 = "(\\s+)";    // White Space 2
            String re5 = "((?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // Day 1
            String re6 = "(\\s+)";    // White Space 3
            //String re7 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re7 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re8 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            String re9 = "(\\s+)";    // White Space 4
            String re10 = "((?:[a-z][a-z]+))";    // TimeZone like PST
            String re11 = "(\\s+)";    // White Space 5
            String re12 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";    // Year 1

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String monthNumber = findMonthNumber(m.group(3));
                String date = m.group(12) + "-" + monthNumber + "-" + m.group(5);
                String time;
                if (ignoreSeconds)
                    time = m.group(7);
                else
                    time = m.group(7) + m.group(8);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 4 - Matching format: "May 23, 2019 11:25:43"
        {
            String re1 = "((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";    // Month 1
            String re2 = "(\\s+)";    // White Space 1
            String re3 = "((?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // Day 1
            String re4 = "(,)";    // Any Single Character 1
            String re5 = "(\\s+)";    // White Space 2
            String re6 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";    // Year 1
            String re7 = "(\\s+)";    // White Space 3
            //String re8 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re8 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re9 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String monthNumber = findMonthNumber(m.group(1));
                System.out.println(monthNumber);
                String date = m.group(6) + "-" + monthNumber + "-" + m.group(3);
                String time;
                if (ignoreSeconds)
                    time = m.group(8);
                else
                    time = m.group(8) + m.group(9);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 5 - Matching format: "2012-05-23 11:25:43"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "(\\s+)";    // White Space 1
            //String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse((date + "T" + time));
            }
        }

        //Pattern 6 -Matching format: "2019-12-31T00:18:45.805263"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            //String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // old one HourMinuteSec 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-9][0-9][0-9][0-9][0-9][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            // String re5 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }
        return null;
    }//method end

    //find Month Number
    private String findMonthNumber(String monthName) {
        String monthNumber = "";
        switch (monthName) {
            case "Jan":
            case "January": {
                monthNumber = "01";
                break;
            }
            case "Feb":
            case "February": {
                monthNumber = "02";
                break;
            }
            case "March":
            case "Mar": {
                monthNumber = "03";
                break;
            }
            case "April":
            case "Apr": {
                monthNumber = "04";
                break;
            }
            case "May": {
                monthNumber = "05";
                break;
            }
            case "June":
            case "Jun": {
                monthNumber = "06";
                break;
            }
            case "July":
            case "Jul": {
                monthNumber = "07";
                break;
            }
            case "August":
            case "Aug": {
                monthNumber = "08";
                break;
            }
            case "September":
            case "Sep": {
                monthNumber = "09";
                break;
            }
            case "October":
            case "Oct": {
                monthNumber = "10";
                break;
            }
            case "November":
            case "Nov": {
                monthNumber = "11";
                break;
            }
            case "December":
            case "Dec": {
                monthNumber = "12";
                break;
            }
        }
        return monthNumber;
    }//switch statement

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

    public void setMaxFailureCount(int maxFailureCount) {
        this.maxFailureCount = maxFailureCount;
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
}

