package game.minigames;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Random;

public class KabinaMiniGame {

    private final Runnable onComplete;
    private final Random random = new Random();

    private final int TOTAL_POINTS = 3;
    private int score = 0;

    // --- KONFIGURACJA PRĘDKOŚCI ---
    private final double BASE_SPEED = 1;
    private final double SPEED_STEP = 0.25;
    private double rotationSpeed = BASE_SPEED;

    private boolean waitingForPress = false;
    private char requiredKey;

    private Arc greenZone;
    private Circle pointerDot;
    private Arc pointerLine;
    private Text keyText;
    private Text titleText;
    private Text progressText;
    private Circle centerBg;

    private boolean pointerInGreen = false;
    private double currentAngle = 0;
    private boolean isGameRunning = true;

    private final double RADIUS = 150;

    public KabinaMiniGame(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public void start(Stage stage) {
        double width = stage.getWidth() > 100 ? stage.getWidth() : 800;
        double height = stage.getHeight() > 100 ? stage.getHeight() : 600;

        Pane root = new Pane();
        root.setPrefSize(width, height);
        root.setStyle("-fx-background-color: #263238;");

        double centerX = width / 2;
        double centerY = height / 2;

        // --- Tło tarczy ---
        Circle outerTrack = new Circle(centerX, centerY, RADIUS);
        outerTrack.setFill(null);
        outerTrack.setStroke(Color.rgb(55, 71, 79));
        outerTrack.setStrokeWidth(20);
        root.getChildren().add(outerTrack);

        // --- Zielona strefa ---
        greenZone = new Arc(centerX, centerY, RADIUS, RADIUS, 90, 45);
        greenZone.setType(ArcType.OPEN);
        greenZone.setFill(null);
        greenZone.setStroke(Color.rgb(0, 230, 118));
        greenZone.setStrokeWidth(20);
        greenZone.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.LIME, 15, 0.5, 0, 0));
        root.getChildren().add(greenZone);

        // --- Wskaźnik ---
        pointerLine = new Arc(centerX, centerY, RADIUS + 25, RADIUS + 25, 0, 0);
        pointerLine.setType(ArcType.OPEN);
        pointerLine.setStroke(Color.WHITE);
        pointerLine.setStrokeWidth(3);

        pointerDot = new Circle(0, 0, 8, Color.WHITE);
        pointerDot.setEffect(new DropShadow(5, Color.WHITE));

        root.getChildren().addAll(pointerLine, pointerDot);

        // --- Środek ---
        centerBg = new Circle(centerX, centerY, RADIUS - 20);
        centerBg.setFill(Color.rgb(38, 50, 56));
        centerBg.setStroke(Color.GRAY);
        centerBg.setStrokeWidth(1);
        root.getChildren().add(centerBg);

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        textBox.setLayoutX(centerX - 100);
        textBox.setLayoutY(centerY - 50);
        textBox.setPrefSize(200, 100);

        Text label = new Text("NACIŚNIJ:");
        label.setFill(Color.LIGHTGRAY);
        label.setFont(Font.font("Consolas", 14));

        keyText = new Text("?");
        keyText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        keyText.setFill(Color.WHITE);

        textBox.getChildren().addAll(label, keyText);
        root.getChildren().add(textBox);

        // --- Napisy ---
        titleText = new Text("Naciśnij wskazany klawisz, gdy strzałka jest na zielonym polu,\naby rozłożyć kabinę do głosowania.");
        titleText.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        titleText.setFill(Color.LIGHTGRAY);

        // KLUCZOWE ZMIANY DLA FORMATOWANIA:
        titleText.setWrappingWidth(width - 40);
        titleText.setTextAlignment(TextAlignment.CENTER);
        titleText.setX(20);
        titleText.setY(40);

        root.getChildren().add(titleText);

        progressText = new Text(width - 150, height - 30, "0 / " + TOTAL_POINTS);
        progressText.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        progressText.setFill(Color.WHITE);
        root.getChildren().add(progressText);

        updateProgressText();
        startNewRound();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Minigra - Kabina wyborcza");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (!waitingForPress || !isGameRunning) return;

            String pressed = e.getText().toUpperCase();
            if (pointerInGreen && pressed.equals(String.valueOf(requiredKey))) {
                handleSuccess();
            } else {
                handleFail();
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isGameRunning) return;

                currentAngle += rotationSpeed;
                if (currentAngle >= 360) currentAngle -= 360;

                pointerLine.setStartAngle(currentAngle);
                pointerLine.setLength(5);

                double rad = Math.toRadians(currentAngle);
                double dotX = centerX + RADIUS * Math.cos(rad);
                double dotY = centerY - RADIUS * Math.sin(rad);

                pointerDot.setCenterX(dotX);
                pointerDot.setCenterY(dotY);

                pointerInGreen = isPointerInGreen(currentAngle);

                if (pointerInGreen) {
                    pointerDot.setFill(Color.LIME);
                } else {
                    pointerDot.setFill(Color.WHITE);
                }
            }
        };
        timer.start();
    }

    private void startNewRound() {
        requiredKey = (char) ('A' + random.nextInt(26));
        keyText.setText(String.valueOf(requiredKey));

        double newStartAngle = random.nextInt(360);
        greenZone.setStartAngle(newStartAngle);

        double length = 50 + random.nextInt(10);
        greenZone.setLength(length);

        waitingForPress = true;
    }

    private void handleSuccess() {
        score++;
        updateProgressText();

        rotationSpeed += SPEED_STEP;

        if (score >= TOTAL_POINTS) {
            isGameRunning = false;
            keyText.setFill(Color.LIME);
            keyText.setText("OK");
            centerBg.setFill(Color.DARKGREEN);

            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) {}
                Platform.runLater(onComplete);
            }).start();
        } else {
            keyText.setFill(Color.LIME);
            waitingForPress = false;

            new Thread(() -> {
                try { Thread.sleep(200); } catch (InterruptedException ex) {}
                Platform.runLater(() -> {
                    keyText.setFill(Color.WHITE);
                    startNewRound();
                });
            }).start();
        }
    }

    private void handleFail() {
        score = Math.max(0, score - 1);
        updateProgressText();

        rotationSpeed = Math.max(BASE_SPEED, rotationSpeed - SPEED_STEP);

        centerBg.setFill(Color.DARKRED);
        keyText.setText("!");
        waitingForPress = false;

        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ex) {}
            Platform.runLater(() -> {
                centerBg.setFill(Color.rgb(38, 50, 56));
                keyText.setFill(Color.WHITE);
                startNewRound();
            });
        }).start();
    }

    private boolean isPointerInGreen(double currentAngle) {
        double normCurrent = currentAngle % 360;
        if (normCurrent < 0) normCurrent += 360;

        double start = greenZone.getStartAngle() % 360;
        if (start < 0) start += 360;

        double length = greenZone.getLength();
        double end = (start + length) % 360;

        if (start < end) {
            return normCurrent >= start && normCurrent <= end;
        } else {
            return normCurrent >= start || normCurrent <= end;
        }
    }

    private void updateProgressText() {
        progressText.setText(score + " / " + TOTAL_POINTS);
    }
}