package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 20.08.2016.
 */
public class PhaseDistribution implements Distribution {

    private double[] alphas;
    private double[][] lambdas;
    private double[][] ps;

    public PhaseDistribution(double[] alphas, double[][] lambdas) {
        this.alphas = alphas;
        this.lambdas = lambdas;
        this.ps = new double[lambdas.length][];
        for(int i = 0; i < lambdas.length; i++) {
            double l = - lambdas[i][i];
            if(l < 0)
                throw new IllegalArgumentException();
            ps[i] = new double[lambdas.length];
            if(l == 0.0)
                continue;
            for(int j = 0; j < lambdas.length; j++) {
                if(j != i) {
                    ps[i][j] = lambdas[i][j] / l;
                }
            }
        }
    }

    public static int sim(Random random, double[] ps)  {
        double p = random.nextDouble();
        for(int i = 0 ; i < ps.length; i++) {
            p -= ps[i];
            if(p < 0.0)
                return i;
        }
        return ps.length;
    }

    @Override
    public double next(Random random) {

        double value = 0.0;

        for(int i = sim(random, alphas);
            i >= 0 && i < lambdas.length;
            i = sim(random, ps[i])) {

            double l = -lambdas[i][i];

            if(l <= 0)
                return Double.POSITIVE_INFINITY;

            value -= Math.log(1.0 - random.nextDouble()) / l;
        }

        return value;
    }

    public static PhaseDistribution fromGson(JsonObject object) {
        JsonArray alphas = object.getAsJsonArray("alphas");
        double[] a = new double[alphas.size()];
        for(int i = 0; i < alphas.size(); i++) {
            a[i] = alphas.get(i).getAsDouble();
        }
        JsonArray lambdas = object.getAsJsonArray("lambdas");
        double[][] l = new double[lambdas.size()][];
        for(int i = 0; i < lambdas.size(); i++) {
            JsonArray lambda = lambdas.get(i).getAsJsonArray();
            l[i] = new double[lambda.size()];
            for(int j = 0; j < lambda.size(); j++) {
                l[i][j] = lambda.get(j).getAsDouble();
            }
        }
        return new PhaseDistribution(a, l);
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        JsonArray alphas = new JsonArray();
        for(double alpha : this.alphas) {
            alphas.add(new JsonPrimitive(alpha));
        }
        JsonArray lambdas = new JsonArray();
        for(double[] lambda : this.lambdas) {
            JsonArray buf = new JsonArray();
            for(double l : lambda) {
                buf.add(new JsonPrimitive(l));
            }
            lambdas.add(buf);
        }
        object.add("alphas", alphas);
        object.add("lambdas", lambdas);
        return object;
    }
}
