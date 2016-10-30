/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rnsim;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import distributions.Distribution;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import qssim.Counter;
import qssim.StatisticHolder;
import qssim.StochasticValue;
import static qssim.StochasticValue.getZ;
import rlsim.Value;
import static rna.ReliabilityNetworkAnalyzer.separator;
import util.IdentityHashSet;

/**
 *
 * @author HerrSergio
 */
public class ReliabilityNetwork implements Runnable, Callable<double[]> {
    
    private long cyclesNeeded;
    private IdentityHashSet<Node> sources;
    private IdentityHashSet<Node> dests;
    private boolean ok;
    private Double firstBreak;
    private Counter breaks = new Counter();
    private Counter recovers = new Counter();
    private double currentTime;
    //private SystemChar ch;
    
    public static final String separator = ";";
    
    private Random random;
    
    private PriorityQueue<Node> nodes = new PriorityQueue<>(new NodeComparatorByTime());
    private IdentityHashMap<Node, ArrayList<Node>> matrix = new IdentityHashMap<>();

    public ReliabilityNetwork(int seed, long cyclesNeeded, ArrayList<NodeChar> nodeChars, 
            Map<NodeChar, ArrayList<NodeChar>> matrix,
            Collection<NodeChar> sources,
            Collection<NodeChar> dests
        ) {
        this.random = new Random(seed);
        this.cyclesNeeded = cyclesNeeded * 2L;
        IdentityHashMap<NodeChar, Node> map = new IdentityHashMap<>();
        for(int i = 0; i < nodeChars.size(); i++) {
            NodeChar ch = nodeChars.get(i);
            Node node = new Node(ch);
            map.put(ch, node);
            node.generateEvent(this.random);
            nodes.add(node);
        }
        for(NodeChar ch : matrix.keySet()) {
            this.matrix.put(map.get(ch), 
                    new ArrayList<>(matrix.get(ch).stream().map((n)->map.get(n)).collect(Collectors.toList()))
            );
        }
        this.sources = new IdentityHashSet<>(sources.stream().map(n->map.get(n)).collect(Collectors.toList()));
        this.dests = new IdentityHashSet<>(dests.stream().map(n->map.get(n)).collect(Collectors.toList()));
        this.ok = isOk(this.matrix, this.sources, this.dests);
        if(this.ok) {
            recovers.in(0);
        } else {
            breaks.in(0);   
            throw new RuntimeException("The system is broken at the start!");
        }
    }
    
    private void checkFor(boolean _break) {
        if(ok == _break) {
            ok = isOk(matrix, sources, dests);
            
            if(_break && !ok) {
                if(firstBreak == null) {
                    firstBreak = currentTime;
                }
                breaks.in(currentTime);
                recovers.out(currentTime);
                cyclesNeeded --;
            }
            
            if(!_break && ok) {
                recovers.in(currentTime);
                breaks.out(currentTime);
                cyclesNeeded --;
            }  
        } 
    }
    
    @Override
    public void run() {
        while(firstBreak == null || cyclesNeeded > 0L) {
            Node node = nodes.poll();
            currentTime = node.getNextEventTime();
            boolean wasOk = node.isOk();
            node.generateEvent(random);
            nodes.add(node);
            if(wasOk != node.isOk())
                checkFor(wasOk);
        }        
    }
    
     private double[] getValues() {
         return new double[] {
             firstBreak, 
             recovers.getAverageCount(currentTime),
             recovers.getAverageSojournTime(currentTime),
             breaks.getAverageSojournTime(currentTime),
             breaks.getAverageCount(currentTime)
         };
     }

    @Override
    public double[] call() throws Exception {
        run();
        return getValues();
    }
    
    public static class NodeCharComparatorByName implements Comparator<NodeChar>  {
        @Override
        public int compare(NodeChar t, NodeChar t1) {
            return  t.getName().compareTo(t1.getName());
        }
        
    }
    
    public static class NodeComparatorByTime implements Comparator<Node> {

        @Override
        public int compare(Node t, Node t1) {
            return Double.compare(t.getNextEventTime(), t1.getNextEventTime());
        }
        
    }
    
    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException, ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(in, JsonObject.class);

            JsonArray nodes = object.getAsJsonArray("nodes");
            
            ArrayList<NodeChar> nodeChars = new ArrayList<>();
            
            for(int i = 0; i < nodes.size(); i ++) {
                JsonObject node = nodes.get(i).getAsJsonObject();
                String name = node.get("name").getAsString();
                Distribution lambda = Distribution.formGson(node.get("lambda").getAsJsonObject());
                Distribution mu = Distribution.formGson(node.get("mu").getAsJsonObject());
                int installedRedundancy = 0;//node.get("installedRedundancy").getAsInt();
                int stashedRedunduncy = 0;//node.get("stashedRedunduncy").getAsInt();
                if(installedRedundancy != 0 || stashedRedunduncy != 0)
                    throw new IllegalArgumentException();
                NodeChar ch = new NodeChar(name, lambda, mu, installedRedundancy, stashedRedunduncy);
                nodeChars.add(ch);
            }
         
            //Collections.sort(nodeChars, new NodeCharComparatorByName());
            
            JsonArray junctions = object.getAsJsonArray("junctions");
            
            //ArrayList<JunctionChar> junctionsChar = new ArrayList<>();
            IdentityHashMap<NodeChar, ArrayList<NodeChar>> matrix = new IdentityHashMap<>();
            
            for(int i = 0; i < junctions.size(); i++) {
                JsonObject junction = junctions.get(i).getAsJsonObject();
                String from = junction.get("from").getAsString();
                String to = junction.get("to").getAsString();
                boolean bidirectional = junction.get("bidirectional").getAsBoolean();
                for(int j = 0; j < nodeChars.size(); j++) {
                    if(nodeChars.get(j).getName().equals(from)) {
                        for(int k = 0; k < nodeChars.size(); k++) {
                            if(nodeChars.get(k).getName().equals(to)) {
                                matrix.putIfAbsent(nodeChars.get(j), new ArrayList<>());
                                matrix.get(nodeChars.get(j)).add(nodeChars.get(k));
                                if(bidirectional){
                                    matrix.putIfAbsent(nodeChars.get(k), new ArrayList<>());
                                    matrix.get(nodeChars.get(k)).add(nodeChars.get(j));
                                }
                            }
                        }
                    }
                }
            }
            
            ArrayList<NodeChar> sources = new ArrayList<>();
            ArrayList<NodeChar> dests = new ArrayList<>();
            
            String source = object.get("source").getAsString();
            String dest = object.get("destination").getAsString();
            
            for(int i = 0; i < nodeChars.size(); i++) {
                if(nodeChars.get(i).getName().equals(source))
                    sources.add(nodeChars.get(i));
                
                if(nodeChars.get(i).getName().equals(dest))
                    dests.add(nodeChars.get(i));
            }

            int cyclesNeeded = object.get("cyclesCount").getAsInt();
            int simulationsCount = object.get("simulationsCount").getAsInt();
            double plotTime = object.get("plotTime").getAsDouble();
            int stepsCount = object.get("stepsCount").getAsInt();
            
            StochasticValue[] values = new StochasticValue[] {
                new StochasticValue(),
                new StochasticValue(),
                new StochasticValue(),
                new StochasticValue()
            }; 
            
            double[] p = new double[stepsCount + 1];
            double dt = plotTime / stepsCount; 
            
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            
            ArrayList<Future<double[]>> results = new ArrayList<>();
            
            for(int i = 0; i < simulationsCount; i++) {
                ReliabilityNetwork sys = new ReliabilityNetwork(i, cyclesNeeded, nodeChars, matrix, sources, dests);
                results.add(service.submit((Callable<double[]>)sys));
            }
            
            service.shutdown();
            
            for(Future<double[]> future : results) {
                double[] result = future.get();
                for(int i = 0; i < values.length; i++) {
                    values[i] =  values[i].join(result[i]);
                }
                int pos = (int)(result[0] / dt);
                if(pos < p.length)
                    p[pos]++;
            }
            
            out.println("Pwrk=" + separator + values[1].getAverage() + separator + "±" + separator + values[1].getError(true) * 100.0 + "%");
            out.println("Twrk=" + separator + values[2].getAverage() + separator + "±" + separator + values[2].getError(true) * 100.0 + "%");
            out.println("Tbrk=" + separator + values[3].getAverage() + separator + "±" + separator + values[3].getError(true) * 100.0 + "%");
            out.println();
            out.println("dynamicTwrk=" + separator + values[0].getAverage() + separator + "±" + separator + values[0].getError(true) * 100.0 + "%");
            out.println();
            
            
            p[0] /= results.size();
            for(int i = 1; i < p.length; i++){
                p[i] /= results.size();
                p[i] += p[i - 1];
            }
          
            for(int i = p.length - 1; i > 0; i--){
                p[i] = 1.0 - p[i - 1];
            }
            p[0] = 1.0;
            
            out.println("Time" + separator + "Pwrk" + separator + "absError");
            for(int i = 0; i < p.length; i++) {
                out.println(i * dt + separator + p[i] + separator +  Math.sqrt(p[i] * (1.0 - p[i]) / results.size()) * getZ(results.size() - 1));
            }
            
        }
    }
    
    public static boolean isOk(Map<Node, ? extends Collection<Node>> matrix,
            Collection<Node> sources,
            Collection<Node> dests
            ) {
        Collection<Node> nodes = extractOks(sources);
        ArrayDeque<Node> points = new ArrayDeque<>(nodes);
        IdentityHashSet<Node> checked = new IdentityHashSet<>(nodes);
        while(points.size() > 0) {
            Node node = points.poll();
            if(dests.contains(node)) {
                return true;
            }
            if(matrix.containsKey(node)) {
                nodes = extractOks(matrix.get(node));
                nodes.removeAll(checked);
                points.addAll(nodes);
                checked.addAll(nodes);
            }            
        }
        return false;
    }
    
    public static IdentityHashSet<Node> extractOks(Collection<Node> nodes) {
        return new IdentityHashSet<>(nodes.stream().filter(n -> n.isOk()).collect(Collectors.toList()));
    }
}
