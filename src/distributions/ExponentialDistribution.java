package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class ExponentialDistribution implements Distribution {
    private double mean;

    public ExponentialDistribution(double mean) {
        this.mean = mean;
    }

    @Override
    public double next(Random random) {
        return generate(random, mean);
    }

    public static double generate(Random random, double mean) {
        return - mean * Math.log(1.0 - random.nextDouble());
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        object.add("mean", new JsonPrimitive(mean));
        return object;
    }

    public static ExponentialDistribution fromGson(JsonObject object) {
        return new ExponentialDistribution(object.get("mean").getAsDouble());
    }
}
