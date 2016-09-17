package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * Created by HerrSergio on 28.08.2016.
 */
public class SemiPhaseDistribution implements Distribution {

    private double[] alphas;
    private Distribution[][] distr;

    public SemiPhaseDistribution(double[] alphas, Distribution[][] distr) {
        this.alphas = alphas;
        this.distr = distr;
    }

    @Override
    public double next(Random random) {

        double value = 0.0;

        for(int i = PhaseDistribution.sim(random, alphas);
            i >= 0 && i < distr.length;
                ) {

            double newValue = Double.POSITIVE_INFINITY;
            int event= - 1;
            for(int j = 0; j < distr[i].length; j++) {
                double buf = distr[i][j].next(random);
                if(buf < newValue) {
                    newValue = buf;
                    event = j;
                }
            }

            value += newValue;
            i = event;
        }

        return value;
    }

    public static SemiPhaseDistribution fromGson(JsonObject object) {
        JsonArray alphas = object.getAsJsonArray("alphas");
        double[] a = new double[alphas.size()];
        for(int i = 0; i < alphas.size(); i++) {
            a[i] = alphas.get(i).getAsDouble();
        }
        JsonArray ds = object.getAsJsonArray("distr");
        Distribution[][] l = new Distribution[ds.size()][];
        for(int i = 0; i < ds.size(); i++) {
            JsonArray d = ds.get(i).getAsJsonArray();
            l[i] = new Distribution[d.size()];
            for(int j = 0; j < d.size(); j++) {
                try {
                    l[i][j] = Distribution.formGson(d.get(j).getAsJsonObject());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new SemiPhaseDistribution(a, l);
    }

    @Override
    public JsonObject toGson() {
        JsonObject object = Distribution.super.toGson();
        JsonArray alphas = new JsonArray();
        for(double alpha : this.alphas) {
            alphas.add(new JsonPrimitive(alpha));
        }
        JsonArray ds = new JsonArray();
        for(Distribution[] d : this.distr) {
            JsonArray buf = new JsonArray();
            for(Distribution d1 : d) {
                buf.add(d1.toGson());
            }
            ds.add(buf);
        }
        object.add("alphas", alphas);
        object.add("distr", ds);
        return object;
    }
}
