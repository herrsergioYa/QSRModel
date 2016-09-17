package taskcomp;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import distributions.Distribution;
import qssim.Task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Created by HerrSergio on 19.08.2016.
 */
public abstract class TaskComparator implements Comparator<Task> {

    public JsonObject toGson() {
        JsonObject object = new JsonObject();
        object.add("class", new JsonPrimitive(getClass().getCanonicalName()));
        return object;
    }

    public static TaskComparator formGson(JsonObject object) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if(!object.has("class"))
            throw new NoSuchElementException();
        Class holder = Class.forName(object.get("class").getAsString());
        if(holder == TaskComparator.class || !TaskComparator.class.isAssignableFrom(holder))
            throw new ClassNotFoundException();
        try {
            Method method = holder.getMethod("fromGson", JsonObject.class);
            if ((method.getModifiers() & Modifier.STATIC) == 0 || !TaskComparator.class.isAssignableFrom(method.getReturnType()))
                throw new NoSuchMethodException();
            return (TaskComparator) method.invoke(null, object);
        } catch (NoSuchMethodException e) {
            return (TaskComparator)holder.newInstance();
        }
    }
}
