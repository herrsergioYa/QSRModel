package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class HyperGammaDistribution implements Distribution {

    private double[] alphas;
    private double[] means;
    private double[] orders;

    public HyperGammaDistribution(double[] alphas, double[] means, double[] orders) {
        this.alphas = alphas;
        this.means = means;
        this.orders = orders;
        if(means.length != orders.length)
            throw new IllegalArgumentException();
    }

    public static double generate(Random random, double[] alphas, double[] means, double[] orders) {
        int event = PhaseDistribution.sim(random, alphas);
        if(event >= means.length)
            return 0;
        else
            return GammaDistribution.generate(random, means[event], orders[event]);
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

    public static HyperGammaDistribution fromGson(JsonObject object) {
        return new HyperGammaDistribution(
                Distribution.fromGson(object, new double[0], "alphas"),
                Distribution.fromGson(object, new double[0], "means"),
                Distribution.fromGson(object, new double[0], "orders")
        );
    }
}
