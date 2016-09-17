package qssim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import distributions.Distribution;
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
 * Created by HerrSergio on 17.08.2016.
 */
public class QueueSystem implements Runnable, Callable<LinkedHashMap<String, StochasticValue>> {
    private Random random;
    private Distribution lambda;
    private Distribution mu;
    private Distribution nu;
    private int executorsCount;
    private int queueLimit;
    private double timeLimit;

    private PriorityQueue<Task> queue;
    private Counter systemCounter = new Counter();
    private Counter queueCounter = new Counter();
    private Counter executorsCounter = new Counter();
    private double currentTime;
    private double nextTask;
    private PriorityQueue<Task> executors;
    //private int succeededCount;
    private Task nextDrop;
    private DropTaskComparator dropTaskComparator = new DropTaskComparator();

    public QueueSystem(int seed, Distribution lambda, Distribution mu, Distribution nu, int executorsCount, int queueLimit, Comparator<Task> taskComparator, double timeLimit) {
        this.random = new Random(seed);
        this.lambda = lambda;
        this.mu = mu;
        this.nu = nu;
        this.executorsCount = executorsCount;
        this.queueLimit = queueLimit;
        if (taskComparator == null)
            throw new NullPointerException();
        this.queue = new PriorityQueue<>(taskComparator);
        this.executors = new PriorityQueue<>(new TaskServedAtComparator());
        this.nextTask = lambda.next(random);
        this.timeLimit = timeLimit;
    }

    protected boolean queueTask(Task task) {
        if (/*executors.size() +*/ queue.size() >= queueLimit && queueLimit >= 0) {
            return false;
        }
        queue.offer(task);
        updateNextDropWhenAdd(task);
        queueCounter.in(currentTime);
        return true;
    }

    protected Task getNextTask() {
        if (queue.size() == 0)
            return null;
        Task task = queue.poll();
        updateNextDropWhenRemove(task);
        queueCounter.out(currentTime);
        return task;
    }

    protected Task dropNextTask() {
        if (nextDrop == null)
            return null;
        Task task = nextDrop;
        queue.remove(task);
        updateNextDrop();
        queueCounter.out(currentTime);
        return task;
    }

    protected boolean executeTask(Task task) {
        if (executors.size() >= executorsCount && executorsCount >= 0) {
            return false;
        }
        task.setServedAt(currentTime + task.getServiceTime());
        executors.offer(task);
        executorsCounter.in(currentTime);
        return true;
    }

    protected Task finishTask() {
        if (executors.size() == 0)
            return null;
        Task task = executors.poll();
        executorsCounter.out(currentTime);
        return task;
    }

    protected Task createTask() {
        double serviceTime = mu.next(random);
        double deadlineTime = currentTime + nu.next(random);
        Task task = new Task(currentTime, serviceTime, deadlineTime);
        systemCounter.in(currentTime);
        return task;
    }

    protected void destroyTask(Task task, boolean succeeded) {
        systemCounter.out(currentTime);
        //if (succeeded)
         //   succeededCount++;
    }

    private Task updateNextDropWhenAdd(Task newTask) {
        if (dropTaskComparator.compare(nextDrop, newTask) <= 0) {
            return nextDrop;
        } else {
            return nextDrop = newTask;
        }
    }

    private Task updateNextDropWhenRemove(Task removedTask) {
        if (removedTask == null || nextDrop != removedTask) {
            return nextDrop;
        } else {
            return updateNextDrop();
        }
    }

    private Task updateNextDrop() {
        return nextDrop = queue.stream().min(dropTaskComparator).orElse(null);
    }

    @Override
    public void run() {
        while (true) {
            double nextEvent = nextTask;
            if (nextDrop != null && nextDrop.getDeadlineTime() < nextEvent)
                nextEvent = nextDrop.getDeadlineTime();
            if (executors.size() > 0 && executors.peek().getServedAt() < nextEvent)
                nextEvent = executors.peek().getServedAt();

            currentTime = Math.min(nextEvent, timeLimit);

            if (currentTime == nextTask) {
                Task task = createTask();
                if (!executeTask(task)) {
                    if (!queueTask(task))
                        destroyTask(task, false);
                }
                nextTask += lambda.next(random);
            } else if (nextDrop != null && currentTime == nextDrop.getDeadlineTime()) {
                Task task = dropNextTask();
                destroyTask(task, false);
            } else if (executors.size() > 0 && currentTime == executors.peek().getServedAt()) {
                Task task = finishTask();
                destroyTask(task, true);
                task = getNextTask();
                if (task != null) {
                    if (!executeTask(task))
                        throw new Error();
                }
            } else {
                break;
            }
        }
    }

    public LinkedHashMap<String, StochasticValue> getValues() {
        LinkedHashMap<String, StochasticValue> map = new LinkedHashMap<>();
        map.put("Доля успешных завершений", new StochasticValue(executorsCounter.getOutcome() * 1.0 / systemCounter.getOutcome()));
        map.put("Среднее время пребывания в системе", new StochasticValue(systemCounter.getAverageSojournTime(currentTime)));
        map.put("Среднее число заданий в системе", new StochasticValue(systemCounter.getAverageCount(currentTime)));
        map.put("Среднее время ожидания", new StochasticValue(queueCounter.getAverageSojournTime(currentTime) * queueCounter.getIncome() * 1.0 / systemCounter.getIncome()));
        map.put("Средняя длина очереди", new StochasticValue(queueCounter.getAverageCount(currentTime)));
        map.put("Доля ожидавших", new StochasticValue(queueCounter.getIncome() * 1.0 / systemCounter.getIncome()));
        map.put("Среднее время ожидания ожидавших", new StochasticValue(queueCounter.getAverageSojournTime(currentTime)));
        map.put("Среднее время обслуживания", new StochasticValue(executorsCounter.getAverageSojournTime(currentTime) * executorsCounter.getOutcome() * 1.0 / systemCounter.getOutcome()));
        map.put("Среднее время обслуживания для обслуженных", new StochasticValue(executorsCounter.getAverageSojournTime(currentTime)));
        map.put("Среднее число активных исполнителей", new StochasticValue(executorsCounter.getAverageCount(currentTime)));
        map.put("Использование системы", new StochasticValue(executorsCounter.getAverageCount(currentTime) / executorsCount));
        return map;
    }

    public static LinkedHashMap<String, StochasticValue> join(Map<String, StochasticValue>... maps) {
        LinkedHashMap<String, StochasticValue> ret = new LinkedHashMap<>();
        for (Map<String, StochasticValue> map : maps)
            for (Map.Entry<String, StochasticValue> entry : map.entrySet())
                if (ret.containsKey(entry.getKey()))
                    ret.put(entry.getKey(), ret.get(entry.getKey()).join(entry.getValue()));
                else
                    ret.put(entry.getKey(), entry.getValue());
        return ret;
    }

    @Override
    public LinkedHashMap<String, StochasticValue> call() throws Exception {
        run();
        return getValues();
    }


    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(in, JsonObject.class);

            Distribution lambda = Distribution.formGson(object.getAsJsonObject("lambda"));
            Distribution mu = Distribution.formGson(object.getAsJsonObject("mu"));
            Distribution nu = Distribution.formGson(object.getAsJsonObject("nu"));
            TaskComparator taskComparator = TaskComparator.formGson(object.getAsJsonObject("taskComparator"));
            int executorsCount = object.getAsJsonPrimitive("executorsCount").getAsInt();
            int queueLimit = object.getAsJsonPrimitive("queueLimit").getAsInt();
            double simulationDuration = object.getAsJsonPrimitive("simulationDuration").getAsDouble();
            int simulationsCount = object.getAsJsonPrimitive("simulationsCount").getAsInt();

            Map<String, StochasticValue> result = new HashMap<>();

            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<LinkedHashMap<String, StochasticValue>>> tasks = new LinkedList<>();
            for (int i = 0; i < simulationsCount; i++) {
                QueueSystem qs = new QueueSystem(i, lambda, mu, nu, executorsCount, queueLimit,
                        taskComparator, simulationDuration);
                tasks.add(executorService.submit((Callable<LinkedHashMap<String, StochasticValue>>) qs));
            }

            executorService.shutdown();

            for (Future<LinkedHashMap<String, StochasticValue>> future : tasks)
                result = QueueSystem.join(result, future.get());

            try (Csv.Writer writer = new Csv.Writer(out)) {
                writer.value("").value("Значение").value("Относительная погрешность").newLine();
                for (Map.Entry<String, StochasticValue> entry : result.entrySet()) {
                    writer.value(entry.getKey()).value(d(entry.getValue().getAverage())).
                            // value(d(entry.getValue().getStandardDeviation(true))).newLine().
                                    value(p(entry.getValue().getError(true))).newLine();
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


