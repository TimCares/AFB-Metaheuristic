= Analysis <Analysis>

== Intuitions on our Improvements <Intuitions>

Swarm algorithm usually include action of agents which can either be classified as exploitation or exploration.
Exploitation means an agent uses its current result and tries to improve it, i.e. the agent continues to go into the direction he previously went in the search tree/space.
For AFB, this is can be achieved using the walk move, so a local search.

Exploration means an agent tries to find a better solution not necessarily dependent on its current solution.
Therefore, it can be seen as a global search. For AFB, this can be achieved using the fly move.

For a swarm algorithm to deliver a good approximation with respect to the global minimum
it needs a good balance between exploitation and exploration:
It needs to be able to improve a good solution,
and search for different solutions if the current one does not seem promising.

If exploitation is too dominant, then the algorithm might get stuck in a local minimum, while other areas of the search space aren't explored at all, and vice versa.

As might have become apparent in the sections prior, the main focus of this paper
was on exploitation (improving a good solution).
This is mainly done by the introduction of 3-opt and that big birds can only join (the most) successful birds.
Even though the latter cannot be seen directly as a local search,
it does lead to more (big) birds performing exploitation of good solutions they adapted from other birds.

We explicitly do not modify the fly move, i.e. selecting a random tour, as this provides us with a rich selection of other possible solutions, which at the same time are completely independent of the current solution (of an agent).

The prior is crucial for the success of our algorithm, because the extreme join
behavior modeled by the algorithm has a high risk of getting stuck in a
local minimum: Starting with our improvements on the swarm behavior,
a big bird will only be able to join the best bird. Because which bird is the best
is not updated within a phase, it could be that during one phase a lot of big birds join the same bird,
putting a lot (not all, as a join is a matter of probability) of agents in one place in the search space.
The fly move enables birds to escape this single solution.

Furthermore, there is only a limited number of big birds, meaning small birds are able
to explore the search space elsewhere, while the big birds are focusing on the best current solution.

Therefore, we get an algorithm that has an empirically tested balance between exploitation and exploration.
It exploits good solutions in a harsh manner while also being able to switch to completely new solutions if they prove to be better.
This process will be repeated until a solution is reached that will either be close to the optimum, or a local minimum.
Either way, the algorithm converges.

== Exploitators and Explorers <ExploitatorsAndExplorators>

The base algorithm consists of two types of agents, small and big birds.
With our improvements it may have been noticeable that we separated the roles
of both agent types more and more from each other:
While small birds can perform the usual 2-opt local search when they are walking,
big birds can perform a more powerful 3-opt walk;
big birds can join other birds.
We do this in order to make the components contained in each swarm algorithm,
exploration and exploitation, a more explicit part of the algorithm:
We delegate small birds to the role of explorers, and big birds to the role of exploitators.

Small birds are able to access vastly different areas of the search space for possible better
solutions than their current one. Using the join-move, big birds are able to profit
from those that have found the best current solution by joining them and
improving that solution using 3-opt (walk).

The circumstance that small birds can also perform exploitation, using their own version of the walk move (2-opt),
is owed to the fact that they otherwise would only be able to perform
the fly move, i.e. jumping between random solutions.
This wouldn’t be a good foundation for the join behavior of big birds (see @small_birds_only_fly),
which is essential for the performance of our algorithm.
Also, since big birds can also join other big birds,
and the solutions for small birds would be rather poor, the probability that
big birds will exclusively join other big birds would be very high,
making small birds essentially useless.

Exactly this can be verified by simply comparing how 
the algorithm performs when (1) small birds can only fly,
(2) all small birds are removed from the algorithm, and only big birds are kept.

Surprisingly, the results show us that configuration (2) performs even better
than variant (1), indicating that in (1) the big birds only
join other big birds, and that small birds, whose only purpose is to perform 
the fly move (so not even returning to their best solution), provide no value to the algorithm.
This is why we decided that small birds are also able to perform the walk move.

#figure(
  table(
    columns: 4,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Configuration], [*Regular*], [Only fly], [No small birds],
    [Error (in %)], [*8*], [15], [10],
  ),
  caption: [
    If small birds are only able to fly, the algorithm performs worse than before.
    Notice however that it still achieves a reasonable performance.
    For our experiments we continuously used 200 birds, 150 of them being small birds.
    So by removing all small birds for experiment (2), we are left with 50 (big) birds.
    ],
) <small_birds_only_fly>

It is important to note that to make this experiment fair, we also reduced the number of iterations by a factor
of three quarters (same ratio with which birds have been reduced), so that
the search depth, meaning the average number of steps each bird can perform in the search space,
is the same as if we use the regular 200 birds (compare with @CANOF).
