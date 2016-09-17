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
}
