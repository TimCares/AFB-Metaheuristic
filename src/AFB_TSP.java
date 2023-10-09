import java.util.Collections;
import java.util.ArrayList;

public class AFB_TSP extends AFB<int[]> {
    private int n_cities;
    private double[][] tsp;
  
    public AFB_TSP(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        double[][] tsp
    ) {
      super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters);

      this.n_cities = tsp.length;
      this.tsp = tsp;
    }

    @Override
    void init() {
        this.bestPositions = new ArrayList<int[]>(
            Collections.nCopies(n_birds, new int[n_cities])
        );
        this.currPositions = new ArrayList<int[]>(
            Collections.nCopies(n_birds, new int[n_cities])
        );

        System.out.format("%d\n", this.currPositions.size());
        for (int i=0; i<this.n_birds; i++) {
            for (int j=0; j<this.n_cities; j++) {
                this.currPositions.get(i)[j] = j;
                this.bestPositions.get(i)[j] = j;
            }
            fly(i);
            cost(i);
            this.bestPositionValues[i] = this.currPositionValues[i];
            this.lastMoves[i] = 2;
            this.isBigBird[i] = (i <= Math.ceil(this.smallBirdRatio*this.n_birds));
        }
        this.curr_iters = 0; // just now, because we call 'cost' above
    }

    @Override
    void cost(int i) { // after each iteration for each bird simultaneously?
        if ((this.curr_iters % 100) == 0) System.out.println("[DEBUG]: Iteration: " + this.curr_iters);

        double cost = 0;
        for (int j = 1; j < this.n_cities; j++) {
            cost += this.tsp[this.currPositions.get(i)[j-1]][this.currPositions.get(i)[j]];
        }
        this.currPositionValues[i] = cost;
        this.curr_iters++;
    }

    @Override
    void fly(int i) { // essentially just shuffles the order
        for (int j = this.n_cities - 1; j > 0; j--) {
            int idx = this.rand.nextInt(j + 1);

            int tmp = this.currPositions.get(i)[idx];
            this.currPositions.get(i)[idx] = this.currPositions.get(i)[j];
            this.currPositions.get(i)[j] = tmp;
        }
    }

    @Override
    void walk(int i) {
        int delta = 0;
        int k = -1;
        for (int u=0; u<100; u++) {
            int j = exclusiveRandInt(i);
            k = this.rand.nextInt(this.n_cities-1)+1;
            int delta_new = position_of(this.currPositions.get(i)[k], j) - position_of(this.currPositions.get(i)[k-1], j);
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
            int temp = this.currPositions.get(i)[u];
            this.currPositions.get(i)[u] = this.currPositions.get(i)[v];
            this.currPositions.get(i)[v] = temp;
        }
    }

    private int position_of(int value, int j) {
        for (int pos=0; pos<this.n_cities; pos++) {
            if (this.currPositions.get(j)[pos] == value) {
                return pos;
            }
        }
        System.err.println("Fehler: Stadt mit Index '" + value + "' nicht in Tour  '" + value + "' gefunden!");
        return 0; // Wird nie der Fall sein, da value, also der index der Stadt immer in der Tour j vorkommt
    }

}
