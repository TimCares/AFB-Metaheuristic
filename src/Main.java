import com.hsh.Evaluable;
import com.hsh.Fitness;
import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;
import result_src.AFBResult;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean statistics = false;
        boolean all = true;

        StatsCreator statsCreator = new StatsCreator();
        if (statistics) {
            statsCreator.collectStatistics();
            return;
        }

        Set<String> files;
        if (!all) {
            files = TSPLoader.listFiles("eil101.tsp");
        } else {
            files = TSPLoader.listFiles();
        }
        Map<String, Integer> getBestCosts = TSPLoader.getBestCosts();

        double[] distances = new double[files.size()];
        double[] error = new double[files.size()];
        double[] timesArray = new double[files.size()];
        int i = 0;

        AFBResult<int[]> res = null;

        for (String filePath : files) {
            System.out.println("Problem: " + filePath);

            Dataset dataset = Parser.read(filePath);
            double[][] tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
            Fitness fitness = new Fitness(dataset);

            Random rand = new Random();
            rand.setSeed(42);

            AFB<int[]> solver = new AFB_TSP(
                200,
                0.1589684022681154,//0.01,
                0.4624556400235943, //0.67,
                0.33611898159023834, //0.07,
                0.6979749881176104, //0.75,
                2_000_000,
                tsp,
                rand,
                0.2
            );

            long start = System.currentTimeMillis();
            res = solver.solve();
            double time = (System.currentTimeMillis() - start) / 1000F;

            distances[i] = res.bestCost;
            timesArray[i] = time;
            error[i] = distances[i] - getBestCosts.get(TSPLoader.getProblemName(filePath));
            i++;

            int[] tour = res.bestPosition;

            ArrayList<Evaluable> examples = new ArrayList<>();
            for (int j=0; j< tour.length; j++) {
                tour[j]++;
            }
            ExamplePath solution = new ExamplePath(tour);
            examples.add(solution);

            fitness.evaluate(examples);

            System.out.println("\nResult:");
            System.out.println("\tFitness: " + examples.get(0).getFitness());
            System.out.println("\tPath: " + examples.get(0).getPath());
            if (!examples.get(0).isValid()) {
                System.out.println("\tERROR!");
                System.out.println("\t" + examples.get(0).getErrorCode());
            }
            System.out.println();
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println("\nCombined Results: ");
        System.out.println("\tAvg. Distance: " + Math.round(mean(distances)));
        System.out.println("\tAvg. Time: " + df.format(mean(timesArray)) + " seconds");
        System.out.println("\tMedian Distance: " + Math.round(median(distances)));
        System.out.println("\tMedian Time: " + df.format(median(timesArray)) + " seconds");

        System.out.println("\nMedian abs Error: " + median(error));
        System.out.println("Mean abs Error: " + Math.round(mean(error)));
    }












    public static double median(double[] m) {
        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (m[middle-1] + m[middle]) / 2.0;
        }
    }

    public static double mean(double[] m) {
        double sum = 0;
        for (double x : m) {
            sum += x;
        }
        return sum / m.length;
    }
}

class ExamplePath extends Evaluable{
    ArrayList<Integer> path;
    public ExamplePath(int[] path) {
        // wandelt int[] in eine ArrayList um
        this.path = new ArrayList<>();
        for(int x : path){
            this.path.add(x);
        }
    }

    @Override
    public ArrayList<Integer> getPath() {
        return path;
    }
}
