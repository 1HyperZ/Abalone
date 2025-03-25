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
    private final Map<Integer, List<Integer>> graph; // Graph of neighbors for each cell
    private final Map<Integer, Player> positions; // Stores player pieces on the board
    private final Player aiPlayer;
    private final Player humanPlayer;
    private final Map<Integer, int[]> indexToCoord; 
    private final Map<String, Integer> coordToIndex;
    private final List<int[]> directions ;


    /**
     * Constructs a new Board with the specified AI and human players.
     * Creates axial direction list for all possible directions, Initializes axial coordinate mappings, builds the neighbor graph, and places initial pieces.
     *
     * @param aiPlayer the AI player
     * @param humanPlayer the human player
     */
    public Board(Player aiPlayer, Player humanPlayer) {
        graph = new HashMap<>();
        positions = new HashMap<>();
        indexToCoord = new HashMap<>();
        coordToIndex = new HashMap<>();
        this.aiPlayer = aiPlayer;
        this.humanPlayer = humanPlayer;
        directions = List.of(new int[]{1, 0}, new int[]{-1, 0}, new int[]{0, 1}, new int[]{0, -1}, new int[]{1, -1}, new int[]{-1, 1}); //Create a list of directions
        initializeAxialHashMaps();  // Build axial coordinate mappings for the board
        initializeGraph();         // Build neighbor graph
        placeStartingPieces();

    }
    
    /**
     * Initializes the axial coordinate mappings for the hexagon board with radius of 4.
     * inserts indexToCoord and coordToIndex mappings for each cell on the board.
     */
    private void initializeAxialHashMaps() {
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
    private void initializeGraph() {        
        //build the neighbor graph using axial directions.

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
     * @return the index of the next cell index, or -1 if its off board
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
     * Checks if a given move is valid.
     *
     * @param move the move to validate
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        
        // Calcluates the next the direction of 'from' to 'to' and checks that it exits.
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];

        int[] direction = {dq, dr};
        if (!directions.stream().anyMatch(d -> java.util.Arrays.equals(d, direction))) {
            return false;
        }
        
        // Get the list of pieces in the direction starting at 'from'.
        List<Integer> group = getListOfPiecesInDirection(from, dq, dr);
        int leading = group.get(group.size() - 1);
        int next = getNextCellInDirection(leading, dq, dr);
        
        // Ok if next cell is on the board and empty.
        if (next != -1 && !positions.containsKey(next)) {
            return true;
        }
        
        // Not okay if next is off board.
        if (next == -1) {
            return false;
        }
        
        // Gets the opponent list of pieces in the direction starting from 'next'.
        List<Integer> opponentGroup = getListOfPiecesInDirection(next, dq, dr);
        
        // OK push if mover's group is larger than opponent group and
        // the destination cell is either off board or empty.
        if (group.size() > opponentGroup.size()) {
            int pushDestination = getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
            if (pushDestination == -1 || !positions.containsKey(pushDestination)) {
                return true;
            }
        }

        // false in any other case.
        return false;
    }
    
    /**
     * Applies a valid move to the board.
     *
     * @param move the move to apply
     */
    public void applyMove(Move move) {
        int from = move.getFrom();
        int to = move.getTo();
        
        // Calcluates the next the direction of 'from' to 'to' and checks that it exits.
        int[] fromCoord = indexToCoord.get(from);
        int[] toCoord = indexToCoord.get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];
        
        // Get the mover's list of pieces in the same direction.
        List<Integer> group = getListOfPiecesInDirection(from, dq, dr);
        int leadingPiece = group.get(group.size() - 1); //leading piece in the direction of the move.
        int next = getNextCellInDirection(leadingPiece, dq, dr);
        
        // Regular move: move one cell forward if the next cell is not off board and empty.
        if (next != -1 && !positions.containsKey(next)) {
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player mover = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, mover);
            }
        } else {
            // Push move.
            List<Integer> opponentGroup = getListOfPiecesInDirection(next, dq, dr);
            // Move opponent group first, from last to first.
            for (int i = opponentGroup.size() - 1; i >= 0; i--) {
                int oppPos = opponentGroup.get(i);
                int destination = getNextCellInDirection(oppPos, dq, dr);
                // If the destination is off board, remove the piece.
                if (destination == -1) {
                    positions.remove(oppPos);
                } else {
                    Player opp = positions.remove(oppPos);
                    positions.put(destination, opp);
                }
            }
            // Afterwards move your group forward.
            for (int i = group.size() - 1; i >= 0; i--) {
                int pos = group.get(i);
                Player mover = positions.remove(pos);
                int dest = getNextCellInDirection(pos, dq, dr);
                positions.put(dest, mover);
            }
        }
    }
    
    /**
     * Returns a list of all possible moves for the specified player.
     * 
     * @param player the player to get possible moves for
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
    
   
    public Map<Integer, Player> getPositionsMap() {
        return positions;
    }
    
    
    /**
     * Returns the list of pieces in the direction starting from start 
     * that are owned by the same player's.
     *
     * @param start the starting cell index
     * @param dq the direction in the  q-coordinate
     * @param dr the direction in the r-coordinate
     * @return a list of indexes of the pieces in the same direction or empty list if empty piece
     */
    List<Integer> getListOfPiecesInDirection(int start, int dq, int dr) {
        List<Integer> list = new ArrayList<>();
        int current = start;
        Player mover = positions.get(start);
        if (mover != null) {
            list.add(current);
            int next = getNextCellInDirection(current, dq, dr);
            while (next != -1 && positions.containsKey(next) && positions.get(next).getName().equals(mover.getName())) {
                list.add(next);
                current = next;
                next = getNextCellInDirection(current, dq, dr);
            }
        }
        return list;
    }

    /**
     * Returns the index of the next cell in the given direction (dq, dr) from the specified cell.
     * Returns -1 if the cell is off-board.
     *
     * @param index the starting cell index
    * @param dq the direction in the  q-coordinate
     * @param dr the direction in the r-coordinate
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
     * Returns the player at the specified position, or null if none.
     *
     * @param position the cell index
     * @return the player at that cell, or null if empty
     */
    public Player getPlayerAt(int position) {
        return positions.get(position);
    }
    
    /**
     * Returns the color of the piece at the provided position.
     *
     * @param position the board cell index
     * @return WHITE if the piece belongs to AI, BLACK if it belongs to Human, or LIGHTGRAY if empty
     */
    public Color getPieceColor(int position) {
        Player player = getPlayerAt(position);
        if (player == null) return Color.GRAY;
        return player.getName().equals(aiPlayer.getName()) ? Color.WHITE : Color.BLACK;
    }

    public Map<Integer, Player> getPositions() {
        return positions;
    }

    public Map<Integer, int[]> getIndexToCoord() {
        return indexToCoord;
    }

    public Map<String, Integer> getCoordToIndex() {
        return coordToIndex;
    }
    
}
