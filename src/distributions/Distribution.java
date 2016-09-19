package distributions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public interface Distribution {
    double next(Random random);

    default JsonObject toGson() {
        JsonObject object = new JsonObject();
        object.add("class", new JsonPrimitive(getClass().getCanonicalName()));
        return object;
    }

    static Distribution formGson(JsonObject object) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if(!object.has("class"))
            throw new NoSuchElementException();
        Class holder = Class.forName(object.get("class").getAsString());
        if(holder == Distribution.class || !Distribution.class.isAssignableFrom(holder))
            throw new ClassNotFoundException();
        try {
            Method method = holder.getMethod("fromGson", JsonObject.class);
            if ((method.getModifiers() & Modifier.STATIC) == 0 || !Distribution.class.isAssignableFrom(method.getReturnType()))
                throw new NoSuchMethodException();
            return (Distribution) method.invoke(null, object);
        } catch (NoSuchMethodException e) {
            return (Distribution)holder.newInstance();
        }
    }

    static double[] fromGson(JsonObject object, double[] unused, String name) {
        JsonArray orders = object.getAsJsonArray(name);
        double[] o = new double[orders.size()];
        for(int i = 0; i < orders.size(); i++) {
            o[i] = orders.get(i).getAsDouble();
        }
        return o;
    }

    static void toGson(JsonObject object, double[] o, String name) {
        JsonArray orders = new JsonArray();
        for(double order : o) {
            orders.add(new JsonPrimitive(order));
        }
        object.add(name, orders);
    }

    static int[] fromGson(JsonObject object, int[] unused, String name) {
        JsonArray orders = object.getAsJsonArray(name);
        int[] o = new int[orders.size()];
        for(int i = 0; i < orders.size(); i++) {
            o[i] = orders.get(i).getAsInt();
        }
        return o;
    }

    static void toGson(JsonObject object, int[] o, String name) {
        JsonArray orders = new JsonArray();
        for(int order : o) {
            orders.add(new JsonPrimitive(order));
        }
        object.add(name, orders);
    }
    
    static double getMean(JsonObject object) {
        if(object.has("mean") == object.has("hazard")) {
            throw new IllegalArgumentException();
        } else if(object.has("mean")) {
            return object.get("mean").getAsDouble();
        } else {
            return 1.0/object.get("hazard").getAsDouble();
        }
    }
    
    static double getOrder(JsonObject object) {
        if(object.has("order") == object.has("cov")) {
            throw new IllegalArgumentException();
        } else if(object.has("order")) {
            return object.get("order").getAsDouble();
        } else {
            double cov = object.get("cov").getAsDouble();
            return 1.0/(cov * cov);
        }
    }
    
    static double[] getMeans(JsonObject object) {
        if(object.has("means") == object.has("hazards"))
            throw new IllegalArgumentException();
        double[] val = null;
        if(object.has("means"))
            return fromGson(object, val, "means");
        val = fromGson(object, val, "hazards");
        for(int i = 0; i < val.length; i++)
            val[i] = 1.0 / val[i];
        return val;
    }
    
    static double[] getOrders(JsonObject object) {
        if(object.has("orders") == object.has("covs"))
            throw new IllegalArgumentException();
        double[] val = null;
        if(object.has("orders"))
            return fromGson(object, val, "orders");
        val = fromGson(object, val, "covs");
        for(int i = 0; i < val.length; i++)
            val[i] = 1.0 / (val[i] * val[i]);
        return val;
    }
}
