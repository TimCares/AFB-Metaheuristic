import java.util.Random;

public class AFB {
    private int n_birds;
    private int n_cities;
    private double smallBirdRatio;
    private double[][] tsp;
    private int[][] bestPositions;
    private int[][] currPositions;
    private double[] bestPositionValues;
    private double[] currPositionValues;
    private boolean[] isBigBird;
    private int[] lastMoves;
    private int max_iters;
    private int curr_iters;
    private double[] probas;
    private double rangeDiff;
    private Random rand;
    //boolean[][] visited = new boolean[][] {}; later (from Tabu search)?

    public AFB(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        double[][] tsp
    ) {
        // configuration
        this.n_birds = n_birds;
        this.n_cities = tsp.length;
        this.smallBirdRatio = smallBirdRatio;
        this.tsp = tsp;


        // per bird data
        this.bestPositions = new int[n_birds][n_cities];
        this.currPositions = new int[n_birds][n_cities];
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
        this.probas = new double[] {probMoveWalk, probMoveRandom, probMoveBest, probMoveJoin};
        this.rangeDiff = 1-probMoveJoin;
        this.rand = new Random();
        this.rand.setSeed(42);

    }

    public Object[] solve() {
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
                    this.currPositions[i] = this.bestPositions[i]; // correct?
                    this.currPositionValues[i] = this.bestPositionValues[i];
                } else {
                    this.lastMoves[i] = 4;
                    int j = exclusiveRandInt(i);
                    this.currPositions[i] = this.currPositions[j];
                    this.currPositionValues[i] = this.currPositionValues[j];
                }
                if (this.currPositionValues[i] <= this.bestPositionValues[i]) {
                    this.bestPositions[i] = this.currPositions[i];
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
        return new Object[] {this.bestPositions[idxBest], bestCost};
    }

    private void init() {
        for (int i=0; i<this.n_birds; i++) {
            for (int j=0; j<this.n_cities; j++) {
                this.currPositions[i][j] = j;
                this.bestPositions[i][j] = j;
            }
            fly(i);
            cost(i);
            this.bestPositionValues[i] = this.currPositionValues[i];
            this.lastMoves[i] = 2;
            this.isBigBird[i] = (i <= Math.ceil(this.smallBirdRatio*this.n_birds));
        }
        this.curr_iters = 0; // just now, because we call 'cost' above
    }

    private void cost(int i) { // after each iteration for each bird simultaneously?
        if ((this.curr_iters % 100) == 0) System.out.println("[DEBUG]: Iteration: " + this.curr_iters);

        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.tsp[this.currPositions[i][j-1]][this.currPositions[i][j]];
        }
        this.currPositionValues[i] = cost;
        this.curr_iters++;
    }

    private void fly(int i) { // essentially just shuffles the order
        for (int j = this.n_cities - 1; j > 0; j--) {
            int idx = this.rand.nextInt(j + 1);

            int tmp = this.currPositions[i][idx];
            this.currPositions[i][idx] = this.currPositions[i][j];
            this.currPositions[i][j] = tmp;
        }
    }

    private void walk(int i) {
        int delta = 0;
        int k = -1;
        for (int u=0; u<100; u++) {
            int j = exclusiveRandInt(i);
            k = this.rand.nextInt(this.n_cities-1)+1;
            int delta_new = position_of(this.currPositions[i][k], j) - position_of(this.currPositions[i][k-1], j);
            int delta_new_abs = Math.abs(delta_new);
            if ( (1 < delta_new_abs) && (delta_new_abs < (this.n_cities-1)) ) {
                delta = delta_new_abs;//delta_new;
                break;
            }
        }
        if (this.n_birds <= 3) {
            delta = 2;
        } else if (delta==0) {
            delta = this.rand.nextInt(this.n_birds-2)+1; // between 2 and n-1
        }
        if (k==-1) System.err.println("Fehler mit K");
        int l = (k + delta) % this.n_cities;
        if (k > l) {
            int tmp = k;
            k = l;
            l = tmp;
        }
        for (int u = k, v = l-1; u < v; u++, v--) { // l-1 => Figure 2 in the Paper!
            int temp = this.currPositions[i][u];
            this.currPositions[i][u] = this.currPositions[i][v];
            this.currPositions[i][v] = temp;
        }
    }

    private int position_of(int value, int j) {
        for (int pos=0; pos<this.n_cities; pos++) {
            if (this.currPositions[j][pos] == value) {
                return pos;
            }
        }
        System.err.println("Fehler: Stadt mit Index '" + value + "' nicht in Tour  '" + value + "' gefunden!");
        return 0; // Wird nie der Fall sein, da value, also der index der Stadt immer in der Tour j vorkommt
    }

    private int exclusiveRandInt(int exclude) {
        int j = exclude;
        while (j == exclude) {
            j = this.rand.nextInt(this.n_birds);
        }
        return j;
    }

}
