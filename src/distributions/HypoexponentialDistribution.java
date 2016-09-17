package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class HypoexponentialDistribution implements Distribution {
    private double means[];

    public HypoexponentialDistribution(double[] means) {
        this.means = means;
    }

    @Override
    public double next(Random random) {
        return generate(random, means);
    }

    public static double generate(Random random, double[] means) {
        double value = 0;
        for(int i = 0; i < means.length; i++) {
            value += ExponentialDistribution.generate(random, means[i]);
        }
        return value;
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        Distribution.toGson(object, means, "means");
        return object;
    }

    public static HypoexponentialDistribution fromGson(JsonObject object) {
        return new HypoexponentialDistribution(
                Distribution.fromGson(object, new double[0], "means")
        );
    }

}
