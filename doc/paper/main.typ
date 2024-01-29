#import "template.typ": *
#show: ieee.with(
  title: "Enhancing Path Efficiency: Innovations in the Artificial Feeding Birds Algorithm for Solving TSP Problems",
  abstract: [
    The process of scientific writing is often tangled up with the intricacies of typesetting, leading to frustration and wasted time for researchers. In this paper, we introduce Typst, a new typesetting system designed specifically for scientific writing. Typst untangles the typesetting process, allowing researchers to compose papers faster. In a series of experiments we demonstrate that Typst offers several advantages, including faster document creation, simplified syntax, and increased ease-of-use.
  ],
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
  index-terms: ("Scientific writing", "Typesetting", "Document creation", "Syntax"),
  bibliography-file: "refs.bib",
)

#include "sections/introduction.typ"
#include "sections/tsp.typ"
#include "sections/algorithm_foundation.typ"
#include "sections/methodology.typ"
#include "sections/improvements.typ"
#include "sections/sop.typ"
#include "sections/analysis.typ"
#include "sections/conclusion.typ"
#include "sections/appendix.typ"

