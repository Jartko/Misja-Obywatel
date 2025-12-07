package game.minigames;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UrnaMiniGame {

    private static final double SNAP_DISTANCE = 40;


    private boolean wiekoDone = false;
    private boolean sruba1Done = false;
    private boolean sruba2Done = false;
    private boolean plombaDone = false;

    private final Runnable onComplete;
    private ImageView wieko, sruba1, sruba2, plomba;
    private ImageView wiekoGhost, srubaGhost1, srubaGhost2, plombaGhost;
    private Text instructionText;

    public UrnaMiniGame(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public void start(Stage stage) {

        Pane root = new Pane();
        double width = stage.getWidth();
        double height = stage.getHeight();
        root.setPrefSize(width, height);

        // --- TŁO ---
        ImageView background = new ImageView(new Image("/images/Urna.png"));
        background.setFitWidth(width);
        background.setFitHeight(height);
        root.getChildren().add(background);

        // --- Ghost WIEKO ---
        wiekoGhost = new ImageView(new Image("/images/Wieko.png")); // Przypisanie do pola
        wiekoGhost.setFitWidth(780);
        wiekoGhost.setFitHeight(570);
        wiekoGhost.setX(95);
        wiekoGhost.setY(-100);
        wiekoGhost.setOpacity(0.35);
        root.getChildren().add(wiekoGhost);

        // --- WIEKO ---
        wieko = new ImageView(new Image("/images/Wieko.png")); // Przypisanie do pola
        wieko.setFitWidth(780);
        wieko.setFitHeight(570);
        wieko.setX(450);
        wieko.setY(100);
        enableDrag(wieko);
        root.getChildren().add(wieko);

        // ghost śruby 1
        srubaGhost1 = new ImageView(new Image("/images/śruba.png")); // Przypisanie do pola
        srubaGhost1.setFitWidth(70);
        srubaGhost1.setFitHeight(70);
        srubaGhost1.setX(200);
        srubaGhost1.setY(250);
        srubaGhost1.setOpacity(0.35);
        srubaGhost1.setVisible(false);
        root.getChildren().add(srubaGhost1);

        // ghost śruby 2
        srubaGhost2 = new ImageView(new Image("/images/śruba.png")); // Przypisanie do pola
        srubaGhost2.setFitWidth(70);
        srubaGhost2.setFitHeight(70);
        srubaGhost2.setX(450);
        srubaGhost2.setY(300);
        srubaGhost2.setOpacity(0.35);
        srubaGhost2.setVisible(false);
        root.getChildren().add(srubaGhost2);

        // --- GHOST PLOMBA  ---
        plombaGhost = new ImageView(new Image("/images/plomba.png"));
        plombaGhost.setFitWidth(140);
        plombaGhost.setFitHeight(120);
        plombaGhost.setRotate(-90);
        plombaGhost.setX(300);
        plombaGhost.setY(300);
        plombaGhost.setOpacity(0.35);
        plombaGhost.setVisible(false);
        root.getChildren().add(plombaGhost);

        // --- ŚRUBY ---
        sruba1 = new ImageView(new Image("/images/śruba.png"));
        sruba1.setFitWidth(70);
        sruba1.setFitHeight(70);
        sruba1.setX(50);
        sruba1.setY(height - 150);
        enableDrag(sruba1);
        root.getChildren().add(sruba1);

        sruba2 = new ImageView(new Image("/images/śruba.png"));
        sruba2.setFitWidth(70);
        sruba2.setFitHeight(70);
        sruba2.setX(width - 150);
        sruba2.setY(height - 150);
        enableDrag(sruba2);
        root.getChildren().add(sruba2);

        // --- PLOMBA ---
        plomba = new ImageView(new Image("/images/plomba.png"));
        plomba.setFitWidth(140);
        plomba.setFitHeight(120);
        plomba.setX(width - 150);
        plomba.setY(height - 150);
        plomba.setRotate(-90);
        enableDrag(plomba);
        root.getChildren().add(plomba);

        // --- TEKST ---
        instructionText = new Text("KROK 1: Nałóż wieko na urnę");
        instructionText.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        instructionText.setFill(Color.WHITE);
        instructionText.setStroke(Color.BLACK);
        instructionText.setStrokeWidth(1.5);
        instructionText.setTextAlignment(TextAlignment.CENTER);

        instructionText.setEffect(new DropShadow(5, Color.BLACK));

        instructionText.setX(20);
        instructionText.setY(50);

        root.getChildren().add(instructionText);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Minigra - Przygotowanie Urny");
        stage.show();

        //--- LOGIKA UKOŃCZENIA ---
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {

            if (!wiekoDone && distance(wieko, wiekoGhost) < SNAP_DISTANCE) {
                snapTo(wieko, wiekoGhost);
                wiekoDone = true;
                wiekoGhost.setVisible(false);
                srubaGhost1.setVisible(true);
                srubaGhost2.setVisible(true);

                instructionText.setText("KROK 2: Wkręć śruby zabezpieczające");
                instructionText.setFill(Color.YELLOW); // Zmiana koloru na kolejny etap
            }

            if (wiekoDone) {
                if (!sruba1Done && distance(sruba1, srubaGhost1) < SNAP_DISTANCE) {
                    snapTo(sruba1, srubaGhost1);
                    sruba1Done = true;
                    srubaGhost1.setVisible(false);
                    animateScrew(sruba1);
                }
                if (!sruba2Done && distance(sruba2, srubaGhost2) < SNAP_DISTANCE) {
                    snapTo(sruba2, srubaGhost2);
                    sruba2Done = true;
                    srubaGhost2.setVisible(false);
                    animateScrew(sruba2);
                }

                if (sruba1Done && sruba2Done && !plombaDone) {
                    instructionText.setText("KROK 3: Zaplombuj urnę");
                    instructionText.setFill(Color.ORANGE);
                }
            }
            if (sruba1Done && sruba2Done) {
                if (!plombaDone && !plombaGhost.isVisible()) {
                    plombaGhost.setVisible(true);
                }

                if (!plombaDone && distance(plomba, plombaGhost) < SNAP_DISTANCE) {
                    snapTo(plomba, plombaGhost);
                    plombaDone = true;
                    plombaGhost.setVisible(false);
                    animateStamp(plomba);
                }
            }

            // --- SPRAWDZENIE KOŃCA ---
            if (wiekoDone && sruba1Done && sruba2Done && plombaDone) {
                instructionText.setText("GOTOWE!");
                instructionText.setFill(Color.LIGHTGREEN);

                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(ev -> onComplete.run());
                delay.play();
            }
        });
    }

    private void enableDrag(ImageView iv) {
        final double[] offset = new double[2];

        iv.setOnMousePressed(e -> {
            if (iv == sruba1 || iv == sruba2) {
                if (!wiekoDone) return;
            }
            if (iv == plomba) {
                if (!sruba1Done || !sruba2Done) return;
            }

            offset[0] = e.getSceneX() - iv.getX();
            offset[1] = e.getSceneY() - iv.getY();
        });

        iv.setOnMouseDragged(e -> {
            if (iv == sruba1 || iv == sruba2) {
                if (!wiekoDone) return;
            }
            if (iv == plomba) {
                if (!sruba1Done || !sruba2Done) return;
            }

            iv.setX(e.getSceneX() - offset[0]);
            iv.setY(e.getSceneY() - offset[1]);
        });
    }

    private double distance(ImageView a, ImageView b) {
        double ax = a.getX() + a.getFitWidth() / 2;
        double ay = a.getY() + a.getFitHeight() / 2;
        double bx = b.getX() + b.getFitWidth() / 2;
        double by = b.getY() + b.getFitHeight() / 2;
        return Math.hypot(ax - bx, ay - by);
    }

    private void snapTo(ImageView item, ImageView target) {
        item.setX(target.getX());
        item.setY(target.getY());
        item.setOpacity(1.0);
        item.setMouseTransparent(true);
    }

    // --- Animacje ---

    private void animateScrew(ImageView screw) {
        RotateTransition rt = new RotateTransition(Duration.seconds(0.5), screw);
        rt.setByAngle(360 * 2);
        rt.play();
    }

    private void animateStamp(ImageView stamp) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), stamp);
        st.setFromX(1.5); st.setFromY(1.5);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
    }
}