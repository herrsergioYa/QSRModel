package rla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class ReliabilityAnalyzer {
    public static String model(InputData inputData) {

        double lambda = inputData.getLambda();
        double mu = inputData.getMu();
        int m = inputData.getM();
        int n = inputData.getN();
        int l = inputData.getL();
        int r = inputData.getR();
        int s = inputData.getS();
        int p = inputData.getP();

        if(lambda <= 0.0 || mu <= 0.0 || m <= 0 || n < 0 || l < 0 || r < 0 || s <= 0 || n < l || p < 1 || p > m)
            throw new IllegalArgumentException();

        int maxBrokenCount = n + r + 1 + (m - p);

        double[] ps = new double[maxBrokenCount + 1];
        double[] rightFlow = new double[maxBrokenCount];
        double[] leftFlow = new double[maxBrokenCount];
        int[] usingCount = new int[maxBrokenCount + 1];

        ps[0] = 1;
        double sum = ps[0];

        for(int i = 0; ; i++) {
            usingCount[i] = m + Math.max(n - i, Math.min(n - i + r, l));

            if(i == maxBrokenCount)
                break;

            rightFlow[i] = usingCount[i] * lambda;
            leftFlow[i] = Math.min(i + 1, s) * mu;

            ps[i + 1] = ps[i] * rightFlow[i] / leftFlow[i];
            sum += ps[i + 1];
        }
        for(int i = 0; i <= maxBrokenCount; i++) {
            ps[i] /= sum;
        }
        double workers[] = new double[usingCount[0] + 1];
        double workersTin[] = new double[usingCount[0] + 1];
        double workersTout[] = new double[usingCount[0] + 1];
        double broken[] = new double[maxBrokenCount];
        double brokenTin[] = new double[maxBrokenCount];
        double brokenTout[] = new double[maxBrokenCount];
        double buf = 0.0;
        for(int i = 0; i < maxBrokenCount; i++) {
            buf += ps[i];
            broken[i] = buf;
            brokenTin[i] = buf / (ps[i] * rightFlow[i]);
            brokenTout[i] = (1 - buf) / (ps[i] * rightFlow[i]);
        }
        buf = 0.0;
        for(int i = 0; i < maxBrokenCount; i++) {
            int j = usingCount[i];
            buf += ps[i];
            workers[j] = buf;
            workersTin[j] = buf / (ps[i] * rightFlow[i]);
            workersTout[j] = (1 - buf) / (ps[i] * rightFlow[i]);
        }
        buf = 0.0;
        for(int i = 0; i < maxBrokenCount; i++) {
            buf += usingCount[i] * ps[i];
        }
        buf /= 1 - ps[maxBrokenCount];

        StringBuilder result = new StringBuilder();
        result.append("condition").append(";").append("P").append(";").append("T").append("\n");;
        for(int i = 0; i < maxBrokenCount; i++) {
            if(i == 0)
                result.append("brokenCount == ");
            else
                result.append("brokenCount <= ");
            result.append(i).append(";");

            result.append(d(broken[i])).append(";");
            result.append(d(brokenTin[i])).append(";");
            //result.append(d(brokenTout[i])).append(";");
            result.append("\n");
        }
        result.append("\n");

        result.append("condition").append(";").append("P").append(";").append("T").append("\n");;
        for(int i = 0; i < maxBrokenCount; i++) {
            result.append("brokenCount > ");
            result.append(i).append(";");

            result.append(d(1 - broken[i])).append(";");
            //result.append(d(brokenTin[i])).append(";");
            result.append(d(brokenTout[i])).append(";");
            result.append("\n");
        }
        result.append("\n");

        result.append("condition").append(";").append("P").append(";").append("T").append("\n");;
        for(int i = p; i <= usingCount[0]; i++) {
            if(i == usingCount[0])
                result.append("activeCount == ");
            else
                result.append("activeCount >= ");
            result.append(i).append(";");

            result.append(d(workers[i])).append(";");
            result.append(d(workersTin[i])).append(";");
            //result.append(d(workersTout[i])).append(";");
            result.append("\n");
        }
        result.append("\n");

        result.append("condition").append(";").append("P").append(";").append("T").append("\n");;
        for(int i = p; i <= usingCount[0]; i++) {
            result.append("activeCount < ");
            result.append(i).append(";");

            result.append(d(1 - workers[i])).append(";");
            //result.append(d(workersTin[i])).append(";");
            result.append(d(workersTout[i])).append(";");
            result.append("\n");
        }
        result.append("\n");

        result.append("\n").append("=================================================").append("\n").append("\n");

        result.append("Pw = ").append(";").append(d(workers[m])).append("\n");
        result.append("Pidle = ").append(";").append(d(1 - workers[m])).append("\n");
        result.append("Tw = ").append(";").append(d(workersTin[m])).append("\n");
        result.append("Tidle = ").append(";").append(d(workersTout[m])).append("\n");
        //result.append("Wavg = ").append(";").append(d(buf)).append("\n");

        return result.toString();
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));

    private static String d(double value){
        return decimalFormat.format(value);
    }

    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Momentum.class, new JsonDeserializer<Momentum>() {
                @Override
                public Momentum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject object = json.getAsJsonObject();
                    double hazard = object.has("mean") ? 1.0 / object.get("mean").getAsDouble() : object.get("hazard").getAsDouble();
                    return new Momentum(hazard);
                }
                
            });
            
            Gson gson = builder.create();

            InputData inputData = gson.fromJson(in, InputData.class);

            String result = ReliabilityAnalyzer.model(inputData);

            out.append(result);
        }
    }
}
