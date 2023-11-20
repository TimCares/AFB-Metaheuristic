import java.util.Random;

// AFB for TSP without locality estimation and 3-opt local search only for big birds.
public class AFB_TSP_TopN_Opt3_S extends AFB_TSP_TopN {
    public AFB_TSP_TopN_Opt3_S(
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
        if (this.birds.get(i).isBigBird) {
            opt_3(i);
        } else {
            opt_2(i);
        }
    }

    void opt_3(int i) {
        // 3-opt local search
        Bird<int[]> bird = this.birds.get(i);
        
        int k1 = this.rand.nextInt(this.n_cities);

        int delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
        int k2 = (k1 + delta + this.n_cities) % this.n_cities;

        delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
        int k3 = (k2 + delta + this.n_cities) % this.n_cities; // danger (probability) that k3 will be equal to k1 will be sufficiently small for large n_cities

        int[] currRoute = bird.position.clone();

        // init
        int k1_offset = (k1+1) % this.n_cities;
        int k2_offset = (k2+1) % this.n_cities;
        int k3_offset = (k3+1) % this.n_cities;

        reverseInRange(currRoute, k1_offset, k2_offset); // actually 2-opt (we start with this one)
        double bestCost = costTour(currRoute);
        double currCost = bestCost;
        int[] newRoute = currRoute.clone();
        
        for (int j=1; j<7; j++) {
            currRoute = bird.position.clone(); // reset route
            switch (j) { // manipulate route
                case 1:
                    reverseInRange(currRoute, k2_offset, k3_offset); // actually 2-opt
                case 2:
                    reverseInRange(currRoute, k3_offset, k1_offset); // actually 2-opt
                case 3:
                    reverseInRange(currRoute, k1_offset, k2_offset);
                    reverseInRange(currRoute, k2_offset, k3_offset);
                case 4:
                    reverseInRange(currRoute, k3_offset, k1_offset);
                    reverseInRange(currRoute, k1_offset, k2_offset);
                case 5:
                    reverseInRange(currRoute, k2_offset, k3_offset);
                    reverseInRange(currRoute, k3_offset, k1_offset);
                case 6:
                    reverseInRange(currRoute, k3_offset, k1_offset);
                    reverseInRange(currRoute, k1_offset, k2_offset);
                    reverseInRange(currRoute, k2_offset, k3_offset);
            }
            currCost = costTour(currRoute);
            if (currCost < bestCost) { // update best route
                bestCost = currCost;
                newRoute = currRoute.clone();
            }
        }
        
        bird.position = newRoute;
    }

    void opt_2 (int i) {
        // 2-opt local search
        Bird<int[]> bird = this.birds.get(i);
        int k = this.rand.nextInt(this.n_cities - 1) + 1;
        int delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
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
