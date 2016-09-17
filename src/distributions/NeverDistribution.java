package distributions;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

/**
 * Created by HerrSergio on 19.08.2016.
 */
public class NeverDistribution implements Distribution {
    @Override
    public double next(Random random) {
        return Double.POSITIVE_INFINITY;
    }
    }
