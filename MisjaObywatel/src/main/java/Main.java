import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import game.GameScene;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/menu.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 720); //USTAWIĆ NA 1920x1080 w finalnej wersji!
        stage.setTitle("Misja Obywatel");
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
