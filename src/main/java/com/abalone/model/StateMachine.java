package com.abalone.model;

import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.AIPlayer;
import com.abalone.model.utils.Players.Player;
import java.util.List;
import java.util.Random;

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
        
        score += evaluatePushMove(move, board, aiPlayer);
        
        score += evaluateCenteringMove(move, board, aiPlayer);

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
            score += 50 * (moverSize - opponentSize);
            int pushDest = board.getNextCellInDirection(opponentGroup.get(opponentGroup.size() - 1), dq, dr);
            if (pushDest == -1) {
                score += 100;
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
            score += (centerDistanceFrom - centerDistanceTo) * 10;
        }
        return score;
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
}
