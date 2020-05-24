package org.planet.earth.jsonUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.*;

public class JsonObjectAssertion {

    private boolean status;
    private Object ID;
    private String object1Name;
    private String object2Name;
    private String missingKeysInObject1;
    private String missingKeysInObject2;
    private JsonArray nonEqualKeys;
    private String statusMessage;

    public JsonObjectAssertion() {
        ID = null;
        object1Name = "firstObject";
        object2Name = "secondObject";
        missingKeysInObject1 = "";
        missingKeysInObject2 = "";
        nonEqualKeys = new JsonArray();
        status = true;
        statusMessage = "No assertions found";
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Object getID() {
        return ID;
    }

    public void setID(Object ID) {
        this.ID = ID;
    }

    public String getObject1Name() {
        return object1Name;
    }

    public void setObject1Name(String object1Name) {
        this.object1Name = object1Name;
    }

    public String getObject2Name() {
        return object2Name;
    }

    public void setObject2Name(String object2Name) {
        this.object2Name = object2Name;
    }

    public String getMissingKeysInObject1() {
        return missingKeysInObject1;
    }

    public void addMissingKeysInObject1(String missingKeysInObject1) {
        if (this.missingKeysInObject1 == "")
            this.missingKeysInObject1 = missingKeysInObject1;
        else
            this.missingKeysInObject1 = this.missingKeysInObject1 + "," + missingKeysInObject1;
    }

    public String getMissingKeysInObject2() {
        return missingKeysInObject2;
    }

    public void addMissingKeysInObject2(String missingKeysInObject2) {
        if (this.missingKeysInObject2 == "")
            this.missingKeysInObject2 = missingKeysInObject2;
        else
            this.missingKeysInObject2 = this.missingKeysInObject2 + "," + missingKeysInObject2;
    }

    public JsonArray getNonEqualKeys() {
        return nonEqualKeys;
    }


    public void addNonEqualKeys(JsonObject t) {
        this.nonEqualKeys.add(t);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonObjectAssertion)) return false;
        JsonObjectAssertion that = (JsonObjectAssertion) o;
        return isStatus() == that.isStatus() &&
                getID().equals(that.getID()) &&
                getObject1Name().equals(that.getObject1Name()) &&
                getObject2Name().equals(that.getObject2Name()) &&
                getMissingKeysInObject1().equals(that.getMissingKeysInObject1()) &&
                getMissingKeysInObject2().equals(that.getMissingKeysInObject2()) &&
                getNonEqualKeys().equals(that.getNonEqualKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isStatus(), getID(), getObject1Name(), getObject2Name(), getMissingKeysInObject1(), getMissingKeysInObject2(), getNonEqualKeys());
    }

    @Override
    public String toString() {
        String strID = (ID != null ? (", ID=" + ID) + "\n" : "");
        String missingKeys1 = missingKeysInObject1 != "" ? (", " + object1Name + " missing keys=" + missingKeysInObject1 + "\n") : "";
        String missingKeys2 = missingKeysInObject2 != "" ? (", " + object2Name + " missing keys=" + missingKeysInObject2 + "\n") : "";
        String nonEqualKeys = "";

        if (this.nonEqualKeys.size() > 0) {
            nonEqualKeys = "nonEqualKeys" + this.nonEqualKeys.toString();
        }

        String str =
                "status=" + status + "\n" +
                        strID +
                        missingKeys1 +
                        missingKeys2 +
                        nonEqualKeys;

        return str;
    }
}


