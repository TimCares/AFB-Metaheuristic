= Basic AFB Algorithm

Artificial Feeding Birds (AFB) is a metaheuristic optimization algorithm.
Unlike most nature-inspired algorithms, AFB is not inspired by rare behaviors exhibited only by a particular species, but by the feeding behavior common to all birds.
The hope is that common behaviors that have been evolutionarily proven in a variety of natural environments will be equally efficient in solving complex optimization problems @afb.

The AFB algorithm models a flock of birds, each bird representing a solution to the problem.
In each iteration, every bird performs one of four moves:
#[
  #set par(first-line-indent: 0pt)
  #set terms(tight: false)
  / Walk: The bird performs a local search in the neighborhood of its current solution.
  / Random Fly: The bird flies to a random location in the search space.
  / Memory: The bird returns to the best solution it has found so far.
  / Join Other: The bird joins another bird and adopts its solution.
]
Which move a bird performs is determined randomly @afb.

Additionally, there is a difference between big and small birds.
The difference being that small birds do not perform the join move because they are afraid of other birds.
This prevents the flock from converging too quickly to a single local optimum @afb.

