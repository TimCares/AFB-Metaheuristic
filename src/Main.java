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

public class Main {
    public static void main(String[] args) throws IOException {
        boolean statistics = false;
        if (statistics) {
            StatsCreator statsCreator = new StatsCreator();
            statsCreator.collectStatistics();
            return;
        }

        // Pa561 optimal route => 2763
        //String xmlFilePath = "../data/pa561.xml";
        //String xmlFilePath = "../data/pa561.xml";
        String xmlFilePath = "./data/eil101.xml";
        //String xmlFilePath = "../data/rl5934.xml";
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);

        Dataset dataset = Parser.read("./data/eil101.tsp");
        Fitness fitness = new Fitness(dataset);

        System.out.println("Solving...");

        Random rand = new Random();
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

        int repeat = 1;
        double times = 0;
        double distance = 0;
        AFBResult<int[]> res = null;
        for (int i=0; i<repeat; i++) {
            long start = System.currentTimeMillis();
            res = solver.solve();
            times += (System.currentTimeMillis() - start) / 1000F;
            distance += res.bestCost;
        }

        int[] tour = res.bestPosition;

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println();
        System.out.println("Distance: " + distance/repeat);
        System.out.println("Time: " + df.format(times/repeat) + " seconds");

        ArrayList<Evaluable> examples = new ArrayList<>();
        for (int i=0; i< tour.length; i++) {
            tour[i]++;
        }
        ExamplePath solution = new ExamplePath(tour);
        examples.add(solution);

        fitness.evaluate(examples);

        for(Evaluable x : examples){
            System.out.println(String.format("%s -- %5d -- %s", x.getPath() , x.getFitness() , x.isValid()));
            System.out.println(x.getErrorCode());
        }
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
