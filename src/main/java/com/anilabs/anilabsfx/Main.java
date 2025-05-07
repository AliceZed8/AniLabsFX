package com.anilabs.anilabsfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.freedesktop.gstreamer.Gst;

import javax.net.ssl.SSLContext;
import java.io.File;


public class Main extends Application {

    public static void main(String[] args) {
        System.out.println("Init GStreamer");
        setupGStreamerPaths();
        Gst.init("AniLabsFX"); // gstreamer
        System.out.println("Launch");
        launch(args);
    }

    private static void setupGStreamerPaths() {
        String appDir = System.getProperty("user.dir");
        String gstreamerPath = appDir + "/gstreamer/win";

        // Добавляем GStreamer bin в PATH
        String path = System.getenv("PATH");
        path = gstreamerPath + "/bin" + File.pathSeparator + path;
        System.setProperty("jna.library.path", gstreamerPath + "/bin");
        System.setProperty("java.library.path", gstreamerPath + "/bin");
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