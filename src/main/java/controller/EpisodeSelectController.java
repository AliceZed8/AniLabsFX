package controller;

import animation.Animations;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import manager.SceneState;
import manager.TabSceneManager;
import model.Anime;
import model.AnimeTranslation;
import org.kordamp.ikonli.javafx.FontIcon;
import service.ApiService;
import service.DbService;
import utils.AnimeUtils;

import java.util.Map;


public class EpisodeSelectController {
    private final ApiService apiService = ApiService.getInstance();
    private final DbService dbService = DbService.getInstance();

    @FXML private Label animeTitle;

    @FXML private VBox translatorContainer;
    @FXML private VBox episodeContainer;

    @FXML private ProgressIndicator translationsLoadingIndicator;



    public void initialize() {

    }

    public void updateEpisodesContainer(Anime anime, AnimeTranslation translation) {
        episodeContainer.getChildren().clear();

        // получим историю просмотра
        Map<Integer, Long> progressMap = dbService.getWatchProgress(anime.getId(), translation.getTranslator_id());

        for (int i: translation.getEpisodes()) {
            HBox episodeBox = new HBox();
            episodeBox.getStyleClass().add("episode-item");
            episodeBox.setMaxHeight(50);
            episodeBox.setMinHeight(50);
            episodeBox.setSpacing(10);


            FontIcon checkIcon = new FontIcon("far-check-circle");
            checkIcon.setIconSize(20);
            checkIcon.setIconColor(Color.WHITE);
            checkIcon.setVisible(false);


            String episodeText = i == 0 ? "Смотреть" : i + " эпизод";
            Label episodeLabel = new Label(episodeText);
            episodeLabel.getStyleClass().add("episode-label");

            VBox episodeTitleBox = new VBox(episodeLabel);
            if (progressMap.containsKey(i)) {
                Label watchProgressLabel = new Label("просмотрено до " + AnimeUtils.formatTime(progressMap.get(i)));
                watchProgressLabel.getStyleClass().add("episode-watch-history-label");
                episodeTitleBox.getChildren().add(watchProgressLabel);
                checkIcon.setVisible(true);
            }


            FontIcon openExternalIcon = new FontIcon("fas-external-link-alt");
            openExternalIcon.setIconSize(20);
            openExternalIcon.setIconColor(Color.WHITE);

            HBox openExternalWrapper = new HBox(openExternalIcon);
            openExternalWrapper.setAlignment(Pos.CENTER);


            openExternalWrapper.setOnMouseClicked(e -> {
                e.consume();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playerScene.fxml"));
                    Stage stage = new Stage();
                    stage.setScene(new Scene(loader.load()));

                    PlayerController controller = loader.getController();
                    controller.setAnime(anime);
                    controller.setTranslation(translation);
                    controller.setEpisode(i);
                    controller.load();

                    stage.setTitle("Плеер");
                    stage.setMinWidth(640);
                    stage.setMinHeight(360);
                    stage.setWidth(640);
                    stage.setHeight(360);

                    stage.show();
                } catch (Exception ee) {
                    System.out.println("Failed to open player in new window");
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            episodeBox.getChildren().addAll(checkIcon, episodeTitleBox, spacer, openExternalWrapper);
            episodeContainer.getChildren().add(episodeBox);

            int delay = i * 100;
            Platform.runLater(() -> animateSlideInTopWithDelay(episodeBox, delay));

            episodeBox.setOnMouseClicked(ee -> {
                // если клик по иконке
                if (ee.getTarget() == openExternalIcon || openExternalWrapper.getChildren().contains(ee.getTarget())) return;

                try {
                    TabSceneManager.createAndShowAnimated("/fxml/playerScene.fxml", controller -> {
                        if (controller instanceof PlayerController playerController) {
                            playerController.setAnime(anime);
                            playerController.setTranslation(translation);
                            playerController.setEpisode(i);
                            playerController.load();
                        }
                    });
                    TabSceneManager.hidePanel();
                } catch (Exception eee) {
                    System.out.println("Failed to load playerScene");
                }
            });

        }
    }

    public void setAnime(Anime anime) {
        animeTitle.setText(anime.getTitle());
        animateSlideInTopWithDelay(animeTitle, 0);

        Platform.runLater(() -> {
            System.out.println("Fetch translations anime_id: " + anime.getId());
            translationsLoadingIndicator.setVisible(true);

            apiService.getAnimeTranslationsAsync(anime.getId())
                    .thenAccept(animeTranslations -> Platform.runLater(() -> {
                        if (animeTranslations == null || animeTranslations.isEmpty()) return;
                        System.out.println("Fetched");

                        int I = 0;
                        for (AnimeTranslation translation: animeTranslations) {
                            HBox translatorBox = new HBox();
                            translatorBox.getStyleClass().add("translator-item");

                            String translationInfo = translation.getTranslator_name();
                            if (!translation.getEpisodes().isEmpty() && (translation.getEpisodes().getFirst() != 0)) {
                                translationInfo += " · " + translation.getEpisodes().size() + " эп.";
                            }
                            Label translatorLabel = new Label(translationInfo);

                            translatorLabel.getStyleClass().add("translator-label");

                            translatorBox.getChildren().add(translatorLabel);
                            translatorContainer.getChildren().add(translatorBox);

                            translatorBox.setOnMouseClicked(e -> {
                                for (Node node: translatorContainer.getChildren()) {
                                    node.getStyleClass().remove("translator-active");
                                }
                                translatorBox.getStyleClass().add("translator-active");

                                updateEpisodesContainer(anime, translation);
                            });
                            int delay = (I++)*100;
                            Platform.runLater(() -> animateSlideInTopWithDelay(translatorBox, delay));
                        }

                        translationsLoadingIndicator.setVisible(false);
                    })).exceptionally(ex -> {
                        System.out.println("Failed to get translations");
                        return null;
                    });
        });
    }

    private void animateSlideInTopWithDelay(Node node, Integer delay) {
        node.setOpacity(0);
        node.setTranslateY(-50);

        TranslateTransition translate = new TranslateTransition(Duration.millis(500), node);
        translate.setFromY(-50);
        translate.setToY(0);

        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition slideIn = new ParallelTransition(translate, fade);
        slideIn.setDelay(Duration.millis(delay));
        slideIn.play();
    }

    public void goBack() {
        SceneState current = TabSceneManager.get();
        TabSceneManager.goBack();
        SceneState previous = TabSceneManager.get();

        if (current == null || previous == null) return;

        // анимируем
        TabSceneManager.showCombined(previous.getNode(), current.getNode());
        Animations.FadeOutSlideVertical(current.getNode(), 0, 500, Duration.millis(400), Duration.ZERO)
                .setOnFinished(e -> TabSceneManager.show());
    }
}
