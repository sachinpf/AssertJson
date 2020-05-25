package org.planet.earth.jsonUtils.support;

import com.google.gson.JsonArray;
import org.planet.earth.jsonUtils.exceptions.IDKeyNameNotFound;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class KeyIndexCallable implements Callable<Map<Object, Integer>> {

    private int startNumber;
    private int endNumber;
    private JsonArray secondArray;
    private String IdKeyName;

    public KeyIndexCallable(int startNumber, int endNumber, JsonArray secondArray, String IdKeyName) {
        this.startNumber = startNumber;
        this.endNumber = Math.min(endNumber, (secondArray.size()-1));
        this.secondArray = secondArray;
        this.IdKeyName = IdKeyName;

        System.out.println("Start and end: " + startNumber + " : " + endNumber);
    }

    @Override
    public Map<Object, Integer> call() throws Exception {
        return process();
    }

    public Map<Object, Integer> process() throws IDKeyNameNotFound {
        Map<Object, Integer> returnMap = new HashMap<>();

        for (int i = startNumber; i <= endNumber; i++) {
            Object id = secondArray.get(i).getAsJsonObject().get(IdKeyName);

            if (id == null) {
                throw new IDKeyNameNotFound();
            }
            //checking if ID is already present, adds duplicate flag
            if (returnMap.containsKey(id)) {
                returnMap.put((id.toString() + "_duplicate"), i);
            } else {
                returnMap.put(id, i);
            }
        }

        return returnMap;
    }
}
