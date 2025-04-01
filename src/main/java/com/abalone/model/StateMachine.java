package com.abalone.model;

import java.util.List;
import java.util.Random;

import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.AIPlayer;
import com.abalone.model.utils.Players.Player;

/**
 * Implements a state machine for AI decision-making.
 * Evaluates all valid moves using various heuristics and selects the best one.
 */
public class StateMachine {

    /**
     * Determines the best move for the AI by evaluating all valid moves.
     *
     * @param board the current board state
     * @param aiPlayer the AI player
     * @return the move with the highest evaluation score, or null if no moves exist
     */
    public Move determineAIMove(Board board, AIPlayer aiPlayer) {
        List<Move> moves = board.getPossibleMoves(aiPlayer);
        if (moves.isEmpty()) return null;
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move move : moves) {
            int score = evaluateMove(move, board, aiPlayer);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        System.out.println("Best move score: " + bestScore);
        return bestMove;
    }
    // סיבוכיות n*m

    /**
     * Evaluates a given move by selecting a scoring function based on whether
     * the move is a push or a simple move.
     *
     * @param move the move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player making the move
     * @return an integer score representing the desirability of the move
     */
    private int evaluateMove(Move move, Board board, AIPlayer aiPlayer) {
        int score = 0;
        if (!board.isValidMove(move)) {
            return 0;
        }

        score += evaluatePushMove(move, board, aiPlayer);
        
        score += evaluateCenteringMove(move, board, aiPlayer);

        score += evaluateDefensiveMove(move, board, aiPlayer);

        score += evaluateBoardControl(move, board, aiPlayer);

        score += evaluateEdgeVulnerability(move, board, aiPlayer);

        // Add a small random factor to break ties.
        score += new Random().nextInt(10);
        return score;
    }

    /**
     * Evaluates a push move.
     * Rewards moves where the mover's contiguous group is larger than the opponent's,
     * and gives extra bonus if an opponent piece is pushed off-board.
     *
     * @param move the move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player making the move
     * @return a score for the push move
     */
    private int evaluatePushMove(Move move, Board board, AIPlayer aiPlayer) {
        int score = 0;
        int from = move.getFrom();
        int[] fromCoord = board.getIndexToCoord().get(from);
        int[] toCoord = board.getIndexToCoord().get(move.getTo());
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];

        List<Integer> moverGroup = board.getListOfPiecesInDirection(from, dq, dr);
        int moverSize = moverGroup.size();
        int moverLeading = moverGroup.get(moverGroup.size() - 1);

        int opponentLeading = board.getNextCellInDirection(moverLeading, dq, dr);
        List<Integer> opponentGroup = board.getListOfPiecesInDirection(opponentLeading, dq, dr);
        int opponentSize = opponentGroup.size();

        if (moverSize > opponentSize && opponentSize > 0) {
            score += 10 * (moverSize - opponentSize);
            int pushDest = board.getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
    
            if (pushDest == -1) {
                score += 100;
            }
            else {
                int[] origCoord = board.getIndexToCoord().get(opponentGroup.get(opponentGroup.size() - 1));
                int[] destCoord = board.getIndexToCoord().get(pushDest);
                int origEdgeDistance = getEdgeDistance(origCoord);
                int destEdgeDistance = getEdgeDistance(destCoord);
                if (destEdgeDistance < origEdgeDistance) {
                    score += 30 * (origEdgeDistance - destEdgeDistance);
                }
            }
        }
        return score;
    }

    /**
     * Evaluates a simple move by rewarding moves that bring pieces closer to the center (0,0).
     *
     * @param move the move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player making the move
     * @return a score for the simple move
     */
    private int evaluateCenteringMove(Move move, Board board, AIPlayer aiPlayer) {
        int score = 0;
        int from = move.getFrom();
        int to = move.getTo();
        int[] fromCoord = board.getIndexToCoord().get(from);
        int[] toCoord = board.getIndexToCoord().get(to);
        int dq = toCoord[0] - fromCoord[0];
        int dr = toCoord[1] - fromCoord[1];

        List<Integer> group = board.getListOfPiecesInDirection(from, dq, dr);
        int leadingPieceFromIndex = group.get(group.size() - 1);
        int leadingPieceToIndex = board.getNextCellInDirection(leadingPieceFromIndex, dq, dr);

        int[] leadingPieceFromCoord = board.getIndexToCoord().get(leadingPieceFromIndex);
        int[] leadingPieceToCoord = board.getIndexToCoord().get(leadingPieceToIndex);

        int centerDistanceFrom = hexDistance(leadingPieceFromCoord, new int[]{0, 0});
        int centerDistanceTo = hexDistance(leadingPieceToCoord, new int[]{0, 0});
        if (centerDistanceTo < centerDistanceFrom) {
            score += (centerDistanceFrom - centerDistanceTo) * 20;
        }
        return score;
    }

    /**
     * Strategy: Defensive Move.
     * Evaluates if applying the move blocks the opponent from achieving a winning move next turn.
     *
     * @param move the move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player
     * @return a bonus score if the move blocks opponent winning threats; 0 otherwise.
     */
    private int evaluateDefensiveMove(Move move, Board board, AIPlayer aiPlayer) {
        int bonus = 0;
        Board simulatedBoard = board.clone();
        simulatedBoard.applyMove(move);
        int oppWinsBefore = board.countOpponentWinningMoves(board, aiPlayer);
        int oppWinsAfter = board.countOpponentWinningMoves(simulatedBoard, aiPlayer);
        if (oppWinsAfter < oppWinsBefore) {
            bonus = 10000 * (oppWinsBefore - oppWinsAfter);
            System.out.println("Prevented " + (oppWinsBefore - oppWinsAfter) + " opponent winning moves");
        }
        return bonus;
    }

    /**
     * Strategy: Board Control.
     * Simulates the board after applying the move and compares the mobility (the number
     * of valid moves) for the AI versus the opponent for before the move and after the move.
     *
     * @param move the move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player making the move
     * @return a bonus score for board control
     */
    private int evaluateBoardControl(Move move, Board board, AIPlayer aiPlayer) {
        Board simulatedBoard = board.clone();
        simulatedBoard.applyMove(move);
        
        // Calculate mobility for AI before the move.
        int beforeAIMobility = board.getPossibleMoves(aiPlayer).size();
        
        // Calculate mobility before for the human opponent.
        int beforeOpponentMobility = board.getPossibleMoves(board.opponentPlayer(aiPlayer)).size();
        
        // Calculate mobility for AI after the move.
        int afterAIMobility = simulatedBoard.getPossibleMoves(aiPlayer).size();
        
        // Calculate mobility for the human opponent.
        int afterOpponentMobility = simulatedBoard.getPossibleMoves(board.opponentPlayer(aiPlayer)).size();
        
        // The bonus is calculated using the difference in mobility for the before and after.
        int bonus = ((afterAIMobility - afterOpponentMobility) - (beforeAIMobility - beforeOpponentMobility))* 5;

        return bonus;
    }

    /**
     * Strategy: Edge Vulnerability.
     * checks if the human (opponent) can push any AI pieces off-board in their next move.
     * Returns a penalty if such moves are available.
     *
     * @param move the candidate AI move to evaluate
     * @param board the current board state
     * @param aiPlayer the AI player making the move
     * @return a negative penalty score if the move leaves AI vulnerable, 0 otherwise.
     */
    private int evaluateEdgeVulnerability(Move move, Board board, AIPlayer aiPlayer) {
        int beforePushingOfEdgeOppertunities = 0;
        int afterPushingOfEdgeOppertunities = 0;

        Board simulatedBoard = board.clone();
        simulatedBoard.applyMove(move);
        
        Player humanOpponent = board.opponentPlayer(aiPlayer);

        List<Move> beforeOpponentMoves = board.getPossibleMoves(humanOpponent);
        for (Move oppMove : beforeOpponentMoves) {
            // Check if the opponent's move is a push move that pushes an AI piece off-board.
            int from = oppMove.getFrom();
            int[] fromCoord = board.getIndexToCoord().get(from);
            int[] toCoord = board.getIndexToCoord().get(oppMove.getTo());
            int dq = toCoord[0] - fromCoord[0];
            int dr = toCoord[1] - fromCoord[1];
            
            // Only consider push moves (destination cell is occupied).
            if (board.getPlayerAt(oppMove.getTo()) != null) {
                List<Integer> oppGroup = board.getListOfPiecesInDirection(from, dq, dr);
                int leadingOpp = oppGroup.get(oppGroup.size() - 1);
                int next = board.getNextCellInDirection(leadingOpp, dq, dr);
                List<Integer> opponentGroup = board.getListOfPiecesInDirection(next, dq, dr);
                if(opponentGroup.size() > 0) {
                    int pushDest = board.getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
                    if (pushDest == -1) {
                        beforePushingOfEdgeOppertunities++; // push off edge move
                    }
                }
            }
        }

        List<Move> afterOpponentMoves = simulatedBoard.getPossibleMoves(humanOpponent);
        for (Move oppMove : afterOpponentMoves) {
            // Check if the opponent's move is a push move that pushes an AI piece off-board.
            int from = oppMove.getFrom();
            int[] fromCoord = simulatedBoard.getIndexToCoord().get(from);
            int[] toCoord = simulatedBoard.getIndexToCoord().get(oppMove.getTo());
            int dq = toCoord[0] - fromCoord[0];
            int dr = toCoord[1] - fromCoord[1];
            
            // Only consider push moves (destination cell is occupied).
            if (simulatedBoard.getPlayerAt(oppMove.getTo()) != null) {
                List<Integer> oppGroup = simulatedBoard.getListOfPiecesInDirection(from, dq, dr);
                int leadingOpp = oppGroup.get(oppGroup.size() - 1);
                int next = simulatedBoard.getNextCellInDirection(leadingOpp, dq, dr);
                List<Integer> opponentGroup = simulatedBoard.getListOfPiecesInDirection(next, dq, dr);
                if(opponentGroup.size() > 0) {
                    int pushDest = simulatedBoard.getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
                    if (pushDest == -1) {
                        afterPushingOfEdgeOppertunities++; // push off edge move
                    }
                }
            }
        }
        return 200 * (beforePushingOfEdgeOppertunities - afterPushingOfEdgeOppertunities);
    }


    /**
     * Computes the hexagonal distance between two axial coordinates.
     * Uses the formula: distance = max(|q1 - q2|, |r1 - r2|, |(q1 + r1) - (q2 + r2)|)
     *
     * @param a the first coordinate [q, r]
     * @param b the second coordinate [q, r]
     * @return the hex distance between a and b
     */
    private int hexDistance(int[] a, int[] b) {
        int dq = Math.abs(a[0] - b[0]);
        int dr = Math.abs(a[1] - b[1]);
        int ds = Math.abs((a[0] + a[1]) - (b[0] + b[1]));
        return Math.max(dq, Math.max(dr, ds));
    }

    /**
     * Computes the distance of a coordinate from the edge .
     * A cell on the edge will have an edge distance of 0.
     *
     * @param coord the axial coordinate [q, r]
     * @return the edge distance
     */
    private int getEdgeDistance(int[] coord) {
        int q = coord[0], r = coord[1];
        int d1 = 4 - Math.abs(q);
        int d2 = 4 - Math.abs(r);
        int d3 = 4 - Math.abs(q + r);
        return Math.min(d1, Math.min(d2, d3));
    }

}
