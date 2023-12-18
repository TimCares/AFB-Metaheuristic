import java.util.ArrayList;
import java.util.Random;

import result_src.AFBResult;
import result_src.AFBResultStats;

// AFB for TSP with 3-opt local search for big birds, big birds can only walk.
public class AFB_TSP_TopN_Opt3_NFB_Track extends AFB_TSP_TopN_Opt3_NFB {
    private ArrayList<Double> costOverTime;
    public AFB_TSP_TopN_Opt3_NFB_Track(
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

    @Override
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

}
