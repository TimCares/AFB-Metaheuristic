import java.util.Random;

public class AFB_OPT {
    private int n_birds;
    private int n_cities;
    private double r;
    private double[][] tsp;
    private int[][] X;
    private int[][] x;
    private double[] F;
    private double[] f;
    private boolean[] s; // Is big bird? true => yes!
    private int[] m;
    private int max_iters;
    private int curr_iters;
    private double[] probas;
    private double rangeDiff;
    private Random rand;
    //boolean[][] visited = new boolean[][] {}; later (from Tabu search)?

    public AFB_OPT(int n_birds, double p1, double p2, double p3, double p4,
               double r, int max_iters, double[][] tsp) {

        // configuration
        this.n_birds = n_birds;
        this.n_cities = tsp.length;
        this.r = r;
        this.tsp = tsp;


        // per bird data
        this.X = new int[n_birds][n_cities];
        this.x = new int[n_birds][n_cities];
        this.F = new double[n_birds];
        this.f = new double[n_birds];
        this.s = new boolean[n_birds];
        this.m = new int[n_birds];

        // stopping criteria
        this.max_iters = max_iters;

        // efficient implementation
        this.probas = new double[] {p1, p2, p3, p4};
        this.rangeDiff = 1-p4;
        this.rand = new Random();
        this.rand.setSeed(42);

    }

    public Object[] solve() {
        init();

        while (this.curr_iters < this.max_iters) {
            for (int i = 0; i < this.n_birds; i++) { // multiprocessing?
                double p;
                if ((this.m[i] > 1) || (this.f[i] == this.F[i])) {
                    p = 1;
                } else if (!this.s[i]) {
                    p = this.rand.nextDouble() * rangeDiff + this.probas[3];
                } else {
                    p = this.rand.nextDouble();
                }
                if (p >= (1 - this.probas[0])) {
                    this.m[i] = 1;
                    walk(i);
                    cost(i);
                } else if (p >= (this.probas[2] + this.probas[3])) {
                    this.m[i] = 2;
                    fly(i);
                    cost(i);
                } else if (p >= this.probas[3]) {
                    this.m[i] = 3;
                    this.x[i] = this.X[i]; // correct?
                    this.f[i] = this.F[i];
                } else {
                    this.m[i] = 4;
                    int j = exclusiveRandInt(i);
                    this.x[i] = this.x[j];
                    this.f[i] = this.f[j];
                }
                if (this.f[i] <= this.F[i]) {
                    this.X[i] = this.x[i];
                    this.F[i] = this.f[i];
                }
            }

        }
        int idxBest = 0;
        double bestCost = this.F[0];
        double curr_cost;
        for (int i=1; i < this.n_birds; i++) {
            curr_cost = this.F[i];
            if (curr_cost < bestCost) {
                bestCost = curr_cost;
                idxBest = i;
            }
        }
        return new Object[] {this.X[idxBest], bestCost};
    }

    private void init() {
        for (int i=0; i<this.n_birds; i++) {
            for (int j=0; j<this.n_cities; j++) {
                this.x[i][j] = j;
                this.X[i][j] = j;
            }
            fly(i);
            cost(i);
            this.F[i] = this.f[i];
            this.m[i] = 2;
            this.s[i] = (i <= Math.ceil(this.r*this.n_birds));
        }
        this.curr_iters = 0; // just now, because we call 'cost' above
    }

    private void cost(int i) { // after each iteration for each bird simultaneously?
        if ((this.curr_iters % 100) == 0) System.out.println("[DEBUG]: Iteration: " + this.curr_iters);

        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.tsp[this.x[i][j-1]][this.x[i][j]];
        }
        this.f[i] = cost;
        this.curr_iters++;
    }

    private void fly(int i) { // essentially just shuffles the order
        for (int j = this.n_cities - 1; j > 0; j--) {
            int idx = this.rand.nextInt(j + 1);

            int tmp = this.x[i][idx];
            this.x[i][idx] = this.x[i][j];
            this.x[i][j] = tmp;
        }
    }

    private void walk(int i) {
        int delta = 0;
        int k = -1;
        for (int u=0; u<100; u++) {
            int j = exclusiveRandInt(i);
            k = this.rand.nextInt(this.n_cities-1)+1;
            int delta_new = position_of(this.x[i][k], j) - position_of(this.x[i][k-1], j);
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
            int temp = this.x[i][u];
            this.x[i][u] = this.x[i][v];
            this.x[i][v] = temp;
        }
    }

    private int position_of(int value, int j) {
        for (int pos=0; pos<this.n_cities; pos++) {
            if (this.x[j][pos] == value) {
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
