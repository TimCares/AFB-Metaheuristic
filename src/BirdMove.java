enum BirdMove {
  Walk,
  FlyRandom,
  FlyBest,
  FlyToOtherBird;

  public boolean isFlying() {
      return (this != Walk);
  }
}

