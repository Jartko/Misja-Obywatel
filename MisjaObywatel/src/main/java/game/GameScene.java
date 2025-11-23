package game;

import game.minigames.UrnaMiniGame;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScene {

    private Pane root;
    private Player player;
    private double speed = 2;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private final List<Rectangle> colliders = new ArrayList<>();
    private boolean miniGameRunning = false;
    private Scene gameScene;
    private boolean ePressed = false;
    private boolean urnaCompleted = false;
    public void start(Stage stage) {

        double width = stage.getWidth();
        double height = stage.getHeight();

        // --- Root i scena ---
        root = new Pane();
        root.setPrefSize(width, height);
        gameScene = new Scene(root, width, height);

        // --- Tło ---
        Image background = new Image(getClass().getResourceAsStream("/images/room.png"));
        ImageView bgView = new ImageView(background);
        bgView.setFitWidth(width);
        bgView.setFitHeight(height);
        root.getChildren().add(bgView);

        // --- Gracz ---
        Image playerImg = new Image(getClass().getResourceAsStream("/images/player.png"));
        player = new Player(playerImg, width / 2, height / 2);
        root.getChildren().add(player.getSprite());

        // --- Urna ---
        Image urnaImg = new Image("images/urna_bezwieka.png");
        ImageView urna = new ImageView(urnaImg);
        urna.setFitWidth(120);
        urna.setFitHeight(120);
        urna.setX(100);
        urna.setY(350);
        root.getChildren().add(urna);

        Rectangle urnaCollider = new Rectangle(100, 380, 100, 60);
        urnaCollider.setOpacity(0.0);
        colliders.add(urnaCollider);
        root.getChildren().add(urnaCollider);
        boolean[] urnaHighlighted = {false};

        // --- Ściany ---
        Rectangle wallTop = new Rectangle(0, -20, width, 100);
        Rectangle wallBottom = new Rectangle(0, height - 80, width, 80);
        Rectangle wallLeft = new Rectangle(-20, 0, 100, height);
        Rectangle wallRight = new Rectangle(width - 80, 0, 20, height);
        wallTop.setOpacity(0);
        wallBottom.setOpacity(0);
        wallLeft.setOpacity(0);
        wallRight.setOpacity(0);
        colliders.addAll(List.of(wallTop, wallBottom, wallLeft, wallRight));
        root.getChildren().addAll(wallTop, wallBottom, wallLeft, wallRight);

        // --- Eventy ---
        gameScene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        gameScene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
        gameScene.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());
            if (e.getCode() == KeyCode.E) ePressed = true;
        });

        gameScene.setOnKeyReleased(e -> {
            pressedKeys.remove(e.getCode());
            if (e.getCode() == KeyCode.E) ePressed = false;
        });
        // --- Animacja ---
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateMovement();

                // --- Podświetlenie urny ---
                if (isNear(player.getSprite(), urna, 120)) {
                    if (!urnaHighlighted[0] && !urnaCompleted) {
                        urna.setStyle("-fx-effect: dropshadow(gaussian, yellow, 20, 0.5, 0, 0);");
                        urnaHighlighted[0] = true;
                    }

                    // --- Wywołanie minigierki Urny ---
                    if (!miniGameRunning && ePressed && !urnaCompleted) {
                        miniGameRunning = true;
                        ePressed = false;
                        System.out.println("Minigierka uruchomiona!");
                        new UrnaMiniGame(() -> {
                            miniGameRunning = false;
                            urnaCompleted = true;
                            urna.setImage(new Image("images/urna_gotowa.png"));
                            urna.setStyle("-fx-effect: none;");
                            stage.setScene(gameScene);
                            root.requestFocus();
                            System.out.println("Gracz wraca do Gamescene!");
                        }).start(stage);
                    }

                } else {
                    if (urnaHighlighted[0]) {
                        urna.setStyle("-fx-effect: none;");
                        urnaHighlighted[0] = false;
                    }
                }
            }
        };
        timer.start();

        // --- Ustawienie sceny ---
        stage.setScene(gameScene);
        stage.setTitle("Misja Obywatel – Lokal wyborczy");
        stage.show();

        root.requestFocus();
    }

    // --- Ruch ---
    private void updateMovement() {
        double dx = 0;
        double dy = 0;

        if (pressedKeys.contains(KeyCode.W)) dy -= 1;
        if (pressedKeys.contains(KeyCode.S)) dy += 1;
        if (pressedKeys.contains(KeyCode.A)) dx -= 1;
        if (pressedKeys.contains(KeyCode.D)) dx += 1;

        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx * dx + dy * dy);
            dx = (dx / len) * speed;
            dy = (dy / len) * speed;
        }

        moveWithCollision(dx, dy);
    }

    // --- Kolizje ---
    private void moveWithCollision(double dx, double dy) {
        ImageView sprite = player.getSprite();
        double oldX = sprite.getX();
        double oldY = sprite.getY();

        sprite.setX(oldX + dx);
        if (isColliding(sprite)) sprite.setX(oldX);

        sprite.setY(oldY + dy);
        if (isColliding(sprite)) sprite.setY(oldY);
    }

    private boolean isColliding(ImageView sprite) {
        for (Rectangle r : colliders) {
            if (sprite.getBoundsInParent().intersects(r.getBoundsInParent())) return true;
        }
        return false;
    }

    private boolean isNear(ImageView player, ImageView obj, double distance) {
        double px = player.getX() + player.getFitWidth() / 2;
        double py = player.getY() + player.getFitHeight() / 2;
        double ox = obj.getX() + obj.getFitWidth() / 2;
        double oy = obj.getY() + obj.getFitHeight() / 2;
        return Math.hypot(px - ox, py - oy) < distance;
    }
}
