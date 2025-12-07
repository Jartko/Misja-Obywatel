package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GlosowanieScene {

    public void start(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();

        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.setStyle("-fx-background-color: #2c3e50;");

        Text title = new Text("ETAP 2: GŁOSOWANIE");
        title.setFont(Font.font("Arial", 50));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("(Tu rozpocznie się symulacja głosowania...)");
        subtitle.setFont(Font.font("Arial", 20));
        subtitle.setFill(Color.LIGHTGRAY);
        subtitle.setTranslateY(50);

        root.getChildren().addAll(title, subtitle);

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Głosowanie");
        stage.show();
    }
}