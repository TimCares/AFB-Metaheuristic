public class AFBResult<T> {
  public T bestPosition;
  public double bestCost;

  public AFBResult(T bestPosition, double bestCost) {
      this.bestPosition = bestPosition;
      this.bestCost = bestCost;
  }
}
