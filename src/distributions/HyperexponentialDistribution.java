package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 27.08.2016.
 */
public class HyperexponentialDistribution implements Distribution {

    private double alphas[], means[];

    public HyperexponentialDistribution(double[] alphas, double[] means) {
        this.alphas = alphas;
        this.means = means;
    }

    @Override
    public double next(Random random) {
        return generate(random, alphas, means);
    }

    public static double generate(Random random, double[] alphas, double[] means) {
        int event = PhaseDistribution.sim(random, alphas);
        if(event >= means.length)
            return 0;
        else
            return ExponentialDistribution.generate(random, means[event]);
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        Distribution.toGson(object, alphas, "alphas");
        Distribution.toGson(object, means, "means");
        return object;
    }

    public static HyperexponentialDistribution fromGson(JsonObject object) {
        return new HyperexponentialDistribution(
                Distribution.fromGson(object, new double[0], "alphas"),
                Distribution.fromGson(object, new double[0], "means")
        );
    }
}
