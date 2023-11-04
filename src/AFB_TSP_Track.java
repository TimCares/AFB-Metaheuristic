import result_src.AFBResultStats;
import java.util.ArrayList;
import java.util.Random;

// AFB for TSP with swap local search.
public class AFB_TSP_Track extends AFB_TSP {
    private ArrayList<Double> costOverTime;
    public AFB_TSP_Track(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,
            int max_iters,
            double[][] tsp,
            Random rand
    ) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand);
        this.costOverTime = new ArrayList<>();
    }

    public AFBResultStats<int[]> solve() {
        init();
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
                        this.costOverTime.add(this.birds.get(calcBestResult()).bestCost);
                        this.curr_iters++;
                        break;
                    case FlyRandom:
                        fly(i);
                        cost(i);
                        this.costOverTime.add(this.birds.get(calcBestResult()).bestCost);
                        this.curr_iters++;
                        break;
                    case FlyBest:
                        bird.position = clone(bird.bestPosition);
                        bird.cost = bird.bestCost;
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
        }
        long time = (System.currentTimeMillis() - start);

        int bestBirdIndex =  calcBestResult();
        return new AFBResultStats<>(
                this.birds.get(bestBirdIndex).bestPosition,
                this.birds.get(bestBirdIndex).bestCost,
                time,
                this.costOverTime
        );
    }
}
