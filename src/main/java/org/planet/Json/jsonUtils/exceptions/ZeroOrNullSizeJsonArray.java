package org.planet.Json.jsonUtils.exceptions;

public class ZeroOrNullSizeJsonArray extends Exception{
    public ZeroOrNullSizeJsonArray(){
        super("One of the Json Array has no objects in it.");
    }
}
