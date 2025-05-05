package controller;

import animation.Animations;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.util.Duration;
import manager.SceneState;
import manager.TabSceneManager;
import service.ApiService;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import service.DbService;

public class LoadingSceneController {
    private final ApiService apiService = ApiService.getInstance();
    private final DbService dbService = DbService.getInstance();

    @FXML private Label statusLabel;
    @FXML private ImageView logoImage;


    @FXML
    public void initialize() {
        // logo
        logoImage.setFitWidth(200);
        logoImage.setPreserveRatio(true);
        Circle clip = new Circle(100, 100, 100); // x, y, радиус
        logoImage.setClip(clip);


        // пытаемся получить данные для фильтрации
        fetchFilterParams();
    }


    private void fetchFilterParams() {
        Task<Void> loadFilterParams = new Task<>() {
            @Override
            protected Void call() {
                final int MAX_RETRIES = 5;
                final long RETRY_DELAY_MS = 1000;


                Platform.runLater(() -> statusLabel.setText("Данные для фильтрации"));
                for (int i = 1; i <= MAX_RETRIES; i++) {
                    try {
                        System.out.println("Fetch filter params");
                        apiService.loadFilterParams();

                        if (ApiService.filterParams != null) {
                            Thread.sleep(RETRY_DELAY_MS);
                            Platform.runLater(() -> {
                                SceneState current = TabSceneManager.get();
                                // создаем хоум
                                TabSceneManager.switchTab("home");
                                SceneState next = TabSceneManager.create("/fxml/homeScene.fxml", controller -> {
                                    if (controller instanceof HomeSceneController homeSceneController) {

                                    }
                                });

                                if (current != null && next != null) {
                                    TabSceneManager.showCombined(next.getNode(), current.getNode());
                                    Animations.FadeOutScale(current.getNode(), 1.0, 1.5, Duration.millis(500), Duration.ZERO)
                                            .setOnFinished(e -> {
                                                TabSceneManager.showPanel();
                                                TabSceneManager.show();
                                            });
                                }
                            });
                        }

                        return null;
                    } catch (Exception e) {
                        int finalI = i;
                        Platform.runLater(() -> statusLabel.setText("Данные для фильтрации " + (finalI > 1 ? finalI + "x" : "")));
                        System.out.println("Failed to fetch filter params. Sleep...");

                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                Platform.runLater(() -> statusLabel.setText("Нет подключения к интернету"));
                return null;
            }
        };

        new Thread(loadFilterParams).start();
    }
}
