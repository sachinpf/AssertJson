package io.github.sachinpf.json.exceptions;

public class ExceedsLimitOfObjectSize extends Exception {
    //thrown only for JsonObject
    public ExceedsLimitOfObjectSize() {
        super("Json Object or Json Array exceed the size allowed for assertion.");
    }
}
