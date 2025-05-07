package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import com.anilabs.anilabsfx.model.FilterRequest;
import com.anilabs.anilabsfx.model.SearchRequest;
import com.anilabs.anilabsfx.service.ApiService;
import com.anilabs.anilabsfx.utils.AnimeUtils;

public class SearchController {
    private final ApiService apiService = ApiService.getInstance();


    @FXML private StackPane mainPane;

    @FXML private ScrollPane searchScroll;
    @FXML private FlowPane container;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private Button scrollToTopButton;

    @FXML private HBox searchHeader;
    @FXML private TextField searchField;

    private final double MIN_V_SCROLL = 0.1;
    private final double MAX_V_LOAD_SCROLL = 0.99;
    private final int PAGE_SIZE = 20;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(500));


    @FXML public void initialize() {
        searchHeader.setMouseTransparent(false);
        searchHeader.setPickOnBounds(false);
        container.setPadding(new Insets(50, 0, 0, 0));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.stop();

            searchDebounce.setOnFinished( e -> {
                if (newVal.length() >= 3 || newVal.isEmpty()) {
                    container.getChildren().clear();
                    search();
                }
            });

            searchDebounce.playFromStart();
        });

        scrollToTopButton.setOnAction(e -> {
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(searchScroll.vvalueProperty(), 0, Interpolator.EASE_OUT);
            KeyFrame kf = new KeyFrame(Duration.millis(500), kv); // за 0.5 сек наверх
            timeline.getKeyFrames().add(kf);
            timeline.play();
        });

        searchScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > MIN_V_SCROLL && !scrollToTopButton.isVisible()) {
                scrollToTopButton.setVisible(true);
                Animations.FadeInScale(scrollToTopButton, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);

            } else if (newVal.doubleValue() <= MIN_V_SCROLL && scrollToTopButton.isVisible()) {
                Animations.FadeOutScale(scrollToTopButton, 1.0, 0.5, Animations.DEFAULT_DURATION, Duration.ZERO)
                        .setOnFinished(e -> Platform.runLater(() -> scrollToTopButton.setVisible(false)));
            }

            boolean isEmpty = (container.getChildren().size() % PAGE_SIZE != 0);
            if (newVal.doubleValue() > MAX_V_LOAD_SCROLL && !isEmpty && !loadingIndicator.isVisible()) search();
        });

        search();
    }

    private void search() {
        boolean isEmpty = (container.getChildren().size() % PAGE_SIZE != 0);
        if (loadingIndicator.isVisible() || isEmpty) return;

        loadingIndicator.setVisible(true);
        String query = searchField.getText();
        int offset = container.getChildren().size();

        System.out.printf("Search... query %s offset %d count %d\n", query, offset, PAGE_SIZE);

        // если пустой запрос
        if (query.isEmpty()) {
            FilterRequest request = new FilterRequest();
            request.setCount(PAGE_SIZE);
            request.setOffset(offset);

            apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
                // добавляем в контейнер
                for (int i = 0; i < animeList.size(); i++) {
                    Node node = AnimeUtils.createAnimeTile(animeList.get(i));
                    container.getChildren().add(node);
                    Animations.FadeInSlideVertical(node, -100, 0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
                }
                loadingIndicator.setVisible(false);
            })).exceptionally(e -> {
                System.out.println("Failed to search");
                Platform.runLater(() -> loadingIndicator.setVisible(false));
                return null;
            });
        } else {
            SearchRequest request = new SearchRequest();
            request.setQuery(query);
            request.setCount(PAGE_SIZE);
            request.setOffset(offset);

            apiService.searchAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
                // добавляем в контейнер
                for (int i = 0; i < animeList.size(); i++) {
                    Node node = AnimeUtils.createAnimeTile(animeList.get(i));
                    container.getChildren().add(node);
                    Animations.FadeInSlideVertical(node, -100, 0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
                }
                loadingIndicator.setVisible(false);
            })).exceptionally(e -> {
                System.out.println("Failed to search " + query);
                Platform.runLater(() -> loadingIndicator.setVisible(false));
                return null;
            });
        }
    }
}
