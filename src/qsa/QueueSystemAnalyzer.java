package qsa;

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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class QueueSystemAnalyzer {

    public static Map<String, Double> model(InputData inputData){
        long n = inputData.getN();
        long m = inputData.getM();

        double lambda = inputData.getLambda();
        double mu = inputData.getMu();

        double nlambda = inputData.getNulambda();
        double nmu = inputData.getNumu();

        double nu = inputData.getNu();
        double nnu = inputData.getNunu();

        if(lambda <= 0 || mu <= 0 || nlambda < 0 || nmu < 0 || nu < 0 || n == 0 || nnu < 0){
            throw new IllegalArgumentException();
        }

        double pSucc = 0, s = 0, Ts = 0, q = 0, Tq = 0, Tq1 = 0, pq = 0, e = 0, Te = 0, Te1 = 0, u = 0;

        if(n < 0) {
            pSucc = 1;
            s = e = lambda / mu;
            Ts = Te = Te1 = 1 / mu;
            Tq1= Double.NaN;
        } else {
            if((nlambda != 1.0 || nmu != 1.0) && (m >= 0 || nu > 0) || nu > 0 && nnu != 1.0 ) {
                throw new RuntimeException("No model");
            }

            long N = n, M = m < 0 ? Long.MAX_VALUE : m + n;

            double r = lambda / mu, theta = nu / mu;

            if(r >= N && m < 0 && nu == 0.0) {
                throw new IllegalStateException();
            }

            if(nlambda != 1.0 || nmu != 1.0 && n != 1) {
                //System.err.println("The data will be rough!");
            }

            double p = 1;
            double sum = 0;
            long i = 0;

            while (i < N && p > Double.MIN_NORMAL) {
                // s += i * p;
                e += i * p;
                pSucc += p;
                sum += p;
                p *= r / ++i;
            }

            while (i < M && p > Double.MIN_NORMAL) {
                // s += i * p;
                e += N * p;
                q += (i - N) * p;
                i++;
                pSucc += p * N / (N + theta * (i - N));
                pq += p;
                sum += p;
                p *= r / (N + theta * (i - N));
            }

            //s += M * p;
            e += N * p;
            q += (M - N) * p;
            sum += p;

            //s /= sum;
            e /= sum;
            q /= sum;
            pSucc /=sum;
            pq /= sum;

            q *= (nlambda * nlambda + nmu * nmu) / 2.0;
            s = e + q;

            Ts = s / lambda;
            Tq = q / lambda;
            Te = e / lambda;
            //if(pq > 0)
                Tq1 = Tq / pq;
            Te1 = 1 / mu;//Te / pSucc;

            u = e / N;
        }


        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        map.put("Доля успешных завершений", pSucc);
        map.put("Среднее время пребывания в системе", Ts);
        map.put("Среднее число заданий в системе", s);
        map.put("Среднее время ожидания", Tq);
        map.put("Средняя длина очереди", q);
        map.put("Доля ожидавших", pq);
        map.put("Среднее время ожидания ожидавших", Tq1);
        map.put("Среднее время обслуживания", Te);
        map.put("Среднее время обслуживания для обслуженных", Te1);
        map.put("Среднее число активных исполнителей", e);
        map.put("Использование системы", u);
        return map;
    }

    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {
  
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Momenta.class, new JsonDeserializer<Momenta>() {
                @Override
                public Momenta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject object = json.getAsJsonObject();
                    double hazard = object.has("mean") ? 1.0 / object.get("mean").getAsDouble() : object.get("hazard").getAsDouble();
                    double cov = object.has("order") ? 1.0 / Math.sqrt(object.get("order").getAsDouble()) : object.get("cov").getAsDouble();
                    return new Momenta(hazard, cov);
                }
                
            });
            
            Gson gson = builder.create();
            
            InputData inputData = gson.fromJson(in, InputData.class);

            DecimalFormat decimalFormat = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));

            Map<String, Double> map = QueueSystemAnalyzer.model(inputData);

            for (Map.Entry<String, Double> entry : map.entrySet()) {
                out.println(entry.getKey() + ";" + decimalFormat.format(entry.getValue()));
            }
        }
    }
}
