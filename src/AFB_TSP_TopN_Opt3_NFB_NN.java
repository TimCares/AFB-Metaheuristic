import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// AFB for TSP with 3-opt local search for big birds, big birds can only walk.
public class AFB_TSP_TopN_Opt3_NFB_NN extends AFB_TSP_TopN_Opt3_NFB {
    public AFB_TSP_TopN_Opt3_NFB_NN(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,
            int max_iters,
            double[][] tsp,
            Random rand,
            double joinTop) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand, joinTop);
    }

    @Override
    void init() {
        this.birds = new ArrayList<Bird<int[]>>(this.n_birds);

        for (int birdIndex = 0; birdIndex < this.n_birds; birdIndex++) {
            int[] nnTour = getNearestNeighborTour();
            Bird<int[]> newBird = new Bird<int[]>(
                    nnTour,
                    0.0,
                    nnTour,
                    0.0,
                    false,
                    BirdMove.FlyRandom);
            this.birds.add(newBird);
            newBird.lastMove = BirdMove.FlyRandom;
            cost(birdIndex);
            newBird.bestPosition = clone(newBird.position);
            newBird.bestCost = newBird.cost;
            newBird.isBigBird = rand.nextDouble() > this.smallBirdRatio;
        }
        Logger.debug("Initialization done.");
    }

    // Perform the greedy nearest neighbor algorithm to get a tour.
    private int[] getNearestNeighborTour() {
        Set<Integer> unvisited = new HashSet<Integer>();
        for (int i = 0; i < this.n_cities; i++) {
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

}
