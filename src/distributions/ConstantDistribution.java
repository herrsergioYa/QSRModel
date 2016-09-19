package distributions;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 19.08.2016.
 */
public class ConstantDistribution implements Distribution {

    private double value;

    public ConstantDistribution(double value) {
        this.value = value;
    }

    @Override
    public double next(Random random) {
        return value;
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        object.add("value", new JsonPrimitive(value));
        return object;
    }

    public static ConstantDistribution fromGson(JsonObject object) {
        if(object.has("value"))
            return new ConstantDistribution(object.get("value").getAsDouble());
        else
            return new ConstantDistribution(Distribution.getMean(object));
    }
}
