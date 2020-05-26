package org.planet.Json.jsonUtils.support;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.planet.Json.jsonUtils.exceptions.IDKeyNameNotFound;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class KeyIndexCallable implements Callable<Map<Object, Integer>> {

    private int startNumber;
    private int endNumber;
    private JsonArray secondArray;
    private String IdKeyName;
    private Gson gson;

    public KeyIndexCallable(int startNumber, int endNumber, JsonArray secondArray, String IdKeyName) {
        this.startNumber = startNumber;
        this.endNumber = Math.min(endNumber, (secondArray.size()-1));
        this.secondArray = secondArray;
        this.IdKeyName = IdKeyName;
        gson = new Gson();
    }

    @Override
    public Map<Object, Integer> call() throws Exception {
        return process();
    }

    public Map<Object, Integer> process() throws IDKeyNameNotFound {
        Map<Object, Integer> returnMap = new HashMap<>();

        for (int i = startNumber; i <= endNumber; i++) {
            Object id = gson.fromJson(secondArray.get(i).getAsJsonObject().get(IdKeyName),Object.class);

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
