package com.abalone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board {
    private Map<Integer, List<Integer>> graph;
    private Map<Integer, Player> positions; // Stores player pieces on the board
    Player aiPlayer;
    Player humanPlayer;
    private Map<Integer, int[]> indexToCoord; 
    private Map<String, Integer> coordToIndex;


    public Board(Player aiPlayer, Player humanPlayer) {
        graph = new HashMap<>();
        positions = new HashMap<>();
        this.aiPlayer = aiPlayer;
        this.humanPlayer = humanPlayer;
        initializeAxialMapping();  // Build axial coordinate mappings
        initializeBoard();         // Build neighbor graph and place pieces
    }
    

    /**
     * Initializes the hexagonal board by defining valid positions
     * and their connections.
     */
    private void initializeBoard() {
        // We'll use axial coordinates for a hexagon of radius 4.
        // In axial coordinates (q, r), the board cells satisfy:
        //    |q| <= 4, |r| <= 4, and |q + r| <= 4.
        // We assign indices in row-order from top to bottom.
        // The rows (from r = -4 to r = 4) have cell counts:
        //    r = -4: 5 cells, r = -3: 6, r = -2: 7, r = -1: 8,
        //    r =  0: 9, r =  1: 8, r =  2: 7, r =  3: 6, r =  4: 5.
        
        // Create maps to translate between axial coordinates and our index.
        Map<String, Integer> coordToIndex = new HashMap<>();
        Map<Integer, int[]> indexToCoord = new HashMap<>();
        
        // For convenience, we define the number of cells per row (from r = -4 to r = 4)
        int[] rowCellCounts = {5, 6, 7, 8, 9, 8, 7, 6, 5};
        // The corresponding r values (top row is r = -4, bottom is r = 4)
        int rStart = -4;
        
        int index = 0;
        // We'll also rebuild a 2D array "boardLayout" for debugging (optional)
        List<int[]> boardRows = new ArrayList<>();
        for (int i = 0; i < rowCellCounts.length; i++) {
            int cellCount = rowCellCounts[i];
            int r = rStart + i;
            // For a hexagon of radius 4, the q values for a given r are:
            // q_min = max(-4, -r-4) and q_max = min(4, -r+4).
            int qMin = Math.max(-4, -r - 4);
            int qMax = Math.min(4, -r + 4);
            // The number of cells is qMax - qMin + 1, which should equal cellCount.
            int[] rowIndices = new int[cellCount];
            for (int q = qMin; q <= qMax; q++) {
                // Create a key for (q, r)
                String key = q + "," + r;
                coordToIndex.put(key, index);
                indexToCoord.put(index, new int[]{q, r});
                rowIndices[q - qMin] = index;
                index++;
            }
            boardRows.add(rowIndices);
        }

        // Axial coordinate mapping for a hexagon of radius 4 (61 cells)
        indexToCoord = new HashMap<>();
        coordToIndex = new HashMap<>();
        // The rows (r from -4 to 4) have cell counts: {5,6,7,8,9,8,7,6,5} respectively.        
        int idx = 0;
        for (int i = 0; i < rowCellCounts.length; i++) {
            int r = rStart + i;
            // For a hexagon of radius 4, the q values range:
            int qMin = Math.max(-4, -r - 4);
            int qMax = Math.min(4, -r + 4);
            for (int q = qMin; q <= qMax; q++) {
                indexToCoord.put(idx, new int[]{q, r});
                coordToIndex.put(q + "," + r, idx);
                idx++;
            }
        }

        // At this point, index should be 61.
        
        // Now build the neighbor graph using axial directions.
        // Standard axial directions: (1, 0), (-1, 0), (0, 1), (0, -1), (1, -1), (-1, 1)
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, -1}, {-1, 1}
        };
        
        // We'll use a temporary graph using sets to avoid duplicates.
        Map<Integer, Set<Integer>> tempGraph = new HashMap<>();
        for (int i = 0; i < 61; i++) {
            tempGraph.put(i, new HashSet<>());
        }
        
        for (int i = 0; i < 61; i++) {
            int[] coord = indexToCoord.get(i);
            int q = coord[0], r = coord[1];
            for (int[] d : directions) {
                int nq = q + d[0];
                int nr = r + d[1];
                String neighborKey = nq + "," + nr;
                if (coordToIndex.containsKey(neighborKey)) {
                    int neighborIndex = coordToIndex.get(neighborKey);
                    tempGraph.get(i).add(neighborIndex);
                }
            }
        }
        
        
        // Convert the sets to lists and store in the graph field.
        for (int i = 0; i < 61; i++) {
            graph.put(i, new ArrayList<>(tempGraph.get(i)));
        }
        
        // --- Debugging: Print the neighbor list ---
        System.out.println("=== DEBUG: NEIGHBOR LIST ===");
        for (int i = 0; i < 61; i++) {
            System.out.println("Position " + i + " neighbors: " + graph.get(i));
        }
        System.out.println("=== END DEBUG ===");
        
        // Finally, place the initial pieces.
        placeStartingPieces();
    }
    
    

    
    
    
    
    /**
     * Given a move from cell 'from' to cell 'to', compute the next cell in that direction.
     * Returns the index of the next cell, or -1 if off-board.
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


    
    /**
 * Initializes the axial coordinate mappings for a hexagon of radius 4 (61 cells).
 * The rows (r from -4 to 4) have cell counts: {5,6,7,8,9,8,7,6,5}.
 */
private void initializeAxialMapping() {
    indexToCoord = new HashMap<>();
    coordToIndex = new HashMap<>();
    
    int[] rowCellCounts = {5, 6, 7, 8, 9, 8, 7, 6, 5};
    int rStart = -4;
    int idx = 0;
    for (int i = 0; i < rowCellCounts.length; i++) {
        int r = rStart + i;
        int qMin = Math.max(-4, -r - 4);
        int qMax = Math.min(4, -r + 4);
        for (int q = qMin; q <= qMax; q++) {
            indexToCoord.put(idx, new int[]{q, r});
            coordToIndex.put(q + "," + r, idx);
            idx++;
        }
    }
}


    /**
     * Places the initial pieces on the board.
     */
    private void placeStartingPieces() {
        int[] whitePositions = {
            0, 1, 2, 3, 4,  
            5, 6, 7, 8, 9, 10, 
            13, 14, 15 // Perfectly centered extra 3 white marbles in 3rd row
        };
    
        int[] blackPositions = {
            60, 59, 58, 57, 56,  
            55, 54, 53, 52, 51, 50, 
            45, 46, 47 // Perfectly centered extra 3 black marbles in 3rd row
        };
    
        Player whitePlayer = humanPlayer; // Use the existing human player
        Player blackPlayer = aiPlayer;    // Use the existing AI player

    
        // Assign white marbles
        for (int pos : whitePositions) {
            positions.put(pos, whitePlayer);
        }
    
        // Assign black marbles
        for (int pos : blackPositions) {
            positions.put(pos, blackPlayer);
        }
    }
    
    

    /**
     * Checks if a move is valid.
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

        // Get the contiguous group of your pieces starting at 'from'.
        List<Integer> group = getContiguousGroup(from, dq, dr);
        // The "leading" cell is the last cell in the group.
        int leading = group.get(group.size() - 1);
        // Check the cell immediately in front of the group.
        int next = getNextCellInDirection(leading, dq, dr);
        
        // Simple move: if next is on-board and empty.
        if (next != -1 && !positions.containsKey(next)) {
            return true;
        }
        
        // Otherwise, if next is occupied, attempt push.
        Player mover = positions.get(from);
        // If next is off-board (-1) or occupied by your own piece, push is not valid.
        if (next == -1 || mover.getName().equals(positions.get(next).getName())) {
            return false;
        }
        
        // Count opponent contiguous group starting from 'next'.
        List<Integer> opponentGroup = new ArrayList<>();
        int current = next;
        while (current != -1 && positions.containsKey(current) &&
               !positions.get(current).getName().equals(mover.getName())) {
            opponentGroup.add(current);
            int nextOpponent = getNextCellInDirection(current, dq, dr);
            if (nextOpponent == -1) break;
            current = nextOpponent;
        }
        
        // Valid push if your group size > opponent group size,
        // and the cell immediately after the opponent group is off-board or empty.
        if (group.size() > opponentGroup.size()) {
            int pushDest = getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
            if (pushDest == -1 || !positions.containsKey(pushDest)) {
                return true;
            }
        }
        return false;
    }
    
    
    
    

    /**
     * Applies a move if it is valid.
     */
    public void applyMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        
        // Compute direction vector.
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];
        
        // Get your contiguous group.
        List<Integer> group = getContiguousGroup(from, dq, dr);
        int leading = group.get(group.size() - 1);
        int next = getNextCellInDirection(leading, dq, dr);
        
        // Simple move: move your group one cell forward.
        if (next != -1 && !positions.containsKey(next)) {
            // Move from the far end back to avoid overwriting.
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player mover = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, mover);
            }
        } else {
            // Push move.
            // Count opponent contiguous group.
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
                    // Opponent ball is pushed off-board.
                    positions.remove(oppPos);
                } else {
                    Player opp = positions.remove(oppPos);
                    positions.put(dest, opp);
                }
            }
            // Then, move your group forward.
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player m = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, m);
            }
        }
    }
    
    
    

    /**
     * Gets a list of possible moves for a player.
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
    

    public List<Player> getPlayersOnBoard() {
        return new ArrayList<>(positions.values()); // Returns all players with pieces on the board
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

    public boolean isValidPosition(int position) {
        return graph.containsKey(position); // Ensures position exists in the board graph
    }
    
    public Player getPlayerAt(int position) {
        return positions.get(position); // Returns the player at this position (or null)
    }
    
    public List<Integer> getValidMoves(int position) {
        List<Integer> validMoves = new ArrayList<>();
    
        // Ensure the position exists on the board
        if (!graph.containsKey(position)) {
            return validMoves;
        }
    
        // Get all possible moves (neighboring positions)
        for (int neighbor : graph.get(position)) {
            if (!positions.containsKey(neighbor)) { // Check if the spot is empty
                validMoves.add(neighbor);
            }
        }
    
        return validMoves;
    }
    
    /**
     * Returns a list of contiguous indices (in-line) starting from 'start' that are occupied by the same player.
     */
    private List<Integer> getContiguousGroup(int start, int dq, int dr) {
        List<Integer> group = new ArrayList<>();
        int current = start;
        Player mover = positions.get(start);
        // Add the starting cell.
        group.add(current);
        while (true) {
            int next = getNextCellInDirection(current, dq, dr);
            if (next == -1) break; // Off-board
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
     * Returns the index of the next cell in the given direction (dq, dr) from the cell at index.
     * Returns -1 if off-board.
     */
    public int getNextCellInDirection(int index, int dq, int dr) {
        int[] coord = indexToCoord.get(index);
        int nextQ = coord[0] + dq;
        int nextR = coord[1] + dr;
        String key = nextQ + "," + nextR;
        Integer nextIndex = coordToIndex.get(key);
        return (nextIndex == null) ? -1 : nextIndex;
    }


}