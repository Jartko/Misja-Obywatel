package game;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import controllers.MenuController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiczenieGlosowScene {
    private final boolean DEBUG_MODE = false;
    private final double[][] POSITIONS = {

            {85, 116},   // 1. Bartoszewicz
            {84.5, 145},  // 2. Biejat
            {80, 177},  // 3. Braun
            {80, 206},  // 4. Hołownia
            {80, 235},  // 5. Jakubiak
            {78, 263},  // 6. Maciak
            {77, 292},  // 7. Mentzen
            {76, 323},  // 8. Nawrocki
            {76, 353},  // 9. Senyszyn
            {76, 384},  // 10. Stanowski
            {75, 415},  // 11. Trzaskowski
            {74, 450},  // 12. Woch
            {74,480}   // 13. Zandberg
    };
    private Stage primaryStage;
    private BorderPane root;
    private int scoreStage1 = 0;
    private int scoreStage2;
    private int scoreStage3 = 0;
    private int currentRound = 0;
    private final int MAX_ROUNDS = 10;

    private StackPane ballotStack;
    private ImageView ballotImage;
    private Pane marksContainer; // ZMIANA: Pane zamiast VBox
    private VBox controlPanel;
    private Text scoreText;
    private Text roundText;
    private javafx.scene.layout.StackPane rootStack;
    private javafx.scene.layout.StackPane dialogueBox;
    private javafx.scene.text.Text dialogueText;
    // --- Dialogi ---
    private int dialogueIndex = 0;
    private boolean dialogueActive = true;
    private final String[] introLines = {
            "Lokale zamknięte. Czas na Etap 3: Liczenie Głosów.",
            "Twoim zadaniem jest ocena ważności karty i głosu.",
            "KROK 1: Sprawdź pieczątkę. Karta MUSI mieć czerwoną pieczątkę OKW.",
            "Brak pieczątki = Karta Nieważna (odrzucamy całość).",
            "KROK 2: Sprawdź znak 'X'. Głos ważny to DOKŁADNIE JEDEN 'X'.",
            "X musi znaleźć się w kratce przy nazwisku kandydata.",
            "Brak znaku, dwa znaki, 'ptaszki', zamazania = Głos Nieważny.",
            "KROK 3: Jeśli głos jest ważny, przypisz go do odpowiedniego kandydata.",
            "Skup się! Każdy pomyłka to sfałszowanie wyników wyborów."
    };

    private BallotCase currentCase;
    private final String[] candidates = {
            "Bartoszewicz Artur", "Biejat Magdalena Agnieszka", "Braun Grzegorz Michał",
            "Hołownia Szymon Franciszek", "Jakubiak Marek", "Maciak Maciej",
            "Mentzen Sławomir Jerzy", "Nawrocki Karol Tadeusz", "Senyszyn Joanna",
            "Stanowski Krzysztof Jakub", "Trzaskowski Rafał Kazmierz", "Woch Marek Marian",
            "Zandberg Adrian Tadeusz"
    };

    public LiczenieGlosowScene(int scoreFromStage2, int scoreFromStage1) {
        this.scoreStage2 = scoreFromStage2;
        this.scoreStage1 = scoreFromStage1;
    }
    public void start(Stage stage) {
        this.primaryStage = stage;

        root = new BorderPane();
        root.setPrefSize(1024, 768);
        root.setStyle("-fx-background-color: #2c3e50;");

        createGameLayout();
        nextRound();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Etap 3: Liczenie Głosów");
        stage.show();
    }

    private void createGameLayout() {
        // --- 1. GÓRA (INFO) ---
        HBox infoBar = new HBox(40);
        infoBar.setAlignment(Pos.CENTER);
        infoBar.setPadding(new Insets(15));
        infoBar.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        roundText = new Text("KARTA: 1/10");
        roundText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        roundText.setFill(Color.WHITE);

        scoreText = new Text("PKT: 0");
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        scoreText.setFill(Color.GOLD);

        infoBar.getChildren().addAll(roundText, scoreText);
        root.setTop(infoBar);

        // --- 2. ŚRODEK (KARTA WYBORCZA) ---
        ballotStack = new StackPane();
        ballotStack.setMaxSize(500, 700);
        ballotStack.setAlignment(Pos.TOP_LEFT);

        ballotImage = new ImageView();
        ballotImage.setFitWidth(500);
        ballotImage.setFitHeight(700);

        // ZMIANA: Używamy Pane do absolutnego pozycjonowania
        marksContainer = new Pane();
        marksContainer.setMaxSize(500, 700); // Rozmiar taki jak obrazka
        marksContainer.setMouseTransparent(true);

        if (DEBUG_MODE) {
            marksContainer.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        }

        ballotStack.getChildren().addAll(ballotImage, marksContainer);

        StackPane centerWrapper = new StackPane(ballotStack);
        centerWrapper.setPadding(new Insets(20));
        root.setCenter(centerWrapper);

        // --- 3. PRAWA STRONA (PANEL) ---
        controlPanel = new VBox(15);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPrefWidth(350);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #7f8c8d; -fx-border-width: 0 0 0 2;");

        root.setRight(controlPanel);
    }

    private void nextRound() {
        if (currentRound >= MAX_ROUNDS) {
            showEndGameSummary();
            return;
        }
        currentRound++;
        roundText.setText("KARTA: " + currentRound + "/" + MAX_ROUNDS);

        generateRandomBallot();

        controlPanel.setVisible(false);
        ballotStack.setOpacity(0);
        ballotStack.setScaleX(0.8);
        ballotStack.setScaleY(0.8);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.3), ballotStack);
        ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.3), ballotStack);
        st.setToX(1.0);
        st.setToY(1.0);
        ft.play();
        st.play();

        st.setOnFinished(e -> showPhase1_CardValidity());
    }

    private void generateRandomBallot() {
        currentCase = new BallotCase();
        marksContainer.getChildren().clear(); // Czyścimy stare znaki

        // Tło
        currentCase.hasStamp = Math.random() < 0.8;
        try {
            String path = currentCase.hasStamp ? "/images/karta.png" : "/images/karta_bez.png";
            ballotImage.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            ballotImage.setImage(null);
        }

        // Losowanie znaku
        double symbolRoll = Math.random();
        String symbol = "X";
        if (symbolRoll > 0.90) symbol = "V";
        else if (symbolRoll > 0.8) symbol = "O";
        currentCase.symbolType = symbol;
        double countRoll = Math.random();
        int marksCount = 1;
        if (countRoll < 0.1) marksCount = 0;
        else if (countRoll > 0.8) marksCount = 2 + new Random().nextInt(3);

        Text[] markViews = new Text[13];
        for (int i = 0; i < 13; i++) {
            markViews[i] = new Text(" ");
            markViews[i].setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 14)); // Rozmiar czcionki
            markViews[i].setFill(Color.NAVY);
            double x = POSITIONS[i][0];
            double y = POSITIONS[i][1];
            markViews[i].setLayoutX(x);
            markViews[i].setLayoutY(y);

            // DEBUG do ustawiania Pozycji
            if (DEBUG_MODE) {
                markViews[i].setText("X");
            }

            marksContainer.getChildren().add(markViews[i]);
        }

        // WSTAWIANIE WŁAŚCIWYCH ZNAKÓW
        List<Integer> markedIndices = new ArrayList<>();
        for (int i = 0; i < marksCount; i++) {
            int idx;
            do {
                idx = new Random().nextInt(13);
            } while (markedIndices.contains(idx));
            markedIndices.add(idx);
            markViews[idx].setText(symbol);
        }

        currentCase.markedIndices = markedIndices;
    }

    // --- FAZA 1 ---
    private void showPhase1_CardValidity() {
        controlPanel.getChildren().clear();
        controlPanel.setVisible(true);

        Label label = new Label("KROK 1:\nCzy na karcie jest pieczątka?");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 16));
        label.setTextAlignment(TextAlignment.CENTER);

        Button btnValid = createButton("KARTA WAŻNA (Jest)", Color.GREEN);
        btnValid.setOnAction(e -> {
            if (currentCase.hasStamp) showPhase2_VoteValidity();
            else handleResult(false, "BŁĄD! Brak pieczątki = Nieważna.");
        });

        Button btnInvalid = createButton("KARTA NIEWAŻNA (Brak)", Color.RED);
        btnInvalid.setOnAction(e -> {
            if (!currentCase.hasStamp) handleResult(true, "Dobrze! Karta bez pieczątki.");
            else handleResult(false, "BŁĄD! Karta ma pieczątkę.");
        });

        controlPanel.getChildren().addAll(label, btnValid, btnInvalid);
    }

    // --- FAZA 2 ---
    private void showPhase2_VoteValidity() {
        controlPanel.getChildren().clear();

        Label label = new Label("KROK 2:\nCzy postawiono dokładnie jeden znak 'X'?");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 16));
        label.setTextAlignment(TextAlignment.CENTER);

        Button btnValid = createButton("GŁOS WAŻNY", Color.GREEN);
        btnValid.setOnAction(e -> {
            boolean isX = currentCase.symbolType.equals("X");
            boolean single = currentCase.markedIndices.size() == 1;

            if (isX && single) showPhase3_SelectCandidate();
            else handleResult(false, "BŁĄD! Głos jest nieważny (Zły znak lub ilość).");
        });

        Button btnInvalid = createButton("GŁOS NIEWAŻNY", Color.RED);
        btnInvalid.setOnAction(e -> {
            boolean isX = currentCase.symbolType.equals("X");
            boolean single = currentCase.markedIndices.size() == 1;

            if (!isX || !single) handleResult(true, "Dobrze! Głos odrzucony.");
            else handleResult(false, "BŁĄD! To jest prawidłowy głos.");
        });

        controlPanel.getChildren().addAll(label, btnValid, btnInvalid);
    }

    // --- FAZA 3 ---
    private void showPhase3_SelectCandidate() {
        controlPanel.getChildren().clear();

        Label label = new Label("KROK 3:\nKto otrzymał głos?");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 16));

        ListView<String> list = new ListView<>();
        list.getItems().addAll(candidates);
        list.setPrefHeight(400);

        Button btnConfirm = createButton("ZATWIERDŹ", Color.ORANGE);
        btnConfirm.setOnAction(e -> {
            int selected = list.getSelectionModel().getSelectedIndex();
            if (selected == -1) return;
            int actualMarked = currentCase.markedIndices.get(0);

            if (selected == actualMarked) handleResult(true, "Głos zaliczony!");
            else handleResult(false, "BŁĄD! Zły kandydat.");
        });

        controlPanel.getChildren().addAll(label, list, btnConfirm);
    }

    private void handleResult(boolean success, String msg) {
        controlPanel.getChildren().clear();
        Text resultText = new Text(msg);
        resultText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        resultText.setWrappingWidth(300);
        resultText.setTextAlignment(TextAlignment.CENTER);

        if (success) {
            scoreStage3 += 100;
            resultText.setFill(Color.LIGHTGREEN);
        } else {
            scoreStage3 -= 100;
            resultText.setFill(Color.RED);
        }
        scoreText.setText("PKT: " + scoreStage3);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.7));
        pause.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.seconds(0.4), ballotStack);
            ft.setToValue(0);
            ft.setOnFinished(ev -> nextRound());
            ft.play();
        });
        controlPanel.getChildren().add(resultText);
        pause.play();
    }

    private void showEndGameSummary() {
        root.getChildren().clear();
        root.setTop(null);
        root.setRight(null);
        root.setCenter(null);

        VBox summaryBox = new VBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 10;");
        summaryBox.setMaxSize(600, 500);

        Text title = new Text("RAPORT KOŃCOWY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        int total = scoreStage1 + scoreStage2 + scoreStage3;

        Text t1 = new Text("Przygotowanie Lokalu(Etap 1): " + scoreStage1 + " pkt");
        Text t2 = new Text("Przeprowadzenie Głosowania(Etap 2): " + scoreStage2 + " pkt");
        Text t3 = new Text("Liczenie Głosów(Etap 3): " + scoreStage3 + " pkt");
        Text tTotal = new Text("RAZEM: " + total + " pkt");
        tTotal.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        tTotal.setFill(Color.DARKBLUE);

        TextField nameInput = new TextField();
        nameInput.setPromptText("Twoje imię");
        nameInput.setMaxWidth(300);

        Button btnMenu = new Button("ZAKOŃCZ I WRÓĆ DO MENU");
        btnMenu.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");

        btnMenu.setOnAction(e -> {
            String name = nameInput.getText();
            if (name == null || name.trim().isEmpty()) name = "Anonim";
            saveScoreToJson(name);
            try {
                String fxmlPath = "/scenes/menu.fxml";

                java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    System.err.println("Błąd: Nie znaleziono pliku menu!");
                    return;
                }

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
                javafx.scene.Parent rootNode = loader.load();
                primaryStage.setTitle("Misja Obywatel – Czas Wyborów");
                Scene menuScene = new Scene(rootNode, 1024, 768);
                primaryStage.setScene(menuScene);
                primaryStage.setWidth(1024);
                primaryStage.setHeight(768);
                primaryStage.centerOnScreen();
                primaryStage.show();

            } catch (java.io.IOException ex) {
                System.err.println("Błąd ładowania pliku FXML!");
                ex.printStackTrace();
            }
        });

        summaryBox.getChildren().addAll(title, t1, t2, t3, new Separator(), tTotal, new Label("Imię:"), nameInput, btnMenu);
        root.setCenter(summaryBox);
    }

    private Button createButton(String text, Color baseColor) {
        Button btn = new Button(text);
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        String hex = String.format("#%02X%02X%02X", (int)(baseColor.getRed()*255), (int)(baseColor.getGreen()*255), (int)(baseColor.getBlue()*255));
        btn.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }

    private static class BallotCase {
        boolean hasStamp;
        String symbolType;
        List<Integer> markedIndices;
    }
    private void saveScoreToJson(String name) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("highscores.json");
        List<ScoreRecord> scores = new ArrayList<>();

        int total = scoreStage1 + scoreStage2 + scoreStage3;

        try {
            if (file.exists()) {
                scores = mapper.readValue(file, new TypeReference<List<ScoreRecord>>() {});
            }
            scores.add(new ScoreRecord(name, scoreStage1, scoreStage2, scoreStage3, total));
            mapper.writeValue(file, scores);
            System.out.println("Zapisano szczegółowy wynik do highscores.json");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd zapisu wyników!");
        }
    }
}