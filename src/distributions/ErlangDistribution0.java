package distributions;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class ErlangDistribution0 implements Distribution {
    private double mean;
    private int order;

    public ErlangDistribution0(double mean, int order) {
        this.mean = mean;
        this.order = order;
    }

    @Override
    public double next(Random random) {
        return generate(random, mean, order);
    }

    public static double generate(Random random, double mean, int order) {
        return ErlangDistribution.generate(random, mean, order + 1);
    }

    public static ErlangDistribution0 fromGson(JsonObject object) {
        return new ErlangDistribution0(object.get("mean").getAsDouble(), object.get("order").getAsInt());
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        object.add("mean", new JsonPrimitive(mean));
        object.add("order", new JsonPrimitive(order));
        return object;
    }
}
