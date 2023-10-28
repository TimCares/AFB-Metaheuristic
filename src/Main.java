import com.hsh.Evaluable;
import com.hsh.Fitness;
import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;
import result_src.AFBResult;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            Stream<String> stringStream = Stream.of("./data/tsp/eil101.tsp");
            files = stringStream.collect(Collectors.toSet());
        } else {
            files = statsCreator.listFiles();
        }

        double times = 0;
        double distance = 0;
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
                0.01,
                0.67,
                0.07,
                1.00,
                1000,
                tsp,
                rand
            );

            long start = System.currentTimeMillis();
            res = solver.solve();
            times += (System.currentTimeMillis() - start) / 1000F;
            distance += res.bestCost;
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
        System.out.println("\tAvg. Distance: " + Math.round(distance/files.size()));
        System.out.println("\tAvg. Time: " + df.format(times/files.size()) + " seconds");
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
