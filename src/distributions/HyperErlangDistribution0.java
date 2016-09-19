package distributions;

import com.google.gson.JsonObject;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class HyperErlangDistribution0 implements Distribution {
    private double[] alphas;
    private double[] means;
    private int[] orders;

    public HyperErlangDistribution0(double[] alphas, double[] means, int[] orders) {
        this.alphas = alphas;
        this.means = means;
        this.orders = orders;
        if(means.length != orders.length)
            throw new IllegalArgumentException();
    }

    public static double generate(Random random, double[] alphas, double[] means, int[] orders) {
        int event = PhaseDistribution.sim(random, alphas);
        if(event >= means.length)
            return 0;
        else
            return ErlangDistribution0.generate(random, means[event], orders[event]);
    }

    @Override
    public double next(Random random) {
        return generate(random, alphas, means, orders);
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        Distribution.toGson(object, alphas, "alphas");
        Distribution.toGson(object, means, "means");
        Distribution.toGson(object, orders, "orders");
        return object;
    }

    public static HyperErlangDistribution fromGson(JsonObject object) {
        return new HyperErlangDistribution(
                Distribution.fromGson(object, new double[0], "alphas"),
                Distribution.getMeans(object),
                Distribution.fromGson(object, new int[0], "orders")
        );
    }
}
