= Methodology

We structure our experiments in an incremental manner.
Each improvement in @Improvements builds on the previous adjustments,
even if it is not explicitly noted.
We verify our improvements using a set of selected TSP problems from TSPLIB, namely
eil101, pa561, pr1002, u2156 and pr2392, ranging from 101 to 2392 cities.

For each experiment, we run the problems 10 times each to account for the randomness
at initialization, to reduce noise, and make it statistically significant.
In order to then compare the performance of different experiments
we record the _Mean Percentage Error_, over the 50 tests, with respect to the optimal solutions.
We also record the _Mean Runtime_ in seconds.

$ "Percent Error"= abs("measured" - "optimal") / "optimal" dot 100 $

All experiments are executed on a MacBook Pro (M1 Pro) 2021.
