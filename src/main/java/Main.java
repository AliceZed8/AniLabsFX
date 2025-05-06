import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import org.freedesktop.gstreamer.Gst;


public class Main extends Application {

    public static void main(String[] args) {
        System.out.println("Init GStreamer");
        Gst.init("AniLabsFX"); // gstreamer
        System.out.println("Launch");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting");

        primaryStage.setTitle("AniLabs");
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(360);
        primaryStage.setWidth(1080);
        primaryStage.setHeight(600);


        // Подгружаем основную сцену
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainScene.fxml"));
        Parent root = loader.load();

        // сцена
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();


        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0); // На всякий случай
        });
    }
}