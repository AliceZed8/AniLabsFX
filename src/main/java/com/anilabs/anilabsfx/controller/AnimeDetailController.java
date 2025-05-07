package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.anilabs.anilabsfx.manager.SceneState;
import com.anilabs.anilabsfx.manager.TabSceneManager;
import com.anilabs.anilabsfx.model.*;
import com.anilabs.anilabsfx.service.ApiService;
import com.anilabs.anilabsfx.utils.AnimeUtils;

public class AnimeDetailController {
    private final ApiService apiService = ApiService.getInstance();



    private final Integer POSTER_WIDTH = 350;
    private final Integer SCREENSHOT_HEIGHT = 200;

    @FXML private StackPane mainPane;
    @FXML private ScrollPane detailScroll;

    @FXML private HBox infoBlockHorizontal;
    @FXML private VBox infoBlockVertical;

    @FXML private VBox infoBoxHorizontal;
    @FXML private VBox infoBoxVertical;

    @FXML private ImageView animePosterImage;
    @FXML private ImageView animePosterImageVertical;

    @FXML private Label animeTitle;
    @FXML private Label animeTitleVertical;

    @FXML private Label animeOrigTitle;
    @FXML private Label animeOrigTitleVertical;

    @FXML private Label animeType;
    @FXML private Label animeTypeVertical;

    @FXML private Label animeSeason;
    @FXML private Label animeSeasonVertical;


    @FXML private HBox animeEpisodesBox;
    @FXML private HBox animeEpisodesBoxVertical;

    @FXML private Label animeEpisodesInfo;
    @FXML private Label animeEpisodesInfoVertical;


    @FXML private HBox animeOngoingBox;
    @FXML private HBox animeOngoingBoxVertical;

    @FXML private Label animeOngoingDay;
    @FXML private Label animeOngoingDayVertical;

    @FXML private FlowPane animeStudios;
    @FXML private FlowPane animeStudiosVertical;

    @FXML private HBox animeStudiosBox;
    @FXML private HBox animeStudiosBoxVertical;

    @FXML private HBox animeGenresBox;
    @FXML private HBox animeGenresBoxVertical;

    @FXML private FlowPane animeGenres;
    @FXML private FlowPane animeGenresVertical;

    @FXML private Label animeDescription;

    @FXML private HBox screenshotsContainer;

    @FXML private Button watchButton;
    @FXML private Button watchButtonVertical;

    @FXML private HBox goToRandomButton;

    public void initialize() {
        // бинды для вертикальной версии
        infoBlockVertical.visibleProperty().bind(infoBlockHorizontal.visibleProperty().not());
        infoBlockVertical.managedProperty().bind(infoBlockHorizontal.managedProperty().not());

        animePosterImageVertical.fitWidthProperty().bind(animePosterImage.fitWidthProperty());
        animePosterImageVertical.smoothProperty().bind(animePosterImage.smoothProperty());
        animePosterImageVertical.cacheProperty().bind(animePosterImage.cacheProperty());
        animePosterImageVertical.preserveRatioProperty().bind(animePosterImage.preserveRatioProperty());
        animePosterImageVertical.imageProperty().bind(animePosterImage.imageProperty());

        animeTitleVertical.textProperty().bind(animeTitle.textProperty());
        animeOrigTitleVertical.textProperty().bind(animeOrigTitle.textProperty());

        animeTypeVertical.onMouseClickedProperty().bind(animeType.onMouseClickedProperty());
        animeTypeVertical.textProperty().bind(animeType.textProperty());
        animeSeasonVertical.textProperty().bind(animeSeason.textProperty());


        animeEpisodesBoxVertical.visibleProperty().bind(animeEpisodesBox.visibleProperty());
        animeEpisodesBoxVertical.visibleProperty().bind(animeEpisodesBox.managedProperty());
        animeEpisodesInfoVertical.textProperty().bind(animeEpisodesInfo.textProperty());



        animeOngoingBoxVertical.onMouseClickedProperty().bind(animeOngoingBox.onMouseClickedProperty());
        animeOngoingBoxVertical.visibleProperty().bind(animeOngoingBox.visibleProperty());
        animeOngoingBoxVertical.managedProperty().bind(animeOngoingBox.managedProperty());
        animeOngoingDayVertical.textProperty().bind(animeOngoingDay.textProperty());

        animeGenresBoxVertical.visibleProperty().bind(animeGenresBox.visibleProperty());
        animeGenresBoxVertical.managedProperty().bind(animeGenresBox.managedProperty());

        animeStudiosBoxVertical.visibleProperty().bind(animeStudiosBox.visibleProperty());
        animeStudiosBoxVertical.managedProperty().bind(animeStudiosBox.managedProperty());


        // показываем вертикальную версию
        detailScroll.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < 700) {
                infoBlockHorizontal.setVisible(false);
                infoBlockHorizontal.setManaged(false);
            } else {
                infoBlockHorizontal.setVisible(true);
                infoBlockHorizontal.setManaged(true);
            }
        });
    }

    // включение рандома (из каталога)
    public void showRandomButton() {
        goToRandomButton.setVisible(true);
    }


    // получить аниме по ID
    public void setAnimeId(Integer animeId) {
        apiService.getAnimeAsync(animeId).thenAccept(anime -> Platform.runLater(() -> {
            setAnime(anime);
        })).exceptionally(e -> {
            System.out.println("Failed to get anime");
            return null;
        });
    }

    public void setAnime(Anime anime) {
        // постер
        {
            animePosterImage.setFitWidth(POSTER_WIDTH);
            animePosterImage.setCache(true);
            animePosterImage.setSmooth(true);
            animePosterImage.setPreserveRatio(true);


            Image image = new Image(anime.getPosterUrl(), true);
            animePosterImage.setImage(image);

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    double width = animePosterImage.getBoundsInParent().getWidth();
                    double height = animePosterImage.getBoundsInParent().getHeight();

                    Rectangle clipH = new Rectangle(width, height);
                    clipH.setArcWidth(32);
                    clipH.setArcHeight(32);
                    animePosterImage.setClip(clipH);

                    Rectangle clipV = new Rectangle(width, height);
                    clipV.setArcWidth(32);
                    clipV.setArcHeight(32);
                    animePosterImageVertical.setClip(clipV);


                    Animations.FadeInScale(animePosterImage, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
                    Animations.FadeInScale(animePosterImageVertical, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
                }
            });
        }


        // Названия
        animeTitle.setText(anime.getTitle());
        animeOrigTitle.setText(anime.getOrigTitle());

        // тип и год
        String year = anime.getYear().toString();
        String type = AnimeUtils.typeMap.getOrDefault(anime.getTypeName().toLowerCase(), anime.getTypeName());
        animeType.setText(type);
        animeSeason.setText(", " + year);

        // При клике открываем фильтр
        animeType.setOnMouseClicked(e -> {
            // создаем тип
            AnimeType animeType_ = new AnimeType();
            animeType_.setId(anime.getType_id());
            animeType_.setType_name(anime.getTypeName());

            // создаем новое состояние
            TabSceneManager.createAndShowAnimated("/fxml/filterScene.fxml", controller -> {
                if (controller instanceof FilterController filterController) {
                    filterController.selectType(animeType_);
                }
            });
        });

        // Информация об эпизодах
        String episodesInfo;
        if (anime.getStatusName().equals("ongoing")) {
            episodesInfo = anime.getEp_aired().toString() + " из " + (anime.getEp_total() != 0 ? anime.getEp_total().toString() : "?") + " эп.";
        } else episodesInfo = anime.getEp_total().toString() + " эп.";
        if (anime.getDuration() != null) episodesInfo += " по ~" + anime.getDuration().toString() + " мин.";
        animeEpisodesInfo.setText(episodesInfo);

        // если фильм, то не показываем
        if (anime.getTypeName().equals("movie")) {
            animeEpisodesBox.setVisible(false);
            animeEpisodesBox.setManaged(false);
        }

        // Если онгоинг, добавляем также день недели след эпизода
        if (anime.getStatusName().equals("ongoing")) {
            animeOngoingBox.setVisible(true);
            animeOngoingBox.setManaged(true);
            if (anime.getNext_ep_at() != null) animeOngoingDay.setText(", " + AnimeUtils.getNextEpisodeDay(anime.getNext_ep_at()));


            // клип по боксу, кидаем в фильтр
            animeOngoingBox.setOnMouseClicked(e -> {
                // создаем статус
                AnimeStatus status = new AnimeStatus();
                status.setId(anime.getStatus_id());
                status.setStatus_name(anime.getStatusName());

                TabSceneManager.createAndShowAnimated("/fxml/filterScene.fxml", controller -> {
                    if (controller instanceof FilterController filterController) {
                        filterController.selectStatus(status);
                    }
                });
            });
        }


        // Студии
        for (Studio studio: anime.getStudios()) {
            Label labelH = new Label(studio.getStudio_name());
            labelH.getStyleClass().add("anime-tag");

            // клип по студии
            labelH.setOnMouseClicked(event -> {
                TabSceneManager.createAndShowAnimated("/fxml/filterScene.fxml", controller -> {
                    if (controller instanceof FilterController filterController) {
                        filterController.selectStudio(studio);
                    }
                });
            });

            animeStudios.getChildren().add(labelH);

            // вертикальная версия
            Label labelV = new Label(studio.getStudio_name());
            labelV.getStyleClass().add("anime-tag");
            labelV.onMouseClickedProperty().bind(labelH.onMouseClickedProperty());
            animeStudiosVertical.getChildren().add(labelV);
        }


        // Жанры
        for (Genre genre: anime.getGenres()) {
            Label labelH = new Label(genre.getGenre_name());
            labelH.getStyleClass().add("anime-tag");

            // клик по жанру
            labelH.setOnMouseClicked(event -> {
                TabSceneManager.createAndShowAnimated("/fxml/filterScene.fxml", controller -> {
                    if (controller instanceof FilterController filterController) {
                        filterController.selectGenre(genre);
                    }
                });
            });
            animeGenres.getChildren().add(labelH);


            // вертикальная версия
            Label labelV = new Label(genre.getGenre_name());
            labelV.getStyleClass().add("anime-tag");
            labelV.onMouseClickedProperty().bind(labelH.onMouseClickedProperty());
            animeGenresVertical.getChildren().add(labelV);
        }

        // Если нет жанров или студий
        if (anime.getGenres().isEmpty()) {
            animeGenresBox.setVisible(false);
            animeGenresBox.setManaged(false);
        }

        if (anime.getStudios().isEmpty()) {
            animeStudiosBox.setVisible(false);
            animeStudiosBox.setManaged(false);
        }


        // Анимируем оба блока
        Animations.FadeInSlideVertical(infoBlockVertical, -50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
        Animations.FadeInSlideVertical(infoBlockHorizontal, -50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);


        // Описание
        animeDescription.setText(anime.getDescription() != null ? AnimeUtils.cleanText(anime.getDescription()) : "Описание отсутсвует.");
        Animations.FadeInSlideHorizontal(animeDescription, -50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);

        // Скриншоты
        screenshotsContainer.getChildren().clear();
        for (String screenshotUrl : anime.getScreenshots()) {
            ImageView imageView = new ImageView();

            Image image = new Image(screenshotUrl, true);
            imageView.setImage(image);
            imageView.setFitHeight(SCREENSHOT_HEIGHT);
            imageView.setPreserveRatio(true);

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    double width = imageView.getBoundsInParent().getWidth();
                    double height = imageView.getBoundsInParent().getHeight();

                    Rectangle clip = new Rectangle(width, height);
                    clip.setArcWidth(32);
                    clip.setArcHeight(32);
                    imageView.setClip(clip);
                    Animations.FadeInScale(imageView, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
                }
            });
            screenshotsContainer.getChildren().add(imageView);
        }

        // Смотреть
        watchButton.setOnAction(e -> {
            SceneState previous = TabSceneManager.get();
            SceneState current = TabSceneManager.create("/fxml/episodeSelectScene.fxml", controller -> {
                if (controller instanceof EpisodeSelectController episodeSelectController) {
                    episodeSelectController.setAnime(anime);
                }
            });

            if (previous != null && current != null) {
                // анимируем
                TabSceneManager.showCombined(previous.getNode(), current.getNode());
                Animations.FadeInSlideVertical(current.getNode(), 500, 0, Duration.millis(400), Duration.ZERO)
                        .setOnFinished(ee -> TabSceneManager.show());
            }
        });

        watchButtonVertical.onActionProperty().bind(watchButton.onActionProperty());

    }


    @FXML
    private void goBack() {
        TabSceneManager.goBackAndShowAnimated();
    }


    // рандом
    public void goToRandom(MouseEvent mouseEvent) {
        apiService.getRandom().thenAccept(animeId -> Platform.runLater(() -> {
            TabSceneManager.createAndShowAnimated("/fxml/animeDetailScene.fxml", controller -> {
                if (controller instanceof AnimeDetailController detailController) {
                    detailController.setAnimeId(animeId);
                    if (goToRandomButton.isVisible()) detailController.showRandomButton();
                }
            });

        })).exceptionally(e -> {
            System.out.println("Failed to get random");
            return null;
        });
    }
}
