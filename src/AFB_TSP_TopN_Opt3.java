import java.util.Random;

// AFB for TSP without locality estimation and 3-opt local search.
public class AFB_TSP_TopN_Opt3 extends AFB_TSP_TopN {
    public AFB_TSP_TopN_Opt3(
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

    void walk(int i) {
        // 3-opt local search
        Bird<int[]> bird = this.birds.get(i);
        
        int k1 = this.rand.nextInt(this.n_cities);
        int k2 = this.rand.nextInt(this.n_cities);
        int k3 = this.rand.nextInt(this.n_cities);

        //int delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
        //int k2 = (k1 + delta + this.n_cities) % this.n_cities;

        //delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
        //int k3 = (k2 + delta + this.n_cities) % this.n_cities; // danger/probability that k3 will be equal to k1 will be sufficiently small for large n_cities

        int[] currRoute = bird.position.clone();

        // init
        reverseInRange(currRoute, (k1+1) % this.n_cities, (k2+1) % this.n_cities); // actually 2-opt (we start with this one)
        double bestCost = costTour(currRoute);
        double currCost = bestCost;
        int[] newRoute = currRoute.clone();
        
        for (int j=1; j<7; j++) {
            currRoute = bird.position.clone(); // reset route
            switch (j) { // manipulate route
                case 1:
                    reverseInRange(currRoute, (k2+1) % this.n_cities, (k3+1) % this.n_cities); // actually 2-opt
                case 2:
                    reverseInRange(currRoute, (k3+1) % this.n_cities, (k1+1) % this.n_cities); // actually 2-opt
                case 3:
                    reverseInRange(currRoute, (k1+1) % this.n_cities, (k2+1) % this.n_cities);
                    reverseInRange(currRoute, (k2+1) % this.n_cities, (k3+1) % this.n_cities);
                case 4:
                    reverseInRange(currRoute, (k3+1) % this.n_cities, (k1+1) % this.n_cities);
                    reverseInRange(currRoute, (k1+1) % this.n_cities, (k2+1) % this.n_cities);
                case 5:
                    reverseInRange(currRoute, (k2+1) % this.n_cities, (k3+1) % this.n_cities);
                    reverseInRange(currRoute, (k3+1) % this.n_cities, (k1+1) % this.n_cities);
                case 6:
                    reverseInRange(currRoute, (k3+1) % this.n_cities, (k1+1) % this.n_cities);
                    reverseInRange(currRoute, (k1+1) % this.n_cities, (k2+1) % this.n_cities);
                    reverseInRange(currRoute, (k2+1) % this.n_cities, (k3+1) % this.n_cities);
            }
            currCost = costTour(currRoute);
            if (currCost < bestCost) { // update best route
                bestCost = currCost;
                newRoute = currRoute.clone();
            }
        }
        
        bird.position = newRoute;
    }

    double costTour(int[] route) {
        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.tsp[route[j-1]][route[j]];
        }
        cost += this.tsp[route[this.n_cities-1]][route[0]];
        return cost;
    }

}
