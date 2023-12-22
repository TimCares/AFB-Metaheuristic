import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.hsh.Evaluable;
import com.hsh.Fitness;
import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;

// AFB for TSP without locality estimation and 3-opt local search only for big birds.
public class FINAL_AFB_TSP_TopN_Opt3_S_NN extends AFB_TSP_TopN_Opt3_S {
    public FINAL_AFB_TSP_TopN_Opt3_S_NN(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,  
            int max_iters,
            double[][] tsp,
            Random rand,
            double joinTop
    ) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand, joinTop);
    }

    @Override
    void init() {
        this.birds = new ArrayList<Bird<int[]>>(this.n_birds);

        for (int birdIndex=0; birdIndex<this.n_birds; birdIndex++) {
            int[] nnTour = getNearestNeighborTour();
            Bird<int[]> newBird = new Bird<int[]>(
                nnTour,
                0.0,
                nnTour,
                0.0,
                false,
                BirdMove.FlyRandom
            );
            this.birds.add(newBird);
            newBird.lastMove = BirdMove.FlyRandom;
            cost(birdIndex);
            newBird.bestPosition = clone(newBird.position);
            newBird.bestCost = newBird.cost;
            newBird.isBigBird = rand.nextDouble() > this.smallBirdRatio;
        }
        Logger.log("[DEBUG]: Initialization done.");
    }

    // Perform the greedy nearest neighbor algorithm to get a tour.
    private int[] getNearestNeighborTour() {
        Set<Integer> unvisited = new HashSet<Integer>();
        for (int i=0; i<this.n_cities; i++) {
            unvisited.add(i);
        }
        int[] tour = new int[this.n_cities];
        int tourIndex = 0;

        // Start at a random city to get some variation.
        int currCity = rand.nextInt(this.n_cities);

        while (unvisited.size() > 0) {
            tour[tourIndex] = currCity;
            tourIndex++;
            unvisited.remove(currCity);
            currCity = getNearestNeighbor(currCity, unvisited);
        }
        return tour;
    }
    
    private int getNearestNeighbor(int city, Set<Integer> unvisited) {
        double minCost = Double.MAX_VALUE;
        int minCity = -1;
        for (int neighbor : unvisited) {
            double cost = this.tsp[city][neighbor];
            if (cost < minCost) {
                minCost = cost;
                minCity = neighbor;
            }
        }
        return minCity;
    }

    public static void main(String[] args) throws IOException{
        Dataset dataset = Parser.read(args[0]);
        double[][] tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
        Fitness fitness = new Fitness(dataset);

        Random rand = new Random();

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

        System.out.println("[DEBUG]: Solving...");
        int[] tour = solver.solve().bestPosition;
        for (int j=0; j< tour.length; j++) {
            tour[j]++;
        }
        System.out.println("[DEBUG]: Done");

        ExamplePath solution = new ExamplePath(tour);

        ArrayList<Evaluable> examples = new ArrayList<>();
        
        examples.add(solution);

        fitness.evaluate(examples);
    }

}
