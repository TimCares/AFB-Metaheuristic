import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.hsh.Evaluable;
import com.hsh.Fitness;
import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;

import result_src.AFBResult;

public class AFB_SOP extends AFB<int[]> {

    public static void main(String[] args) throws IOException {
        Random rand = new Random();
        Dataset dataset = Parser.read("./data/sop/br17.10.sop");
        double[][] sop = TSPLoader.generateTSPFromNodes(dataset.getNodes());
        Fitness fitness = new Fitness(dataset);

        AFB<int[]> solver = new AFB_SOP(
                20,
                0.1589684022681154, // 0.01,
                0.4624556400235943, // 0.67,
                0.33611898159023834, // 0.07,
                0.6979749881176104, // 0.75,
                4_000,
                sop,
                rand);

        AFBResult<int[]> result = solver.solve();
        int[] tour = result.bestPosition;

        for (int j = 0; j < tour.length; j++) {
            tour[j]++;
        }

        ExamplePath solution = new ExamplePath(tour);
        ArrayList<Evaluable> examples = new ArrayList<Evaluable>();
        examples.add(solution);
        fitness.evaluate(examples);

        System.out.println("Best cost: " + result.bestCost);

    }

    protected int n_cities;
    protected double[][] sop;

    public AFB_SOP(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,
            int max_iters,
            double[][] sop,
            Random rand) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, rand);

        this.n_cities = sop.length;
        Logger.log("Processing SOP with length " + this.n_cities);
        this.sop = sop;
    }

    @Override
    int[] clone(int[] old) {
        return old.clone();
    }

    @Override
    void init() {
        this.birds = new ArrayList<Bird<int[]>>(this.n_birds);

        for (int birdIndex = 0; birdIndex < this.n_birds; birdIndex++) {
            Bird<int[]> newBird = new Bird<int[]>(
                    new int[this.n_cities],
                    0.0,
                    new int[this.n_cities],
                    0.0,
                    false,
                    BirdMove.FlyRandom);
            for (int cityIndex = 0; cityIndex < this.n_cities; cityIndex++) {
                newBird.position[cityIndex] = cityIndex;
                newBird.bestPosition[cityIndex] = cityIndex;
            }
            this.birds.add(newBird);
            fly(birdIndex);
            newBird.lastMove = BirdMove.FlyRandom;
            cost(birdIndex);
            newBird.bestPosition = clone(newBird.position);
            newBird.bestCost = newBird.cost;
            newBird.isBigBird = rand.nextDouble() > this.smallBirdRatio;
        }
        Logger.log("[DEBUG]: Initialization done.");
    }

    // Check if route is valid.
    // If an edge sop[i][j] is -1, then j **must** be visited before i.
    boolean isValid(int[] route) {
        HashSet<Integer> visited = new HashSet<Integer>();
        for (int i = 0; i < this.n_cities; i++) {
            // Loop through the route.
            for (int j = 0; j < this.n_cities; j++) {
                if (j != i // For every other city
                        && this.sop[route[i]][route[j]] == -1 // If the edge is invalid (i.e. j must have already been
                                                              // visited)
                        && !visited.contains(route[j]) // But j has not been visited yet
                ) {
                    // Then the route is invalid.
                    return false;
                }
            }
            visited.add(route[i]);
        }
        return true;
    }

    @Override
    void cost(int birdIndex) {
        Bird<int[]> bird = this.birds.get(birdIndex);
        int[] route = bird.position;

        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.sop[route[j - 1]][route[j]];
        }
        cost += this.sop[route[this.n_cities - 1]][route[0]];
        bird.cost = cost;
    }

    @Override
    void fly(int birdIndex) {
        Bird<int[]> bird = this.birds.get(birdIndex);
        do {
            ArrayList<Integer> boxedRoute = Arrays.stream(bird.position).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(boxedRoute);
            bird.position = boxedRoute.stream().mapToInt(i -> i).toArray();
        } while (!isValid(bird.position));
    }

    @Override
    void walk(int i) {
        // 2-opt local search
        Bird<int[]> bird = this.birds.get(i);
        do {
            int delta = 0;
            int k = -1;
            for (int u = 0; u < 100; u++) {
                Bird<int[]> otherBird = randomBirdExcept(i);
                k = this.rand.nextInt(this.n_cities - 1) + 1;
                assert k >= 1 && k < this.n_cities;
                int delta_new = findPositionOfCityInTour(bird.position[k], otherBird)
                        - findPositionOfCityInTour(bird.position[k - 1], otherBird);
                int delta_new_abs = Math.abs(delta_new);
                if ((1 < delta_new_abs) && (delta_new_abs < (this.n_cities - 1))) {
                    delta = delta_new;
                    break;
                }
            }
            assert k != -1;
            if (delta == 0) {
                delta = this.rand.nextInt(this.n_birds - 2) + 2; // between 2 and n-1
                assert delta >= 2 && delta <= this.n_birds - 1;
            }
            int l = (k + delta + this.n_cities) % this.n_cities;
            assert l >= 0;
            if (k > l) {
                int tmp = k;
                k = l;
                l = tmp;
            }
            int[] newRoute = bird.position.clone();
            reverseInRange(newRoute, k, l);
            bird.position = newRoute;
        } while (!isValid(bird.position));
    }

    // Reverses the order of the elements in the range [startInclusive,
    // endExclusive) of the given array.
    public static void reverseInRange(int[] route, int startInclusive, int endExclusive) {
        int start = Math.min(startInclusive, endExclusive);
        int end = Math.max(startInclusive, endExclusive);
        assert start <= end;
        for (int u = start, v = end - 1; u < v; u++, v--) { // l-1 => Figure 2 in the Paper!
            int temp = route[u];
            route[u] = route[v];
            route[v] = temp;
        }
    }

    // Performs a linear search for the position of the city with index 'cityIndex'
    // in the route of bird 'bird'.
    protected int findPositionOfCityInTour(int cityIndex, Bird<int[]> bird) {
        for (int routeIndex = 0; routeIndex < this.n_cities; routeIndex++) {
            if (bird.position[routeIndex] == cityIndex) {
                return routeIndex;
            }
        }
        assert false;
        System.err.println("Error: City with index '" + cityIndex + "' not found in tour.");
        return 0; // Should never happen since every tour contains every city.
    }

}
