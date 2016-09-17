import qsa.QueueSystemAnalyzer;
import qsgm.QueueSystemGM;
import qssim.QueueSystem;
import rla.ReliabilityAnalyzer;
import rlsim.ReliabilitySystem;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class Loader {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InterruptedException, ExecutionException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        try(InputStream in = getInput();
            OutputStream out = getOutput();
            OutputStream log = getLogger()) {

            if(args.length < 1)
                throw new IllegalArgumentException();

            switch (args[0]) {
                case "qsa":
                case "-qsa":
                case "/qsa":
                    QueueSystemAnalyzer.main(in, out, log);
                    break;
                case "qsgm":
                case "-qsgm":
                case "/qsm":
                    QueueSystemGM.main(in, out, log);
                    break;
                case "qss":
                case "-qss":
                case "/qss":
                    QueueSystem.main(in, out, log);
                    break;
                case "rla":
                case "-rla":
                case "/rla":
                    ReliabilityAnalyzer.main(in, out, log);
                    break;
                case "rls":
                case "-rls":
                case "/rls":
                    ReliabilitySystem.main(in, out, log);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

        }
    }

    private static boolean redirect = true;

    private static InputStream getInput() throws FileNotFoundException {
        if(redirect)
            return System.in;
        else
            return new FileInputStream("in.txt");
    }

    private static OutputStream getOutput() throws FileNotFoundException {
        if(redirect)
            return System.out;
        else
            return new FileOutputStream("out.csv");
    }

    private static OutputStream getLogger() throws FileNotFoundException {
        if(redirect)
            return System.err;
        else
            return new FileOutputStream("log.txt");
    }
}
