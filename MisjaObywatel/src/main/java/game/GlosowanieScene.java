package game;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.effect.ColorAdjust;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
public class GlosowanieScene {
    public GlosowanieScene(int scoreStage1) {
        this.scoreStage1 = scoreStage1;
    }
    public static class VoterData {
        public int id;
        public String action;
        public boolean hasCertificate;
        public String personImage;
        public DocumentObj document;
        public RegisterObj register;
        public VoterData() {}
    }
    public static class DocumentObj {
        public String firstName;
        public String lastName;
        public String pesel;
        public String birthDate;
        public String photo;
        public DocumentObj() {}
    }
    public static class RegisterObj {
        public boolean isInRegister;
        public String gminaInfo;
        public String firstName;
        public String lastName;
        public String pesel;
        public String address;
        public String city;
        public boolean hasVoted;

        public RegisterObj() {
        }
    }
    public static class TabletEntry {
        String fullAddress;
        String fullName;
        String pesel;
        boolean hasVoted;
        boolean signedByPlayer;
        VoterData linkedVoter;

        public TabletEntry(String address, String name, String pesel, boolean hasVoted, VoterData v) {
            this.fullAddress = address;
            this.fullName = name;
            this.pesel = pesel;
            this.hasVoted = hasVoted;
            this.linkedVoter = v;
            this.signedByPlayer = false;
        }
    }
    public static class PlayerActions {
        public boolean checkedID = false;
        public boolean askedAddress = false;
        public boolean checkedTablet = false;
        public boolean calledGmina = false;
        public boolean addedToRegister = false;

        public void reset() {
            checkedID = false;
            askedAddress = false;
            checkedTablet = false;
            calledGmina = false;
            addedToRegister = false;
        }
    }
    public static class ScoreEntry {
        public String voterName;
        public int score;
        public List<String> details;

        public ScoreEntry(String name, int score, List<String> details) {
            this.voterName = name;
            this.score = score;
            this.details = details;
        }
    }

    private int scoreStage1;
    private Pane root;
    private Scene scene;
    private Stage primaryStage;
    private Pane phoneOverlay;
    private Pane tabletOverlay;
    private Text phoneScreenText;
    private VBox listContentBox;
    private List<VoterData> allVotersScenarios = new ArrayList<>();
    private List<TabletEntry> tabletDatabase = new ArrayList<>();
    private VoterData currentVoter;
    private StackPane dowodOverlay;
    private StackPane speechBubble;
    private Text speechText;
    private TabletEntry selectedInTablet = null;
    private ImageView npcView;
    private StackPane certificateOnDesk;
    private StackPane certOverlay;
    private StackPane refusalOverlay;
    private int currentVoterIndex = 0;
    private Pane policeOverlay;
    private PlayerActions currentActions = new PlayerActions();
    private int totalScore = 0;
    private List<ScoreEntry> dailyReport = new ArrayList<>();
    // ---  Dialogi ---

    public void start(Stage stage) {
        this.primaryStage = stage;
        double width = stage.getWidth() > 0 ? stage.getWidth() : 1024;
        double height = stage.getHeight() > 0 ? stage.getHeight() : 768;
        loadVotersFromJson();

        root = new Pane();
        root.setPrefSize(width, height);

        // --- TŁO ---
        ImageView bg = new ImageView(new Image(getClass().getResourceAsStream("/images/background_room.jpg")));
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        root.getChildren().add(bg);

        // --- WYBORCA ---
        npcView = new ImageView();
        npcView.setFitWidth(500);
        npcView.setFitHeight(700);
        npcView.setPreserveRatio(true);
        npcView.setLayoutX(230);
        npcView.setTranslateY(height / 2 -240);

        // --- PRZEDMIOTY ---
        ImageView karta = createItem("/images/karta.png", 100, 130, 75, 585);
        karta.setRotate(15);
        karta.setOnMouseClicked(e -> handleDecision("ISSUE_CARD"));

        ImageView tablet = createItem("/images/tablet.png", 220, 220, 400, 525);
        tablet.setOnMouseClicked(e -> openTablet());

        ImageView pen = createItem("/images/długopis.png", 120, 120, 575, 625);
        pen.setRotate(135);
        pen.setOnMouseClicked(e -> handlePhysicalPenClick());

        ImageView phone = createItem("/images/telefon.png", 170, 170, 650, 500);
        phone.setOnMouseClicked(e -> openPhone());

        // --- UI ---
        createSpeechBubble(350, 60);
        createActionButtons(820, 560);
        createPhoneUI(width, height);
        createTabletUI(width, height);
        createDowodUI(width, height);
        createCertOverlay(width, height);
        createCertificateOnDesk(250, 550);
        createRefusalOverlay(width, height);
        createPoliceOverlay(width, height);

        root.getChildren().add(npcView);
        root.getChildren().addAll(karta, tablet, phone, pen,certificateOnDesk);
        root.getChildren().addAll(phoneOverlay, tabletOverlay,dowodOverlay,certOverlay,refusalOverlay,policeOverlay);
        updateVisuals();
        scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Głosowanie");
        stage.show();
    }

    //  --- LOGIKA VOTERS ---
    private void loadVotersFromJson() {
        allVotersScenarios.clear();
        tabletDatabase.clear();

        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream is = getClass().getResourceAsStream("/data/voters.json");
            if (is == null) {
                System.err.println("BŁĄD: Nie znaleziono pliku voters.json w resources/data/!");
                return;
            }
            allVotersScenarios = mapper.readValue(is, new TypeReference<List<VoterData>>() {});
            System.out.println("Załadowano " + allVotersScenarios.size() + " wyborców z pliku JSON.");
            for (VoterData v : allVotersScenarios) {
                if (v.register != null && v.register.isInRegister) {
                    String fullAddr = v.register.city + ", " + v.register.address;
                    String fullName = v.register.lastName + " " + v.register.firstName;

                    tabletDatabase.add(new TabletEntry(
                            fullAddr,
                            fullName,
                            v.register.pesel,
                            v.register.hasVoted,
                            v
                    ));
                }
            }
            Collections.sort(tabletDatabase, Comparator.comparing(e -> e.fullAddress));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Błąd podczas parsowania voters.json!");
        }
        currentVoter = allVotersScenarios.get(0);
    }

    // --- Przedmioty i ich UI ---
    private void createCertificateOnDesk(double x, double y) {
        certificateOnDesk = new StackPane();
        certificateOnDesk.setLayoutX(x);
        certificateOnDesk.setLayoutY(y);
        certificateOnDesk.setVisible(false);

        ImageView icon = new ImageView();
        try {
            icon.setImage(new Image(getClass().getResourceAsStream("/images/zaswiadczenie.png")));
        } catch (Exception e) {}
        icon.setFitWidth(120);
        icon.setFitHeight(160);


        DropShadow glow = new DropShadow();
        glow.setColor(Color.GOLD);
        glow.setWidth(30);
        glow.setHeight(30);

        if (icon.getImage() != null) {
            certificateOnDesk.getChildren().add(icon);
        } else {
            certificateOnDesk.getChildren().add(new Text("ZAŚWIADCZENIE"));
        }
        certificateOnDesk.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (certOverlay != null) {
                    certOverlay.setVisible(true);
                    certOverlay.toFront();
                }
            }
        });
        certificateOnDesk.setOnMouseEntered(e -> {
            if (icon.getImage() != null) icon.setEffect(glow);
            certificateOnDesk.setScaleX(1.05);
            certificateOnDesk.setScaleY(1.05);
            certificateOnDesk.setCursor(javafx.scene.Cursor.HAND);
        });

        certificateOnDesk.setOnMouseExited(e -> {
            if (icon.getImage() != null) icon.setEffect(null);
            certificateOnDesk.setScaleX(1.0);
            certificateOnDesk.setScaleY(1.0);
        });
    }
    private void handlePhysicalPenClick() {
        if (selectedInTablet == null) {
            System.out.println("Zaznacz osobę w spisie!");
            return;
        }
        if (selectedInTablet.hasVoted || selectedInTablet.signedByPlayer) {
            System.out.println("Tu już jest podpis.");
            return;
        }
        System.out.println("Podpisano: " + selectedInTablet.fullName);
        selectedInTablet.signedByPlayer = true;

        if (currentVoter != null && selectedInTablet.linkedVoter == currentVoter) {
            currentActions.checkedTablet = true;
            System.out.println("LOG: Podpisano poprawną osobę.");
        } else {
            System.out.println("LOG: BŁĄD! Podpisano niewłaściwą osobę!");
        }

        if (tabletOverlay.isVisible()) {
            refreshRegisterList();
        }
    }
    private void createTabletUI(double sceneW, double sceneH) {
        tabletOverlay = new StackPane();
        tabletOverlay.setPrefSize(sceneW, sceneH);
        tabletOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        tabletOverlay.setVisible(false);

        VBox tabletBody = new VBox();
        tabletBody.setMaxSize(750, 500);
        tabletBody.setPickOnBounds(true);
        tabletBody.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #bdc3c7; -fx-border-width: 2;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("SPIS WYBORCÓW");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("ZAMKNIJ");
        closeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> tabletOverlay.setVisible(false));

        topBar.getChildren().addAll(title, spacer, closeBtn);

        HBox tableHeader = createRowUI("ADRES ZAMIESZKANIA", "NAZWISKO I IMIĘ", "PESEL", "PODPIS", true, null);
        tableHeader.setStyle("-fx-background-color: #95a5a6; -fx-padding: 5; -fx-border-color: black; -fx-border-width: 0 0 2 0;");

        listContentBox = new VBox(0);
        listContentBox.setStyle("-fx-background-color: white;");
        ScrollPane scroll = new ScrollPane(listContentBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");
        refreshRegisterList();

        tabletBody.getChildren().addAll(topBar, new Text(" "), tableHeader, scroll);
        tabletOverlay.getChildren().add(tabletBody);
    }
    private void refreshRegisterList() {
        listContentBox.getChildren().clear();
        for (TabletEntry entry : tabletDatabase) {

            String signText = "";
            if (entry.hasVoted || entry.signedByPlayer) {
                String rawLastName = entry.linkedVoter.register.lastName;
                if (rawLastName != null && rawLastName.contains("-")) {
                    signText = rawLastName.split("-")[0];
                } else {
                    signText = rawLastName;
                }
                // ---------------------------------------------
            }

            HBox row = createRowUI(
                    entry.fullAddress,
                    entry.fullName,
                    entry.pesel,
                    signText,
                    false,
                    entry
            );
            listContentBox.getChildren().add(row);
        }
    }
    private HBox createRowUI(String address, String name, String pesel, String signText, boolean isHeader, TabletEntry entryRef) {
        HBox row = new HBox(10);
        row.setPadding(new javafx.geometry.Insets(8));
        row.setAlignment(Pos.CENTER_LEFT);

        if (!isHeader) {
            if (selectedInTablet == entryRef) {
                row.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 0 0 1 0;");
            } else {
                row.setStyle("-fx-background-color: transparent; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
            }
        }

        Text tAddr = new Text(address); tAddr.setWrappingWidth(250);
        Text tName = new Text(name);    tName.setWrappingWidth(200);
        Text tPesel = new Text(pesel);  tPesel.setWrappingWidth(110);

        Font f = isHeader ? Font.font("Arial", FontWeight.BOLD, 14) : Font.font("Arial", 14);
        Color c = (!isHeader && selectedInTablet == entryRef) ? Color.WHITE : Color.BLACK;

        tAddr.setFont(f); tAddr.setFill(c);
        tName.setFont(f); tName.setFill(c);
        tPesel.setFont(f); tPesel.setFill(c);

        StackPane signBox = new StackPane();
        signBox.setPrefSize(100, 30);

        if (isHeader) {
            Text tSign = new Text(signText); tSign.setFont(f);
            signBox.getChildren().add(tSign);
        } else {
            Rectangle border = new Rectangle(100, 30, Color.TRANSPARENT);
            border.setStroke(Color.LIGHTGRAY);
            if (selectedInTablet == entryRef) border.setStroke(Color.WHITE);
            signBox.getChildren().add(border);

            if (signText != null && !signText.isEmpty()) {
                Text signature = new Text(signText);
                signature.setFont(Font.font("Segoe Script", 16));
                signature.setFill(selectedInTablet == entryRef ? Color.NAVY : Color.DARKBLUE);
                signature.setTextAlignment(TextAlignment.CENTER);
                signBox.getChildren().add(signature);
            }
        }
        row.getChildren().addAll(tAddr, tName, tPesel, signBox);

        if (!isHeader) {
            row.setOnMouseClicked(e -> {
                selectedInTablet = entryRef;
                System.out.println("Wybrano: " + name);
                refreshRegisterList();
            });
        }
        return row;
    }
    private void createPhoneUI(double w, double h) {
        phoneOverlay = new StackPane();
        phoneOverlay.setPrefSize(w, h);
        phoneOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        phoneOverlay.setVisible(false);

        HBox mainContainer = new HBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPickOnBounds(false);

        // --- TŁO ---
        VBox phoneBody = new VBox(20);
        phoneBody.setMaxSize(300, 520);
        phoneBody.setAlignment(Pos.TOP_CENTER);
        phoneBody.setPadding(new javafx.geometry.Insets(25));
        phoneBody.setStyle("-fx-background-color: #34495e; -fx-background-radius: 40; -fx-border-color: #2c3e50; -fx-border-width: 8; -fx-effect: dropshadow(three-pass-box, black, 20, 0, 0, 10);");

        // --- EKRAN ---
        StackPane screen = new StackPane();
        screen.setPrefSize(220, 60);
        screen.setMaxWidth(220);
        screen.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #7f8c8d; -fx-border-width: 3; -fx-background-radius: 5;");

        phoneScreenText = new Text("");
        phoneScreenText.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        phoneScreenText.setFill(Color.BLACK);
        phoneScreenText.setWrappingWidth(200);
        phoneScreenText.setTextAlignment(TextAlignment.CENTER);

        screen.getChildren().add(phoneScreenText);

        // --- Klawiatura ---
        Pane dialPane = new Pane();
        dialPane.setPrefSize(240, 240);
        dialPane.setMaxSize(240, 240);
        Circle dialBg = new Circle(120, 120, 120); dialBg.setFill(Color.TRANSPARENT); dialBg.setStroke(Color.web("#bdc3c7")); dialBg.setStrokeWidth(2);
        dialPane.getChildren().add(dialBg);

        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        double radius = 90, centerX = 120, centerY = 120, startAngle = 60, angleStep = 30;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            double rad = Math.toRadians(startAngle - (i * angleStep));
            Button b = new Button(key);
            b.setPrefSize(45, 45);
            b.setLayoutX(centerX + radius * Math.cos(rad) - 22.5);
            b.setLayoutY(centerY - radius * Math.sin(rad) - 22.5);
            b.setStyle("-fx-background-radius: 50%; -fx-background-color: #ecf0f1; -fx-font-weight: bold; -fx-font-size: 18px; -fx-border-color: #bdc3c7; -fx-border-width: 0; -fx-cursor: hand;");

            b.setOnAction(e -> {
                if (phoneScreenText.getText().length() > 11 || phoneScreenText.getText().contains(" ")) {
                    phoneScreenText.setText("");
                }

                if (phoneScreenText.getText().length() < 11) {
                    phoneScreenText.setText(phoneScreenText.getText() + key);
                }
            });
            dialPane.getChildren().add(b);
        }
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button callBtn = createPhoneActionButton("ZADZWOŃ", "#27ae60");
        callBtn.setOnAction(e -> handleCall());

        Button clearBtn = createPhoneActionButton("C", "#f39c12");
        clearBtn.setOnAction(e -> phoneScreenText.setText(""));

        Button closeBtn = createPhoneActionButton("X", "#c0392b");
        closeBtn.setOnAction(e -> phoneOverlay.setVisible(false));

        actions.getChildren().addAll(callBtn, clearBtn, closeBtn);
        phoneBody.getChildren().addAll(screen, dialPane, actions);

        VBox note = new VBox(10); note.setMaxSize(180, 220); note.setStyle("-fx-background-color: #f1c40f; -fx-padding: 15; -fx-rotate: -3;");
        note.getChildren().add(new Text("111 - GMINA\n997 - POLICJA"));
        mainContainer.getChildren().addAll(phoneBody, note);
        phoneOverlay.getChildren().add(mainContainer);
    }
    private Button createPhoneActionButton(String text, String colorHex) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return b;
    }
    private ImageView createItem(String path, double w, double h, double x, double y) {
        ImageView iv = new ImageView();
        iv.setImage(new Image(getClass().getResourceAsStream(path)));
        iv.setFitWidth(w); iv.setFitHeight(h);
        iv.setX(x); iv.setY(y);
        iv.setPickOnBounds(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        DropShadow glow = new DropShadow(); glow.setColor(Color.GOLD);
        iv.setOnMouseEntered(e -> iv.setEffect(glow));
        iv.setOnMouseExited(e -> iv.setEffect(null));
        return iv;
    }
    private void createActionButtons(double x, double y) {
        VBox decisionPanel = new VBox(12);
        decisionPanel.setLayoutX(x); decisionPanel.setLayoutY(y);
        String style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 200; -fx-pref-height: 40;";
        Button b1 = new Button("POPROŚ O DOKUMENT"); b1.setStyle(style + "-fx-background-color: #ecf0f1;");
        b1.setOnAction(e -> {
            currentActions.checkedID = true;
            System.out.println("LOG: Sprawdzono dowód.");
            if (currentVoter != null && currentVoter.document != null) {
                dowodOverlay.setVisible(true);
            }
        });
        Button b2 = new Button("ZAPYTAJ O ADRES"); b2.setStyle(style + "-fx-background-color: #ecf0f1;");
        b2.setOnAction(e -> handleAskAddress());

        Button b3 = new Button("ODMÓW GŁOSU"); b3.setStyle(style + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
        b3.setOnAction(e -> {
            if (refusalOverlay != null) {
                refusalOverlay.setVisible(true); // Pokaż okno
                refusalOverlay.toFront();        // Daj na wierzch
            } else {
                System.out.println("BŁĄD: refusalOverlay nie został utworzony!");
            }
        });

        decisionPanel.getChildren().addAll(b1, b2, b3);

        root.getChildren().add(decisionPanel);
    }
    private void createDowodUI(double w, double h) {
        dowodOverlay = new StackPane();
        dowodOverlay.setPrefSize(w, h);
        dowodOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        dowodOverlay.setVisible(false);

        Pane dowodBody = new Pane();
        dowodBody.setPrefSize(600, 380);
        dowodBody.setMaxSize(600, 380);
        // --- TŁO ---
        ImageView bg = new ImageView( new Image(getClass().getResourceAsStream("/images/DowodOsobisty.png")));
        bg.setFitWidth(600);
        bg.setFitHeight(380);
        // --- DANE ---
        Text tFirstName = new Text();
        Text tLastName  = new Text();
        Text tnationality = new Text();
        Text tPesel     = new Text();
        Text tBirth     = new Text();
        Text tSex       = new Text();

        Font fontMain = Font.font("Arial", FontWeight.BOLD, 22);
        Font fontSmall = Font.font("Arial",FontWeight.BOLD, 16);
        tFirstName.setFont(fontMain);
        tLastName.setFont(fontMain);
        tPesel.setFont(fontSmall);
        tBirth.setFont(fontSmall);
        tnationality.setFont(fontSmall);
        tSex.setFont(fontSmall);
        // --- POZYCJA ---
        tFirstName.setLayoutX(220);
        tFirstName.setLayoutY(155);
        tLastName.setLayoutX(220);
        tLastName.setLayoutY(115);
        tnationality.setLayoutX(220);
        tnationality.setLayoutY(200);
        tPesel.setLayoutX(220);
        tPesel.setLayoutY(240);
        tBirth.setLayoutX(415);
        tBirth.setLayoutY(200);
        tSex.setLayoutX(415);
        tSex.setLayoutY(250);

        ImageView photo = new ImageView();
        photo.setFitWidth(180);
        photo.setFitHeight(500);
        photo.setLayoutX(30);
        photo.setLayoutY(160);
        photo.setPreserveRatio(true);

        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1);
        photo.setEffect(grayscale);

        Button close = new Button("ZAMKNIJ");
        close.setLayoutX(520);
        close.setLayoutY(-20);
        close.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        close.setOnAction(e -> dowodOverlay.setVisible(false));

        dowodBody.getChildren().addAll(
                bg,
                tFirstName, tLastName,tnationality, tPesel, tBirth,tSex,
                photo,
                close
        );

        dowodOverlay.getChildren().add(dowodBody);

        dowodOverlay.visibleProperty().addListener((obs, oldV, newV) -> {
            if (newV && currentVoter != null && currentVoter.document != null) {
                tFirstName.setText(currentVoter.document.firstName);
                tLastName.setText(currentVoter.document.lastName);
                tnationality.setText("POLSKIE");
                tPesel.setText(currentVoter.document.pesel);
                tBirth.setText(currentVoter.document.birthDate);
                if (currentVoter.document.firstName != null && !currentVoter.document.firstName.isEmpty()) {
                    String plecSymbol = currentVoter.document.firstName.toLowerCase().endsWith("a") ? "K" : "M";
                    tSex.setText(plecSymbol);
                }
                try {
                    photo.setImage(new Image(
                            getClass().getResourceAsStream("/images/" + currentVoter.document.photo)
                    ));
                } catch (Exception ex) {
                    photo.setImage(null);
                }
            }
        });
    }
    private void createCertOverlay(double w, double h) {
        certOverlay = new StackPane();
        certOverlay.setPrefSize(w, h);
        certOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        certOverlay.setVisible(false);

        Pane certBody = new Pane();
        certBody.setPrefSize(500, 700);
        certBody.setMaxSize(500, 700);

        // 1. TŁO
        ImageView bg = new ImageView();
        bg.setImage(new Image(getClass().getResourceAsStream("/images/zaswiadczenie.png")));
        bg.setFitWidth(500);
        bg.setFitHeight(700);
        ImageView hologram = new ImageView();
        hologram.setImage(new Image(getClass().getResourceAsStream("/images/hologram.png")));
        hologram.setFitWidth(80);
        hologram.setFitHeight(80);
        hologram.setLayoutX(320);
        hologram.setLayoutY(25);
        hologram.setOpacity(0.5);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), hologram);
        ft.setFromValue(0.4);
        ft.setToValue(1);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();
        // --- Dane ---
        Text tName    = new Text();
        Text tSurname = new Text();
        Text tPesel   = new Text();
        Text tCity    = new Text();
        Text tStreet  = new Text();
        Font font = Font.font("Courier New", FontWeight.BOLD, 18);
        tName.setFont(font); tSurname.setFont(font); tPesel.setFont(font);
        tCity.setFont(font); tStreet.setFont(font);
        // --- Pozycja ---
        tName.setLayoutX(140);
        tName.setLayoutY(335);
        tSurname.setLayoutX(140);
        tSurname.setLayoutY(355);
        tPesel.setLayoutX(140);
        tPesel.setLayoutY(375);
        tCity.setLayoutX(140);
        tCity.setLayoutY(445);
        tStreet.setLayoutX(140);
        tStreet.setLayoutY(470);
        // --- Przycisk ---
        Button btnAdd = new Button("DOPISZ DO SPISU");
        btnAdd.setLayoutX(140);
        btnAdd.setLayoutY(640);
        btnAdd.setPrefWidth(200);
        btnAdd.setPrefHeight(40);
        btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");

        btnAdd.setOnAction(e -> {
            handleAddToRegister();
            certOverlay.setVisible(false);
        });

        Button close = new Button("X");
        close.setLayoutX(450);
        close.setLayoutY(10);
        close.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 24; -fx-cursor: hand;");
        close.setOnAction(e -> certOverlay.setVisible(false));
        certBody.getChildren().addAll(bg, hologram, tName, tSurname, tPesel, tCity, tStreet, btnAdd, close);
        certOverlay.getChildren().add(certBody);

        certOverlay.visibleProperty().addListener((obs, oldV, newV) -> {
            if (newV && currentVoter != null) {
                tName.setText(currentVoter.document.firstName.toUpperCase());
                tSurname.setText(currentVoter.document.lastName.toUpperCase());
                tPesel.setText(currentVoter.document.pesel);
                if (currentVoter.register != null) {
                    tCity.setText(currentVoter.register.city);
                    tStreet.setText(currentVoter.register.address);
                }
            }
        });
    }
    private void createRefusalOverlay(double w, double h) {
        refusalOverlay = new StackPane();
        refusalOverlay.setPrefSize(w, h);
        refusalOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        refusalOverlay.setVisible(false);
        VBox box = new VBox(15);
        box.setMaxSize(400, 350);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #c0392b; -fx-border-width: 4;");
        Text title = new Text("POWÓD ODMOWY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String btnStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand; -fx-pref-width: 300; -fx-pref-height: 40;";

        Button bAge = new Button("NIEPEŁNOLETNI");
        bAge.setStyle(btnStyle);
        bAge.setOnAction(e -> {
            refusalOverlay.setVisible(false);
            handleDecision("REFUSE_NO_RIGHTS");
        });

        Button bSpecial = new Button("SPIS SPECJALNY (Szpital/Więzienie)");
        bSpecial.setStyle(btnStyle);
        bSpecial.setOnAction(e -> {
            refusalOverlay.setVisible(false);
            handleDecision("REFUSE_SPECIAL_REGISTER");
        });

        Button bDistrict = new Button("BŁĘDNY OBWÓD / ADRES");
        bDistrict.setStyle(btnStyle);
        bDistrict.setOnAction(e -> {
            refusalOverlay.setVisible(false);
            handleDecision("REFUSE_WRONG_PRECINCT");
        });

        // Anuluj
        Button bCancel = new Button("ANULUJ (Wróć)");
        bCancel.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-pref-width: 150;");
        bCancel.setOnAction(e -> refusalOverlay.setVisible(false));

        box.getChildren().addAll(title, new Region(), bAge, bSpecial, bDistrict, new Region(), bCancel);
        refusalOverlay.getChildren().add(box);
    }

    // --- Open ---
    private void openPhone() { phoneScreenText.setText(""); phoneOverlay.setVisible(true); }
    private void openTablet() {refreshRegisterList();tabletOverlay.setVisible(true);}

    // --- MOWA ---
    private void speak(String text) {
        speechText.setText(text);
        speechBubble.setVisible(true);
        speechBubble.toFront();
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> speechBubble.setVisible(false));
        pause.play();
    }
    private void createSpeechBubble(double x, double y) {
        speechBubble = new StackPane();
        speechBubble.setLayoutX(x);
        speechBubble.setLayoutY(y);
        speechBubble.setVisible(false);


        Rectangle bubbleShape = new Rectangle(250, 80, Color.WHITE);
        bubbleShape.setArcWidth(20);
        bubbleShape.setArcHeight(20);
        bubbleShape.setStroke(Color.BLACK);
        bubbleShape.setStrokeWidth(2);
        bubbleShape.setEffect(new DropShadow(5, Color.gray(0.5)));

        speechText = new Text();
        speechText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        speechText.setTextAlignment(TextAlignment.CENTER);
        speechText.setWrappingWidth(230);

        Rectangle tail = new Rectangle(20, 20, Color.WHITE);
        tail.setRotate(45);
        tail.setStroke(Color.BLACK);
        tail.setTranslateY(40);

        speechBubble.getChildren().addAll(tail, bubbleShape, speechText);

        root.getChildren().add(speechBubble);
    }
    private void handleAskAddress() {
        if (currentVoter == null) return;
        currentActions.askedAddress = true;
        System.out.println("LOG: Zapytano o adres.");
        String city = currentVoter.register.city;
        String addr = currentVoter.register.address;

        String message;
        if (addr != null && !addr.isEmpty()) {
            message = "Mieszkam w: " + city + ",\nna " + addr;
        } else {
            message = "Yyy... dopiero się wprowadziłem, nie pamiętam adresu.";
        }

        speak(message);
    }

    private void updateVisuals() {
        if (currentVoter == null) {
            if (dowodOverlay != null) dowodOverlay.setVisible(false);
            if (certOverlay != null) certOverlay.setVisible(false);
            if (certificateOnDesk != null) certificateOnDesk.setVisible(false);
            if (npcView != null) npcView.setImage(null);
            return;
        }

        // 2. Ładowanie obrazka NPC
        try {
            String imagePath = "/images/" + currentVoter.personImage;
            if (getClass().getResourceAsStream(imagePath) != null) {
                npcView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } else {
                npcView.setImage(null);
            }
        } catch (Exception e) {
            npcView.setImage(null);
        }
        if (dowodOverlay != null) dowodOverlay.setVisible(false);
        if (certOverlay != null) certOverlay.setVisible(false);
        if (certificateOnDesk != null) {
            certificateOnDesk.setVisible(false);
        }
        if (currentVoter.hasCertificate) {
            if (certificateOnDesk != null) {
                certificateOnDesk.setVisible(true);
            }
        }
    }

    // --- POLICJA ---
    private void createPoliceOverlay(double w, double h) {
        policeOverlay = new StackPane();
        policeOverlay.setPrefSize(w, h);
        policeOverlay.setVisible(false);
        policeOverlay.setMouseTransparent(true);
    }
    private void triggerPoliceAction() {
        phoneOverlay.setVisible(false);
        policeOverlay.setVisible(true);
        System.out.println("POLICJA JEDZIE!");
        Timeline lights = new Timeline(
                new KeyFrame(Duration.ZERO, e ->
                        policeOverlay.setStyle("-fx-background-color: rgba(255, 0, 0, 0.4);")),
                new KeyFrame(Duration.seconds(0.15), e ->
                        policeOverlay.setStyle("-fx-background-color: rgba(0, 0, 255, 0.4);")),
                new KeyFrame(Duration.seconds(0.3), e -> {})
        );
        lights.setCycleCount(7);
        handleDecision("CALL_POLICE");
        lights.setOnFinished(e -> {
            policeOverlay.setVisible(false);
        });

        lights.play();
    }

    // --- PUNKTACJA i DECYZJE ---
    private void nextVoter() {
        if (dowodOverlay != null) dowodOverlay.setVisible(false);
        if (certificateOnDesk != null) certificateOnDesk.setVisible(false);
        if (certOverlay != null) certOverlay.setVisible(false);
        TranslateTransition leave = new TranslateTransition(Duration.seconds(0.6), npcView);
        leave.setToX(-1000);
        leave.setInterpolator(Interpolator.EASE_IN);

        leave.setOnFinished(e -> {
            currentVoterIndex++;
            currentActions.reset();
            if (currentVoterIndex < 8) {
                currentVoter = allVotersScenarios.get(currentVoterIndex);
                selectedInTablet = null;
                if (phoneScreenText != null) phoneScreenText.setText("");
                updateVisuals();
                if (certificateOnDesk != null) certificateOnDesk.setVisible(false);
                npcView.setTranslateX(1000);
                TranslateTransition enter = new TranslateTransition(Duration.seconds(0.6), npcView);
                enter.setToX(0);
                enter.setInterpolator(Interpolator.EASE_OUT);
                enter.setOnFinished(ev -> {
                    if (currentVoter.hasCertificate && certificateOnDesk != null) {
                        certificateOnDesk.setVisible(true);
                    }
                    System.out.println("Nowy klient: " + currentVoter.document.lastName);
                });

                enter.play();

            } else {
                System.out.println("KONIEC GRY!");
                currentVoter = null;
                updateVisuals();
                showEndGameSummary();
            }
        });

        leave.play();
    }
    private void handleDecision(String playerAction) {
        if (currentVoter == null) return;

        int score = 0;
        List<String> logs = new ArrayList<>();
        boolean correctDecision = false;

        String expectedAction = currentVoter.action;

        // --- 1. WERYFIKACJA GŁÓWNEJ DECYZJI ---

        if (expectedAction.equals("ADD_AND_ISSUE")) {
            if (playerAction.equals("ISSUE_CARD") && currentActions.addedToRegister) {
                correctDecision = true;
            }
        }
        else if (playerAction.equals(expectedAction)) {
            correctDecision = true;
        }

        // --- 2. BAZA PUNKTOWA ---

        if (correctDecision) {
            score = 1000;
            logs.add("Prawidłowa decyzja +1000");
        } else {
            score = 0; // Startujemy z 0 (kary sprawią, że będzie ujemny)
            logs.add("BŁĘDNA DECYZJA (Baza 0)");
        }

        // A. WYDANIE KARTY (ISSUE_CARD)
        if (expectedAction.equals("ISSUE_CARD")) {
            if (!currentActions.checkedID) { score -= 200; logs.add("Brak sprawdz. dowodu -200"); }
            if (!currentActions.askedAddress) { score -= 200; logs.add("Brak zapytania o adres -200"); }
            if (!currentActions.checkedTablet) { score -= 200; logs.add("Brak podpisu w spisie -200"); }

            if (currentActions.calledGmina) {
                score -= 500;
                logs.add("Niepotrzebny tel. do Gminy -500");
            }
        }
        // B. DOPISANIE I WYDANIE (ADD_AND_ISSUE)
        else if (expectedAction.equals("ADD_AND_ISSUE")) {
            if (!currentActions.checkedID) { score -= 200; logs.add("Brak sprawdz. dowodu -200"); }
            if (!currentActions.askedAddress) { score -= 200; logs.add("Brak zapytania o adres -200"); }
            if (!currentActions.addedToRegister) { score -= 200; logs.add("Brak dopisania do spisu -200"); }
            if (!currentActions.checkedTablet) { score -= 200; logs.add("Brak podpisu w spisie -200");
            }
            // C. ODMOWA: NIELETNI / BRAK PRAW (REFUSE_AGE, REFUSE_NO_RIGHTS)
            else if (expectedAction.equals("REFUSE_AGE") || expectedAction.equals("REFUSE_NO_RIGHTS")) {
                if (!currentActions.checkedID) { score -= 200; logs.add("Brak sprawdz. dowodu -200"); }
                if (correctDecision) {
                    score += 200;
                    logs.add("BONUS: Prawidłowa odmowa (bez dzwonienia) +200");
                }
            }
        }
        // D. ODMOWA: SPIS / OBWÓD (REFUSE_SPECIAL_REGISTER, REFUSE_WRONG_PRECINCT)
        else if (expectedAction.equals("REFUSE_SPECIAL") || expectedAction.equals("REFUSE_SPECIAL_REGISTER") ||
                expectedAction.equals("REFUSE_DISTRICT") || expectedAction.equals("REFUSE_WRONG_PRECINCT")) {

            if (!currentActions.checkedID) { score -= 200; logs.add("Brak sprawdz. dowodu -200"); }
            if (!currentActions.askedAddress) { score -= 200; logs.add("Brak zapytania o adres -200"); }

            if (!currentActions.calledGmina && expectedAction.equals("REFUSE_SPECIAL") ) {
                score -= 500;
                logs.add("KARA: Brak obowiązkowego telefonu do Gminy -500");
            } else {
                logs.add("Weryfikacja w Gminie wykonana (OK)");
            }
        }

        // E. POLICJA (CALL_POLICE)
        else if (expectedAction.equals("CALL_POLICE")) {
            if (!currentActions.checkedID) { score -= 200; logs.add("Brak sprawdz. dowodu -200"); }
            if (!currentActions.askedAddress) { score -= 200; logs.add("Brak zapytania o adres -200"); }
        }

        totalScore += score;
        String name = currentVoter.document.lastName + " " + currentVoter.document.firstName;
        dailyReport.add(new ScoreEntry(name, score, logs));

        System.out.println("ZAKOŃCZONO: " + name + " | Wynik: " + score);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.0));
        delay.setOnFinished(e -> nextVoter());
        delay.play();
    }
    private void handleAddToRegister() {
        if (currentVoter == null) return;
        boolean exists = tabletDatabase.stream()
                .anyMatch(e -> e.pesel.equals(currentVoter.document.pesel));

        if (exists) {
            System.out.println("Ta osoba już jest w spisie!");
            return;
        }
        RegisterObj r = currentVoter.register;
        String fullAddr = r.city + ", " + r.address;
        String fullName = r.lastName + " " + r.firstName;

        TabletEntry newEntry = new TabletEntry(
                fullAddr,
                fullName,
                currentVoter.document.pesel,
                false,
                currentVoter
        );
        tabletDatabase.add(newEntry);
        if (tabletOverlay.isVisible()) {
            refreshRegisterList();
        }
        currentActions.addedToRegister = true;
        System.out.println("Dopisano wyborcę do spisu na tablecie.");
    }
    private void handleCall() {
        String input = phoneScreenText.getText();

        if (input.equals("111")) {
            if (currentVoter != null && currentVoter.register != null) {
                String info = currentVoter.register.gminaInfo;
                currentActions.calledGmina = true;
                System.out.println("LOG: Telefon do gminy wykonany.");
                if (info != null && !info.isEmpty()) {
                    phoneScreenText.setText(info);
                } else {
                    if (currentVoter.register.isInRegister) {
                        phoneScreenText.setText("FIGURUJE W SPISIE");
                    } else {
                        phoneScreenText.setText("BRAK W BAZIE");
                    }
                }
            } else {
                phoneScreenText.setText("BRAK DANYCH");
            }
        }
        else if (input.equals("997") || input.equals("112")) {
            phoneScreenText.setText("WEZWANIE...");
            PauseTransition pause = new PauseTransition(Duration.seconds(0.7));
            pause.setOnFinished(e -> triggerPoliceAction());
            pause.play();
        }
        else {
            phoneScreenText.setText("NIE MA TAKIEGO NUMERU");
        }
    }
    private void showEndGameSummary() {
        root.getChildren().forEach(node -> node.setVisible(false));
        StackPane summaryOverlay = new StackPane();
        summaryOverlay.setPrefSize(root.getWidth(), root.getHeight());
        summaryOverlay.setStyle("-fx-background-color: rgba(44, 62, 80, 0.95);");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(800, 600);

        Text title = new Text("RAPORT KOŃCOWY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        Text scoreText = new Text("CAŁKOWITY WYNIK: " + totalScore + " pkt");
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreText.setFill(Color.GOLD);


        VBox list = new VBox(10);
        list.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);

        for (ScoreEntry entry : dailyReport) {
            VBox entryBox = new VBox(5);
            entryBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0; -fx-padding: 5;");

            HBox header = new HBox(10);
            Text tName = new Text(entry.voterName);
            tName.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            Text tScore = new Text(String.valueOf(entry.score));
            tScore.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            tScore.setFill(entry.score > 0 ? Color.GREEN : Color.RED);

            Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
            header.getChildren().addAll(tName, r, tScore);

            entryBox.getChildren().add(header);

            // Detale
            for (String detail : entry.details) {
                Text tDet = new Text(" • " + detail);
                tDet.setFont(Font.font("Arial", 12));
                tDet.setFill(Color.DARKGRAY);
                entryBox.getChildren().add(tDet);
            }
            list.getChildren().add(entryBox);
        }
        Button exitBtn = new Button("ZAKOŃCZ PRACĘ");
        exitBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        exitBtn.setOnAction(e -> primaryStage.close());

        Button nextStageBtn = new Button("PRZEJDŹ DO LICZENIA GŁOSÓW");
        nextStageBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        nextStageBtn.setOnAction(e -> {
            LiczenieGlosowScene stage3 = new LiczenieGlosowScene(scoreStage1,totalScore);
            stage3.start(primaryStage);
        });
        content.getChildren().addAll(title, scoreText, scroll, nextStageBtn);
        summaryOverlay.getChildren().add(content);

        root.getChildren().add(summaryOverlay);
    }
}