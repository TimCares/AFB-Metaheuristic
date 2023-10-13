import java.util.Random;
import java.util.ArrayList;

// A generic implementation of the Artificial Feeding Birds metaheuristic.
// The type `T` is the type of the position of a bird in the search space.
abstract public class AFB<T> {

    protected int n_birds;
    protected double smallBirdRatio;
    protected double probMoveWalk;
    protected double probMoveRandom;
    protected double probMoveBest;
    protected double probMoveJoin;
    protected ArrayList<Bird<T>> birds;

    protected int max_iters;
    protected int curr_iters;
    protected Random rand;

    protected double rangeDiff;

    //boolean[][] visited = new boolean[][] {}; later (from Tabu search)?

    public AFB(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        Random rand
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
    }

    private BirdMove determineNextMove(Bird<T> bird) {
        if (bird.lastMove.isFlying()
         || bird.position.equals(bird.bestPosition)) {
            // If the bird just flew or is at the best position yet, always walk.
            return BirdMove.Walk;
        }
        if (bird.isBigBird && rand.nextDouble() < this.probMoveJoin) {
            // A small bird does not join other birds.
            return BirdMove.FlyBest;
        }

        double p = this.rand.nextDouble(this.probMoveWalk + this.probMoveRandom + this.probMoveBest);
        if (p <= this.probMoveWalk) {
            return BirdMove.Walk;
        } else if (p <= this.probMoveWalk + this.probMoveRandom) {
            return BirdMove.FlyRandom;
        } else {
            return BirdMove.FlyBest;
        }
    }

    public AFBResult<T> solve() {
        init();

        while (this.curr_iters < this.max_iters) {
            for (int i = 0; i < this.n_birds; i++) { // multiprocessing?
                Bird<T> bird = this.birds.get(i);
                BirdMove nextMove = determineNextMove(bird);

                bird.lastMove = nextMove;
                switch (nextMove) {
                    case Walk:
                        walk(i);
                        cost(i);
                        break;
                    case FlyRandom:
                        fly(i);
                        cost(i);
                        break;
                    case FlyBest:
                        bird.position = clone(bird.bestPosition);
                        bird.cost = bird.bestCost;
                        break;
                    case FlyToOtherBird:
                        // TODO: Improvement idea: Don't join any bird somehow prefer successful birds.

                        // Exclude i so the bird doesn't join itself
                        int otherBirdIndex = exclusiveRandInt(i);
                        Bird<T> otherBird = this.birds.get(otherBirdIndex);
                        bird.position = clone(otherBird.position);
                        bird.cost = otherBird.cost;
                        break;
                }
                
                if (bird.cost < bird.bestCost) {
                    bird.bestPosition = clone(bird.position);
                    bird.bestCost = bird.cost;
                }
            }
        }
        
        
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

        return new AFBResult<T>(
            this.birds.get(bestBirdIndex).bestPosition,
            bestCost
        );
    }

    abstract void init();
    abstract void cost(int i);
    abstract void fly(int i);
    abstract void walk(int i);

    abstract T clone(T old);

    protected int exclusiveRandInt(int exclude) {
        int j = exclude;
        while (j == exclude) {
            j = this.rand.nextInt(this.n_birds);
        }
        return j;
    }

}
