/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author HerrSergio
 */
public class ReliabilityNetworkAnalyzer {

    public static final String separator = ";";

    protected static int reverse_mask(int start, int n) {
        int buf = 0;

        for (int i = 0; i < n; i++) {
            buf <<= 1;
            buf |= start & 1;
            start >>= 1;
        }

        return buf;
    }

    protected static void func(ArrayList<int[]> gr, ArrayList<Integer> cur, int dst, ArrayList<Integer> msk) {
        if (cur.isEmpty()) {
            return;
        }

        /*for (int i = 0; i < cur.size(); i++) {
            cerr << cur[i] << " ";
        }
        cerr << endl;*/
        int src = cur.get(cur.size() - 1);

        if (src == dst) {
            int buf = 0;

            for (int i = 0; i < cur.size(); i++) {
                buf |= 1 << cur.get(i);
            }

            //buf |= 1 << dst;
            msk.add(buf);
            //cerr << "*" << setbase(ios::binary) << buf << setbase(ios::dec) << endl;
        } else {
            for (int i = 0; i < gr.size(); i++) {
                int d;

                if (gr.get(i)[0] == src) {
                    d = gr.get(i)[1];
                } //else if(gr[i].seconf == src)
                //d = gr[i].first;
                else {
                    continue;
                }

                if (cur.contains(d)) {
                    continue;
                }

                cur.add(d);
                func(gr, cur, dst, msk);
                cur.remove(cur.size() - 1);
            }
        }
    }

    public static <E> int binaryFind(List<? extends Comparable<? super E>> list, E e) {
        int pos = Collections.binarySearch(list, e);
        if (pos < 0) {
            return list.size();
        }
        return pos;
    }

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
                lls[i] = new double[]{
                    getHazard(node.get("lambda").getAsJsonObject()),
                    getHazard(node.get("mu").getAsJsonObject())
                };//.first >> lls[i].second;
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

            ArrayList<Integer> msk = new ArrayList<>(),
                    wrk = new ArrayList<>(),
                    cur = new ArrayList<>();

            String source = object.get("source").getAsString();
            String destination = object.get("destination").getAsString();
            boolean dynamicTwrk = object.get("dynamicTwrk").getAsBoolean();
            boolean dynamicPwrk = object.get("dynamicPwrk").getAsBoolean();

            for (int j = 0; j < n; j++) {
                if (names[j].equals(source)) {
                    for (int k = 0; k < n; k++) {
                        if (names[k].equals(destination)) {
                            cur.add(j);
                            func(cnn, cur, k, msk);
                            cur.clear();
                        }
                    }
                }
            }

            /*for (int i = 0; i < msk.size(); i++) {
                cerr << msk[i] << " ";
            }
            cerr << endl;*/
            m = msk.size();

            for (int i = 0; i < (1 << n); i++) {
                for (int j = 0; j < m; j++) {
                    if ((i & msk.get(j)) == 0) {
                        wrk.add(i);
                        //cerr << i << " ";
                        break;
                    }
                }
            }

            //sort(pbegin(wrk), pend(wrk));
            int l = wrk.size();
            //cerr << "(total  " << l << ")" << endl;

            double avIndex = 0, cycleHazard = 0;

            for (int i = 0; i < wrk.size(); i++) {
                double avIndexPart = 1, cycleHazardPart = 0;
                for (int j = 0; j < n; j++) {
                    avIndexPart /= lls[j][0] + lls[j][1];
                    if ((wrk.get(i) & (1 << j)) != 0) {
                        avIndexPart *= lls[j][0];
                    } else {
                        avIndexPart *= lls[j][1];
                        int newSt = wrk.get(i) | (1 << j);
                        if (Collections.binarySearch(wrk, newSt) < 0) {
                            //!binary_search(pbegin(wrk), pend(wrk), newSt)) {
                            cycleHazardPart += lls[j][0];
                        }
                    }
                }
                avIndex += avIndexPart;
                cycleHazard += cycleHazardPart * avIndexPart;
            }
            out.println("AvIndex" + separator + avIndex);
            out.println("Twrk" + separator + avIndex / cycleHazard);
            out.println("Tbrk" + separator + (1 - avIndex) / cycleHazard);
            out.println();

            int start = 0, buf = 0;

            for (int i = 0; i < n; i++) {
                buf <<= 1;
                buf |= start & 1;
                start >>= 1;
            }

            start = Collections.binarySearch(wrk, buf);

            if (dynamicTwrk) {
                int precision = object.get("precision").getAsInt();
                int k = l + 1;
                double e = Math.pow(10, -precision);
                double[] sys = new double[k];//ArrayList(Stream.iterate(0, (i) -> 0).limit(k).collect(Collectors.toList()));
                //cerr << "sys.size() = " << sys.size() << endl;

                for (boolean flag = true; flag;) {

                    flag = false;

                    for (int i = 0; i < l; i++) {

                        double ll = 0, jj = 1;

                        for (int j = 0; j < n; j++) {
                            int pos = binaryFind(wrk, wrk.get(i) ^ (1 << j));

                            if ((wrk.get(i) & (1 << j)) != 0) {
                                ll += lls[j][1];
                                jj += lls[j][1] * sys[pos];
                            } else {
                                ll += lls[j][0];
                                jj += lls[j][0] * sys[pos];
                            }
                        }

                        jj /= ll;

                        flag = flag || Math.abs(sys[i] - jj) > e;
                        sys[i] = jj;
                    }
                }
                out.println("dynamicTwrk" + separator + sys[start]);
                out.println();
            }

            if (dynamicPwrk) {

                double time = object.get("plotTime").getAsDouble();
                int stepsCount = object.get("stepsCount").getAsInt();
                int innerStepsCount = object.get("innerStepsCount").getAsInt();

                double dt = time / (stepsCount * innerStepsCount), rbuf = time;
                int step = innerStepsCount;
                //cin >> dt >> step >> rbuf >> start;
                //cerr << dt << " " << step << " " << rbuf << " " << start << endl;
                //dt /= step;
                int cnt = stepsCount * innerStepsCount + 1;

                out.println("Time" + separator + "Pwrk");

                double src[] = new double[l + 1];
                double dest[] = new double[l + 1];
                src[start] = 1;

                for (int z = 0; z <= cnt; z++) {
                    if (z % step == 0) {
                        out.print(dt * z + separator);
                        //for(int i = 0; i < l; i++)
                        //	cout << source[i] << " ";
                        out.println(1.0 - src[l]);
                    }

                    if (z == cnt) {
                        continue;
                    }

                    System.arraycopy(src, 0, dest, 0, src.length);

                    for (int i = 0; i < l; i++) {
                        for (int j = 0; j < n; j++) {
                            /*int pos = wrk.size();
				int it = std::lower_bound(pbegin(wrk), pend(wrk), wrk[i] ^ (1<<j)) - pbegin(wrk);
				if(it < pos && wrk[it] == (wrk[i] ^ (1<<j)))
					pos = it;*/
                            int pos = binaryFind(wrk, wrk.get(i) ^ (1 << j));

                            double delta = src[i] * dt;

                            if ((wrk.get(i) & (1 << j)) != 0) {
                                delta *= lls[j][1];
                            } else {
                                delta *= lls[j][0];
                            }

                            dest[i] -= delta;
                            dest[pos] += delta;
                        }
                    }

                    double[] buf2 = dest;
                    dest = src;
                    src = buf2;
                }
            }
        }
    }
    
    public static double getHazard(JsonObject object) {
        if(object.has("mean")) {
            return 1.0 / object.get("mean").getAsDouble();
        } else {
            return object.get("hazard").getAsDouble();
        }
    }
}
