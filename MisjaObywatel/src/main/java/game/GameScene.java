package game;

import game.minigames.UrnaMiniGame;
import game.minigames.KabinaMiniGame;
import game.minigames.TablicaMiniGame;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

public class GameScene {

    private Pane root;
    private Player player;
    private Scene gameScene;
    private Stage primaryStage;
    private AnimationTimer timer;

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final List<Rectangle> colliders = new ArrayList<>();
    private double speed = 2;

    private boolean miniGameRunning = false;
    private boolean ePressed = false;

    // --- LOGIKA UKOŃCZENIA ---
    private boolean urnaCompleted = false;
    private boolean urnaHighlighted = false;
    private boolean tablicaHighlighted = false;
    private boolean tablicaCompleted = false;
    private boolean gameFinished = false;

    // --- PAUZA I MENU ---
    private boolean isPaused = false;
    private VBox pauseMenu;
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;

    // --- TIMER I PUNKTY ---
    private Text timerText;
    private long startTime = 0;
    private double elapsedTime = 0;
    private int finalScore = 0;

    private final int MAX_POINTS = 1000;
    private final int MIN_POINTS = 0;
    private final double TIME_FOR_MAX = 45.0;
    private final double TIME_FOR_MIN = 120.0;

    // --- ZMIENNE DO DIALOGU ---
    private boolean dialogueActive = true;
    private boolean isEndingDialogue = false;
    private int dialogueIndex = 0;
    private Pane dialogueBox;
    private Text dialogueText;

    private final String[] introLines = {
            "Witaj w lokalu wyborczym, młodzieńcze!",
            "Jestem Przewodniczącym Komisji. Mamy mało czasu.",
            "Lokal musi być gotowy przed przyjściem wyborców.",
            "Twoje zadania: Złóż urnę, przygotuj kabiny i powieś ogłoszenia.",
            "Do roboty! Pośpiesz się (Czas startuje po tym dialogu!)"
    };

    private final String[] endingLinesTemplate = {
            "Świetna robota! Lokal wygląda profesjonalnie.",
            "Urna stoi, kabiny gotowe, ogłoszenia wiszą.",
            "Właśnie otwieramy drzwi dla wyborców.",
            "SCORE_PLACEHOLDER"
    };
    private List<String> currentDialogueLines;

    // --- Kabiny ---
    private class Kabina {
        ImageView view;
        Rectangle collider;
        boolean done = false;
        boolean highlighted = false;

        Kabina(double x, double y) {
            view = new ImageView(new Image("images/kabina_niegotowa.png"));
            view.setFitWidth(180);
            view.setFitHeight(180);
            view.setX(x);
            view.setY(y);
            view.setOpacity(0.85);
            root.getChildren().add(view);
            collider = new Rectangle(x+50, y+50, 60,60 );
            collider.setOpacity(0);
            colliders.add(collider);
            root.getChildren().add(collider);
        }
    }
    private List<Kabina> kabiny;

    public GameScene() {
        currentDialogueLines = new ArrayList<>(Arrays.asList(introLines));
    }

    public void start(Stage stage) {
        this.primaryStage = stage;

        double width = stage.getWidth();
        double height = stage.getHeight();

        // --- ROOT & SCENE ---
        root = new Pane();
        root.setPrefSize(width, height);
        gameScene = new Scene(root, width, height);

        // --- Background ---
        try {
            Image background = new Image(getClass().getResourceAsStream("/images/room.png"));
            ImageView bgView = new ImageView(background);
            bgView.setFitWidth(width);
            bgView.setFitHeight(height);
            root.getChildren().add(bgView);
        } catch (Exception e) {}

        // --- PLAYER ---
        Image playerImg = new Image(getClass().getResourceAsStream("/images/player.png"));
        player = new Player(playerImg, width / 2, height / 2);
        root.getChildren().add(player.getSprite());

        // --- PRZEWODNICZĄCY KOMISJI ---
        ImageView przewodniczacy = new ImageView();
        try {
            przewodniczacy.setImage(new Image("images/przewodniczacy.png"));
        } catch (Exception e) {}
        przewodniczacy.setFitWidth(150);
        przewodniczacy.setFitHeight(120);
        przewodniczacy.setX(520);
        przewodniczacy.setY(500);
        root.getChildren().add(przewodniczacy);

        Rectangle przewodniczacyCollider = new Rectangle(570, 520, 30, 50);
        przewodniczacyCollider.setOpacity(0);
        colliders.add(przewodniczacyCollider);
        root.getChildren().add(przewodniczacyCollider);

        // --- URNA ---
        ImageView urna = new ImageView(new Image("images/urna_bezwieka.png"));
        urna.setFitWidth(150);
        urna.setFitHeight(150);
        urna.setX(100);
        urna.setY(350);
        root.getChildren().add(urna);

        Rectangle urnaCollider = new Rectangle(100, 380, 100, 60);
        urnaCollider.setOpacity(0);
        colliders.add(urnaCollider);
        root.getChildren().add(urnaCollider);

        // --- KABINY ---
        Kabina kabina1 = new Kabina(250, 50);
        Kabina kabina2 = new Kabina(450, 50);
        Kabina kabina3 = new Kabina(650, 50);
        kabiny = List.of(kabina1, kabina2, kabina3);

        // --- TABLICA OGŁOSZEŃ ---
        ImageView tablica = new ImageView(new Image("images/tablica_niegotowa.png"));
        tablica.setFitWidth(200);
        tablica.setFitHeight(200);
        tablica.setX(770);
        tablica.setY(200);
        root.getChildren().add(tablica);
        Rectangle tablicaCollider = new Rectangle(tablica.getX()+50, tablica.getY(), 100, 150);
        tablicaCollider.setOpacity(0);
        colliders.add(tablicaCollider);

        // --- STÓŁ ---
        ImageView stol = new ImageView(new Image("images/stół.png"));
        stol.setFitWidth(350);
        stol.setFitHeight(200);
        stol.setX(600);
        stol.setY(540);
        root.getChildren().add(stol);
        Rectangle stolCollider = new Rectangle(stol.getX()+50, stol.getY()+50, 300, 200);
        stolCollider.setOpacity(0);
        colliders.add(stolCollider);

        // --- Krzesła ---
        ImageView krzesloStol = new ImageView(new Image("images/krzesło.png"));
        krzesloStol.setFitWidth(120);
        krzesloStol.setFitHeight(120);
        krzesloStol.setScaleX(-1);
        krzesloStol.setX(710);
        krzesloStol.setY(600);
        root.getChildren().add(krzesloStol);
        ImageView krzesloStol1 = new ImageView(new Image("images/krzesło.png"));
        krzesloStol1.setFitWidth(120);
        krzesloStol1.setFitHeight(120);
        krzesloStol1.setScaleX(-1);
        krzesloStol1.setX(810);
        krzesloStol1.setY(600);
        root.getChildren().add(krzesloStol1);

        Rectangle krzesloStolCollider = new Rectangle(krzesloStol.getX() + 20, krzesloStol.getY() + 20, 80, 80);
        krzesloStolCollider.setOpacity(0);
        colliders.add(krzesloStolCollider);
        root.getChildren().add(krzesloStolCollider);

        // --- WALL COLLIDERS ---
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

        // --- TIMER UI ---
        timerText = new Text("00:00");
        timerText.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        timerText.setFill(Color.WHITE);
        timerText.setStroke(Color.BLACK);
        timerText.setStrokeWidth(1.5);
        timerText.setX(width - 120);
        timerText.setY(35);
        root.getChildren().add(timerText);

        // --- SYSTEM DIALOGÓW (UI) ---
        createDialogueBox(width, height);
        root.getChildren().add(dialogueBox);

        // --- PAUSE MENU (UI) ---
        createPauseMenu(width, height, stage);
        root.getChildren().add(pauseMenu);

        // --- EVENT HANDLING ---
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (!miniGameRunning && !gameFinished && !dialogueActive) {
                    togglePause();
                }
                return;
            }

            if (isPaused) return;

            if (dialogueActive) {
                if (e.getCode() == KeyCode.F) advanceDialogue();
                else if (e.getCode() == KeyCode.K) skipDialogue();
                return;
            }

            pressedKeys.add(e.getCode());
            if (e.getCode() == KeyCode.E) ePressed = true;
        });

        gameScene.setOnKeyReleased(e -> {
            pressedKeys.remove(e.getCode());
            if (e.getCode() == KeyCode.E) ePressed = false;
        });

        // --- GAME LOOP ---
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused) {
                    if (pauseStartTime == 0) pauseStartTime = now;
                    return;
                } else {
                    if (pauseStartTime != 0) {
                        totalPausedTime += (now - pauseStartTime);
                        pauseStartTime = 0;
                    }
                }

                if (dialogueActive) return;

                if (startTime == 0) startTime = now;

                if (!miniGameRunning && !gameFinished) {
                    elapsedTime = (now - startTime - totalPausedTime) / 1_000_000_000.0;
                    updateTimerUI(elapsedTime);
                    checkGameCompletion();
                }

                if (miniGameRunning) return;

                updateMovement();

                // --- URNA ---
                if (isNear(player.getSprite(), urna, 120) && !urnaCompleted) {
                    if (!urnaHighlighted) {
                        urna.setStyle("-fx-effect: dropshadow(gaussian, yellow, 20, 0.5, 0, 0)");
                        urnaHighlighted = true;
                    }
                    if (!miniGameRunning && ePressed) {
                        miniGameRunning = true;
                        ePressed = false;
                        new UrnaMiniGame(() -> {
                            miniGameRunning = false;
                            urnaCompleted = true;
                            urna.setImage(new Image("images/urna_gotowa.png"));
                            urna.setStyle("-fx-effect: none;");
                            stage.setScene(gameScene);
                            root.requestFocus();
                            checkGameCompletion();
                        }).start(stage);
                    }
                } else if (urnaHighlighted && !urnaCompleted) {
                    urna.setStyle("-fx-effect: none;");
                    urnaHighlighted = false;
                }

                // --- KABINY ---
                for (Kabina k : kabiny) {
                    if (k.done) continue;
                    boolean near = isNear(player.getSprite(), k.view, 120);
                    if (near) {
                        if (!k.highlighted) {
                            k.view.setOpacity(1.0);
                            k.view.setStyle("-fx-effect: dropshadow(gaussian, yellow, 20, 0.5, 0, 0)");
                            k.highlighted = true;
                        }
                        if (!miniGameRunning && ePressed) {
                            miniGameRunning = true;
                            ePressed = false;
                            new KabinaMiniGame(() -> {
                                miniGameRunning = false;
                                k.done = true;
                                k.view.setImage(new Image("images/kabina_gotowa.png"));
                                k.view.setY(k.view.getY()-30);
                                k.view.setOpacity(1.0);
                                k.view.setStyle("-fx-effect: none;");
                                stage.setScene(gameScene);
                                root.requestFocus();
                            }).start(stage);
                        }
                    } else if (k.highlighted) {
                        k.view.setOpacity(0.85);
                        k.view.setStyle("-fx-effect: none;");
                        k.highlighted = false;
                    }
                }

                // --- TABLICA ---
                if (!tablicaCompleted && isNear(player.getSprite(), tablica, 120)) {
                    if (!tablicaHighlighted) {
                        tablica.setStyle("-fx-effect: dropshadow(gaussian, yellow, 20, 0.5, 0, 0);");
                        tablicaHighlighted = true;
                    }
                    if (!miniGameRunning && ePressed) {
                        miniGameRunning = true;
                        ePressed = false;
                        new TablicaMiniGame(() -> {
                            miniGameRunning = false;
                            tablicaCompleted = true;
                            tablica.setImage(new Image("images/tablica_gotowa.png"));
                            tablica.setStyle("-fx-effect: none;");
                            stage.setScene(gameScene);
                            root.requestFocus();
                        }).start(stage);
                    }
                } else {
                    if (tablicaHighlighted) {
                        tablica.setStyle("-fx-effect: none;");
                        tablicaHighlighted = false;
                    }
                }
            }
        };
        timer.start();

        stage.setScene(gameScene);
        stage.setTitle("Misja Obywatel – Lokal wyborczy");
        stage.show();
        root.requestFocus();
    }

    // --- MENU PAUZY ---
    private void createPauseMenu(double width, double height, Stage stage) {
        pauseMenu = new VBox(20);
        pauseMenu.setPrefSize(width, height);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        pauseMenu.setVisible(false);

        Text pauseTitle = new Text("PAUZA");
        pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        pauseTitle.setFill(Color.WHITE);
        pauseTitle.setStroke(Color.BLACK);
        pauseTitle.setStrokeWidth(2);

        String btnStyle = "-fx-background-color: #ffffff; -fx-text-fill: black; -fx-font-size: 20px; -fx-padding: 10 20 10 20; -fx-cursor: hand;";

        Button resumeBtn = new Button("Powrót do gry");
        resumeBtn.setStyle(btnStyle);
        resumeBtn.setOnAction(e -> togglePause());

        Button restartBtn = new Button("Resetuj poziom");
        restartBtn.setStyle(btnStyle);
        restartBtn.setOnAction(e -> {
            if (timer != null) timer.stop();
            new GameScene().start(stage);
        });

        Button exitBtn = new Button("Wyjście do menu");
        exitBtn.setStyle(btnStyle);
        exitBtn.setOnAction(e -> {
            System.out.println("Wyjście do menu...");
            Platform.exit();
        });

        pauseMenu.getChildren().addAll(pauseTitle, resumeBtn, restartBtn, exitBtn);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseMenu.setVisible(isPaused);
    }


    // --- LOGIKA PUNKTACJI I KOŃCA ---
    private void updateTimerUI(double seconds) {
        int mm = (int) (seconds / 60);
        int ss = (int) (seconds % 60);
        timerText.setText(String.format("%02d:%02d", mm, ss));
    }

    private void checkGameCompletion() {
        if (gameFinished) return;
        boolean allKabiny = true;
        for (Kabina k : kabiny) {
            if (!k.done) {
                allKabiny = false;
                break;
            }
        }

        if (urnaCompleted && tablicaCompleted && allKabiny) {
            gameFinished = true;
            calculateFinalScore();
            triggerEndingDialogue();
        }
    }

    private void calculateFinalScore() {
        if (elapsedTime <= TIME_FOR_MAX) {
            finalScore = MAX_POINTS;
        } else if (elapsedTime >= TIME_FOR_MIN) {
            finalScore = MIN_POINTS;
        } else {
            double progress = (elapsedTime - TIME_FOR_MAX) / (TIME_FOR_MIN - TIME_FOR_MAX);
            finalScore = (int) (MAX_POINTS - (progress * (MAX_POINTS - MIN_POINTS)));
        }

        timerText.setFill(Color.LIGHTGREEN);
    }

    private void triggerEndingDialogue() {
        isEndingDialogue = true;
        currentDialogueLines = new ArrayList<>(Arrays.asList(endingLinesTemplate));
        currentDialogueLines.set(currentDialogueLines.size() - 1, "Twój wynik: " + finalScore + " pkt. Przechodzimy do głosowania!");

        dialogueIndex = 0;
        dialogueText.setText(currentDialogueLines.get(0));
        dialogueActive = true;
        dialogueBox.setVisible(true);
    }

    // --- SYSTEM DIALOGÓW ---
    private void createDialogueBox(double width, double height) {
        dialogueBox = new StackPane();
        dialogueBox.setPrefSize(width - 100, 150);
        dialogueBox.setLayoutX(50);
        dialogueBox.setLayoutY(height - 180);

        Rectangle bg = new Rectangle(width - 100, 150);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Color.color(0, 0, 0, 0.8));
        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(3);

        dialogueText = new Text(currentDialogueLines.get(0));
        dialogueText.setFont(Font.font("Arial", 24));
        dialogueText.setFill(Color.WHITE);
        dialogueText.setTextAlignment(TextAlignment.CENTER);
        dialogueText.setWrappingWidth(width - 140);

        Text skipText = new Text("[F] Dalej  [K] Pomiń wszystko");
        skipText.setFont(Font.font("Consolas", 14));
        skipText.setFill(Color.YELLOW);
        StackPane.setAlignment(skipText, javafx.geometry.Pos.BOTTOM_RIGHT);
        skipText.setTranslateX(-20);
        skipText.setTranslateY(-15);

        ((StackPane) dialogueBox).getChildren().addAll(bg, dialogueText, skipText);
        dialogueBox.setVisible(true);
    }

    private void advanceDialogue() {
        dialogueIndex++;
        if (dialogueIndex < currentDialogueLines.size()) {
            dialogueText.setText(currentDialogueLines.get(dialogueIndex));
        } else {
            if (isEndingDialogue) {
                goToGlosowanieScene();
            } else {
                closeDialogue();
            }
        }
    }

    private void skipDialogue() {
        if (isEndingDialogue) {
            goToGlosowanieScene();
        } else {
            closeDialogue();
        }
    }

    private void closeDialogue() {
        dialogueActive = false;
        dialogueBox.setVisible(false);
        pressedKeys.remove(KeyCode.F);
        pressedKeys.remove(KeyCode.K);
    }
    private void goToGlosowanieScene() {
        if (timer != null) timer.stop();
        new GlosowanieScene().start(primaryStage);
    }

    // --- MOVEMENT ---
    private void updateMovement() {
        double dx = 0, dy = 0;

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

    // --- COLLISION ---
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