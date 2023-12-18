import java.util.Random;

import result_src.AFBResult;

// AFB for TSP without locality estimation and 3-opt local search.
public class AFB_TSP_TopN_Opt3_NN_ES extends AFB_TSP_TopN_Opt3_NN {
    private int patience;
    private int nPhasesNoImprovement;
    private double bestCost;
    public AFB_TSP_TopN_Opt3_NN_ES(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,  
            int max_iters,
            double[][] tsp,
            Random rand,
            double joinTop,
            double patienceWeight
    ) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand, joinTop);
        this.patience = (int) (patienceWeight*max_iters/n_birds);
        this.nPhasesNoImprovement = 0;
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
                        this.curr_iters++;
                        break;
                    case FlyRandom:
                        fly(i);
                        cost(i);
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
            double currBestCost = this.birds.get(this.birdOrder[0]).bestCost;
            if (currBestCost < this.bestCost) {
                this.nPhasesNoImprovement = 0;
                this.bestCost = currBestCost;
            } else {
                this.nPhasesNoImprovement++;
            }
            if (this.patience == this.nPhasesNoImprovement) break;
        }
        long time = (System.currentTimeMillis() - start);

        int bestBirdIndex =  calcBestResult();
        return new AFBResult<int[]>(
                this.birds.get(bestBirdIndex).bestPosition,
                this.birds.get(bestBirdIndex).bestCost,
                time
        );
    }

}
