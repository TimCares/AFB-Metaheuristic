#import "template.typ": *
#show: ieee.with(
  title: "Enhancing Path Efficiency: Innovations in the Artificial Feeding Birds Algorithm for Solving TSP and SOP Problems",
  abstract: [
    The Traveling Salesman Problem (TSP) and the Sequential Ordering Problem (SOP) are well-known NP-hard problems in combinatorial optimization.
    In recent years, nature-inspired metaheuristics have become a popular method to tackle large TSP configurations, one of which is the Artificial Feeding Birds (AFB) algorithm.
    In this paper, we explore possible improvements of the AFB algorithm on TSP and SOP, and benchmark them on well-known problems from TSPLIB.
    Since our changes rely on newly introduced hyperparameters, we propose to use the AFB algorithm itself to optimize the hyperparameters when solving the TSP.
    Finally, we discuss possible explanations for the observed results and improvements.
  ],
  paper-size: "a4",
  authors: (
    (
      name: "Tim Cares",
      department: [Fakultät IV],
      organization: [Hochschule Hannover],
      location: [Hanover, Germany],
      email: "tim.cares@stud.hs-hannover.de"
    ),
    (
      name: "Pit Simon Hüne",
      department: [Fakultät IV],
      organization: [Hochschule Hannover],
      location: [Hanover, Germany],
      email: "pit-simon.huene@stud.hs-hannover.de"
    ),
  ),
  bibliography-file: "refs.bib",
  appendix: include "sections/appendix.typ"
)
#include "sections/introduction.typ"
#include "sections/tsp.typ"
#include "sections/algorithm_foundation.typ"
#include "sections/methodology.typ"
#include "sections/improvements.typ"
#include "sections/sop.typ"
#include "sections/analysis.typ"
#include "sections/conclusion.typ"
