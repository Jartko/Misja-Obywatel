package game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ScoreboardScene {

    private Stage primaryStage;

    public void start(Stage stage) {
        this.primaryStage = stage;

        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40;");
        Text title = new Text("TABELA WYNIKÓW");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(javafx.scene.paint.Color.WHITE);
        title.setStroke(javafx.scene.paint.Color.BLACK);
        title.setStrokeWidth(1);
        TableView<ScoreRecord> table = new TableView<>();
        table.setMaxWidth(900); // Troszkę szersza
        table.setPrefHeight(500);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label placeholder = new Label("Brak zapisanych wyników");
        placeholder.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        table.setPlaceholder(placeholder);


        // 1. Gracz
        TableColumn<ScoreRecord, String> nameCol = new TableColumn<>("Gracz");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(150);
        nameCol.setMaxWidth(300);

        // 2. Etap 1: Przygotowanie Lokalu
        TableColumn<ScoreRecord, Integer> s1Col = new TableColumn<>("Przygotowanie Lokalu");
        s1Col.setCellValueFactory(new PropertyValueFactory<>("stage1"));
        s1Col.setStyle("-fx-alignment: CENTER;"); // Wyśrodkowanie liczb

        // 3. Etap 2: Przeprowadzenie Głosowania
        TableColumn<ScoreRecord, Integer> s2Col = new TableColumn<>("Przeprowadzenie Głosowania");
        s2Col.setCellValueFactory(new PropertyValueFactory<>("stage2"));
        s2Col.setStyle("-fx-alignment: CENTER;");

        // 4. Etap 3: Liczenie Głosów
        TableColumn<ScoreRecord, Integer> s3Col = new TableColumn<>("Liczenie Głosów");
        s3Col.setCellValueFactory(new PropertyValueFactory<>("stage3"));
        s3Col.setStyle("-fx-alignment: CENTER;");

        // 5. SUMA
        TableColumn<ScoreRecord, Integer> totalCol = new TableColumn<>("SUMA PKT");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        totalCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        totalCol.setSortType(TableColumn.SortType.DESCENDING);

        table.getColumns().addAll(nameCol, s1Col, s2Col, s3Col, totalCol);
        table.setItems(loadScores());
        table.getSortOrder().add(totalCol);
        table.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-base: #ecf0f1;" +
                        "-fx-control-inner-background: #ffffff;" +
                        "-fx-background-color: transparent;"
        );

        // --- PRZYCISK POWROTU ---
        Button btnBack = new Button("POWRÓT DO MENU");
        btnBack.setPrefWidth(250);
        btnBack.setPrefHeight(45);
        btnBack.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 5;"
        );

        btnBack.setOnMouseEntered(e -> btnBack.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;"));
        btnBack.setOnMouseExited(e -> btnBack.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;"));

        btnBack.setOnAction(e -> backToMenu());

        root.getChildren().addAll(title, table, btnBack);

        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Tabela Wyników");
        stage.show();
    }

    private ObservableList<ScoreRecord> loadScores() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("highscores.json");
        ObservableList<ScoreRecord> list = FXCollections.observableArrayList();

        if (file.exists()) {
            try {
                List<ScoreRecord> data = mapper.readValue(file, new TypeReference<List<ScoreRecord>>() {});
                list.addAll(data);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku JSON: " + e.getMessage());
            }
        }
        return list;
    }

    private void backToMenu() {
        try {
            String fxmlPath = "/scenes/menu.fxml";
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent rootNode = loader.load();
            Scene menuScene = new Scene(rootNode, 1024, 768);

            primaryStage.setScene(menuScene);
            primaryStage.setTitle("Misja Obywatel – Czas Wyborów");
            primaryStage.setWidth(1024);
            primaryStage.setHeight(768);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}