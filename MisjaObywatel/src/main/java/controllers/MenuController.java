package controllers;

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

        boolean debugEtap2 = true; // <-- ustaw na true jeśli chcesz zacząć od GlosowanieScene

        if (debugEtap2) {
            GlosowanieScene glosowanie = new GlosowanieScene();
            glosowanie.start(stage);
        } else {
            GameScene gameScene = new GameScene();
            gameScene.start(stage);
        }
    }

    private void showScoreboard(javafx.event.ActionEvent e) {
        System.out.println("Otwarcie tabeli wyników — dodamy później");
    }

    private void exit() {
        System.exit(0);
    }
}
