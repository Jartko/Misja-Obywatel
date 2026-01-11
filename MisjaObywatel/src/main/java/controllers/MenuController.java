package controllers;

import game.LiczenieGlosowScene;
import game.ScoreboardScene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

// Sceny
import game.GameScene;
import game.GlosowanieScene;

public class MenuController {

    @FXML private Button rozpocznijGreButton;
    @FXML private Button tabelaWynikowButton;
    @FXML private Button wyjscieButton;

    @FXML
    public void initialize() {
        rozpocznijGreButton.setOnAction(this::startGame);
        tabelaWynikowButton.setOnAction(this::showScoreboard);
        wyjscieButton.setOnAction(e -> exit());
    }

    private void startGame(javafx.event.ActionEvent e) {
        Stage stage = (Stage) rozpocznijGreButton.getScene().getWindow();

        boolean debugEtap2 = false;
        boolean debugEtap3 = false;
        if(debugEtap3){
            LiczenieGlosowScene liczenie = new LiczenieGlosowScene(0,0);
            liczenie.start(stage);
        }
        else if (debugEtap2) {
            GlosowanieScene glosowanie = new GlosowanieScene(0);
            glosowanie.start(stage);
        } else {
            GameScene gameScene = new GameScene();
            gameScene.start(stage);
        }
    }

    private void showScoreboard(javafx.event.ActionEvent e) {
        ScoreboardScene scoreboard = new ScoreboardScene();
        Stage stage = (Stage) tabelaWynikowButton.getScene().getWindow();
        scoreboard.start(stage);
    }

    private void exit() {
        System.exit(0);
    }
}
