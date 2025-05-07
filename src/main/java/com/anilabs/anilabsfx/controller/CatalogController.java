package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.anilabs.anilabsfx.manager.TabSceneManager;
import com.anilabs.anilabsfx.model.Anime;
import com.anilabs.anilabsfx.model.FilterRequest;
import com.anilabs.anilabsfx.service.ApiService;
import com.anilabs.anilabsfx.utils.AnimeUtils;

import java.util.List;

import static com.anilabs.anilabsfx.utils.AnimeUtils.getStatusIdByName;

public class CatalogController {
    private final ApiService apiService = ApiService.getInstance();

    @FXML public StackPane sliderPane;
    @FXML private HBox sliderContent;
    @FXML private ScrollPane sliderScroll;

    private final Duration SLIDE_INTERVAL = Duration.seconds(4);
    private final int SLIDE_HEIGHT = 250;
    private Timeline sliderTimeline;

    @FXML public HBox lastUpdatedContainer;
    @FXML public HBox ongoingContainer;
    @FXML public HBox moviesContainer;


    public void initialize() {
        // Слайдер верхний
        sliderPane.setMinHeight(SLIDE_HEIGHT);
        sliderPane.setMaxHeight(SLIDE_HEIGHT);

        sliderScroll.setMinHeight(SLIDE_HEIGHT + 10);
        sliderScroll.setMaxHeight(SLIDE_HEIGHT + 10);


        // подгружаем слайды
        Platform.runLater(() -> {
            loadSlides();
            startAutoSlide();
        });

        // остальные тайтлы
        Platform.runLater(() -> {
            loadLastUpdated();
            loadOngoings();
            loadMovies();
        });


        // сохраняем аспект
        sliderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            double height = width * 9.0 / 16.0;
            sliderPane.setPrefHeight(height);
        });
    }


    // авто слайд
    private void startAutoSlide() {
        sliderTimeline = new Timeline();
        sliderTimeline.setCycleCount(Animation.INDEFINITE);

        Duration slideDuration = Duration.millis(500);
        Duration interval = SLIDE_INTERVAL;

        sliderTimeline.getKeyFrames().add(new KeyFrame(interval, e -> {
            double viewportWidth = sliderScroll.getViewportBounds().getWidth();
            double contentWidth = sliderContent.getWidth();

            double slideWidth = contentWidth / sliderContent.getChildren().size();

            double relativeStep = slideWidth / (contentWidth - viewportWidth);
            double current = sliderScroll.getHvalue();
            double next = current + relativeStep;

            if (next >= 1.0) next = 0;

            Timeline anim = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(sliderScroll.hvalueProperty(), current)),
                    new KeyFrame(slideDuration, new KeyValue(sliderScroll.hvalueProperty(), next, Interpolator.EASE_BOTH))
            );
            anim.play();
        }));

        sliderTimeline.play();
    }


    // Слайды
    private void loadSlides() {
        apiService.getSliderItems().thenAccept(animeList -> Platform.runLater(() -> {
                int I = 0;
                for (Anime anime : animeList) {
                    StackPane slide = createSlide(anime);
                    sliderContent.getChildren().add(slide);
                    Animations.FadeInScale(slide, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.millis((I++)*200));
                }
            }))
        .exceptionally(ex -> {
            System.out.println("Failed to load slides");
            return null;
        });
    }


    // Онгоинги
    private void loadOngoings() {
        FilterRequest request = new FilterRequest();
        request.setCount(20);
        request.setOffset(0);
        request.setStatuses(List.of(getStatusIdByName("ongoing")));

        apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
            // добавляем в контейнер
            for (int i = 0; i < animeList.size(); i++) {
                Node node = AnimeUtils.createVTile(animeList.get(i));
                ongoingContainer.getChildren().add(node);
                Animations.FadeInScale(node, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
            }
        })).exceptionally(e -> {
            System.out.println("Failed to fetch ongoings");
            return null;
        });

    }

    // Последние
    private void loadLastUpdated() {

        apiService.lastUpdatedAsync(0, 20).thenAccept(animeList -> Platform.runLater(() -> {
            // добавляем в контейнер
            for (int i = 0; i < animeList.size(); i++) {
                Node node = AnimeUtils.createVTile(animeList.get(i));
                lastUpdatedContainer.getChildren().add(node);
                Animations.FadeInScale(node, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
            }
        })).exceptionally(e -> {
            System.out.println("Failed to fetch last updated");
            return null;
        });
    }

    // Фильмы
    private void loadMovies() {
        FilterRequest request = new FilterRequest();
        request.setTypes(List.of(AnimeUtils.getTypeIdByName("movie")));
        request.setCount(20);
        request.setOffset(0);

        apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
            // добавляем в контейнер
            for (int i = 0; i < animeList.size(); i++) {
                Node node = AnimeUtils.createVTile(animeList.get(i));
                moviesContainer.getChildren().add(node);
                Animations.FadeInScale(node, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
            }
        })).exceptionally(e -> {
            System.out.println("Failed to fetch last updated");
            return null;
        });
    }


    // Слайд
    private StackPane createSlide(Anime anime) {
        double height = sliderPane.getMinHeight();
        double width = height * 16.0 / 9.0;

        StackPane slide = new StackPane();
        slide.setPrefSize(width, height);
        slide.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        slide.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        slide.getStyleClass().add("slider-item");


        Image image = new Image(anime.getSlider_poster_url(), true);

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setCache(true);

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0) {
                double imageRatio = image.getWidth() / image.getHeight();
                double containerRatio = width / height;

                if (imageRatio > containerRatio) imageView.setFitHeight(height);
                else imageView.setFitWidth(width);

                Animations.FadeInScale(imageView, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            }
        });



        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        slide.setClip(clip);


        Label title = new Label(anime.getTitle());
        title.getStyleClass().add("slider-title");
        StackPane.setAlignment(title, Pos.BOTTOM_LEFT);
        StackPane.setMargin(title, new Insets(0, 0, 20, 20));


        Region gradientOverlay = new Region();
        gradientOverlay.getStyleClass().add("slider-gradient-overlay");
        gradientOverlay.setPrefSize(width, 100);
        StackPane.setAlignment(gradientOverlay, Pos.BOTTOM_CENTER);


        HBox titleBox = new HBox(title);
        titleBox.getStyleClass().add("slider-title-box");
        titleBox.setMaxHeight(30);
        titleBox.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(titleBox, Pos.BOTTOM_CENTER);


        slide.setOnMouseClicked(e -> {
            TabSceneManager.createAndShowAnimated("/fxml/animeDetailScene.fxml", controller -> {
                if (controller instanceof AnimeDetailController detailController) {
                    detailController.setAnimeId(anime.getId());
                }
            });

        });

        slide.getChildren().addAll(imageView, gradientOverlay, titleBox);
        return slide;
    }


    // Фильтр
    public void goToFilter(MouseEvent mouseEvent) {
        TabSceneManager.createAndShowAnimated("/fxml/filterScene.fxml", controller -> {});
    }

    public void goToSchedule(MouseEvent mouseEvent) {
        TabSceneManager.createAndShowAnimated("/fxml/scheduleScene.fxml", controller -> {});
    }

    public void goToRandom(MouseEvent mouseEvent) {
        apiService.getRandom().thenAccept(animeId -> Platform.runLater(() -> {
            TabSceneManager.createAndShowAnimated("/fxml/animeDetailScene.fxml", controller -> {
                if (controller instanceof AnimeDetailController detailController) {
                    detailController.setAnimeId(animeId);
                    detailController.showRandomButton();
                }
            });
        })).exceptionally(e -> {
            System.out.println("Failed to get random");
            return null;
        });
    }
}
