package game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class GameScene {
    private Pane root;
    private Player player;
    private double speed = 2;
    private Set<KeyCode> pressedKeys = new HashSet<>();

    public void start(Stage stage) {
        root = new Pane();

        // Pobieramy wymiary okna z Main (Stage)
        double width = stage.getWidth();
        double height = stage.getHeight();
        root.setPrefSize(width, height);

        // Wczytanie spirte gracza
        Image playerImage = new Image(getClass().getResourceAsStream("/images/player.png"));
        player = new Player(playerImage, width / 2, height / 2); // start w centrum
        root.getChildren().add(player.getSprite());

        Scene scene = new Scene(root, width, height);

        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                movePlayer();
            }
        };
        timer.start();

        stage.setScene(scene);
        stage.setTitle("Misja Obywatel - Lokal wyborczy");
        stage.show();

        root.requestFocus();
    }

    private void movePlayer() {
        double dx = 0;
        double dy = 0;

        if (pressedKeys.contains(KeyCode.W)) dy -= 1;
        if (pressedKeys.contains(KeyCode.S)) dy += 1;
        if (pressedKeys.contains(KeyCode.A)) dx -= 1;
        if (pressedKeys.contains(KeyCode.D)) dx += 1;

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx*dx + dy*dy);
            dx = dx / length * speed;
            dy = dy / length * speed;
            player.getSprite().setX(player.getSprite().getX() + dx);
            player.getSprite().setY(player.getSprite().getY() + dy);
        }

        // ograniczenie ruchu do granic sceny
        double width = root.getWidth();
        double height = root.getHeight();
        if (player.getSprite().getX() < 0) player.getSprite().setX(0);
        if (player.getSprite().getY() < 0) player.getSprite().setY(0);
        if (player.getSprite().getX() > width - player.getSprite().getFitWidth())
            player.getSprite().setX(width - player.getSprite().getFitWidth());
        if (player.getSprite().getY() > height - player.getSprite().getFitHeight())
            player.getSprite().setY(height - player.getSprite().getFitHeight());
    }
}
