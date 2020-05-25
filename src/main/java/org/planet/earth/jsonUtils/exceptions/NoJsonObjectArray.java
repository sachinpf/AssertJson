package org.planet.earth.jsonUtils.exceptions;

public class NoJsonObjectArray extends Exception{
    public NoJsonObjectArray(){
        super("No json object array found. This method works only with JsonObject array.");
    }
}
