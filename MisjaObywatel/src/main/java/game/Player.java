package game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player {
    private ImageView sprite;
    private double speed = 5; // prędkość ruchu

    public Player(Image image, double startX, double startY) {
        sprite = new ImageView(image);
        sprite.setX(startX);
        sprite.setY(startY);
        sprite.setFitWidth(50);  // szerokość postaci
        sprite.setFitHeight(50); // wysokość postaci
    }

    public ImageView getSprite() {
        return sprite;
    }

    public void moveUp() {
        sprite.setY(sprite.getY() - speed);
    }

    public void moveDown() {
        sprite.setY(sprite.getY() + speed);
    }

    public void moveLeft() {
        sprite.setX(sprite.getX() - speed);
    }

    public void moveRight() {
        sprite.setX(sprite.getX() + speed);
    }
}
