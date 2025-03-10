package com.abalone;

import com.abalone.controller.GameController;
import com.abalone.model.GameManager;
import com.abalone.view.GameView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class that launches the game.
 * Initializes the game manager, view, and controller, then starts the game.
 */
public class AbaloneGame extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Sets up the game components and starts the game.
     * @param primaryStage the main stage
     */
    @Override
    public void start(Stage primaryStage) {
        GameManager gameManager = new GameManager();
        GameView gameView = new GameView(primaryStage);
        GameController gameController = new GameController(gameManager, gameView);
        gameView.setController(gameController);
        gameController.startGame();
    }
}
