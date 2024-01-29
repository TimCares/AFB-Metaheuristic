= Methodology

We structure our experiments in an incremental manner.
Each improvement in @Improvements builds on the previous adjustments,
even if it is not explicitly noted.
We verify our improvements using a set of selected TSP problems from TSPLIB, namely
eil101, pa561, pr1002, u2156 and pr2392, ranging from 101 to 2392 cities.

For each experiment, we run the problems 10 times each to account for the randomness
at initialization, to reduce noise, and make it statistically significant. // TODO: Ich glaube statistische relevanz müssten wir noch weiter belegen.
In order to then compare the performance of different experiments
we record the _Mean Error_, over the 50 tests, with respect to the optimal solutions.
We also record the _Mean Runtime_ in seconds.

// TODO: Das abs ist unnötig, measured ist immer größer als optimal. Und dann ist die Berechnung so einfach, dass es sich eigentlich gar nicht lohnt die Formel zu zeigen.

$ "Error"= abs("measured" - "optimal") / "optimal" $

All experiments are executed on a MacBook Pro (M1 Pro) 2021.
