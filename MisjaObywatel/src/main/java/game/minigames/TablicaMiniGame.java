package game.minigames;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TablicaMiniGame {

    private final Runnable onComplete;
    private final List<ImageView> posters = new ArrayList<>();
    private boolean isGameActive = true;

    public TablicaMiniGame(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public void start(Stage stage) {
        // Ustawiamy dość duże okno, żeby pomieścić oryginalne proporcje
        double targetWidth = 1024;
        double targetHeight = 768;

        if (stage.getWidth() < targetWidth) stage.setWidth(targetWidth);
        if (stage.getHeight() < targetHeight) stage.setHeight(targetHeight);

        double width = stage.getWidth();
        double height = stage.getHeight();

        Pane root = new Pane();
        root.setPrefSize(width, height);
        root.setStyle("-fx-background-color: #31acfe;");

        // --- Instrukcja ---
        Label instruction = new Label("Legenda:\nCZERWONY - Nachodzi na inne (rozsuń!)\nŻÓŁTY - Zła rotacja (kliknij!)\nBEZ KOLORU - Jest OK");
        instruction.setTextFill(Color.WHITE);
        instruction.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        instruction.setTextAlignment(TextAlignment.LEFT);
        instruction.setLayoutX(10);
        instruction.setLayoutY(10);
        instruction.setMouseTransparent(true);
        instruction.setEffect(new DropShadow(2, Color.BLACK));
        root.getChildren().add(instruction);

        // --- Pliki ---
        String[] imagePaths = {
                "/images/obwieszczenie_sklad.jpg",
                "/images/obwieszczenie_sposob.png",
                "/images/obwieszczenie_kandydaci.jpg",
                "/images/obwieszczenie_lokale.jpg",
                "/images/obwieszczenie_granice.jpg"
        };

        Random random = new Random();

        // Granice losowania pozycji
        double margin = 50;
        double maxWidth = width - 250;
        double maxHeight = height - 250;

        for (String path : imagePaths) {
            Image img = new Image(path);
            ImageView poster = new ImageView(img);

            poster.setPreserveRatio(true);
            poster.setSmooth(true);

            poster.setFitWidth(250);

            poster.setLayoutX(margin + random.nextDouble() * (maxWidth - margin));
            poster.setLayoutY(margin + random.nextDouble() * (maxHeight - margin));
            int[] rotations = {90, 180, 270};
            poster.setRotate(rotations[random.nextInt(rotations.length)]);

            addInteractions(poster);
            posters.add(poster);
            root.getChildren().add(poster);
        }

        updateVisuals();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Minigra - Porządkowanie Tablicy");
        stage.show();
    }

    private void addInteractions(ImageView node) {
        final class Delta { double x, y, startX, startY; }
        final Delta dragDelta = new Delta();

        node.setOnMousePressed(e -> {
            if (!isGameActive) return;
            node.toFront();
            dragDelta.x = node.getLayoutX() - e.getSceneX();
            dragDelta.y = node.getLayoutY() - e.getSceneY();
            dragDelta.startX = e.getSceneX();
            dragDelta.startY = e.getSceneY();
        });

        node.setOnMouseDragged(e -> {
            if (!isGameActive) return;
            node.setLayoutX(e.getSceneX() + dragDelta.x);
            node.setLayoutY(e.getSceneY() + dragDelta.y);
            updateVisuals();
        });

        node.setOnMouseReleased(e -> {
            if (!isGameActive) return;
            double dist = Math.hypot(e.getSceneX() - dragDelta.startX, e.getSceneY() - dragDelta.startY);

            if (dist < 5) {
                node.setRotate(node.getRotate() + 90);
            }

            updateVisuals();
            checkWinCondition();
        });
    }

    // --- PRIORYTETY ---
    private void updateVisuals() {
        if (!isGameActive) return;

        for (ImageView iv1 : posters) {
            boolean overlaps = false;

            for (ImageView iv2 : posters) {
                if (iv1 == iv2) continue;
                if (iv1.getBoundsInParent().intersects(iv2.getBoundsInParent())) {
                    overlaps = true;
                    break;
                }
            }
            boolean isRotatedWrong = (iv1.getRotate() % 360 != 0);
            if (overlaps) {
                iv1.setEffect(new DropShadow(20, Color.RED));
            } else if (isRotatedWrong) {
                iv1.setEffect(new DropShadow(15, Color.GOLD));
            } else {
                iv1.setEffect(new DropShadow(5, Color.BLACK));
            }
        }
    }

    private void checkWinCondition() {
        boolean allCorrect = true;

        for (int i = 0; i < posters.size(); i++) {
            ImageView iv1 = posters.get(i);

            if (iv1.getRotate() % 360 != 0) {
                allCorrect = false;
                break;
            }

            for (int j = i + 1; j < posters.size(); j++) {
                if (iv1.getBoundsInParent().intersects(posters.get(j).getBoundsInParent())) {
                    allCorrect = false;
                    break;
                }
            }
            if (!allCorrect) break;
        }

        if (allCorrect) {
            isGameActive = false;
            for(ImageView iv : posters) {
                iv.setEffect(new DropShadow(25, Color.LIGHTGREEN));
            }

            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.0));
            delay.setOnFinished(event -> onComplete.run());
            delay.play();
        }
    }
}