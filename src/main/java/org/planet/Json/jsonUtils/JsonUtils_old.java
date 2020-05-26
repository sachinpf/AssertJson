package org.planet.Json.jsonUtils;


import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.openqa.selenium.json.Json;


public class JsonUtils_old {

    //field used for flattening the json
    public static JsonObject flatObject;

    private static Gson gson;

    public JsonUtils_old() {
        if (gson == null) {
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        }
    }

    public static JsonArray removeBlankFields(JsonArray jArray) {
        JsonArray tmpArray = new JsonArray();
        for (int i = 0; i < jArray.size(); i++) {
            JsonObject tmpObject = removeBlankFields(jArray.get(i).getAsJsonObject());
            tmpArray.add(tmpObject);
        }
        return tmpArray;
    }

    public static JsonObject removeBlankFields(JsonObject jObject) {
//        Need to fix code for JsonArrays within JsonObjects
        JsonObject tmpObject = new JsonObject();
        for (String key : jObject.keySet()) {
            if (jObject.get(key).isJsonArray()) { //this condtion will skip blank check in arrays for now
                tmpObject.add(key, jObject.get(key));
            } else if (!jObject.get(key).isJsonNull() && !jObject.get(key).getAsString().equals("")) {
                tmpObject.add(key, jObject.get(key));
            }
        }
        return tmpObject;
    }

    public static JsonArray isJsonArraysEqual(JsonArray jsonArray1, JsonArray jsonArray2, String ID) {
        return isJsonArraysEqual(jsonArray1, jsonArray2, ID, Arrays.asList(""), true, false);
    }

    public static JsonArray isJsonArraysEqual(JsonArray jsonArray1, JsonArray jsonArray2, String ID, List<String> ignoreFields) {
        return isJsonArraysEqual(jsonArray1, jsonArray2, ID, ignoreFields, true, false);
    }

    public static JsonArray isJsonArraysEqual(JsonArray jsonArray1, JsonArray jsonArray2, String ID, boolean considerBlankOrNullAsMissing) {
        return isJsonArraysEqual(jsonArray1, jsonArray2, ID, Arrays.asList(""), considerBlankOrNullAsMissing, false);
    }

    public static JsonArray isJsonArraysEqual(JsonArray jsonArray1, JsonArray jsonArray2, String ID, List<String> ignoreFields, boolean considerBlankOrNullAsMissing, boolean ignoreSecondsInDateTimeField) {
        JsonArray returnArray = new JsonArray();
        JsonArray allMismatches = new JsonArray();

        if (jsonArray1.size() <= 0 || jsonArray2.size() <= 0) {
            JsonObject tmp = new JsonObject();
            tmp.addProperty("status", "false");
            tmp.addProperty("ZeroSize", "true");
            tmp.addProperty("message", "Failed: size of Json Array 1 or 2 is less than equal to zero.");
            returnArray.add(tmp);
            return returnArray;
        }

        //define all required variables
        JsonArray missingIDs = new JsonArray();
        JsonArray rowsWithMissingIDFields = new JsonArray();
        //main loop
        for (int i = 0; i < jsonArray1.size(); i++) {
            JsonObject firstObject = jsonArray1.get(i).getAsJsonObject();
            String currentID = firstObject.get(ID).getAsString();

            boolean IDFound = false;
            JsonObject foundIDComparisionResults = new JsonObject();

            //second loop: finding a record by key in second json array and then comparing it
            for (int j = 0; j < jsonArray2.size(); j++) {
                JsonObject secondObject = jsonArray2.get(j).getAsJsonObject();
                try {
                    if (secondObject.get(ID).getAsString().equalsIgnoreCase(currentID)) {
                        foundIDComparisionResults = isJsonObjectsEqual(firstObject, secondObject, ignoreFields, considerBlankOrNullAsMissing, ignoreSecondsInDateTimeField);
                        foundIDComparisionResults.addProperty("ID", currentID);
                        IDFound = true;
                        break;
                    }
                } catch (NullPointerException ne) {
                    if (rowsWithMissingIDFields.size() > 0 && !rowsWithMissingIDFields.toString().contains(secondObject.toString())) {
                        rowsWithMissingIDFields.add(secondObject);
                        System.out.println("here");
                    } else if (rowsWithMissingIDFields.size() == 0) {
                        rowsWithMissingIDFields.add(secondObject);
                    }
                }
            }

            //checking if record was found in second json array, if no, adding missing key
            if (!IDFound)
                missingIDs.add(currentID);
            else if (!foundIDComparisionResults.get("status").getAsBoolean()) { //checking if comparision failed
                allMismatches.add(foundIDComparisionResults);
            }
        }//end of first loop

        //putting array level status and all mismatches
        if (allMismatches.size() > 0) {
            JsonObject tmp = new JsonObject();
            tmp.addProperty("status", "false");
            tmp.addProperty("ZeroSize", "false");
            tmp.addProperty("message", "Failed: review all mismatches");
            returnArray.add(tmp);
            returnArray.add(allMismatches);
        }

        //checking if missing keys found and then adding it to return array
        if (missingIDs.size() > 0) {
            JsonObject tmp = new JsonObject();
            tmp.add("missingIDs", missingIDs);
            tmp.addProperty("status", "false");
            tmp.addProperty("ZeroSize", "false");
            tmp.addProperty("message", "Failed: review all mismatches");
            returnArray.add(tmp);
        } else if (allMismatches.size() == 0) // if all is well, need to add true message
        {
            JsonObject tmp = new JsonObject();
            tmp.addProperty("status", "true");
            tmp.addProperty("ZeroSize", "false");
            tmp.addProperty("message", "Passed: given both json arrays are equal");
            returnArray.add(tmp);
        }


        //adding missing id field issues to return array
        if (rowsWithMissingIDFields.size() > 0) {
            //JsonArray rowsWithMissingIDFieldsRemovedDuplicates = removeDuplicates(rowsWithMissingIDFields);
            JsonObject tmp2 = new JsonObject();
            tmp2.add("rowsWithMissingIDFieldsInSecondObject", rowsWithMissingIDFields);
            returnArray.add(tmp2);
        }
        return returnArray;
    }//method end


    /* method that compares two JsonObject - still under construction
    Note:
    1. This method is non case sensitive
    2. before you can convert SQL to Json, ensure you use correct date format as below:
       gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    3. Date comparision is not yet done.
    */
    public static JsonObject isJsonObjectsEqual(JsonObject jsonObject1, JsonObject jsonObject2) {
        return isJsonObjectsEqual(jsonObject1, jsonObject2, Arrays.asList(""), true, false);
    }

    public static JsonObject isJsonObjectsEqual(JsonObject jsonObject1, JsonObject jsonObject2, List<String> ignoreFields) {
        return isJsonObjectsEqual(jsonObject1, jsonObject2, ignoreFields, true, false);
    }

    public static JsonObject isJsonObjectsEqual(JsonObject jsonObject1, JsonObject jsonObject2, boolean considerBlankOrNullAsMissing) {
        return isJsonObjectsEqual(jsonObject1, jsonObject2, Arrays.asList(""), considerBlankOrNullAsMissing, false);
    }

    public static JsonObject isJsonObjectsEqual(JsonObject jsonObject1, JsonObject jsonObject2, List<String> ignoreFields, boolean considerBlankOrNullAsMissing, boolean ignoreSecondsInDateTimeField) {
        if (ignoreFields != null)
            ignoreFields.replaceAll(String::toLowerCase); // ensuring all fields being ignored are lower case as this peice of code converts json keys to lower case
        else
            ignoreFields = new ArrayList<>();

        JsonObject returnObject = new JsonObject();
        returnObject.addProperty("status", true);
        returnObject.addProperty("message", "success");

        //A. going by simple match
        if (jsonObject1.equals(jsonObject2))
            return returnObject;

        //B. going by detailed match
        //find lowest possible node for jsonObject1
        flatObject = new JsonObject();  //initiating the static variable to store flat values
        new JsonUtils_old().getFlatJsonObject(jsonObject1, "root");
        JsonObject flatJsonObject1 = flatObject;
        //  System.out.println("FlatJsonObject 1: " + flatJsonObject1);

        //find lowest possible node for jsonObject2
        flatObject = new JsonObject(); //initiating the static variable to store flat values
        new JsonUtils_old().getFlatJsonObject(jsonObject2, "root");
        JsonObject flatJsonObject2 = flatObject;
        //System.out.println("FlatJsonObject 2: " + flatJsonObject2);

        //Step 1 - Checking if both objects have same keys, if not, it returns error
        {
            if (!flatJsonObject1.keySet().equals(flatJsonObject2.keySet())) {
                List<String> missingElements = new ArrayList<>();
                String value;
                for (String str : flatJsonObject1.keySet()) {
                    //removing ==> and getting the actual field name
                    List<String> str2 = Arrays.asList(str.split("==>"));
                    String str3 = str2.get(str2.size() - 1);
                    if (!ignoreFields.contains(str3) && !flatJsonObject2.keySet().toString().toLowerCase().contains(str.toLowerCase())) {
                        if (flatJsonObject1.get(str).toString().replace("\"", "").equalsIgnoreCase("[]")) {
                            value = "";
                        } else {
                            value = flatJsonObject1.get(str).isJsonNull() ? "null" : flatJsonObject1.get(str).getAsString().trim();
                        }
                        if (!considerBlankOrNullAsMissing && !value.equalsIgnoreCase("") && !value.equalsIgnoreCase("null")) {
                            //above condition ensures that if there are null or blank, then it won't consider
                            missingElements.add(str);
                            returnObject.addProperty("status", false);
                            returnObject.addProperty("missingElements", true);
                            returnObject.addProperty("message", "review results");
                        } else if (considerBlankOrNullAsMissing) {
                            missingElements.add(str);
                            returnObject.addProperty("status", false);
                            returnObject.addProperty("message", "review results");
                            returnObject.addProperty("missingElements", true);
                        }
                    }
                }//fend

                if (missingElements.size() > 0)
                    returnObject.addProperty("missingElementsInSecondObject", missingElements.toString());

                missingElements = new ArrayList<>();
                for (String str : flatJsonObject2.keySet()) {
                    //removing ==> and getting the actual field name
                    List<String> str2 = Arrays.asList(str.split("==>"));
                    String str3 = str2.get(str2.size() - 1);
                    if (!ignoreFields.contains(str3) && !flatJsonObject1.keySet().toString().toLowerCase().contains(str.toLowerCase())) {
                        value = flatJsonObject2.get(str).isJsonNull() ? "null" : flatJsonObject2.get(str).getAsString().trim();
                        if (!considerBlankOrNullAsMissing && !value.equalsIgnoreCase("") && !value.equalsIgnoreCase("null")) {
                            //above condition ensures that if there are null or blank, then it won't consider
                            missingElements.add(str);
                            returnObject.addProperty("status", false);
                            returnObject.addProperty("missingElements", true);
                            returnObject.addProperty("message", "review results");
                        } else if (considerBlankOrNullAsMissing) {
                            missingElements.add(str);
                            returnObject.addProperty("status", false);
                            returnObject.addProperty("missingElements", true);
                            returnObject.addProperty("message", "review results");
                        }
                    }
                }//fend
                if (missingElements.size() > 0) {
                    returnObject.addProperty("missingElementsInFirstObject", missingElements.toString());
                    //return returnObject;
                }

            }
        }


        //Step 2 - Checking individual values by keys in each object
        {
            String currentCheck = "Just starting to compare values";
            boolean status = true;
            String message = "All values matched";


            JsonArray misMatchingFields = new JsonArray();


            for (String key : flatJsonObject1.keySet()) {
                List<String> str2 = Arrays.asList(key.split("==>"));
                String str3 = str2.get(str2.size() - 1);
                if (flatJsonObject2.keySet().contains(key) && !ignoreFields.contains(str3)) {
//                    System.out.println("Current Key: " + key);
//                    System.out.println("Ignore Fields: " + ignoreFields + " contains: " + ignoreFields.contains(key));
//                   Made changes in the code to avoid additional spaces in the comparison string
                    String strValue1 = flatJsonObject1.get(key).toString().replaceAll("\"", "")
                            .replace("[\\", "[").replace("\\,]", "]").replace("\\]", "]").replace("\\, ]", "]").replace("\\", "").trim();
                    String strValue2 = flatJsonObject2.get(key).toString().replaceAll("\"", "")
                            .replace("[\\", "[").replace("\\,]", "]").replace("\\]", "]").replace("\\, ]", "]").replace("\\", "").trim();
                    try {
                        if (strValue1.equalsIgnoreCase(strValue2)) {
                            //Condition 1 - checking String
                            currentCheck = "String comparision";
                            //System.out.println("String Keys: " + key);
                            continue;
                        } //string check ends
                        else if ((strValue1.equals("true") || strValue1.equals("false") || strValue2.equals("true") || strValue2.equals("false"))) {
                            //Condition 2 - Boolean
                            currentCheck = "Boolean comparision";
                            // System.out.println("Boolean Keys: " + key);

                            Boolean fBoolean = false;
                            Boolean sBoolean = false;

                            if (strValue1.equalsIgnoreCase("true") || strValue1.equals("1") || strValue1.equalsIgnoreCase("y"))
                                fBoolean = true;
                            if (strValue2.equalsIgnoreCase("true") || strValue2.equals("1") || strValue2.equalsIgnoreCase("y"))
                                sBoolean = true;

                        /*System.out.println("F Boolean: " + fBoolean);
                        System.out.println("S Boolean: " + sBoolean);*/

                            if (!fBoolean.equals(sBoolean)) {
                                status = false;
                                message = "Found mismatches";
                                JsonObject tObject = new JsonObject();
                                tObject.addProperty("fieldName", key);
                                tObject.addProperty("firstObjectValue", strValue1);
                                tObject.addProperty("secondObjectValue", strValue2);
                                misMatchingFields.add(tObject);
                            }

                        } //boolean check ends
                        else if (NumberUtils.isParsable(strValue1) && NumberUtils.isParsable(strValue2)) {
                            //condition 3 - Checking for number
//                        System.out.println("Number Keys: " + key);
                            currentCheck = "Parsing To Number";
                            strValue1 = strValue1.replaceAll(",", "");
                            strValue2 = strValue2.replaceAll(",", "");
                            Double fn = Double.valueOf(strValue1);
                            Double sn = Double.valueOf(strValue2);

                            if (!fn.equals(sn)) {
                                status = false;
                                message = "Found mismatches";
                                JsonObject tObject = new JsonObject();
                                tObject.addProperty("fieldName", key);
                                tObject.addProperty("firstObjectValue", fn);
                                tObject.addProperty("secondObjectValue", sn);
                                misMatchingFields.add(tObject);
                            }
                        }//numberUtils if ends
                        //covers else condition too
                        else {
                            //Condition 4.1 - Checking DateTime Field
                            //Condition 4.2 - Checking Date Field
                            //Condition 4.3 - Checking Time
                            currentCheck = "Date comparision";
                            //System.out.println("String Keys: " + key);
                            JsonObject isDateFO = isDateOrTime(strValue1, ignoreSecondsInDateTimeField);
                            JsonObject isDateSO = isDateOrTime(strValue2, ignoreSecondsInDateTimeField);
                            boolean isDateFOStatus = Boolean.parseBoolean(isDateFO.get("status").toString().replaceAll("\"", ""));
                            boolean isDateSOStatus = Boolean.parseBoolean(isDateSO.get("status").toString().replaceAll("\"", ""));
                            if (isDateFOStatus && isDateSOStatus) {//if date & time, it enters here
                                LocalDate FODate, SODate;
                                LocalTime FOTime, SOTime;
                                FODate = LocalDate.parse(isDateFO.get("date").toString().replaceAll("\"", ""));
                                FOTime = LocalTime.parse(isDateFO.get("time").toString().replaceAll("\"", ""));
                                SODate = LocalDate.parse(isDateSO.get("date").toString().replaceAll("\"", ""));
                                SOTime = LocalTime.parse(isDateSO.get("time").toString().replaceAll("\"", ""));

                                if (FODate.compareTo(SODate) == 0 && FOTime.compareTo(SOTime) == 0)
                                    continue;
                            }
                            //Condition 5 Array comparision
                            // array sorting already addressed in flatten Json Object method, however if second object is not array but first is, that needs to be addressed
                            else if (strValue1.length() > 1 && strValue1.substring(0, 1).equalsIgnoreCase("[") && strValue1.substring(strValue1.length() - 1, strValue1.length()).equalsIgnoreCase("]")
                                    && strValue2.length() > 1 && strValue2.substring(0, 1).equalsIgnoreCase("[") && strValue2.substring(strValue2.length() - 1, strValue2.length()).equalsIgnoreCase("]")
                            ) {
                                //below command will remove [] brackets and spaces and then split
                                String[] fArray = StringUtils.normalizeSpace(strValue1.toLowerCase().replaceAll("(\\[)", "").replaceAll("]", "").trim().replaceAll(", ", ",")).split(",");
                                String[] sArray = StringUtils.normalizeSpace(strValue2.toLowerCase().replaceAll("(\\[)", "").replaceAll("]", "").trim()).replaceAll(", ", ",").split(",");
                                Arrays.sort(fArray);
                                Arrays.sort(sArray);

                                boolean detailsMatch = true;
                                //detailed match using trim
                                if (fArray.length == sArray.length) {

                                    for (int i = 0; i < fArray.length; i++) {
                                        if (!fArray[i].trim().equalsIgnoreCase(sArray[i].trim())) {
                                            /*System.out.println("Trim : " + fArray[i].trim());
                                            System.out.println("Trim 2: " + sArray[i].trim());*/
                                            detailsMatch = false;
                                        }
                                    }

                                    if (detailsMatch)
                                        continue;
                                }//loop ends

                            }

                            //Condition 6 now fails
                            //System.out.println("Neither Keys: " + key);
                            status = false;
                            message = "Found mismatches";
                            JsonObject tObject = new JsonObject();
                            tObject.addProperty("fieldName", key);
                            tObject.addProperty("firstObjectValue", strValue1);
                            tObject.addProperty("secondObjectValue", strValue2);
                            misMatchingFields.add(tObject);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
//                        ReportSteps.INFO("Exception in comparision: " + currentCheck, true);
//                        ReportSteps.INFO("First Object Key & Value: " + key + " <--> " + strValue1, true);
//                        ReportSteps.INFO("Second Object Key & Value: " + key + " <--> " + strValue2, true);
                        status = false;
                        message = "Exception";
                        break;
                    }//end of catch
                }//checking if second object also has the key
            }//f-end

            //adding vaues to return Object
            if (!status) {
                returnObject.addProperty("status", status);
                returnObject.addProperty("message", message);
                returnObject.add("misMatchingFields", misMatchingFields);
            }

        }//step B.2 ends


        return returnObject;
    }

    //matches given string with date format
    public static JsonObject isDateOrTime(String dateTime, boolean ignoreSecondsInDateTimeField) {
        JsonObject returnValue = new JsonObject();
        returnValue.addProperty("status", "false");

        //Pattern 1 - Matching format: "2018-08-30T06:28:13Z"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            //String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // old one HourMinuteSec 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            String re5 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSecondsInDateTimeField)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
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
                if (ignoreSecondsInDateTimeField)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
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
                if (ignoreSecondsInDateTimeField)
                    time = m.group(7);
                else
                    time = m.group(7) + m.group(8);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
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
                if (ignoreSecondsInDateTimeField)
                    time = m.group(8);
                else
                    time = m.group(8) + m.group(9);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
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
                if (ignoreSecondsInDateTimeField)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
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
                if (ignoreSecondsInDateTimeField)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                returnValue.addProperty("status", "true");
                returnValue.addProperty("date", date);
                returnValue.addProperty("time", time);
                return returnValue;
            }
        }
        return returnValue;
    }//method end

    //find Month Number
    public static String findMonthNumber(String monthName) {
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

    //write to a Json File
    static public boolean writeJsonToFile(String filePath, JsonObject objectToBeWritten) {
        try {
            gson.toJson(objectToBeWritten, new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // read file and return JsonObject
    static public JsonObject readJsonFile(String filePath, Class className) {
        JsonObject returnObject = new JsonObject();
        try {
            InputStream inputStream;
            inputStream = className.getClassLoader().getResourceAsStream(filePath);
            returnObject = (JsonObject) new JsonParser().parse(readFromInputStream(inputStream));
        } catch (IOException e) {
            System.out.println("Unable to read json file." + e.getMessage());
            e.printStackTrace();
        }
        return returnObject;
    }

    // read file and return JsonArray
    static public JsonArray readJsonArrayFile(String filePath, Class className) {
        JsonArray returnObject = new JsonArray();
        try {
            InputStream inputStream;
            inputStream = className.getClassLoader().getResourceAsStream(filePath);
            returnObject = (JsonArray) new JsonParser().parse(readFromInputStream(inputStream));
        } catch (IOException e) {
            System.out.println("Unable to read json file." + e.getMessage());
            e.printStackTrace();
        }
        return returnObject;
    }

    public static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cause: " + e.getCause());
            System.out.println("Message: " + e.getMessage());
        }
        return resultStringBuilder.toString();
    }// read from inputs stream method ends

    //method to convert JsonObject Comparision Result to CSV format
//public static File createCSVFileFromAssertionResults(JsonArray assertionResultsJsonArray)
    /*public static void createCSVFileFromAssertionResults(JsonArray assertionResultsJsonArray, ExcelUtils e) throws IOException {
        int rowNum;
        //call composeJsonObjectFromAssertionResults from here for each object with in the given array
        for (int z = 0; z < assertionResultsJsonArray.size(); z++) {

            if (e.getRowCount("Sheet1") != 0)
                rowNum = e.getRowCount("Sheet1");
            else
                rowNum = 0;

            if (assertionResultsJsonArray.get(z).getAsJsonObject().keySet().contains("missingElementsInFirstObject")) {
                String firstObjectValue = assertionResultsJsonArray.get(z).getAsJsonObject().get("missingElementsInFirstObject").toString();
                firstObjectValue = firstObjectValue.replaceAll("\"\\[", "");
                firstObjectValue = firstObjectValue.replaceAll("root==>", "");
                firstObjectValue = firstObjectValue.replaceAll("]\"", "");
                e.setCellData(1, rowNum, firstObjectValue);
            }
            if (assertionResultsJsonArray.get(z).getAsJsonObject().keySet().contains("missingElementsInSecondObject")) {
                String missingElementsInSecondObject = assertionResultsJsonArray.get(z).getAsJsonObject().get("missingElementsInSecondObject").toString();
                missingElementsInSecondObject = missingElementsInSecondObject.replaceAll("\"\\[", "");
                missingElementsInSecondObject = missingElementsInSecondObject.replaceAll("root==>", "");
                missingElementsInSecondObject = missingElementsInSecondObject.replaceAll("]\"", "");

                e.setCellData(2, rowNum, missingElementsInSecondObject);
            }
            String id = assertionResultsJsonArray.get(z).getAsJsonObject().get("ID").toString();

            e.setCellData(0, rowNum, id);


            // System.out.println("ValueMismatch-FieldName :"+assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields"));

            if (assertionResultsJsonArray.get(z).getAsJsonObject().keySet().contains("misMatchingFields")) {
                int size = assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().size();
                for (int c = 0; c < size; c++) {
                    if (assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("fieldName") != null) {
                        String fieldName = assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("fieldName").toString();
                        fieldName = fieldName.replaceAll("\"root==>", "");
                        fieldName = fieldName.replaceAll("\"", "");
                        e.setCellData(3, rowNum, fieldName);
                    }
                    if (assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("firstObjectValue") != null) {
                        String firstObjectValue = assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("firstObjectValue").toString();
                        firstObjectValue = firstObjectValue.replaceAll("\"", "");
                        e.setCellData(4, rowNum, firstObjectValue);
                    }
                    if (assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("secondObjectValue") != null) {
                        String secondObjectValue = assertionResultsJsonArray.get(z).getAsJsonObject().get("misMatchingFields").getAsJsonArray().get(c).getAsJsonObject().get("secondObjectValue").toString();
                        secondObjectValue = secondObjectValue.replaceAll("\"", "");
                        e.setCellData(5, rowNum, secondObjectValue);
                    }
                }

            }

        }
        //once you get json object back, convert it to csv
        //return null;
    }*/

    //method to convert JsonObject Comparision Result to CSV format
    /*
    Consider only below columns from given json:
        "ID": "",
        missingElementsInFirstObject: "",
        missingElementsInSecondObject: "",
        "misMatchingFields":[{
        ValueMismatch-FieldName: ""
        firstObjectValue:""
        secondObjectValue:""
        }
        {
        ValueMismatch-FieldName: ""
        firstObjectValue:""
        secondObjectValue:""
        }]
    * */
/*    public static void composeAssertionResultsExcel(JsonArray assertionResults, String fileNameAndPath) {
        JsonArray top = assertionResults;
        // String path = "./solr/src/test/resources/temp.xlsx";
        try {
            FileOutputStream fileOut = new FileOutputStream(fileNameAndPath);
            //creating blank excel file first
            XSSFWorkbook workbook = new XSSFWorkbook();
            workbook.createSheet();
            workbook.write(fileOut);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExcelUtils e = new ExcelUtils(fileNameAndPath);

        if (e.isSheetExist("Sheet1")) {
            e.removeSheet("Sheet1");
            e.addSheet("Sheet1");
        } else
            e.addSheet("Sheet1");

        e.addColumn("Sheet1", "ID");
        e.addColumn("Sheet1", "missingElementsInFirstObject");
        e.addColumn("Sheet1", "missingElementsInSecondObject");
        e.addColumn("Sheet1", "ValueMismatch-FieldName");
        e.addColumn("Sheet1", "firstObjectValue");
        e.addColumn("Sheet1", "secondObjectValue");

        for (int i = 0; i < top.size(); i++) {
            Set keys = top.get(i).getAsJsonObject().keySet();
            Iterator t = keys.iterator();
            String one = t.next().toString();
            JsonArray levelTwo = (JsonArray) top.get(i).getAsJsonObject().get(one);
            for (int j = 0; j < levelTwo.size(); j++) {
                Set keysOne = levelTwo.get(j).getAsJsonObject().keySet();
                Iterator t1 = keysOne.iterator();
                String two = t1.next().toString();
                JsonArray levelThree = (JsonArray) levelTwo.get(j).getAsJsonObject().get(two);
                for (int w = 0; w < levelThree.size(); w++) {
                    if (levelThree.get(w).isJsonArray()) {
                        int size = levelThree.get(w).getAsJsonArray().size();
                        for (int y = 0; y < size; y++) {
                            try {
                                JsonUtils.createCSVFileFromAssertionResults(levelThree.get(w).getAsJsonArray(), e);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        e.close(fileNameAndPath);
        *//*      Code to write the data into CSV file
                CSVWriter writer =null;
                String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +".csv";
                 String csvPath = "./solr/target/"+name;
                 ExcelUtils excelFile = new ExcelUtils(path);

                try {
                    FileWriter outputfile = new FileWriter(csvPath);

                    writer = new CSVWriter(outputfile, '|',
                            CSVWriter.NO_QUOTE_CHARACTER,
                            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                            CSVWriter.DEFAULT_LINE_END);

                      int rowCount = excelFile.getRowCount("Sheet1");
                      int columnCount = excelFile.getColumnCount("Sheet1");
                    String csvRow[]=  new String[6];
                      for(int i=1;i<rowCount;i++){
                          for(int j=0;j<columnCount;j++){
                              csvRow[j]  =   excelFile.getCellData("Sheet1",j,i);
                          }
                          writer.writeNext(csvRow);
                      }
                            writer.close();

                }catch (Exception so){
                    so.printStackTrace();
           *//*
    }*/

    //method that helps flatten the jsonObject
    //may not work with JsonObject that has JsonArray, but will work with String Array
    public void getFlatJsonObject(JsonObject jObj, String rootTag) {
        for (String key : jObj.keySet()) {
            if (jObj.get(key).isJsonObject()) {
                new JsonUtils_old().getFlatJsonObject(jObj.get(key).getAsJsonObject(), (rootTag + "==>" + key.toLowerCase()));
            } else if (jObj.get(key).isJsonArray()) {
                JsonArray tArray = jObj.get(key).getAsJsonArray();
                if (tArray.get(0).isJsonObject()) {
                    for (int i = 0; i < tArray.size(); i++) {
                        new JsonUtils_old().getFlatJsonObject(tArray.get(i).getAsJsonObject(), (rootTag + "==>" + key.toLowerCase() + "[" + i + "]"));
                    }
                } else {
                    List<String> str = new ArrayList<>();
                    for (int i = 0; i < tArray.size(); i++) {
                        str.add(tArray.get(i).toString());
                    }
                    Collections.sort(str);
                    flatObject.addProperty(rootTag + "==>" + key.toLowerCase(), (str.toString().replaceAll("\"", "")));
                    //added replace All above just to ensure it does not added double inverted to the string array
                }
            } else {
                flatObject.add(rootTag + "==>" + key.toLowerCase(), jObj.get(key));
            }
        }
    }

    //Method that compares the data
    //All the below methods will be deprecated soon, don't use
    @Deprecated
    public static JsonObject compareTwoJsonArraysByKey(JsonArray SQLResultObject, JsonArray Object2, String uniqueKey) {
        JsonObject returnValue;
        returnValue = new JsonObject();

        //Step 1 - Matching the size
        if (SQLResultObject.size() == 0 || Object2.size() == 0) {
            if (SQLResultObject.size() == 0) {
                returnValue.addProperty("Zero_records_in_DB", "true");
            }
            if (Object2.size() == 0) {
                returnValue.addProperty("Zero_records_in_Solr", "true");
            }
        }
        if (SQLResultObject.size() != Object2.size()) {
            returnValue.addProperty("Size_Of_Both_Objects_Matched", "false");
            returnValue.addProperty("Size_Difference", Math.abs(SQLResultObject.size() - Object2.size()));
        } else {
            returnValue.addProperty("Size_Of_Both_Objects_Matched", "true");
        }


        //Step 2 - Duplicate Key check with in the same object
        List<String> obj1Keys = new ArrayList<String>();
        List<String> obj2Keys = new ArrayList<String>();

        int i, j, k;
        for (i = 0; i < SQLResultObject.size(); i++)
            obj1Keys.add(SQLResultObject.get(i).getAsJsonObject().get(uniqueKey).toString());

        for (i = 0; i < Object2.size(); i++)
            obj2Keys.add(Object2.get(i).getAsJsonObject().get(uniqueKey).toString());

        returnValue.addProperty("Duplicates_In_SQLResultObject", findDuplicate(obj1Keys));
        returnValue.addProperty("Duplicates_In_Object2", findDuplicate(obj2Keys));


        //Step 3 - Matching the keys between both the objects to see if anything is missing
        returnValue.addProperty("Non_Duplicate_Values", findNonDuplicate(obj1Keys, obj2Keys));

        //Step 4 - Matching the values by given keys
//        JsonObject myMismatches = matchValuesByGivenKey(SQLResultObject, Object2, uniqueKey);
/*
        if (myMismatches.size() > 0) {
            returnValue.add("Mismatches", myMismatches);
        }
*/

        //Step 5 - Matching the keys between both the objects to see if anything is additional
//        JsonObject myAdditionalData = additionalKeysCheck(SQLResultObject, Object2, uniqueKey);
//        if (myAdditionalData.size() > 0) {
//            returnValue.add("Excessive data Mismatches", myAdditionalData);
//        }

        return returnValue;
    }

    //match values by given keys
    private JsonObject matchValuesByGivenKey(JsonArray SQLResultObject, JsonArray Object2, String uniqueKey) {
        int i, j, k;
        JsonObject firstObject;// = new JsonObject();
        JsonObject secondObject = new JsonObject();
        JsonObject returnObject = new JsonObject();

        int minSize = Math.min(SQLResultObject.size(), Object2.size());

        String currentKey;
        String TRUE = "true";
        String FALSE = "false";
        JsonArray strMissingFields = new JsonArray();
        //List<String> strMissingFields = new ArrayList <>();
        k = 1;
        for (i = 0; i < SQLResultObject.size(); i++) {
            firstObject = SQLResultObject.get(i).getAsJsonObject();
            currentKey = firstObject.get(uniqueKey).toString();
            Boolean mathchingKeyFound = false;
            // Here transversing the entire solr response for given current key. so changed from minSize to Object2.size()
            for (j = 0; j < Object2.size(); j++) // locating json object by key
            {
                if (Object2.get(j).getAsJsonObject().get(uniqueKey).toString().equals(currentKey)) {
                    secondObject = Object2.get(j).getAsJsonObject();
                    j = Object2.size();
                    mathchingKeyFound = true;
                }
            }

            //testing record values only if matching values found
            if (mathchingKeyFound) {
                if (!firstObject.equals(secondObject)) {
                    JsonObject tmpObject = new JsonObject();
                    JsonObject tmpMissingKeys = new JsonObject();

                    Set<Map.Entry<String, JsonElement>> entrySet = firstObject.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        try {
                            //condition for misssing fields - if they have values
                            if (!secondObject.has(entry.getKey()) && !firstObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase("")
                                    && !firstObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase(" ") && !firstObject.get(entry.getKey()).equals(null)) {
                                //strMissingFields.add("For ID: " + currentKey + " Missing Field Name: " + entry.getKey() + " Value: " + firstObject.get(entry.getKey()).getAsString());
                                if (!firstObject.get(entry.getKey()).getAsString().equalsIgnoreCase("0") && !firstObject.get(entry.getKey()).getAsString().equalsIgnoreCase("false")
                                        && !firstObject.get(entry.getKey()).getAsString().equalsIgnoreCase("na")
                                ) {
                                    JsonObject tmpO = new JsonObject();
                                    tmpO.addProperty("ID", currentKey);
                                    tmpO.addProperty("Missing_Field_Name", entry.getKey());
                                    tmpO.addProperty("Expected_Value", firstObject.get(entry.getKey()).getAsString());
                                    strMissingFields.add(tmpO);
                                }
                            } else if (secondObject.has(entry.getKey())) {
                                String tKey1 = entry.getKey() + "_SQL";
                                String tKey2 = entry.getKey() + "_Solr";
                                //validating date values
                                if (entry.getKey().matches("(.+)_ts(.+)") || entry.getKey().matches("(.+)date(.+)") || entry.getKey().matches("date(.+)") || entry.getKey().matches("(.+)date") || entry.getKey().matches("(.+)Date(.+)")) {
                                    Date tStrDate1 = locateDateFromString(firstObject.get(entry.getKey()).getAsString());
                                    Date tStrDate2 = locateDateFromString(secondObject.get(entry.getKey()).getAsString());
                                    if ((tStrDate1 != null && (tStrDate1.getYear() + tStrDate1.getMonth() + tStrDate1.getDay()) != (tStrDate2.getYear() + tStrDate2.getMonth() + tStrDate2.getDay())) || (tStrDate1 == null && tStrDate2 != null) || (tStrDate1 != null && tStrDate2 == null)) {
                                        tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString());
                                        tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString());
                                    }
                                } else if ((firstObject.get(entry.getKey()).getAsString().contains(" AM") || firstObject.get(entry.getKey()).getAsString().contains(" PM"))
                                        && (!secondObject.get(entry.getKey()).getAsString().contains(" AM") && !secondObject.get(entry.getKey()).getAsString().contains(" PM"))) {
                                    String[] tmpStr = firstObject.get(entry.getKey()).getAsString().split("AM");
                                    String fo_value = tmpStr[0].trim();
                                    String so_value = secondObject.get(entry.getKey()).getAsString();
                                    if (!fo_value.equals(so_value)) {
                                        tmpObject.addProperty(tKey1.toString(), fo_value);
                                        tmpObject.addProperty(tKey2.toString(), so_value);
                                    }
                                } else if (NumberUtils.isParsable(firstObject.get(entry.getKey()).getAsString()) && !secondObject.get(entry.getKey()).isJsonArray() && NumberUtils.isParsable(secondObject.get(entry.getKey()).getAsString())) {
                                    Double fn = (Double) firstObject.get(entry.getKey()).getAsDouble();
                                    Double sn = (Double) secondObject.get(entry.getKey()).getAsDouble();

                                    if (Math.round(fn) != Math.round(sn)) {
                                        tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString());
                                        tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString());
                                    }
                                } else if (!secondObject.get(entry.getKey()).isJsonArray() && (isThisBoolean(secondObject.get(entry.getKey()).getAsString().trim()) == 1 || isThisBoolean(firstObject.get(entry.getKey()).getAsString().trim()) == 1)) {
                                    Boolean sBl;
                                    if (secondObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase("1"))
                                        sBl = true;
                                    else if (secondObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase("0"))
                                        sBl = false;
                                    else
                                        sBl = Boolean.valueOf(secondObject.get(entry.getKey()).getAsBoolean());

                                    Boolean fBl;
                                    if (firstObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase("1"))
                                        fBl = true;
                                    else if (firstObject.get(entry.getKey()).getAsString().trim().equalsIgnoreCase("0"))
                                        fBl = false;
                                    else
                                        fBl = Boolean.valueOf(firstObject.get(entry.getKey()).getAsBoolean());

                                    if (fBl != sBl) {
                                        tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString());
                                        tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString());
                                    }
                                } else if (!firstObject.get(entry.getKey()).equals(secondObject.get(entry.getKey()))) {
                                    if (secondObject.get(entry.getKey()).isJsonArray() || firstObject.get(entry.getKey()).isJsonArray()) {

                                        if (!firstObject.get(entry.getKey()).getAsString().contains("[")) {
                                            String[] splitValuesFirstObject = firstObject.get(entry.getKey()).getAsString().trim().split("\\s*,\\s*");
                                        }
                                        String[] splitValuesFirstObject = firstObject.get(entry.getKey()).getAsString().replace("[", "").replace("]", "").split(",");
                                        int maxSizeSecondObject = secondObject.get(entry.getKey()).getAsJsonArray().size();
                                        JsonArray jSecondObjectArray = secondObject.get(entry.getKey()).getAsJsonArray();
                                        int maxSizeSecondObject1 = 0;
                                        if (maxSizeSecondObject == 1) {
                                            maxSizeSecondObject1 = jSecondObjectArray.get(0).toString().split(",").length;
                                        } else {
                                            maxSizeSecondObject1 = maxSizeSecondObject;
                                        }

                                        if (maxSizeSecondObject1 != splitValuesFirstObject.length) {
                                            tmpObject.add(tKey1, firstObject.get(entry.getKey()));
                                            tmpObject.add(tKey2, secondObject.get(entry.getKey()));
                                        } else {
                                            Boolean arrayMatch = true;

                                            //sorting json array now
                                            String[] splitValuesSecondObject = new String[maxSizeSecondObject];
                                            if (maxSizeSecondObject == 1) {
                                                if (splitValuesFirstObject[0].contains("\"")) {
                                                    splitValuesFirstObject[0] = splitValuesFirstObject[0].replace("\"", "");
                                                }
                                                splitValuesSecondObject = jSecondObjectArray.get(0).getAsString().trim().split(",");
                                            }
                                            if (maxSizeSecondObject > 1) {
                                                for (int i1 = 0; i1 < splitValuesFirstObject.length; i1++) {
                                                    splitValuesFirstObject[i1] = splitValuesFirstObject[i1].replace("\"", "").trim();
                                                }
                                                for (int ik = 0; ik < maxSizeSecondObject; ik++) {
                                                    splitValuesSecondObject[ik] = jSecondObjectArray.get(ik).getAsString().trim();
                                                }
                                            }

                                            Arrays.sort(splitValuesFirstObject);
                                            Arrays.sort(splitValuesSecondObject);

                                            for (int iArray = 0; iArray < maxSizeSecondObject; iArray++) {
                                            /*System.out.println("Here 1: " + splitValuesFirstObject[iArray]);
                                            System.out.println("Here 2: " + splitValuesSecondObject[iArray]);*/
                                                if (!splitValuesFirstObject[iArray].trim().equalsIgnoreCase(splitValuesSecondObject[iArray].trim())) {
                                                    arrayMatch = false;
                                                    break;
                                                }
                                            }

                                            if (arrayMatch == false) {
                                                tmpObject.add(tKey1, firstObject.get(entry.getKey()));
                                                tmpObject.add(tKey2, secondObject.get(entry.getKey()));
                                            }
                                        }
                                    } else if (entry.getKey().matches("(.+)_time")) {
                                        try {
                                            Date tStrTime1 = locateTimeFromString(firstObject.get(entry.getKey()).getAsString());
                                            Date tStrTime2 = locateTimeFromString(secondObject.get(entry.getKey()).getAsString());
                                            if ((tStrTime1 != null && (tStrTime1.getTime() != tStrTime2.getTime()) || (tStrTime1 == null && tStrTime2 != null) || (tStrTime1 != null && tStrTime2 == null))) {
                                                tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString());
                                                tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else if (!StringUtils.normalizeSpace(firstObject.get(entry.getKey()).getAsString()).equalsIgnoreCase(StringUtils.normalizeSpace(secondObject.get(entry.getKey()).getAsString()))) {
                                        tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString().trim());
                                        tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString().trim());
                                    }

                                }//if end

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception first value: " + entry.getKey() + "==" + firstObject.get(entry.getKey()).toString());
                            System.out.println("Exception 2nd Value: " + entry.getKey() + "==" + secondObject.get(entry.getKey()).toString());
                        }
                    }//end inside for
                    //returnObject = returnObject + tmpObject;
                    if (tmpObject.getAsJsonObject().size() > 0) {
                        tmpObject.addProperty(uniqueKey, currentKey);
                        returnObject.add(currentKey, (JsonObject) new JsonParser().parse((tmpObject.toString())));
                    }
                    k++;
                }
            }//matching key condition
            else {
                returnObject.addProperty("Missing_Key" + i, currentKey);
            }
        }//end of for

        //adding if any missing fields present
        if (strMissingFields.size() > 0) {
            returnObject.add("Missing_Fields", strMissingFields);
        }
        //adding all missting keys
        //System.out.println(returnObject);
        return returnObject;
    }

    //checking additional keys
    private JsonObject additionalKeysCheck(JsonArray SQLResultObject, JsonArray Object2, String uniqueKey) {
        int i, j, k;
        JsonObject firstObject = new JsonObject();
        JsonObject secondObject = new JsonObject();
        JsonObject returnObject = new JsonObject();

        int minSize = Math.max(SQLResultObject.size(), Object2.size());

        String currentKey;
        String TRUE = "true";
        String FALSE = "false";
        JsonArray strAdditionalFields = new JsonArray();
        k = 1;
        for (i = 0; i < Object2.size(); i++) {
            firstObject = Object2.get(i).getAsJsonObject();
            currentKey = firstObject.get(uniqueKey).toString();
            Boolean mathchingKeyFound = false;
            for (j = 0; j < minSize; j++) // locating json object by key
            {
                if (SQLResultObject.get(j).getAsJsonObject().get(uniqueKey).toString().equals(currentKey)) {
                    secondObject = SQLResultObject.get(j).getAsJsonObject();
                    j = SQLResultObject.size();
                    mathchingKeyFound = true;
                }
            }
            if (mathchingKeyFound) {
                if (!firstObject.equals(secondObject)) {
                    JsonObject tmpObject = new JsonObject();
                    JsonObject tmpMissingKeys = new JsonObject();
                    Set<Map.Entry<String, JsonElement>> entrySet = secondObject.entrySet();

                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        String tKey1 = entry.getKey() + "_SQL";
                        String tKey2 = entry.getKey() + "_Solr";
                        if (!StringUtils.normalizeSpace(firstObject.get(entry.getKey()).getAsString()).equalsIgnoreCase(StringUtils.normalizeSpace(secondObject.get(entry.getKey()).getAsString()))) {
                            tmpObject.addProperty(tKey1.toString(), firstObject.get(entry.getKey()).getAsString().trim());
                            tmpObject.addProperty(tKey2.toString(), secondObject.get(entry.getKey()).getAsString().trim());
                        }
                    }
                    //returnObject = returnObject + tmpObject;
                    if (tmpObject.getAsJsonObject().size() > 0) {
                        tmpObject.addProperty(uniqueKey, currentKey);
                        returnObject.add(currentKey, (JsonObject) new JsonParser().parse((tmpObject.toString())));
                    }
                    k++;
                }
            } else {
                returnObject.addProperty("Additional_Key" + i, currentKey);
            }
        }
        //adding if any additional fields present
        if (strAdditionalFields.size() > 0) {
            returnObject.add("Additional_Fields", strAdditionalFields);
        }
        //adding all additional keys
        //System.out.println(returnObject);
        return returnObject;
    }

    // find duplicates with in the same object
    public static String findDuplicate(List<String> myObject) {
        String duplicateValues = "";
        int i, j;

        for (i = 0; i < myObject.size(); i++) {
            for (j = i + 1; j < myObject.size(); j++) {
                if (myObject.get(i).equals(myObject.get(j))) {
                    duplicateValues = duplicateValues + "," + myObject.get(i);
                }
            }
        }

        if (duplicateValues.equals("")) {
            duplicateValues = "No duplicate values.";
        }
        return duplicateValues;
    }

    // find non duplicates between two objects
    private static String findNonDuplicate(List<String> mySQLResultObject, List<String> myObject2) {

        String nonDuplicateValues = "";
        Boolean ndv;
        int i, j;
        if (myObject2.size() > 0 && mySQLResultObject.size() > 0) {
            for (i = 0; i < myObject2.size(); i++) {
                ndv = false;
                for (j = 0; j < mySQLResultObject.size(); j++) {
                    if (myObject2.get(i).equals(mySQLResultObject.get(j))) {
                        ndv = true;
                    }
                }

                if (ndv == false) {
                    nonDuplicateValues = nonDuplicateValues + "," + myObject2.get(i);
                }
            }


            //checking opposite case
            for (i = 0; i < mySQLResultObject.size(); i++) {
                ndv = false;
                for (j = 0; j < myObject2.size(); j++) {
                    if (mySQLResultObject.get(i).equals(myObject2.get(j))) {
                        ndv = true;
                    }
                }

                if (ndv == false) {
                    nonDuplicateValues = nonDuplicateValues + "," + mySQLResultObject.get(i);
                }
            }
        }

        if (nonDuplicateValues.equals("")) {
            nonDuplicateValues = "Object 1 and 2 has same keys";
        }
        return nonDuplicateValues;
    }

    //running test
    private void runMyTest() throws IOException {
        //test variables defined
        JsonArray testArray1, testArray2;
        String fPath;

        //test data assigned
        fPath = "\\Users\\safegade\\MyProject\\Automation\\solrservices-qa-automation\\solr\\src\\test\\resources\\TestJson\\LotCollection.json";
        testArray1 = (JsonArray) new JsonParser().parse(readTestFile(fPath));
        fPath = "\\Users\\safegade\\MyProject\\Automation\\solrservices-qa-automation\\solr\\src\\test\\resources\\TestJson\\LotCollection_2.json";
        testArray2 = (JsonArray) new JsonParser().parse(readTestFile(fPath));

        compareTwoJsonArraysByKey(testArray1, testArray2, "lot_number");
    }

    private String readTestFile(String fPath)
            throws IOException {
        FileReader fr = new FileReader(fPath);
        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            //ReportSteps.FAIL(e.getMessage());
            e.printStackTrace();
            //throw e;
        }
        return resultStringBuilder.toString();
    }

    //boolean function
    public static int isThisBoolean(String txt) {
        try {
            if (txt.equalsIgnoreCase("true") || txt.equalsIgnoreCase("false"))
                return 1;
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }

    private Date locateTimeFromString(String txt) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.US);
        return format.parse(txt);
    }

    //method to update Json Object with standard values
    public static JsonObject updateJsonObjectValues(JsonObject jsonObjectToBeUpdated) {
        if (!jsonObjectToBeUpdated.isJsonNull()) {
            JsonObject returnObject = new JsonObject();
            for (String key : jsonObjectToBeUpdated.keySet()) {
                System.out.println("Key: " + key);
                if (jsonObjectToBeUpdated.get(key).isJsonArray()) {
                    JsonArray tArray = jsonObjectToBeUpdated.get(key).getAsJsonArray();
                    if (tArray.get(0).isJsonObject()) //since the Zero number element is a jsonObject, tArray can be considered as JsonObject Array and not normal string or integer array
                    {
                        JsonArray updatedArray = new JsonArray();
                        for (int i = 0; i < tArray.size(); i++) {
                            updatedArray.add(updateJsonObjectValues(tArray.get(i).getAsJsonObject()));
                        }
                        returnObject.add(key, updatedArray);
                    } else { //if string or int array
                        JsonPrimitive j = tArray.get(0).getAsJsonPrimitive();
                        if (j.isNumber())
                            tArray.add(121);
                        else
                            tArray.add("_updTest");

                        returnObject.add(key, tArray);
                    }
                } else if (jsonObjectToBeUpdated.get(key).isJsonObject()) {
                    returnObject.add(key, updateJsonObjectValues(jsonObjectToBeUpdated.get(key).getAsJsonObject()));
                } else if (NumberUtils.isParsable(jsonObjectToBeUpdated.get(key).getAsString())) {
                    returnObject.addProperty(key, (jsonObjectToBeUpdated.get(key).getAsInt() + 11));
                } else if (isThisBoolean(jsonObjectToBeUpdated.get(key).getAsString()) == 1) {
                    boolean b = jsonObjectToBeUpdated.get(key).getAsBoolean() == true ? false : true;
                    returnObject.addProperty(key, b);
                } else if (isDateOrTime(jsonObjectToBeUpdated.get(key).getAsString(), true).get("status").getAsBoolean()) {
                    returnObject.addProperty(key, LocalDateTime.now().toString());
                } else if (isDateOrTime(jsonObjectToBeUpdated.get(key).getAsString(), false).get("status").getAsBoolean()) {
                    returnObject.addProperty(key, LocalDate.now().toString());
                } else {
                    returnObject.addProperty(key, jsonObjectToBeUpdated.get(key).getAsString() + "_updValue");
                }
            }

            return returnObject;
        }

        return null;
    }


    //below method is used for updating keys with mapped keys so that we have same keys between two data sets
    public static JsonArray updateKeys(JsonArray objectWithKeysToBeUpdated, JsonObject keysMapper) {
        if (objectWithKeysToBeUpdated.size() > 0) {
            JsonArray updatedArray = new JsonArray();
            for (int i = 0; i < objectWithKeysToBeUpdated.size(); i++) {
                // updatedArray.addAll(JsonUtils.updateKeys(objectWithKeysToBeUpdated.get(i).getAsJsonObject(), keysMapper));
                updatedArray.add(updateKeys(objectWithKeysToBeUpdated.get(i).getAsJsonObject(), keysMapper));
            }
            return updatedArray;
        }
        return null;
    }

    //below method is used for updating keys with mapped keys so that we have same keys between two data sets
    public static JsonObject updateKeys(JsonObject objectWithKeysToBeUpdated, JsonObject keysMapper) {
        if (!objectWithKeysToBeUpdated.isJsonNull() && !keysMapper.isJsonNull()) {
            JsonObject updateObject = new JsonObject();
            for (String key : objectWithKeysToBeUpdated.keySet()) {
                try {
                    updateObject.add(keysMapper.get(key).getAsString(), objectWithKeysToBeUpdated.get(key));
                } catch (NullPointerException ne) {
                    System.out.println("unmapped key : " + key);
                    updateObject.add(key, objectWithKeysToBeUpdated.get(key));
                    //ne.printStackTrace();
                }
            }
            return updateObject;
        }
        return null;
    }


    //all deprecated methods below
    @Deprecated
    private Date locateDateFromString(String txt) {
        // System.out.println(txt);
        if (txt.length() > 5) {
            String matchValue1 = txt.substring(txt.length() - 2, txt.length());
            String matchValue2 = txt.substring(txt.length() - 1, txt.length());

            String returnValue = null;

            if (matchValue1.equals("AM") || matchValue1.equals("PM")) {

                //String re1="(Aug)";	// Month 1
                String re1 = "((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";    // Month 1
                String re2 = "(\\s+)";    // White Space 1
                String re3 = "((?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // Day 1
                String re4 = "(,)";    // Any Single Character 1
                String re5 = "(\\s+)";    // White Space 2
                String re6 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";    // Year 1
                String re7 = "(\\s+)";    // White Space 3
                String re8 = "(\\d+)";    // Integer Number 1
                String re9 = "(:)";    // Any Single Character 2
                String re10 = "(\\d+)";    // Integer Number 2
                String re11 = "(:)";    // Any Single Character 3
                String re12 = "(\\d+)";    // Integer Number 3
                String re13 = "(\\s+)";    // White Space 4
                String re14 = "((?:[a-z][a-z]+))";    // Word 1
                Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12 + re13 + re14, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(txt);
                if (m.find()) {
                    String month1 = m.group(1);
                    String ws1 = m.group(2);
                    String day1 = m.group(3);
                    String c1 = m.group(4);
                    String ws2 = m.group(5);
                    String year1 = m.group(6);
                    String ws3 = m.group(7);
                    String int1 = m.group(8);
                    String c2 = m.group(9);
                    String int2 = m.group(10);
                    String c3 = m.group(11);
                    String int3 = m.group(12);
                    String ws4 = m.group(13);
                    String word1 = m.group(14);
                    //System.out.print("("+month1.toString()+")"+"("+ws1.toString()+")"+"("+day1.toString()+")"+"("+c1.toString()+")"+"("+ws2.toString()+")"+"("+year1.toString()+")"+"("+ws3.toString()+")"+"("+int1.toString()+")"+"("+c2.toString()+")"+"("+int2.toString()+")"+"("+c3.toString()+")"+"("+int3.toString()+")"+"("+ws4.toString()+")"+"("+word1.toString()+")"+"\n");
                    int monthNumber = 0;
                    switch (month1) {
                        case "Jan":
                            monthNumber = 1;
                        case "Feb":
                            monthNumber = 2;
                        case "Mar":
                            monthNumber = 3;
                        case "Apr":
                            monthNumber = 4;
                        case "May":
                            monthNumber = 5;
                        case "Jun":
                            monthNumber = 6;
                        case "Jul":
                            monthNumber = 7;
                        case "Aug":
                            monthNumber = 8;
                        case "Sep":
                            monthNumber = 9;
                        case "Oct":
                            monthNumber = 10;
                        case "Nov":
                            monthNumber = 11;
                        case "Dec":
                            monthNumber = 12;
                    }

                    returnValue = year1 + "-" + monthNumber + "-" + day1;
                }
            } else if (matchValue2.equals("Z")) {
                String re1 = "(\\d)";    // Any Single Digit 1
                String re2 = "(\\d)";    // Any Single Digit 2
                String re3 = "(\\d)";    // Any Single Digit 3
                String re4 = "(\\d)";    // Any Single Digit 4
                String re5 = "(-)";    // Any Single Character 1
                String re6 = "(\\d)";    // Any Single Digit 5
                String re7 = "(\\d)";    // Any Single Digit 6
                String re8 = "(-)";    // Any Single Character 2
                String re9 = "(\\d)";    // Any Single Digit 7
                String re10 = "(\\d)";    // Any Single Digit 8

                Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(txt.substring(0, 10));
                if (m.find()) {
                    String d1 = m.group(1);
                    String d2 = m.group(2);
                    String d3 = m.group(3);
                    String d4 = m.group(4);
                    String d5 = m.group(6);
                    String d6 = m.group(7);
                    String d7 = m.group(9);
                    String d8 = m.group(10);

                    String year1 = d1 + d2 + d3 + d4;
                    String month1 = d5 + d6;
                    String day1 = d7 + d8;
                    returnValue = year1 + "-" + month1 + "-" + day1;
                    // System.out.print("("+d1.toString()+")"+"("+d2.toString()+")"+"("+d3.toString()+")"+"("+d4.toString()+")"+"("+c1.toString()+")"+"("+d5.toString()+")"+"("+d6.toString()+")"+"("+c2.toString()+")"+"("+d7.toString()+")"+"("+d8.toString()+")"+"("+w1.toString()+")"+"("+d9.toString()+")"+"("+d10.toString()+")"+"("+c3.toString()+")"+"("+d11.toString()+")"+"("+d12.toString()+")"+"("+c4.toString()+")"+"("+d13.toString()+")"+"("+d14.toString()+")"+"("+w2.toString()+")"+"\n");
                }
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd", Locale.US);
                if (returnValue != null)
                    return format.parse(returnValue);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }// if txt.lentgh>5

        return null;
    }
//

    //method to add uniqe key
    public JsonArray composeMergedKey(JsonArray jArray, String columnNamesCommaSeparated) {
        List<String> columns = new ArrayList<String>(Arrays.asList(columnNamesCommaSeparated.split(",")));
        String mergedValue = "";
        JsonArray returnArray = new JsonArray();
        for (int i = 0; i < jArray.size(); i++) {
            JsonObject t2Object = jArray.get(i).getAsJsonObject();
            for (String key : columns) {
                if (t2Object.keySet().contains(key)) {
                    mergedValue = mergedValue.concat(" " + t2Object.get(key).getAsString());
                    t2Object.remove(key);
                }
            }
            t2Object.addProperty(columnNamesCommaSeparated, mergedValue);
            mergedValue = "";
            returnArray.add(t2Object);
        }
        System.out.println("return array : " + returnArray);
        return returnArray;
    }

    public JsonArray composeMergedKey(JsonArray jArray, String... columnNames) {
        String mergedValue = "";
        String mergedColumnName = Arrays.toString(columnNames);
        JsonArray returnArray = new JsonArray();
        for (int i = 0; i < jArray.size(); i++) {
            JsonObject t2Object = jArray.get(i).getAsJsonObject();
            for (String key : columnNames) {
                if (t2Object.keySet().contains(key)) {
                    mergedValue = mergedValue.concat(" " + t2Object.get(key).getAsString());
                    t2Object.remove(key);
                }
            }
            t2Object.addProperty(mergedColumnName, mergedValue);
            mergedValue = "";
            returnArray.add(t2Object);
        }
        System.out.println("return array : " + returnArray);
        return returnArray;
    }

    public static JsonObject concatJsonObjects(JsonObject object1, JsonObject object2) {
        JsonObject finalJson = new JsonObject();
        JsonObject returnJson = new JsonObject();
        for (String key : object1.getAsJsonObject().keySet()) {
            for (String key1 : object1.get(key).getAsJsonObject().keySet()) {
                finalJson.add(key1, object1.get(key).getAsJsonObject().get(key1));
            }
        }
        for (String key : object2.getAsJsonObject().keySet()) {
            for (String key1 : object2.get(key).getAsJsonObject().keySet()) {
                finalJson.add(key1, object2.get(key).getAsJsonObject().get(key1));
            }
        }
        returnJson.add("cfr&dispatch", finalJson);
        return returnJson;
    }

}//class end
