package distributions;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class ErlangDistribution implements Distribution {
    private double mean;
    private int order;

    public ErlangDistribution(double mean, int order) {
        this.mean = mean;
        this.order = order;
    }

    @Override
    public double next(Random random) {
        return generate(random, mean, order);
    }

    public static double generate(Random random, double mean, int order) {
        if(order > 50) {
            return mean * Math.abs(1.0 + random.nextGaussian() / Math.sqrt(order));
        }

        if(order <= 0) {
            throw new IllegalStateException();
        }

        double value = 0.0;

        for(int i = 0; i < order; i++)
            value += ExponentialDistribution.generate(random, mean);

        return value / order;
    }

    public static ErlangDistribution fromGson(JsonObject object) {
        return new ErlangDistribution(object.get("mean").getAsDouble(), object.get("order").getAsInt());
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        object.add("mean", new JsonPrimitive(mean));
        object.add("order", new JsonPrimitive(order));
        return object;
    }
}
