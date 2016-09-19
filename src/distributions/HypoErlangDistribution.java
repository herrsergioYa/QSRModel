package distributions;

import com.google.gson.JsonObject;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class HypoErlangDistribution implements Distribution {
    private double means[];
    private int[] orders;

    public HypoErlangDistribution(double[] means, int[] orders) {
        this.means = means;
        this.orders = orders;
        if(means.length != orders.length)
            throw new IllegalArgumentException();
    }

    @Override
    public double next(Random random) {
        return generate(random, means, orders);
    }

    public static double generate(Random random, double[] means, int[] orders) {
        double value = 0;
        for(int i = 0; i < means.length; i++) {
            value += ErlangDistribution.generate(random, means[i], orders[i]);
        }
        return value;
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        Distribution.toGson(object, means, "means");
        Distribution.toGson(object, orders, "orders");
        return object;
    }

    public static HypoErlangDistribution fromGson(JsonObject object) {
        return new HypoErlangDistribution(
                Distribution.getMeans(object),
                Distribution.fromGson(object, new int[0], "orders")
        );
    }
}
