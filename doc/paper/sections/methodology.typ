= Methodology

We structure our experiments incrementally.
Each improvement in @Improvements builds on the previous adjustments, even if it is not explicitly stated.
We verify our improvements on a set of selected TSP problems from TSPLIB, namely eil101, pa561, pr1002, u2156, and pr2392, ranging from 101 to 2392 cities.

For each experiment, we run the problems 10 times.
This accounts for the randomness of the initialization and reduces noise.
We then record the _Mean Error_, over the 50 trials, with respect to the optimal solutions, in order to compare the performance of different experiments.
We also record the _Mean Runtime_ in seconds.

// TODO: Das abs ist unnötig, measured ist immer größer als optimal. Und dann ist die Berechnung so einfach, dass es sich eigentlich gar nicht lohnt die Formel zu zeigen.
// $ "Error"= abs("measured" - "optimal") / "optimal" $

All experiments are executed on a MacBook Pro (M1 Pro) 2021.
