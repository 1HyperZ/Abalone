package com.abalone.view;

import com.abalone.controller.GameController;
import com.abalone.model.Board;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


public class GameView {
    private final Stage stage;
    private GameController controller;

    private final BorderPane root;
    private final Pane boardGrid;

    private final HBox bottomPanel;
    private final Button restartButton;
    private final Button instructionsButton;

    private final HBox topPanel;
    private final Label turnLabel;
    private final Label humanScoreLabel;
    private final Label aiScoreLabel;

    /**
     * Initializes the GameView, sets up the top panel with turn and score labels,
     * the center board, and the bottom panel with control buttons.
     * @param stage the primary Stage of the application
     */
    public GameView(Stage stage) {
        this.stage = stage;
        this.boardGrid = new Pane();

        //top panel
        this.turnLabel = new Label("Turn: Human");
        this.humanScoreLabel = new Label("Player 1: 14");
        this.aiScoreLabel = new Label("Player 2: 14");
        turnLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        humanScoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        aiScoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        topPanel = new HBox(20, turnLabel, humanScoreLabel, aiScoreLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(10));

        //bottom panel
        restartButton = new Button("Start New Game");
        restartButton.setOnAction(e -> controller.startNewGame());
        instructionsButton = new Button("Instructions");
        instructionsButton.setOnAction(e -> showInstructions());
        bottomPanel = new HBox(20, restartButton, instructionsButton);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(10));

        //root layout
        root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(boardGrid);
        root.setBottom(bottomPanel);
        
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sets the GameController for this view.
     * @param controller the GameController instance
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Renders the game board with provided Board.
     * @param board the current game board state
     */
    public void renderBoard(Board board) {
        boardGrid.getChildren().clear();
        double hexSize = 30; // board size 
        double xOffset = hexSize * Math.sqrt(3);
        double yOffset = hexSize * 1.5; 
        int[][] layout = {
            {0,  1,  2,  3,  4},
            {5,  6,  7,  8,  9, 10},
            {11, 12, 13, 14, 15, 16, 17},
            {18, 19, 20, 21, 22, 23, 24, 25},
            {26, 27, 28, 29, 30, 31, 32, 33, 34},
            {35, 36, 37, 38, 39, 40, 41, 42},
            {43, 44, 45, 46, 47, 48, 49},
            {50, 51, 52, 53, 54, 55},
            {56, 57, 58, 59, 60}
        };
        double boardWidth = 800;
        double boardHeight = 800;
        double centerX = boardWidth / 2;
        double centerY = boardHeight / 2 - (yOffset * layout.length / 2);
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                int position = layout[row][col];
                double xPos = centerX + (col - layout[row].length / 2.0) * xOffset;
                double yPos = centerY + row * yOffset;
                Circle piece = new Circle(hexSize / 2);
                piece.setUserData(position);
                piece.setFill(board.getPieceColor(position));
                piece.setOnMouseClicked(event -> {
                    if (controller.isHumanTurn()) {
                        controller.clickedBoardCell(position);
                    }
                });
                piece.setLayoutX(xPos);
                piece.setLayoutY(yPos);
                boardGrid.getChildren().add(piece);
            }
        }
    }

    /**
     * Clears highlighting on all pieces.
     */
    public void clearHighlight() {
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof Circle) {
                Circle piece = (Circle) node;
                piece.setStroke(Color.TRANSPARENT);
            }
        }
    }

    /**
     * Highlights the piece at the provided board position.
     * @param position the board cell index to be highlighted
     */
    public void highlightPiece(int position) {
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof Circle) {
                Circle piece = (Circle) node;
                if ((int) piece.getUserData() == position) {
                    System.out.println("Highlighted piece at: " + position);
                    piece.setStroke(Color.RED);
                    piece.setStrokeWidth(3);
                    return;
                }
            }
        }
    }

    /**
     * Displays a game over alert with the given message.
     * @param message the string to display in the alert
     */
    public void showGameOver(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates the turn label to know which player should play.
     * @param text the turn text "Human" or "AI"
     */
    public void updateTurnLabel(String text) {
        turnLabel.setText("Turn: " + text);
    }

    /**
     * Updates the score labels with provided scores.
     * @param humanScore the score of Human
     * @param aiScore the score of AI
     */
    public void updateScores(int humanScore, int aiScore) {
        humanScoreLabel.setText("Player 1: " + humanScore);
        aiScoreLabel.setText("Player 2: " + aiScore);
    }

    /**
     * Displays an instructions box with the game rules.
     */
    private void showInstructions() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Instructions");
        alert.setHeaderText("How to Play Abalone");
        alert.setContentText(
            "1. Select one of your marbles (or a contiguous group) by clicking it.\n" +
            "2. Move it to an adjacent cell or push opponent marbles.\n" +
            "3. The first to push 6 opponent marbles off the board wins.\n" +
            "..."
        );
        alert.showAndWait();
    }
}
