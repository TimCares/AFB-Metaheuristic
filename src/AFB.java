import java.util.Random;
import java.util.ArrayList;

abstract public class AFB<T> {
    protected int n_birds;
    protected double smallBirdRatio;
    protected ArrayList<T> bestPositions;
    protected ArrayList<T> currPositions;
    protected double[] bestPositionValues;
    protected double[] currPositionValues;
    protected boolean[] isBigBird;
    protected int[] lastMoves;
    protected int max_iters;
    protected int curr_iters;
    protected double[] probas;
    protected double rangeDiff;
    protected Random rand;
    //boolean[][] visited = new boolean[][] {}; later (from Tabu search)?

    public AFB(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters
    ) {
        // configuration
        this.n_birds = n_birds;
        this.smallBirdRatio = smallBirdRatio;

        // per bird data
        this.bestPositionValues = new double[n_birds];
        this.currPositionValues = new double[n_birds];
        this.isBigBird = new boolean[n_birds];
        this.lastMoves = new int[n_birds];

        // stopping criteria
        this.max_iters = max_iters;

        double probMoveWalk = 1.0 - probMoveRandom - probMoveBest - probMoveJoin;
        if (probMoveWalk < 0.0) {
            throw new Error("Probabilities can't add up to more than 100%");
        }
        // efficient implementation
        this.probas = new double[] {
            probMoveWalk,
            probMoveRandom,
            probMoveBest,
            probMoveJoin
        };
        this.rangeDiff = 1-probMoveJoin;
        this.rand = new Random();
        this.rand.setSeed(42);

    }


    public AFBResult<T> solve() {
        init();

        while (this.curr_iters < this.max_iters) {
            for (int i = 0; i < this.n_birds; i++) { // multiprocessing?
                double p;
                if ((this.lastMoves[i] > 1) || (this.currPositionValues[i] == this.bestPositionValues[i])) {
                    p = 1;
                } else if (!this.isBigBird[i]) {
                    p = this.rand.nextDouble() * rangeDiff + this.probas[3];
                } else {
                    p = this.rand.nextDouble();
                }
                if (p >= (1 - this.probas[0])) {
                    this.lastMoves[i] = 1;
                    walk(i);
                    cost(i);
                } else if (p >= (this.probas[2] + this.probas[3])) {
                    this.lastMoves[i] = 2;
                    fly(i);
                    cost(i);
                } else if (p >= this.probas[3]) {
                    this.lastMoves[i] = 3;
                    this.currPositions.set(i, this.bestPositions.get(i)); // correct?
                    this.currPositionValues[i] = this.bestPositionValues[i];
                } else {
                    this.lastMoves[i] = 4;
                    int j = exclusiveRandInt(i);
                    this.currPositions.set(i, this.clone(this.currPositions.get(j)));
                    this.currPositionValues[i] = this.currPositionValues[j];
                }
                if (this.currPositionValues[i] <= this.bestPositionValues[i]) {
                    this.bestPositions.set(i, this.currPositions.get(i));
                    this.bestPositionValues[i] = this.currPositionValues[i];
                }
            }

        }
        int idxBest = 0;
        double bestCost = this.bestPositionValues[0];
        double curr_cost;
        for (int i=1; i < this.n_birds; i++) {
            curr_cost = this.bestPositionValues[i];
            if (curr_cost < bestCost) {
                bestCost = curr_cost;
                idxBest = i;
            }
        }
        return new AFBResult<T>(this.bestPositions.get(idxBest), bestCost);
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
