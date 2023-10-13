// AFB for TSP with swap local search.
public class AFB_TSP_Swap extends AFB_TSP {
    public AFB_TSP_Swap(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        double[][] tsp
    ) {
      super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp);
    }

    // Performs a random swap of two cities for local search.
    @Override
    void walk(int birdIndex) {
        Bird<int[]> bird = this.birds.get(birdIndex);
        int cityIndex1 = this.rand.nextInt(this.n_cities);
        int cityIndex2 = this.rand.nextInt(this.n_cities);

        int tempCity = bird.position[cityIndex1];
        bird.position[cityIndex1] = bird.position[cityIndex2];
        bird.position[cityIndex2] = tempCity;
    }
}
