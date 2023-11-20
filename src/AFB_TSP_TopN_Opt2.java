import java.util.Random;

// AFB for TSP without locality estimation.
public class AFB_TSP_TopN_Opt2 extends AFB_TSP_TopN {
    public AFB_TSP_TopN_Opt2(
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
        // 2-opt local search
        Bird<int[]> bird = this.birds.get(i);
        int k = this.rand.nextInt(this.n_cities - 1) + 1;
        int delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1;
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

}
