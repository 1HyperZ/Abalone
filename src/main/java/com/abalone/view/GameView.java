package com.abalone.view;

import com.abalone.controller.GameController;
import com.abalone.model.Board;
import com.abalone.model.utils.Players.Player;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javafx.scene.layout.*;

public class GameView {
    private Stage stage;
    private GameController controller;
    private Pane boardGrid;
    private Label turnLabel;
    private Label humanScoreLabel;
    private Label aiScoreLabel;

    

    /**
     * Constructor for GameView that initializes the UI components.
     * @param stage The main JavaFX window.
     */
    public GameView(Stage stage) {
        this.stage = stage;
        this.boardGrid = new Pane();
        this.turnLabel = new Label("Turn: Human");
        this.humanScoreLabel = new Label("Player 1: 14");
        this.aiScoreLabel = new Label("Player 2: 14");
        
        humanScoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        aiScoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        

        // add topPanel to the top
        HBox topPanel = new HBox(20, turnLabel, humanScoreLabel, aiScoreLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(10));

        // put your boardGrid inside a StackPane
        StackPane boardContainer = new StackPane(boardGrid);

        Button restartButton = new Button("Start New Game");
        restartButton.setOnAction(e -> controller.startNewGame());

        Button instructionsButton = new Button("Instructions");
        instructionsButton.setOnAction(e -> showInstructions());

        HBox bottomPanel = new HBox(20, restartButton, instructionsButton);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(boardContainer);
        root.setBottom(bottomPanel);


        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sets the controller so it can interact with the view.
     * @param controller The GameController instance.
     */
    public void setController(GameController controller) {
        this.controller = controller;
    } 

    public void renderBoard(Board board) {
        boardGrid.getChildren().clear();
    
        double hexSize = 30; // Adjust size as needed
        double xOffset = hexSize * Math.sqrt(3); // Horizontal spacing
        double yOffset = hexSize * 1.5; // Vertical spacing
    
        // Corrected Hexagonal Layout for Abalone (61 positions)
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
    
        // Centering
        double boardWidth = 800;
        double boardHeight = 800;
        double centerX = boardWidth / 2;
        double centerY = boardHeight / 2 - (yOffset * layout.length / 2);
    
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                int position = layout[row][col];
    
                // Adjust X positioning so the hexagonal structure is formed properly
                double xPos = centerX + (col - layout[row].length / 2.0) * xOffset;
                double yPos = centerY + row * yOffset;
    
                Circle piece = new Circle(hexSize / 2);
                piece.setUserData(position); // Store position inside each piece

    
                Player playerAtPos = board.getPlayerAt(position);
                if (playerAtPos == null) {
                    piece.setFill(Color.LIGHTGRAY);
                } else {
                    piece.setFill(playerAtPos.getName().equals("AI") ? Color.WHITE : Color.BLACK);
                }
    
                final int cellIndex = position; // Capture index for lambda
                piece.setOnMouseClicked(event -> {
                    if (controller.isHumanTurn()) {
                        controller.handleMove(cellIndex);
                    }
                });
    
                piece.setLayoutX(xPos);
                piece.setLayoutY(yPos);
                boardGrid.getChildren().add(piece);
            }
        }
    }
    
    public void clearHighlight() {
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof Circle) {
                Circle piece = (Circle) node;
                piece.setStroke(Color.TRANSPARENT);
            }
        }
    }
    
    
    public void highlightPiece(int position) {
        System.out.println("Trying to highlight position: " + position);
    
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof Circle) {
                Circle piece = (Circle) node;
                System.out.println("Checking piece at position: " + piece.getUserData()); // Debugging output
                
                if ((int) piece.getUserData() == position) { 
                    System.out.println("Highlighting piece at: " + position);
                    piece.setStroke(Color.RED);
                    piece.setStrokeWidth(3);
                    return;
                }
            }
        }
    }
    
    
    public void refreshBoard() {
        boardGrid.requestLayout(); // Force JavaFX to refresh UI
    }
    
    
    public void showGameOver(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    
    /**
    * Updates the turn label to show whose turn it is.
    * @param text The current player's turn (e.g., "Human" or "AI").
    */
    public void updateTurnLabel(String text) {
        turnLabel.setText("Turn: " + text);
    }

    /**
     * Updates the two score labels at the top of the screen.
     * Call this whenever the scores change (e.g., after each move).
     */
    public void updateScores(int humanScore, int aiScore) {
        humanScoreLabel.setText("Player 1: " + humanScore);
        aiScoreLabel.setText("Player 2: " + aiScore);
    }

    /**
     * Displays an instructions popup dialog.
     */
    private void showInstructions() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Instructions");
        alert.setHeaderText("How to Play Abalone");
        alert.setContentText(
            "1. Select one of your marbles (or a group in a straight line).\n" +
            "2. Move it to an adjacent cell or push opponent marbles.\n" +
            "3. The first to push 6 opponent marbles off the board wins.\n" +
            "...Add more details here..."
        );
        alert.showAndWait();
    }

    
}
