# Tree Search Ms. Pac-Man
Original repo by https://github.com/solar-1992/PacManEngine.<br/>
A simple tree search controller for Ms. Pac-Man Vs Ghost Team Competition
implemented based on this paper https://www.researchgate.net/publication/221157530_A_simple_tree_search_method_for_playing_Ms_Pac-Man

At each time-step, Ms pacman will perform a spatial tree-search (not state search) which returns a set of paths.
It then decides using a hard-coded rule to select the best path (e.g, the path with most rewards and fewer enemies)

Run Main.java<br/>
![Alt text](screenshot.png?raw=true "tree search Ms. pacman")

