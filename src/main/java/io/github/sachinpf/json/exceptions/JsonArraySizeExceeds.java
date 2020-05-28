package io.github.sachinpf.json.exceptions;

public class JsonArraySizeExceeds extends Exception{
    public JsonArraySizeExceeds(){
        super("Difference of size in both arrays exceeds 500.");
    }
}
