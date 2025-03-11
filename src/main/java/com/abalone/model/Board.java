package com.abalone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.Player;

import javafx.scene.paint.Color;

/**
 * Represents the game board. It handles board state, validation of moves,
 * move applications, and calculations of neighbors.
 */
public class Board {
    private Map<Integer, List<Integer>> graph; // Graph of neighbors for each cell
    private Map<Integer, Player> positions; // Stores player pieces on the board
    Player aiPlayer;
    Player humanPlayer;
    private Map<Integer, int[]> indexToCoord; 
    private Map<String, Integer> coordToIndex;

    /**
     * Constructs a new Board with the specified AI and human players.
     * Initializes axial coordinate mappings, builds the neighbor graph, and places initial pieces.
     *
     * @param aiPlayer the AI player
     * @param humanPlayer the human player
     */
    public Board(Player aiPlayer, Player humanPlayer) {
        graph = new HashMap<>();
        positions = new HashMap<>();
        this.aiPlayer = aiPlayer;
        this.humanPlayer = humanPlayer;
        initializeAxialHashMaps();  // Build axial coordinate mappings for the board
        initializeBoard();         // Build neighbor graph and place pieces
    }
    
    /**
     * Initializes the axial coordinate mappings for the hexagon board with radius of 4.
     * inserts indexToCoord and coordToIndex mappings for each cell on the board.
     */
    private void initializeAxialHashMaps() {
        indexToCoord = new HashMap<>();
        coordToIndex = new HashMap<>();
        
        int[] rowCellCounts = {5, 6, 7, 8, 9, 8, 7, 6, 5};
        int rStart = -4;
        int index = 0;
        for (int i = 0; i < rowCellCounts.length; i++) {
            int r = rStart + i;
            int qMin = Math.max(-4, -r - 4);
            int qMax = Math.min(4, -r + 4);
            for (int q = qMin; q <= qMax; q++) {
                indexToCoord.put(index, new int[]{q, r});
                coordToIndex.put(q + "," + r, index);
                index++;
            }
        }
    }

    /**
     * Initializes the board by constructing the neighbor graph using axial directions,
     * and placing the initial pieces on the board.
     */
    private void initializeBoard() {        
        //build the neighbor graph using axial directions.
        // axial directions are: (1, 0), (-1, 0), (0, 1), (0, -1), (1, -1), (-1, 1)
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, -1}, {-1, 1}
        };
        
        //temporary graph using sets to not add duplicates.
        Map<Integer, Set<Integer>> tempGraph = new HashMap<>();
        for (int i = 0; i < 61; i++) {
            tempGraph.put(i, new HashSet<>());
        }
        
        for (int i = 0; i < 61; i++) {
            int[] coord = indexToCoord.get(i);
            int q = coord[0], r = coord[1];
            for (int[] d : directions) {
                int neighborQ = q + d[0];
                int neighborR = r + d[1];
                String neighborKey = neighborQ + "," + neighborR;
                if (coordToIndex.containsKey(neighborKey)) {
                    int neighborIndex = coordToIndex.get(neighborKey);
                    tempGraph.get(i).add(neighborIndex);
                }
            }
        }
    
        // Convert the sets to lists and insert to the main graph.
        for (int i = 0; i < 61; i++) {
            graph.put(i, new ArrayList<>(tempGraph.get(i)));
        }
        
        placeStartingPieces();
    }
    
    /**
     * Places the starting pieces for both players in the positions HashMap.
     */
    private void placeStartingPieces() {
        int[] whitePositions = {
            0, 1, 2, 3, 4,  
            5, 6, 7, 8, 9, 10, 
            13, 14, 15 
        };
    
        int[] blackPositions = {
            60, 59, 58, 57, 56,  
            55, 54, 53, 52, 51, 50, 
            45, 46, 47 
        };
    
        for (int pos : whitePositions) {
            positions.put(pos, aiPlayer);
        }
        for (int pos : blackPositions) {
            positions.put(pos, humanPlayer);
        }
    }
    
    /**
     * Calcluates the next cell in the direction of 'from' to 'to'.
     * Returns the index of the next cell, or -1 if off board.
     *
     * @param from the starting cell index
     * @param to the destination cell index
     * @return the index of the next cell, or -1 if its off board
     */
    public int getNextCell(int from, int to) {
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];
        int nextQ = toCoord[0] + dq;
        int nextR = toCoord[1] + dr;
        String key = nextQ + "," + nextR;
        Integer nextIndex = coordToIndex.get(key);
        return (nextIndex == null) ? -1 : nextIndex;
    }
//----------------------------------------------------------------------------------------------------------------------------
    /**
     * Checks if a given move is valid. Supports simple moves and push moves.
     *
     * @param move the move to validate
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        
        // Compute direction vector (dq, dr) from 'from' to 'to'.
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];
        boolean isUnitDirection = (dq == 1 && dr == 0) ||
                          (dq == -1 && dr == 0) ||
                          (dq == 0 && dr == 1) ||
                          (dq == 0 && dr == -1) ||
                          (dq == 1 && dr == -1) ||
                          (dq == -1 && dr == 1);
        if (!isUnitDirection) {
            return false;
        }
        
        // Get the contiguous group of the mover starting at 'from'.
        List<Integer> group = getContiguousGroup(from, dq, dr);
        int leading = group.get(group.size() - 1);
        int next = getNextCellInDirection(leading, dq, dr);
        
        // Simple move: if next cell is on-board and empty.
        if (next != -1 && !positions.containsKey(next)) {
            return true;
        }
        
        // Push move: next must be occupied by opponent.
        Player mover = positions.get(from);
        if (next == -1 || mover.getName().equals(positions.get(next).getName())) {
            return false;
        }
        
        // Count contiguous opponent group starting from 'next'.
        List<Integer> opponentGroup = new ArrayList<>();
        int current = next;
        while (current != -1 && positions.containsKey(current) &&
               !positions.get(current).getName().equals(mover.getName())) {
            opponentGroup.add(current);
            int nextOpponent = getNextCellInDirection(current, dq, dr);
            if (nextOpponent == -1) break;
            current = nextOpponent;
        }
        
        // Valid push if mover's group is larger than opponent group and
        // the cell immediately after opponent group is off-board or empty.
        if (group.size() > opponentGroup.size()) {
            int pushDest = getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
            if (pushDest == -1 || !positions.containsKey(pushDest)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Applies a valid move to the board.
     * Supports both simple moves and push moves.
     *
     * @param move the move to apply
     */
    public void applyMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        
        // Compute direction vector.
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];
        
        // Get the mover's contiguous group.
        List<Integer> group = getContiguousGroup(from, dq, dr);
        int leading = group.get(group.size() - 1);
        int next = getNextCellInDirection(leading, dq, dr);
        
        // Simple move: move group one cell forward.
        if (next != -1 && !positions.containsKey(next)) {
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player mover = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, mover);
            }
        } else {
            // Push move.
            List<Integer> opponentGroup = new ArrayList<>();
            int current = next;
            Player mover = positions.get(from);
            while (current != -1 && positions.containsKey(current) &&
                   !positions.get(current).getName().equals(mover.getName())) {
                opponentGroup.add(current);
                int nextOpponent = getNextCellInDirection(current, dq, dr);
                if (nextOpponent == -1) break;
                current = nextOpponent;
            }
            // Move opponent group first, from farthest to nearest.
            for (int i = opponentGroup.size() - 1; i >= 0; i--) {
                int oppPos = opponentGroup.get(i);
                int dest = getNextCellInDirection(oppPos, dq, dr);
                if (dest == -1) {
                    positions.remove(oppPos);
                } else {
                    Player opp = positions.remove(oppPos);
                    positions.put(dest, opp);
                }
            }
            // Then move your group forward.
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player m = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, m);
            }
        }
    }
    
    /**
     * Returns a list of all possible moves for the specified player.
     * Considers both simple moves and push moves.
     *
     * @param player the player for whom to get moves
     * @return a list of valid moves
     */
    public List<Move> getPossibleMoves(Player player) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Integer, Player> entry : positions.entrySet()) {
            if (entry.getValue().equals(player)) {
                int from = entry.getKey();
                for (int to : graph.get(from)) {
                    Move move = new Move(from, to);
                    if (isValidMove(move)) {
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }
    
    /**
     * Returns a list of all players currently on the board.
     *
     * @return a list of players on the board
     */
    public List<Player> getPlayersOnBoard() {
        return new ArrayList<>(positions.values());
    }
    
    /**
     * Prints the board state for debugging.
     */
    public void printBoard() {
        System.out.println("Board State:");
        for (int i = 0; i < 61; i++) {
            System.out.print(positions.containsKey(i) ? positions.get(i).getName().charAt(0) : "-");
            if (i % 7 == 6) System.out.println();
        }
    }

    /**
     * Checks whether a given position is valid on the board.
     *
     * @param position the position index
     * @return true if the position exists on the board, false otherwise
     */
    public boolean isValidPosition(int position) {
        return graph.containsKey(position);
    }
    
    /**
     * Returns the player at the specified position, or null if none.
     *
     * @param position the board cell index
     * @return the player at that cell, or null if empty
     */
    public Player getPlayerAt(int position) {
        return positions.get(position);
    }
    
    /**
     * Returns a list of valid moves (empty neighboring positions) for the specified position.
     *
     * @param position the board cell index
     * @return a list of valid move destinations
     */
    public List<Integer> getValidMoves(int position) {
        List<Integer> validMoves = new ArrayList<>();
        if (!graph.containsKey(position)) {
            return validMoves;
        }
        for (int neighbor : graph.get(position)) {
            if (!positions.containsKey(neighbor)) {
                validMoves.add(neighbor);
            }
        }
        return validMoves;
    }
    
    /**
     * Returns a list of contiguous indices starting from 'start' in the direction (dq, dr)
     * that are occupied by the same player's pieces.
     *
     * @param start the starting cell index
     * @param dq the change in q-coordinate per step
     * @param dr the change in r-coordinate per step
     * @return a list of contiguous cell indices
     */
    private List<Integer> getContiguousGroup(int start, int dq, int dr) {
        List<Integer> group = new ArrayList<>();
        int current = start;
        Player mover = positions.get(start);
        group.add(current);
        while (true) {
            int next = getNextCellInDirection(current, dq, dr);
            if (next == -1) break;
            if (positions.containsKey(next) && positions.get(next).getName().equals(mover.getName())) {
                group.add(next);
                current = next;
            } else {
                break;
            }
        }
        return group;
    }

    /**
     * Returns the index of the next cell in the given direction (dq, dr) from the specified cell.
     * Returns -1 if the cell is off-board.
     *
     * @param index the starting cell index
     * @param dq the change in q-coordinate per step
     * @param dr the change in r-coordinate per step
     * @return the index of the next cell, or -1 if off-board
     */
    public int getNextCellInDirection(int index, int dq, int dr) {
        int[] coord = indexToCoord.get(index);
        int nextQ = coord[0] + dq;
        int nextR = coord[1] + dr;
        String key = nextQ + "," + nextR;
        Integer nextIndex = coordToIndex.get(key);
        return (nextIndex == null) ? -1 : nextIndex;
    }

    /**
     * Returns the color of the piece at the provided position.
     *
     * @param position the board cell index
     * @return WHITE if the piece belongs to AI, BLACK if it belongs to Human, or LIGHTGRAY if empty
     */
    public Color getPieceColor(int position) {
        Player player = getPlayerAt(position);
        if (player == null) return Color.LIGHTGRAY;
        return player.getName().equals(aiPlayer.getName()) ? Color.WHITE : Color.BLACK;
    }
}
