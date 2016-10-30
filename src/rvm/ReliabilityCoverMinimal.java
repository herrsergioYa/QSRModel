package rvm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import static rna.ReliabilityNetworkAnalyzer.getHazard;
import rnsim.Node;
import rnsim.NodeChar;
import rnsim.ReliabilityNetwork;
import rvsim.ReliabilityCover;
import java.util.function.Function;
import static qssim.StochasticValue.getZ;
import rla.ReliabilityAnalyzer;
import rna.ReliabilityNetworkAnalyzer;

/**
 *
 * @author HerrSergio
 */
public class ReliabilityCoverMinimal {

    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException, ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            Gson gson = new Gson();

            JsonObject object = gson.fromJson(in, JsonObject.class);

            JsonArray nodes = object.getAsJsonArray("nodes");
            int n = nodes.size();

            double[][] lls = new double[n][];
            String[] names = new String[n];

            for (int i = 0; i < n; i++) {
                JsonObject node = nodes.get(i).getAsJsonObject();
                if (node.has("p")) {
                    lls[i] = new double[]{node.get("p").getAsDouble()};
                } else {
                    lls[i] = new double[]{
                        getHazard(node.get("lambda").getAsJsonObject()),
                        getHazard(node.get("mu").getAsJsonObject())
                    };//.first >> lls[i].second;
                }
                names[i] = node.get("name").getAsString();
            }

            JsonArray junctions = object.getAsJsonArray("junctions");

            int m = junctions.size();

            ArrayList<int[]> cnn = new ArrayList<>();

            for (int i = 0; i < m; i++) {
                JsonObject junction = junctions.get(i).getAsJsonObject();
                String a = junction.get("from").getAsString(), b = junction.get("to").getAsString();
                boolean c = junction.get("bidirectional").getAsBoolean();

                if (a.equals(b)) {
                    continue;
                }

                for (int j = 0; j < n; j++) {
                    if (names[j].equals(a)) {
                        for (int k = 0; k < n; k++) {
                            if (names[k].equals(b)) {
                                cnn.add(new int[]{j, k});
                                if (c) {
                                    cnn.add(new int[]{k, j});
                                }
                            }
                        }
                    }
                }
            }

            List<String> sources = new ArrayList<>();
            if (object.has("source")) {
                sources.add(object.get("source").getAsString());
            }
            if (object.has("sources")) {
                for (JsonElement element : object.get("sources").getAsJsonArray()) {
                    sources.add(element.getAsString());
                }
            }
            List<String> destinations = new ArrayList<>();
            if (object.has("destination")) {
                destinations.add(object.get("destination").getAsString());
            }
            if (object.has("destinations")) {
                for (JsonElement element : object.get("destinations").getAsJsonArray()) {
                    destinations.add(element.getAsString());
                }
            }
            //boolean dynamicTwrk = object.get("dynamicTwrk").getAsBoolean();
            //boolean dynamicPwrk = object.get("dynamicPwrk").getAsBoolean();

            IdentityHashMap<Node, List<Node>> map = new IdentityHashMap<>();

            List<Node> list = new ArrayList<>();
            for (int i = 0; i < lls.length; i++) {
                if (lls[i].length > 1) {
                    lls[i] = new double[]{lls[i][1] / (lls[i][0] + lls[i][1])};
                }
                NodeChar ch = new NodeChar();
                ch.setName(names[i]);
                Node node = new Node(ch);
                list.add(node);
                map.putIfAbsent(node, new ArrayList<>());
            }

            for (int[] cn : cnn) {
                map.get(list.get(cn[0])).add(list.get(cn[1]));
            }

            List<Node> sourceNodes = new ArrayList<>();
            List<Node> destinationNodes = new ArrayList<>();

            for (int j = 0; j < n; j++) {
                if (sources.contains(names[j])) {
                    sourceNodes.add(list.get(j));
                }

                if (destinations.contains(names[j])) {
                    destinationNodes.add(list.get(j));
                }
            }

            Map<String, Double> status = destinations.stream().collect(
                    Collectors.toMap(Function.identity(), nd -> 0.0)
            );
            status = new TreeMap<>(status); 

            int simulationsCount = object.get("simulationsCount").getAsInt();
            
            for (int r = 0; r < simulationsCount; r++) {

                Random random = new Random(r);
                
                for (int i = 0; i < n; i++) {
                    list.get(i).setOk(random.nextDouble() < lls[i][0]);
                }

                List<String> ok = ReliabilityCover.getNames(ReliabilityCover.areOk(map, sourceNodes, destinationNodes));

                for (String name : ok) {
                    status.put(name, status.get(name) + 1.0);
                }

            }
            
            double minP = 1.0, maxP = 0.0, avgP = 0.0;
            
            for(String name : status.keySet()) {
                double p = status.get(name) / simulationsCount;
                status.put(name, p);
                avgP += p;
                minP = Math.min(minP, p);
                maxP = Math.max(maxP, p);
            }
            
            avgP /= status.size();

            printValue(out, "Min", minP, simulationsCount);
            printValue(out, "Avg", avgP, simulationsCount);
            printValue(out, "Max", maxP, simulationsCount);

            out.println();
            
            for(String name : status.keySet()) {
                printValue(out, name, status.get(name), simulationsCount);
            }
        }
    }

    private static void printValue(final PrintWriter out, String name, double p, int simulationsCount) {
        out.print(name + " = ");
        out.print(ReliabilityNetworkAnalyzer.separator);
        out.print(p);
        out.print(ReliabilityNetworkAnalyzer.separator);
        out.print("±");
        out.print(ReliabilityNetworkAnalyzer.separator);
        out.print(Math.sqrt((1 - p) * p / simulationsCount) * getZ(simulationsCount - 1));
        out.println();
    }
}
