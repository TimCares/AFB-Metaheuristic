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
        boolean statistics = true;
        boolean all = true;

        StatsCreator statsCreator = new StatsCreator();
        if (statistics) {
            statsCreator.collectStatistics();
            return;
        }

        Set<String> files;
        if (!all) {
            files = TSPLoader.listFile("eil101.tsp");
        } else {
            files = TSPLoader.listFiles(false, 10_000);
        }
        Map<String, Integer> getBestCosts = TSPLoader.getBestCosts();

        int n_trails_per_problem = 10;

        int size = files.size() * n_trails_per_problem;

        double[] distances = new double[size];
        double[] error = new double[size];
        double[] errorRelative = new double[size];
        double[] timesArray = new double[size];
        int i = 0;

        AFBResult<int[]> res = null;

        Random rand = new Random();
        rand.setSeed(42);

        for (int k=0; k< n_trails_per_problem; k++) {
            for (String filePath : files) {
                System.out.println("Problem: " + filePath);

                Dataset dataset = Parser.read(filePath);
                double[][] tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
                Fitness fitness = new Fitness(dataset);


                AFB<int[]> solver = null;
                if (tsp.length <= 101) {
                  // 0 - 101 cities
                  // Optimized for eil101.tsp
                  solver = new AFB_TSP_TopN_Opt3_S_NN(
                          619,
                          0.2235857343494696,
                          0.5745181637759841,
                          0.1379234632372206,
                          0.6028477073869289,
                          30_000_000,
                          tsp,
                          rand,
                          0.5095838567445682);
                } else if (tsp.length <= 493) {
                  // 102 - 493 cities
                  // Optimized for d493.tsp
                  solver = new AFB_TSP_TopN_Opt3_S_NN(
                          817,
                          0.050676743875328945,
                          0.5735153978684355,
                          0.25699240705138704,
                          0.08721972062950567,
                          20_000_000,
                          tsp,
                          rand,
                          0.24050371005872195);
                } else if (tsp.length <= 1000) {
                  // 494 - 1000
                  // Optimized for dsj1000.tsp
                  solver = new AFB_TSP_TopN_Opt3_S_NN(
                          386,
                          0.19409974797046914,
                          0.2073344109915185,
                          0.4623957982487684,
                          0.19421454763215162,
                          5_000_000,
                          tsp,
                          rand,
                          0.14837291455539625);
                } else {
                  // 1001+
                  // Optimized for fnl4461.tsp
                  solver = new AFB_TSP_TopN_Opt3_S_NN(
                          10,
                          0.3314722966118387,
                          0.0802001772255807,
                          0.421615891136749,
                          0.18933803292002926,
                          500_000,
                          tsp,
                          rand,
                          0.421615891136749 // topJoin is exactly identical to probMoveJoin. Very weird.
                        );
                }

                long start = System.currentTimeMillis();
                res = solver.solve();
                double time = (System.currentTimeMillis() - start) / 1000F;

                distances[i] = res.bestCost;
                timesArray[i] = time;
                error[i] = distances[i] - getBestCosts.get(TSPLoader.getProblemName(filePath));
                errorRelative[i] = error[i] / getBestCosts.get(TSPLoader.getProblemName(filePath));
                assert errorRelative[i] >= 0;
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
            System.out.println("------------------------------------------------Problems done: " + i + "------------------------------------------------");
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

        System.out.println("\nMedian rel Error: " + doubleAsPercent(median(errorRelative)));
        System.out.println("Mean rel Error: " + doubleAsPercent(mean(errorRelative)));
    }

    // Print a double as a percent value with two decimal places
    public static String doubleAsPercent(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value * 100) + "%";
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
