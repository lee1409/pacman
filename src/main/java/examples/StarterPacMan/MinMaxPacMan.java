package examples.StarterPacMan;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.internal.PacMan;

import java.util.*;


// Special case
// You need to walk at least 4 times to eat a pill
public class MinMaxPacMan extends PacmanController {
    @Override
    public Constants.MOVE getMove(Game game, long l) {
        // Minimax function

        int pacmanCurrentNodeIndex = game.getPacmanCurrentNodeIndex();

        ArrayList<Ghost> ghosts = new ArrayList<>();
        ghosts.add(new Ghost(game, Constants.GHOST.BLINKY));
        ghosts.add(new Ghost(game, Constants.GHOST.SUE));
        ghosts.add(new Ghost(game, Constants.GHOST.INKY));
        ghosts.add(new Ghost(game, Constants.GHOST.PINKY));
        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(pacmanCurrentNodeIndex);

        HashSet<Integer> pills = new HashSet<>();

        for (int index: game.getActivePillsIndices()) {
            pills.add(index);
        }

        BestMove pacmanMove = getPacmanBestMove(
                game,
                pacmanCurrentNodeIndex,
                120,
                0,
                Constants.MOVE.LEFT,
                hashSet,
                ghosts,
                pills
        );
        System.out.println(String.format("Best move: %s", pacmanMove.move));
        System.out.println(String.format("Best reward: %.5f", pacmanMove.reward));
        return pacmanMove.move;
    }

    // Game param is only use to access neighbour and possible move
    BestMove getPacmanBestMove(
            Game game,
            int pacmanCurrentNodeIndex,
            int depth,
            double reward,
            Constants.MOVE selectedMove,
            HashSet<Integer> visited,
            ArrayList<Ghost> ghosts,
            HashSet<Integer> pills
    ) {
        // Enough search, find out how much reward from going this
        // Or lose already
        if (depth == 0 || reward < 0) {
            return new BestMove(reward, selectedMove);
        }
        Constants.MOVE[] moves = game.getPossibleMoves(pacmanCurrentNodeIndex);

        Constants.MOVE bestMove = Constants.MOVE.NEUTRAL; // Not doing anything is the best
        double highestReward = Integer.MIN_VALUE;
        // Expand nodes
        for (Constants.MOVE move: moves) {
            // If visited no need to visit
            int newPacmanNodeIndex = game.getNeighbour(pacmanCurrentNodeIndex, move);

            if (visited.contains(newPacmanNodeIndex) || move == Constants.MOVE.NEUTRAL) {
                continue;
            } else {
                visited.add(newPacmanNodeIndex);
            }
            double localReward = 0;

            boolean containPill = false;
            if (pills.contains(newPacmanNodeIndex)) {
                localReward ++;
                containPill = true;
                pills.remove(newPacmanNodeIndex);
            }

            // Update ghost
            ArrayList<Ghost> copyGhosts = new ArrayList<>();
            double ghostHighestReward = Integer.MIN_VALUE;
            outer:for (Ghost ghost: ghosts) {
                if (ghost.index != -1) {
                    BestMove ghostBestMoveIndex = getGhostBestMoveIndex(game, ghost, newPacmanNodeIndex);
                    int index = game.getNeighbour(ghost.index, ghostBestMoveIndex.move);

                    copyGhosts.add(new Ghost(index, ghost.id));

                    if (ghostBestMoveIndex.reward > ghostHighestReward) {
                        ghostHighestReward = ghostBestMoveIndex.reward;
                    }
                    // If pacman confirm will lose
                    if (ghostHighestReward == Integer.MAX_VALUE) {
                        break outer;
                    }
                }
            }
            // Prvent ghost reward too strong
            localReward = localReward - ghostHighestReward / 4000;

            BestMove bestMove1 = getPacmanBestMove(
                    game,
                    newPacmanNodeIndex,
                    depth - 1,
                    reward + localReward,
                    move,
                    visited,
                    copyGhosts,
                    pills
            );
            if (bestMove1.reward > highestReward) {
                highestReward = bestMove1.reward;
                bestMove = move;
            }

            if (containPill) {
                pills.add(newPacmanNodeIndex);
            }
            visited.remove(newPacmanNodeIndex);
        }
        // Probably nearest not found and no enemy
        // Pick the first pills
        return new BestMove(highestReward, bestMove);
    }

    // Get ghost best move
    BestMove getGhostBestMoveIndex(Game game, Ghost ghost, int pacmanIndex) {
        Constants.MOVE[] moves = game.getPossibleMoves(ghost.index);
        int shortestDistance = Integer.MAX_VALUE;
        Constants.MOVE bestMove = Constants.MOVE.NEUTRAL;
        for (Constants.MOVE move: moves) {
            // Get the shortest distance to determine which will always the best move
            int ghostNewIndex = game.getNeighbour(ghost.index, move);
            int distance = game.getShortestPathDistance(ghostNewIndex, pacmanIndex);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                bestMove = move;
            }
        }
        double reward = -shortestDistance;
        // If ghost catched pacman, reward maximum
        if (game.isGhostEdible(ghost.id)) {
            if (shortestDistance < 3) {
                reward = -2000 + (1.0 / shortestDistance); ;
            } else {
                reward = 0;
            }
            return new BestMove(reward, Constants.MOVE.NEUTRAL);
        } else {
            if (shortestDistance < 5) {
                reward = Integer.MAX_VALUE;
            }
            return new BestMove(reward, bestMove);
        }
    }

    double ghostEvaluator() {
        return 0;
    }

    class BestMove {
        private double reward;
        private Constants.MOVE move;
        BestMove(double reward, Constants.MOVE move) {
            this.reward = reward;
            this.move = move;
        }

        public double getReward() {
            return reward;
        }

        public Constants.MOVE getMove() {
            return move;
        }
    }

    class Ghost {
        Constants.GHOST id;
        int index;
        Ghost(Game game, Constants.GHOST ghost) {
            this.index = game.getGhostCurrentNodeIndex(ghost);
            this.id = ghost;
        }
        Ghost(int index, Constants.GHOST id) {
            this.index = index;
            this.id = id;
        }
    }
}
