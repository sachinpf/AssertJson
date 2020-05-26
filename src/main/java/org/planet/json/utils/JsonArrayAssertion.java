package org.planet.json.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonArrayAssertion {
    private List<Object> missingIDs;
    private List<JsonObjectAssertion> jsonObjectAssertions;
    private boolean status;
    private String statusMessage;
    private Integer firstArraySize;
    private Integer secondArraySize;
    private Long countOfFailedObjects;

    public JsonArrayAssertion() {
        status = true;
        missingIDs = new ArrayList<>();
        jsonObjectAssertions = new ArrayList<>();
    }

    public List<Object> getMissingIDs() {
        return missingIDs;
    }

    public void addMissingIDs(Object ID) {
        this.missingIDs.add(ID);
    }

    public List<JsonObjectAssertion> getJsonObjectAssertions() {
        return jsonObjectAssertions;
    }

    public void addJsonObjectAssertions(JsonObjectAssertion jsonObjectAssertions) {
        this.jsonObjectAssertions.add(jsonObjectAssertions);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setMissingIDs(List<Object> missingIDs) {
        this.missingIDs = missingIDs;
    }

    public void setJsonObjectAssertions(List<JsonObjectAssertion> jsonObjectAssertions) {
        this.jsonObjectAssertions = jsonObjectAssertions;
    }

    public Integer getFirstArraySize() {
        return firstArraySize;
    }

    public void setFirstArraySize(Integer firstArraySize) {
        this.firstArraySize = firstArraySize;
    }

    public Integer getSecondArraySize() {
        return secondArraySize;
    }

    public void setSecondArraySize(Integer secondArraySize) {
        this.secondArraySize = secondArraySize;
    }

    public Long getCountOfFailedObjects() {
        return countOfFailedObjects;
    }

    public void setCountOfFailedObjects(Long countOfFailedObjects) {
        this.countOfFailedObjects = countOfFailedObjects;
    }

    //returns only failed one
    public JsonArrayAssertion getFailedOnly() {
        List<JsonObjectAssertion> jsonObjectAssertions = new ArrayList<>();

        jsonObjectAssertions =
                this.jsonObjectAssertions
                        .stream()
                        .parallel()
                        .filter(o -> !o.isStatus())
                        .collect(Collectors.toList());

        if (jsonObjectAssertions.size() > 0)
            this.status = false;

        this.countOfFailedObjects =
                StreamSupport.stream(jsonObjectAssertions.spliterator(),true)
                .parallel()
                .filter(o->!o.isStatus())
                .count();

        JsonArrayAssertion returnAssertions = new JsonArrayAssertion();
        returnAssertions.setCountOfFailedObjects(this.countOfFailedObjects);
        returnAssertions.setStatusMessage(this.statusMessage);
        returnAssertions.setStatus(this.status);
        returnAssertions.setFirstArraySize(this.firstArraySize);
        returnAssertions.setSecondArraySize(this.secondArraySize);
        returnAssertions.setMissingIDs(this.missingIDs);
        returnAssertions.setJsonObjectAssertions(jsonObjectAssertions);

        return returnAssertions;
    }

    public JsonObject getAsJsonObject() {
        Gson gson = new Gson();

        JsonObject returnObject = new JsonObject();
        returnObject.addProperty("firstArraySize",this.firstArraySize);
        returnObject.addProperty("secondArraySize",this.secondArraySize);
        returnObject.addProperty("status", this.status);
        returnObject.addProperty("statusMessage",this.statusMessage);
        returnObject.addProperty("countOfFailedObjects",this.countOfFailedObjects);

        JsonArray tArray2 = gson.fromJson(missingIDs.toString(), JsonArray.class);
        returnObject.add("missingIDs", tArray2);

        JsonArray jArray = new JsonArray();

        StreamSupport.stream(jsonObjectAssertions.spliterator(), true)
                .parallel()
                .filter(o -> !o.isStatus())
                .forEach(o -> {
                    jArray.add(o.getAsJsonObject());
                        }
                );

        returnObject.add("NonEqualObjects", jArray);

        return returnObject;
    }

    @Override
    public String toString() {
        return "JsonArrayAssertion{" +
                ", missingIDsInSecondObject=" + missingIDs +
                ", jsonObjectAssertions=" + jsonObjectAssertions +
                ", status=" + status +
                '}';
    }
}
