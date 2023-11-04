import result_src.AFBResult;

import java.util.Random;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.Comparator;

// A generic implementation of the Artificial Feeding Birds metaheuristic.
// The type `T` is the type of the position of a bird in the search space.
abstract public class AFB<T> {

    // The number of birds in the swarm.
    // Does not change the number of iterations, more birds means less iterations per bird.
    protected int n_birds;
    protected double smallBirdRatio;
    protected double probMoveWalk;
    protected double probMoveRandom;
    protected double probMoveBest;
    protected double probMoveJoin;
    protected ArrayList<Bird<T>> birds;

    // The maximum evalutations of the cost function.
    protected int max_iters;
    protected int curr_iters;
    protected Random rand;

    protected double rangeDiff;

    protected int[] birdOrder;
    protected int joinTopN; // The number of best performing birds to join.

    public AFB(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        Random rand,
        double joinTop
    ) {
        // configuration
        this.n_birds = n_birds;
        this.smallBirdRatio = smallBirdRatio;
        this.birds = new ArrayList<Bird<T>>(n_birds);

        // stopping criteria
        this.max_iters = max_iters;

        this.probMoveRandom = probMoveRandom;
        this.probMoveBest = probMoveBest;
        this.probMoveJoin = probMoveJoin;
        this.probMoveWalk = 1.0 - probMoveRandom - probMoveBest - probMoveJoin;
        if (this.probMoveWalk < 0.0) {
            throw new Error("Probabilities can't add up to more than 100%");
        }
        this.rand = rand;
        this.rangeDiff = 1.0 - this.probMoveJoin;
        this.joinTopN = (int) (joinTop*this.n_birds);
    }

    protected BirdMove determineNextMove(Bird<T> bird) {
        if (bird.lastMove.isFlying()
         || bird.position.equals(bird.bestPosition)) {
            // If the bird just flew or is at the best position yet, always walk.
            return BirdMove.Walk;
        }
        if (bird.isBigBird && rand.nextDouble() < this.probMoveJoin) {
            // A small bird does not join other birds.
            return BirdMove.FlyToOtherBird;
        }

        double p = this.rand.nextDouble()*this.rangeDiff + this.probMoveJoin;
        if (p <= this.probMoveWalk) {
            return BirdMove.Walk;
        } else if (p <= this.probMoveWalk + this.probMoveRandom) {
            return BirdMove.FlyRandom;
        } else {
            return BirdMove.FlyBest;
        }
    }

    protected AFBResult<T> solve() {
        init();
        calcBestResult(); // Initialize birdOrder
        this.curr_iters = 0;

        long start = System.currentTimeMillis();
        while (this.curr_iters < this.max_iters) {
            for (int i = 0; i < this.n_birds; i++) {
                Bird<T> bird = this.birds.get(i);
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
                        // TODO: Improvement idea: Don't join any bird somehow prefer successful birds.

                        // Exclude i so the bird doesn't join itself
                        Bird<T> otherBird = randomBirdExcept(i);
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
        
        
        int bestBirdIndex = calcBestResult();

        return new AFBResult<T>(
            this.birds.get(bestBirdIndex).bestPosition,
            this.birds.get(bestBirdIndex).bestCost,
            time
        );
    }

    abstract void init();
    abstract void cost(int i);
    abstract void fly(int i);
    abstract void walk(int i);

    abstract T clone(T old);

    // Generate a random bird except for the bird at index `excludedBirdIndex`.
    protected Bird<T> randomBirdExcept(int excludedBirdIndex) {
        int j = excludedBirdIndex;
        while (j == excludedBirdIndex) {
            j = this.rand.nextInt(this.joinTopN);
        }
        return this.birds.get(this.birdOrder[j]);
    }

    protected int calcBestResult() {
        this.birdOrder = IntStream.range(0, this.birds.size())
            .boxed()
            .sorted(Comparator.comparing(i -> this.birds.get(i).bestCost))
            .mapToInt(Integer::intValue)
            .toArray();
        return this.birdOrder[0];
    }

    protected int calcBestResultOld() {
        int bestBirdIndex = -1;
        double bestCost = Double.MAX_VALUE;
        for (int birdIndex=0; birdIndex < this.n_birds; birdIndex++) {
            Bird<T> bird = this.birds.get(birdIndex);
            if (bird.bestCost < bestCost) {
                bestCost = bird.bestCost;
                bestBirdIndex = birdIndex;
            }
        }
        assert bestBirdIndex != -1;
        return bestBirdIndex;
    }

    protected Bird<T> randomBirdExceptOld(int excludedBirdIndex) {
        int j = excludedBirdIndex;
        while (j == excludedBirdIndex) {
            j = this.rand.nextInt(this.n_birds);
        }
        return this.birds.get(j);
    }
}
