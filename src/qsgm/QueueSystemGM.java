package qsgm;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class QueueSystemGM {
    public static LinkedHashMap<String, Double> model(InputData inputData) {

        double alphas[] = inputData.getAlphas();
        double lambdas[] = inputData.getLambdas();
        double nus[] = inputData.getNus();
        double mu = inputData.getMu();

        if(lambdas.length != nus.length || nus.length != alphas.length + 1 || mu <= 0.0)
            throw new IllegalArgumentException();

        alphas = adjustAlphas(alphas);

        double ps[] = new double[lambdas.length];
        double p = 0.0;

        for(int i = 0; i < nus.length; i++) {
            if(nus[i] < 0.0 || lambdas[i] <= 0.0)
                throw new IllegalArgumentException();
            nus[i] *= nus[i];
            nus[i] = 1.0 / nus[i];
            ps[i] = lambdas[i] / mu;
            p += alphas[i] / ps[i];
        }

        p = 1.0 / p;

        if(p >= 1.0)
            throw new IllegalArgumentException();

        double s = getSigma(p, alphas, ps, nus);


        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        map.put("Доля успешных завершений", 1.0);
        map.put("Среднее время пребывания в системе", 1.0 / mu / (1.0 - s));
        map.put("Среднее число заданий в системе", p / (1.0 - s));
        map.put("Среднее время ожидания", s / mu / (1.0 - s));
        map.put("Средняя длина очереди", p * s / (1.0 - s));
        map.put("Доля ожидавших", s);
        map.put("Среднее время ожидания ожидавших", 1.0 / mu / (1.0 - s));
        map.put("Среднее время обслуживания", 1.0 / mu);
        map.put("Среднее время обслуживания для обслуженных", 1.0/mu);
        map.put("Среднее число активных исполнителей", p);
        map.put("Использование системы", p);

        return map;
    }

    private static double A(double s, double alphas[], double ps[], double nus[]) {
        double sum = 0.0;
        for(int i = 0; i < alphas.length; i++) {
            double nu = nus[i], p = ps[i], alpha = alphas[i];
            if (nu < Double.POSITIVE_INFINITY)
                sum += alpha * Math.pow(p / ((1 - s) / nu + p), nu);
            else
                sum +=  alpha * Math.exp((s - 1.0) / p);
        }
        return sum;
    }

    private static double getSigma(double s, double alphas[], double ps[], double nus[]) {
        //double s = 0.5;
        for(int i = 0; i < 1_000_000; i++) {
            double newS = A(s, alphas, ps, nus);
            if(s == newS)
                break;
            s = newS;
        }
        return s;
    }

    private static double[] adjustAlphas(double[] alphas) {
        double[] newAlphas = new double[alphas.length + 1];
        double remains = 1.0;
        for(int i = 0; i < alphas.length; i++) {
            remains -= alphas[i];
            if(alphas[i] < 0.0 || alphas[i] > 1.0 || remains < 0.0)
                throw new IllegalArgumentException();
            newAlphas[i] = alphas[i];
        }
        newAlphas[alphas.length] = remains;
        return  newAlphas;
    }

    public static void main(InputStream input, OutputStream output, OutputStream logger) throws IOException {

        try (InputStreamReader in = new InputStreamReader(input, StandardCharsets.UTF_8);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8.displayName()))) {

            Gson gson = new Gson();

            InputData inputData = gson.fromJson(in, InputData.class);

            DecimalFormat decimalFormat = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));

            Map<String, Double> map = QueueSystemGM.model(inputData);

            for (Map.Entry<String, Double> entry : map.entrySet()) {
                out.println(entry.getKey() + ";" + decimalFormat.format(entry.getValue()));
            }
        }

    }
}
