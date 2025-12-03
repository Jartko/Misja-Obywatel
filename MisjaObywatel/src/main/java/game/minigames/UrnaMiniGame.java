package game.minigames;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class UrnaMiniGame {

    private static final double SNAP_DISTANCE = 40;
    private boolean wiekoDone = false;
    private boolean sruba1Done = false;
    private boolean sruba2Done = false;
    private final Runnable onComplete;
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
        ImageView wiekoGhost = new ImageView(new Image("/images/Wieko.png"));
        wiekoGhost.setFitWidth(780);
        wiekoGhost.setFitHeight(570);
        wiekoGhost.setX(95);
        wiekoGhost.setY(-100);
        wiekoGhost.setOpacity(0.35);
        root.getChildren().add(wiekoGhost);

        // --- WIEKO ---
        ImageView wieko = new ImageView(new Image("/images/Wieko.png"));
        wieko.setFitWidth(780);
        wieko.setFitHeight(570);
        wieko.setX(450);
        wieko.setY(100);
        enableDrag(wieko);
        root.getChildren().add(wieko);

        // ghost śruby 1
        ImageView srubaGhost1 = new ImageView(new Image("/images/śruba.png"));
        srubaGhost1.setFitWidth(70);
        srubaGhost1.setFitHeight(70);
        srubaGhost1.setX(200);
        srubaGhost1.setY(250);
        srubaGhost1.setOpacity(0.35);
        root.getChildren().add(srubaGhost1);

        // ghost śruby 2
        ImageView srubaGhost2 = new ImageView(new Image("/images/śruba.png"));
        srubaGhost2.setFitWidth(70);
        srubaGhost2.setFitHeight(70);
        srubaGhost2.setX(450);
        srubaGhost2.setY(300);
        srubaGhost2.setOpacity(0.35);
        root.getChildren().add(srubaGhost2);


        // --- ŚRUBA 1 ---
        ImageView sruba1 = new ImageView(new Image("/images/śruba.png"));
        sruba1.setFitWidth(70);
        sruba1.setFitHeight(70);
        sruba1.setX(50);
        sruba1.setY(height - 150);
        enableDrag(sruba1);
        root.getChildren().add(sruba1);

        // --- ŚRUBA 2 ---
        ImageView sruba2 = new ImageView(new Image("/images/śruba.png"));
        sruba2.setFitWidth(70);
        sruba2.setFitHeight(70);
        sruba2.setX(width - 150);
        sruba2.setY(height - 150);
        enableDrag(sruba2);
        root.getChildren().add(sruba2);

        // --- ghost Plomba ---
        ImageView plombaGhost = new ImageView(new Image("/images/plomba.png"));
        plombaGhost.setFitWidth(70);
        plombaGhost.setFitHeight(70);
        plombaGhost.setX(50);
        plombaGhost.setY(150);
        plombaGhost.setOpacity(0.35);
        root.getChildren().add(plombaGhost);

        // --- Plomba ---
        ImageView plomba = new ImageView(new Image("/images/plomba.png"));
        plomba.setFitWidth(70);
        plomba.setFitHeight(70);
        plomba.setX(width - 150);
        plomba.setY(height - 150);
        enableDrag(plomba);
        root.getChildren().add(plomba);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Minigra - Przygotowanie Urny");
        stage.show();


        //--- Blokowanie gdy obiekt na miejscu ---
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e ->{

            if (!wiekoDone && distance(wieko, wiekoGhost) < SNAP_DISTANCE) {
                snapTo(wieko, wiekoGhost);
                wiekoDone = true;
            }
            if (!sruba1Done && distance(sruba1, srubaGhost1) < SNAP_DISTANCE) {
                snapTo(sruba1, srubaGhost1);
                sruba1Done = true;
            }
            if (!sruba2Done && distance(sruba2, srubaGhost2) < SNAP_DISTANCE) {
                snapTo(sruba2, srubaGhost2);
                sruba2Done = true;
            }
            if (wiekoDone && sruba1Done && sruba2Done) {

                onComplete.run();
                System.out.println("Minigierka ukończona!");
            }
        });
    }
    private void enableDrag(ImageView iv) {
        final double[] offset = new double[2];

        iv.setOnMousePressed(e -> {
            offset[0] = e.getSceneX() - iv.getX();
            offset[1] = e.getSceneY() - iv.getY();
        });

        iv.setOnMouseDragged(e -> {
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
}


