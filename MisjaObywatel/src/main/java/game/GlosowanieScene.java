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

public class GlosowanieScene {

    public static class VoterData {
        public int id;
        public String action; // Typ akcji np. "ISSUE_CARD"
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
        public RegisterObj() {}
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
    private Text certName, certPesel, certAddress;
    private Text certText;                 // Tekst na zaświadczeniu

    public void start(Stage stage) {
        this.primaryStage = stage;
        double width = stage.getWidth() > 0 ? stage.getWidth() : 1024;
        double height = stage.getHeight() > 0 ? stage.getHeight() : 768;
        loadVotersFromJson();

        root = new Pane();
        root.setPrefSize(width, height);

        // --- TŁO ---
        try {
            ImageView bg = new ImageView(new Image(getClass().getResourceAsStream("/images/background_room.jpg")));
            bg.setFitWidth(width);
            bg.setFitHeight(height);
            root.getChildren().add(bg);
        } catch (Exception e) {
            root.getChildren().add(new Rectangle(width, height, Color.DARKGRAY));
        }
        // --- WYBORCA ---
        npcView = new ImageView();
        npcView.setFitWidth(500);
        npcView.setFitHeight(700);
        npcView.setPreserveRatio(true);
        npcView.setLayoutX(230);
        npcView.setLayoutY(145);

        // --- PRZEDMIOTY ---
        ImageView karta = createItem("/images/karta.png", 100, 130, 75, 585);
        karta.setRotate(15);
        karta.setOnMouseClicked(e -> System.out.println("Kliknięto kartę"));

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
        createCertificateUI(100,300 );
        root.getChildren().add(npcView);
        root.getChildren().addAll(karta, tablet, phone, pen);
        root.getChildren().addAll(phoneOverlay, tabletOverlay,dowodOverlay);
        root.getChildren().add(certificateOnDesk);

        updateVisuals();
        scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Misja Obywatel – Głosowanie");
        stage.show();
    }
    //  --- LOGIKA VOTERS JSON ---

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
        currentVoter = allVotersScenarios.get(4);
    }

    //  --- LOGIKA GRY ---

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

        if (tabletOverlay.isVisible()) {
            refreshRegisterList();
        }
    }

    private void handleIssueBallot() {
        System.out.println("[AKCJA] Próba wydania karty...");
    }

    //  --- TABLET UI ---

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
                signText = entry.linkedVoter.register.lastName;
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

    private void openTablet() {
        refreshRegisterList();
        tabletOverlay.setVisible(true);
    }
    //      --- TELEFON UI ---

    private void createPhoneUI(double w, double h) {
        phoneOverlay = new StackPane();
        phoneOverlay.setPrefSize(w, h);
        phoneOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        phoneOverlay.setVisible(false);

        HBox mainContainer = new HBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPickOnBounds(false);

        // --- TELEFON ---
        VBox phoneBody = new VBox(20);
        phoneBody.setMaxSize(300, 520);
        phoneBody.setAlignment(Pos.TOP_CENTER);
        phoneBody.setPadding(new javafx.geometry.Insets(25));
        phoneBody.setStyle("-fx-background-color: #34495e; -fx-background-radius: 40; -fx-border-color: #2c3e50; -fx-border-width: 8; -fx-effect: dropshadow(three-pass-box, black, 20, 0, 0, 10);");

        // Ekran (Zmieniony na StackPane z zawijanym tekstem)
        StackPane screen = new StackPane();
        screen.setPrefSize(220, 60); // Trochę wyższy
        screen.setMaxWidth(220);
        screen.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #7f8c8d; -fx-border-width: 3; -fx-background-radius: 5;");

        phoneScreenText = new Text("");
        phoneScreenText.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        phoneScreenText.setFill(Color.BLACK);
        phoneScreenText.setWrappingWidth(200);
        phoneScreenText.setTextAlignment(TextAlignment.CENTER);

        screen.getChildren().add(phoneScreenText);

        // Klawiatura
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

    private void handleCall() {
        String input = phoneScreenText.getText();

        if (input.equals("111")) {

            if (currentVoter != null && currentVoter.register != null) {
                String info = currentVoter.register.gminaInfo;
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
        else if (input.equals("997")) {
            phoneScreenText.setText("ZGŁOSZENIE PRZYJĘTE");
        }
        else {
            phoneScreenText.setText("NIE MA TAKIEGO NUMERU");
        }
    }

    private Button createPhoneActionButton(String text, String colorHex) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return b;
    }

    private void openPhone() { phoneScreenText.setText(""); phoneOverlay.setVisible(true); }
    private ImageView createItem(String path, double w, double h, double x, double y) {
        ImageView iv = new ImageView();
        try { iv.setImage(new Image(getClass().getResourceAsStream(path))); } catch (Exception e) {}
        iv.setFitWidth(w); iv.setFitHeight(h);
        iv.setX(x); iv.setY(y);
        iv.setPickOnBounds(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        DropShadow glow = new DropShadow(); glow.setColor(Color.GOLD);
        iv.setOnMouseEntered(e -> iv.setEffect(glow));
        iv.setOnMouseExited(e -> iv.setEffect(null));
        return iv;
    }
    // --- Przyciski ---
    private void createActionButtons(double x, double y) {
        VBox decisionPanel = new VBox(12);
        decisionPanel.setLayoutX(x); decisionPanel.setLayoutY(y);
        String style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-pref-width: 200; -fx-pref-height: 40;";
        Button b1 = new Button("POPROŚ O DOKUMENT"); b1.setStyle(style + "-fx-background-color: #ecf0f1;");
        b1.setOnAction(e -> {
            if (currentVoter != null && currentVoter.document != null) {
                dowodOverlay.setVisible(true);
            }
        });
        Button b2 = new Button("ZAPYTAJ O ADRES"); b2.setStyle(style + "-fx-background-color: #ecf0f1;");
        b2.setOnAction(e -> handleAskAddress());

        Button b3 = new Button("ODMÓW GŁOSU"); b3.setStyle(style + "-fx-background-color: #e74c3c; -fx-text-fill: white;");

        decisionPanel.getChildren().addAll(b1, b2, b3);

        root.getChildren().add(decisionPanel);
    }
    private void createCertificateUI(double x, double y) {
        certificateOnDesk = new StackPane();
        certificateOnDesk.setLayoutX(x);
        certificateOnDesk.setLayoutY(y);
        certificateOnDesk.setVisible(false); // Domyślnie ukryte

        // Tło - plik graficzny (lub biały prostokąt, jeśli nie masz pliku)
        ImageView bg = new ImageView();
        bg.setImage(new Image(getClass().getResourceAsStream("/images/zaswiadczenie.png")));
        Rectangle fallbackBg = new Rectangle(200, 280, Color.FLORALWHITE);
        fallbackBg.setStroke(Color.GRAY);
        fallbackBg.setEffect(new DropShadow(5, Color.gray(0.4)));

        // Kontener na tekst
        VBox textBox = new VBox(10);
        textBox.setAlignment(Pos.TOP_LEFT);
        textBox.setPadding(new javafx.geometry.Insets(60, 10, 10, 20)); // Margines od góry (pod nagłówek)

        certName = new Text();
        certName.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        certPesel = new Text();
        certPesel.setFont(Font.font("Courier New", 12));

        certAddress = new Text();
        certAddress.setFont(Font.font("Courier New", 11));
        certAddress.setWrappingWidth(160);

        textBox.getChildren().addAll(certName, certPesel, new Text(" "), certAddress);

        if (bg.getImage() != null) {
            certificateOnDesk.getChildren().add(bg);
        } else {
            certificateOnDesk.getChildren().add(fallbackBg);
        }
        certificateOnDesk.getChildren().add(textBox);

        certificateOnDesk.setOnMouseEntered(e -> {
            certificateOnDesk.setScaleX(1.5);
            certificateOnDesk.setScaleY(1.5);
            certificateOnDesk.toFront();
        });
        certificateOnDesk.setOnMouseExited(e -> {
            certificateOnDesk.setScaleX(1.0);
            certificateOnDesk.setScaleY(1.0);
        });
    }
    private void createDowodUI(double w, double h) {
        dowodOverlay = new StackPane();
        dowodOverlay.setPrefSize(w, h);
        dowodOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        dowodOverlay.setVisible(false);

        Pane dowodBody = new Pane();
        dowodBody.setPrefSize(600, 380);
        dowodBody.setMaxSize(600, 380);

        ImageView bg = new ImageView(
                new Image(getClass().getResourceAsStream("/images/DowodOsobisty.png"))
        );
        bg.setFitWidth(600);
        bg.setFitHeight(380);

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
    private void handleAskAddress() {
        if (currentVoter == null) return;

        String city = currentVoter.register.city;
        String addr = currentVoter.register.address;

        String message;
        if (addr != null && !addr.isEmpty()) {
            message = "Mieszkam w: " + city + ",\nna ulicy " + addr;
        } else {
            message = "Yyy... dopiero się wprowadziłem, nie pamiętam adresu.";
        }

        speak(message);
    }
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
    private void updateVisuals() {
        if (currentVoter == null) {
            if (dowodOverlay != null) dowodOverlay.setVisible(false);
            if (certificateOnDesk != null) certificateOnDesk.setVisible(false);
            if (npcView != null) npcView.setImage(null);
            return;
        }
        try {
            String imagePath = "/images/" + currentVoter.personImage;
            if (getClass().getResourceAsStream(imagePath) != null) {
                npcView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } else {
                System.out.println("Nie znaleziono pliku: " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Błąd ładowania NPC: " + currentVoter.personImage);
            npcView.setImage(null);
        }

        if (certificateOnDesk != null) {
            boolean maZaswiadczenie = currentVoter.hasCertificate;
            certificateOnDesk.setVisible(maZaswiadczenie);

            if (maZaswiadczenie) {
                // Wypełniamy tekst danymi z JSON
                // Imię/Nazwisko/PESEL bierzemy z sekcji 'document' (bo to jest na papierze)
                String name = currentVoter.document.firstName + " " + currentVoter.document.lastName;
                String pesel = currentVoter.document.pesel;

                // Adres bierzemy z sekcji 'register' (tam w JSONie wpisaliśmy adres zamieszkania dla turysty)
                String address = currentVoter.register.city + ", " + currentVoter.register.address;

                if (certText != null) {
                    certText.setText(
                            "ZAŚWIADCZENIE\n" +
                                    "O PRAWIE DO GŁOSOWANIA\n\n" +
                                    name.toUpperCase() + "\n" +
                                    "PESEL: " + pesel + "\n\n" +
                                    "Adres zamieszkania:\n" + address
                    );
                }
            }
        }
    }
    private void handleDecision(String playerAction) {
        if (currentVoter == null) return;
        boolean success = false;
        String msg = "";

        if (playerAction.equals("ADD_AND_ISSUE")) {
            if (currentVoter.action.equals("ADD_AND_ISSUE")) {
                success = true;
                RegisterObj r = currentVoter.register;
                String fullAddr = r.city + ", " + r.address;
                String fullName = r.lastName + " " + r.firstName;

                TabletEntry newEntry = new TabletEntry(fullAddr, fullName, r.pesel, true, currentVoter); // true -> hasVoted (bo wydajemy)
                newEntry.signedByPlayer = true;

                tabletDatabase.add(newEntry);
                System.out.println("Dopisano wyborcę do spisu!");
            } else {
                msg = "BŁĄD: Tej osoby nie można dopisać!";
            }
        }
        else if (playerAction.equals(currentVoter.action)) {
            success = true;
        } else {
            msg = "BŁĄD! Oczekiwano: " + currentVoter.action;
        }

        System.out.println(success ? "SUKCES!" : msg);
        updateVisuals();
    }
}