package org.planet.Json.jsonUtils.exceptions;

public class ExceedsLimitOfObjectSize extends Exception {

    public ExceedsLimitOfObjectSize() {
        super("Json Object or Json Array exceed the size allowed for assertion.");
    }
}
