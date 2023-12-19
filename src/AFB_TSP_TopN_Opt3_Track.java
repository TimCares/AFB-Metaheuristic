import java.util.ArrayList;
import java.util.Random;

import result_src.AFBResult;
import result_src.AFBResultStats;

// AFB for TSP without locality estimation and 3-opt local search.
public class AFB_TSP_TopN_Opt3_Track extends AFB_TSP_TopN_Opt3 {
    private ArrayList<Double> costOverTime;
    public AFB_TSP_TopN_Opt3_Track(
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
        this.costOverTime = new ArrayList<>();
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
                        this.costOverTime.add(this.birds.get(calcBestResultTrack()).bestCost);
                        this.curr_iters++;
                        break;
                    case FlyRandom:
                        fly(i);
                        cost(i);
                        this.costOverTime.add(this.birds.get(calcBestResultTrack()).bestCost);
                        this.curr_iters++;
                        break;
                    case FlyBest:
                        bird.position = clone(bird.bestPosition);
                        bird.cost = bird.bestCost;
                        break;
                    case FlyToOtherBird:
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
        return new AFBResultStats<>(
                this.birds.get(bestBirdIndex).bestPosition,
                this.birds.get(bestBirdIndex).bestCost,
                time,
                this.costOverTime
        );
    }

    protected int calcBestResultTrack() {
        int bestBirdIndex = -1;
        double bestCost = Double.MAX_VALUE;
        for (int birdIndex=0; birdIndex < this.n_birds; birdIndex++) {
            Bird<int[]> bird = this.birds.get(birdIndex);
            if (bird.bestCost < bestCost) {
                bestCost = bird.bestCost;
                bestBirdIndex = birdIndex;
            }
        }
        assert bestBirdIndex != -1;
        return bestBirdIndex;
    }
}
