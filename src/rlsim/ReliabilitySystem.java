package rlsim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import distributions.Distribution;
import distributions.ExponentialDistribution;
import javenue.csv.Csv;
import taskcomp.TaskComparator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by HerrSergio on 09.09.2016.
 */
public class ReliabilitySystem implements Runnable, Callable<LinkedHashMap<String, Value>> {
    private Random random;
    private Distribution lambda;
    private Distribution mu;
    private int n, m, l, r;
    private int serviceCount;
    private int rCurrent;
    private double timeLimit;

    private PriorityQueue<Double> breaks = new PriorityQueue<>();
    private PriorityQueue<Double> services = new PriorityQueue<>();
    private int servicesPending;
    private double currentTime;

    private Counter[] brokenCount, activeCount;
    private int lastBrokenCount, lastActiveCount;

    public ReliabilitySystem(int seed, Distribution lambda, Distribution mu, int n, int m, int l, int r, int serviceCount, double timeLimit) {
        this.random = new Random(seed);
        this.lambda = lambda;
        if(n < m || l < 1 || l > m || r < 0 || serviceCount < 1)
            throw new IllegalArgumentException();
        this.mu = mu;
        this.n = n;
        this.m = m;
        this.l = l;
        this.r = r;
        this.serviceCount = serviceCount;
        this.timeLimit = timeLimit;
        for(int i = 0; i < n; i++)
            breaks.add(lambda.next(random));
        this.rCurrent = r;
        this.brokenCount = new Counter[n + r - (l - 1) + 1];
        for(int i = 0; i < brokenCount.length; i++) {
            brokenCount[i] = new Counter();
            brokenCount[i].in(currentTime);
        }
        this.activeCount = new Counter[n + 1];
        for(int i = 0; i < activeCount.length; i++){
            activeCount[i] = new Counter();
            activeCount[i].in(currentTime);
        }
        this.lastActiveCount = n;
        this.lastBrokenCount = 0;
    }


    @Override
    public void run() {
        while(true) {
            Boolean nextBreak = null;
            double time = Double.POSITIVE_INFINITY;
            if (getActiveCount() > 0 && time > breaks.peek()) {
                time = breaks.peek();
                nextBreak = true;
            }
            if (getBrokenCount() > 0 && time > services.peek()) {
                time = services.peek();
                nextBreak = false;
            }
            if(time > timeLimit) {
                currentTime = timeLimit;
                break;
            }
            if (nextBreak) {
                currentTime = breaks.poll();
                if(services.size() < serviceCount)
                    generateNewRecover();
                else
                    servicesPending++;
                if(getActiveCount() < m) {
                    if(rCurrent > 0) {
                        rCurrent--;
                        generateNewBreak();
                    }
                }
                if(getActiveCount() < l) {
                    double delta = this.services.peek() - currentTime;
                    PriorityQueue<Double> breaks = new PriorityQueue<>();
                    for(Double d : this.breaks)
                        breaks.add(d + delta);
                    this.breaks = breaks;
                }
            } else {
                currentTime = services.poll();
                if(servicesPending > 0) {
                    generateNewRecover();
                    servicesPending--;
                }
                if(getActiveCount() < m) {
                    generateNewBreak();
                } else if(rCurrent < r) {
                    rCurrent++;
                } else {
                    generateNewBreak();
                }
            }
            adjustValues();
        }
    }

    private void generateNewRecover() {
        services.add(currentTime + mu.next(random));
    }

    private void generateNewBreak() {
        breaks.add(currentTime + lambda.next(random));
    }

    private void adjustValues() {
        while(lastBrokenCount > getBrokenCount()) {
            brokenCount[--lastBrokenCount].in(currentTime);
        }
        while(lastBrokenCount < getBrokenCount()) {
            brokenCount[lastBrokenCount++].out(currentTime);
        }
        while(lastActiveCount > getActiveCount()) {
            activeCount[lastActiveCount--].out(currentTime);
        }
        while(lastActiveCount < getActiveCount()) {
            activeCount[++lastActiveCount].in(currentTime);
        }
    }

    private int getActiveCount() {
        return breaks.size();
    }

    private int getBrokenCount() {
        return services.size() + servicesPending;
    }

    public LinkedHashMap<String, Value> getValues() {
        LinkedHashMap<String, Value> map = new LinkedHashMap<>();
        for(int i = 0; i < brokenCount.length - 1; i++) {
            String name = String.format("brokenCount %s %d",
                    i == 0 ? "=" : "<=",
                    i
            );
            double pValue = brokenCount[i].getAverageCount(currentTime);
            double tValue = brokenCount[i].getAverageSojournTime(currentTime);
            if (brokenCount[i].getThroughCount() < 0.75)
                tValue = Double.NaN;
            map.put(name, new Value(pValue, tValue));
        }

        for(int i = 0; i < brokenCount.length - 1; i++) {
            String name = String.format("brokenCount %s %d",
                    ">",
                    i
            );
            double pValue = 1.0 - brokenCount[i].getAverageCount(currentTime);
            double tValue = (1.0 - brokenCount[i].getAverageCount(currentTime)) / brokenCount[i].getAverageCount(currentTime) *
                    brokenCount[i].getAverageSojournTime(currentTime) *
                    brokenCount[i].getThroughCount() / (brokenCount[i].getThroughCount() - 0.5) ;
            if(brokenCount[i].getThroughCount() < 1.25)
                tValue = Double.NaN;
            map.put(name, new Value(pValue, tValue));
        }
        for(int i = l; i < activeCount.length; i++) {
            String name = String.format("activeCount %s %d",
                    i < n ? ">=" : "=",
                    i
            );
            double pValue = activeCount[i].getAverageCount(currentTime);
            double tValue = activeCount[i].getAverageSojournTime(currentTime);
            if (activeCount[i].getThroughCount() < 0.75)
                tValue = Double.NaN;
            map.put(name, new Value(pValue, tValue));
        }
        for(int i = l; i < activeCount.length; i++) {
            String name = String.format("activeCount %s %d",
                    "<",
                    i
            );
            double pValue = 1.0 - activeCount[i].getAverageCount(currentTime);
            double tValue = (1.0 - activeCount[i].getAverageCount(currentTime)) / activeCount[i].getAverageCount(currentTime) *
                    activeCount[i].getAverageSojournTime(currentTime) *
                    activeCount[i].getThroughCount() / (activeCount[i].getThroughCount() - 0.5) ;
            if(activeCount[i].getThroughCount() < 1.25)
                tValue = Double.NaN;
            map.put(name, new Value(pValue, tValue));
        }
        return map;
    }

    @Override
    public LinkedHashMap<String, Value> call() {
        run();
        return getValues();
    }

    public static LinkedHashMap<String, Value> join(LinkedHashMap<String, Value> ... maps) {
        LinkedHashMap<String, Value> result = new LinkedHashMap<>();
        for(LinkedHashMap<String, Value> map : maps) {
            for(String name : map.keySet()) {
                if (result.containsKey(name))
                    result.put(name, result.get(name).join(map.get(name)));
                else
                    result.put(name, map.get(name));
            }
        }
        return result;
    }


    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException, ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(in, JsonObject.class);

            Distribution lambda = Distribution.formGson(object.getAsJsonObject("lambda"));
            Distribution mu = Distribution.formGson(object.getAsJsonObject("mu"));
            int m = object.getAsJsonPrimitive("m").getAsInt();
            int n = object.getAsJsonPrimitive("n").getAsInt();
            int l = object.getAsJsonPrimitive("l").getAsInt();
            int r = object.getAsJsonPrimitive("r").getAsInt();
            int s = object.getAsJsonPrimitive("s").getAsInt();
            double simulationDuration = object.getAsJsonPrimitive("simulationDuration").getAsDouble();
            int simulationsCount = object.getAsJsonPrimitive("simulationsCount").getAsInt();

            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            ArrayList<Future<LinkedHashMap<String, Value>>> results = new ArrayList<>();

            for (int i = 0; i < simulationsCount; i++) {
                ReliabilitySystem rs = new ReliabilitySystem(
                        i,
                        lambda,
                        mu,
                        m + n, m + l, m, r,
                        s, simulationDuration
                );
                results.add(service.submit((Callable<LinkedHashMap<String, Value>>) rs));
            }

            service.shutdown();

            LinkedHashMap<String, Value> map = new LinkedHashMap<>();
            for (Future<LinkedHashMap<String, Value>> result : results) {
                map = ReliabilitySystem.join(map, result.get());
            }

            try (Csv.Writer writer = new Csv.Writer(out)) {
                writer.value("Состояние").value("Вероятность").value("Относительная погрешность").
                        value("Среднее время пребывания").value("Относительная погрешность").newLine();
                for (Map.Entry<String, Value> entry : map.entrySet()) {
                    writer.value(entry.getKey()).value(d(entry.getValue().getPossibility().getAverage())).
                            // value(d(entry.getValue().getPossibility().getStandardDeviation(true)))
                                    value(p(entry.getValue().getPossibility().getError(true))).
                            value(d(entry.getValue().getDuration().getAverage())).
                            // value(d(entry.getValue().getDuration().getStandardDeviation(true)))
                                    value(p(entry.getValue().getDuration().getError(true))).
                            newLine();
                }
            }
        }
    }


    private static DecimalFormat decimalFormat = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));
    private static DecimalFormat decimalFormat2 = new DecimalFormat("0.000%", DecimalFormatSymbols.getInstance(Locale.US));

    private static String d(double value){
        return decimalFormat.format(value);
    }

    private static String p(double value){
        return decimalFormat2.format(value);
    }

}
