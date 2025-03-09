package com.abalone;

import javafx.application.Application;
import javafx.stage.Stage;

public class AbaloneGame extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GameManager gameManager = new GameManager();
        GameView gameView = new GameView(primaryStage);
        GameController gameController = new GameController(gameManager, gameView);

        gameView.setController(gameController);
        gameController.startGame();
    }
}