package testUtils.testDataCreator.pojo;

import com.google.gson.JsonObject;
import testUtils.testDataCreator.pojo.random.RandomGenerator;

import java.util.Objects;

public class InventoryInfo {
    private boolean outOfStock;
    private String lastStockCheckedDate;
    private float decimalValue;
    private String autoReorder;

    public InventoryInfo(boolean generateDefault) {
        if (generateDefault) {
            outOfStock = RandomGenerator.randomBoolean();
            decimalValue = RandomGenerator.randomInt(2);
            autoReorder = "Y";
            lastStockCheckedDate = "2018-02-19T10:59:23Z";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryInfo)) return false;
        InventoryInfo that = (InventoryInfo) o;
        return isOutOfStock() == that.isOutOfStock() &&
                Float.compare(that.getDecimalValue(), getDecimalValue()) == 0 &&
                Objects.equals(getLastStockCheckedDate(), that.getLastStockCheckedDate()) &&
                Objects.equals(getAutoReorder(), that.getAutoReorder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOutOfStock(), getLastStockCheckedDate(), getDecimalValue(), getAutoReorder());
    }

    public JsonObject toJsonObject() {
        JsonObject r = new JsonObject();
        r.addProperty("OutOfStock", outOfStock);
        r.addProperty("LastStockCheckedDate", lastStockCheckedDate);
        r.addProperty("DecimalValue", decimalValue);
        r.addProperty("AutoReorder", autoReorder);
        return r;
    }


    public boolean isOutOfStock() {
        return outOfStock;
    }

    public String getLastStockCheckedDate() {
        return lastStockCheckedDate;
    }

    public float getDecimalValue() {
        return decimalValue;
    }

    public String getAutoReorder() {
        return autoReorder;
    }
}
