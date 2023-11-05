import result_src.AFBResult;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

// AFB for TSP with joining of top n birds.
public class AFB_TSP_TopN extends AFB_TSP {
    private int joinTopN; // The number of best performing birds to join.
    public AFB_TSP_TopN(
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
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand);
        this.joinTopN = (int) (joinTop*this.n_birds);
    }

    public AFBResult<int[]> solve() {
        init();
        calcBestResult(); // Initialize birdOrder
        this.curr_iters = 0;

        long start = System.currentTimeMillis();
        while (this.curr_iters < this.max_iters) {
            for (int i = 0; i < this.n_birds; i++) {
                Bird<int[]> bird = this.birds.get(i);
                BirdMove nextMove = determineNextMove(bird);

                bird.lastMove = nextMove;
                switch (nextMove) {
                    case Walk:
                        walk(i);
                        cost(i);
                        this.curr_iters++;
                        break;
                    case FlyRandom:
                        fly(i);
                        cost(i);
                        this.curr_iters++;
                        break;
                    case FlyBest:
                        bird.position = clone(bird.bestPosition);
                        bird.cost = bird.bestCost; // TODO: Can we update the ranking here?
                        break;
                    case FlyToOtherBird:
                        // TODO: Improvement idea: Don't join any bird somehow prefer successful birds.

                        // Exclude i so the bird doesn't join itself
                        Bird<int[]> otherBird = randomBirdExcept(i);
                        bird.position = clone(otherBird.position);
                        bird.cost = otherBird.cost;
                        break;
                }

                if (bird.cost <= bird.bestCost) {
                    bird.bestPosition = clone(bird.position);
                    bird.bestCost = bird.cost;
                }
            }
            calcBestResult();
        }
        long time = (System.currentTimeMillis() - start);

        int bestBirdIndex =  calcBestResult();
        return new AFBResult<int[]>(
                this.birds.get(bestBirdIndex).bestPosition,
                this.birds.get(bestBirdIndex).bestCost,
                time
        );
    }

    @Override
    void walk(int i) {
        // 2-opt local search
        Bird<int[]> bird = this.birds.get(i);
        int delta = 0;
        int k = -1;
        for (int u=0; u<100; u++) {
            Bird<int[]> otherBird = randomBirdAllExcept(i);
            k = this.rand.nextInt(this.n_cities - 1) + 1;
            assert k >= 1 && k < this.n_cities;
            int delta_new = findPositionOfCityInTour(bird.position[k], otherBird) - findPositionOfCityInTour(bird.position[k-1], otherBird);
            int delta_new_abs = Math.abs(delta_new);
            if ( (1 < delta_new_abs) && (delta_new_abs < (this.n_cities-1)) ) {
                delta = delta_new;
                break;
            }
        }
        assert k != -1;
        if (delta == 0) {
            delta = this.rand.nextInt(this.n_birds-2)+2; // between 2 and n-1
            assert delta >= 2 && delta <= this.n_birds-1;
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
    }

    protected Bird<int[]> randomBirdExcept(int excludedBirdIndex) {
        int j = excludedBirdIndex;
        while (j == excludedBirdIndex) {
            j = this.rand.nextInt(this.joinTopN);
        }
        return this.birds.get(this.birdOrder[j]);
    }

    protected Bird<int[]> randomBirdAllExcept(int excludedBirdIndex) {
        int j = excludedBirdIndex;
        while (j == excludedBirdIndex) {
            j = this.rand.nextInt(this.n_birds);
        }
        return this.birds.get(j);
    }

    protected int calcBestResult() {
        this.birdOrder = IntStream.range(0, this.birds.size())
            .boxed()
            .sorted(Comparator.comparing(i -> this.birds.get(i).bestCost))
            .mapToInt(Integer::intValue)
            .toArray();
        return this.birdOrder[0];
    }
}
