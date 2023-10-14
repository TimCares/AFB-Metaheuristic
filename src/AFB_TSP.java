import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class AFB_TSP extends AFB<int[]> {
    protected int n_cities;
    protected double[][] tsp;
  
    public AFB_TSP(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        double[][] tsp,
        Random rand
    ) {
      super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, rand);

      this.n_cities = tsp.length;
      Logger.log("Processing TSP with length " + this.n_cities);
      this.tsp = tsp;
    }

    @Override
    int[] clone(int[] old) {
      return old.clone();
    }

    @Override
    void init() {
        this.birds = new ArrayList<Bird<int[]>>(this.n_birds);

        for (int birdIndex=0; birdIndex<this.n_birds; birdIndex++) {
            Bird<int[]> newBird = new Bird<int[]>(
                new int[this.n_cities],
                0.0,
                new int[this.n_cities],
                0.0,
                false,
                BirdMove.FlyRandom
            );
            for (int cityIndex=0; cityIndex<this.n_cities; cityIndex++) {
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
        this.curr_iters = 0; // just now, because we call 'cost' above
        Logger.log("[DEBUG]: Initialization done.");
    }

    @Override
    void cost(int birdIndex) { // after each iteration for each bird simultaneously?
        if ((this.curr_iters % 100) == 0) Logger.log("[DEBUG]: Iteration: " + this.curr_iters);

        Bird<int[]> bird = this.birds.get(birdIndex);
        int[] route = bird.position;
        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.tsp[route[j-1]][route[j]];
        }
        bird.cost = cost;
        this.curr_iters++;
    }

    @Override
    void fly(int birdIndex) {
        Bird<int[]> bird = this.birds.get(birdIndex);
        ArrayList<Integer> boxedRoute = new ArrayList<>(Arrays.stream(bird.position).boxed().toList());
        Collections.shuffle(boxedRoute);
        bird.position = boxedRoute.stream().mapToInt(i -> i).toArray();
    }

    @Override
    void walk(int i) {
        // 2-opt local search
        Bird<int[]> bird = this.birds.get(i);
        int delta = 0;
        int k = -1;
        for (int u=0; u<100; u++) {
            Bird<int[]> otherBird = randomBirdExcept(i);
            k = this.rand.nextInt(this.n_cities-1)+1;
            int delta_new = findPositionOfCityInTour(bird.position[k], otherBird) - findPositionOfCityInTour(bird.position[k-1], otherBird);
            int delta_new_abs = Math.abs(delta_new);
            if ( (1 < delta_new_abs) && (delta_new_abs < (this.n_cities-1)) ) {
                delta = delta_new_abs;//delta_new;
                break;
            }
        }
        if (this.n_birds <= 3) {
            delta = 2;
        } else if (delta==0) {
            delta = this.rand.nextInt(this.n_birds-2)+1; // between 2 and n-1
        }
        if (k==-1) System.err.println("Fehler mit K");
        int l = (k + delta) % this.n_cities;
        if (k > l) {
            int tmp = k;
            k = l;
            l = tmp;
        }
        for (int u = k, v = l-1; u < v; u++, v--) { // l-1 => Figure 2 in the Paper!
            int temp = bird.position[u];
            bird.position[u] = bird.position[v];
            bird.position[v] = temp;
        }
    }

    // Performs a linear search for the position of the city with index 'cityIndex' in the route of bird 'bird'.
    private int findPositionOfCityInTour(int cityIndex, Bird<int[]> bird) {
        for (int routeIndex=0; routeIndex<this.n_cities; routeIndex++) {
            if (bird.position[routeIndex] == cityIndex) {
                return routeIndex;
            }
        }
        assert false;
        System.err.println("Error: City with index '" + cityIndex + "' not found in tour.");
        return 0; // Should never happen since every tour contains every city.
    }

}
