package game;

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

    public void start(Stage stage) {

        //Pobranie wymiarów okna z Main
        double width = stage.getWidth();
        double height = stage.getHeight();

        root = new Pane();
        root.setPrefSize(width, height);

        //Tło pomieszczenia
        Image background = new Image(getClass().getResourceAsStream("/images/room.png"));
        ImageView bgView = new ImageView(background);
        bgView.setFitWidth(width);
        bgView.setFitHeight(height);
        root.getChildren().add(bgView);

        //Gracz
        Image playerImg = new Image(getClass().getResourceAsStream("/images/player.png"));
        player = new Player(playerImg, width / 2, height / 2);
        root.getChildren().add(player.getSprite());

        //Colliders

        Rectangle wallTop = new Rectangle(0, -20, width, 100);
        Rectangle wallBottom = new Rectangle(0, height-80, width, 80);
        Rectangle wallLeft = new Rectangle(-20, 0, 100, height);
        Rectangle wallRight = new Rectangle(width-80, 0, 20, height);
        wallTop.setOpacity(0);
        wallBottom.setOpacity(0);
        wallLeft.setOpacity(0);
        wallRight.setOpacity(0);
        /*
        wallTop.setOpacity(1);
        wallBottom.setOpacity(1);
        wallLeft.setOpacity(1);
        wallRight.setOpacity(1);
        */

        colliders.add(wallTop);
        colliders.add(wallBottom);
        colliders.add(wallLeft);
        colliders.add(wallRight);

        root.getChildren().addAll(wallTop, wallBottom, wallLeft, wallRight);

        // ====== SCENA ======
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateMovement();
            }
        };
        timer.start();

        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Lokal wyborczy");
        stage.show();

        root.requestFocus();
    }

    // Ruch i weryfikacja kolizji
    private void updateMovement() {

        double dx = 0;
        double dy = 0;

        if (pressedKeys.contains(KeyCode.W)) dy -= 1;
        if (pressedKeys.contains(KeyCode.S)) dy += 1;
        if (pressedKeys.contains(KeyCode.A)) dx -= 1;
        if (pressedKeys.contains(KeyCode.D)) dx += 1;

        //Normalizacja ruchu
        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx*dx + dy*dy);
            dx = (dx / len) * speed;
            dy = (dy / len) * speed;
        }

        moveWithCollision(dx, dy);
    }

    //Kolizje
    private void moveWithCollision(double dx, double dy) {

        ImageView sprite = player.getSprite();

        double oldX = sprite.getX();
        double oldY = sprite.getY();

        // oś X
        sprite.setX(oldX + dx);
        if (isColliding(sprite)) {
            sprite.setX(oldX);
        }

        // oś Y
        sprite.setY(oldY + dy);
        if (isColliding(sprite)) {
            sprite.setY(oldY);
        }
    }

    private boolean isColliding(ImageView sprite) {
        for (Rectangle r : colliders) {
            if (sprite.getBoundsInParent().intersects(r.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }
}
