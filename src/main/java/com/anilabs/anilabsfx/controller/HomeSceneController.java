package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.anilabs.anilabsfx.model.*;
import org.kordamp.ikonli.javafx.FontIcon;
import com.anilabs.anilabsfx.service.ApiService;
import com.anilabs.anilabsfx.utils.AnimeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anilabs.anilabsfx.utils.AnimeUtils.getStatusIdByName;
import static com.anilabs.anilabsfx.utils.AnimeUtils.getTypeIdByName;

public class HomeSceneController {
    private final ApiService apiService = ApiService.getInstance();

    // для категорий
    @FXML private HBox tabHeader;
    @FXML private StackPane tabContentContainer;
    @FXML private Rectangle tabIndicator;

    private final List<String> categories = List.of("Последнее", "Онгоинги", "Фильмы", "Спешл", "OVA", "ONA");
    private final Map<String, Node> categoryContentMap = new HashMap<>();


    // настройки для скролла
    private final double MIN_V_SCROLL = 0.1;
    private final double MAX_V_LOAD_SCROLL = 0.99;
    private final int PAGE_SIZE = 20;



    public void initialize() {
        tabHeader.setMouseTransparent(false);
        tabHeader.setPickOnBounds(false);

        // добавляем кнопки категорий
        for (String category: categories) {
            Button tabButton = new Button(category);

            tabButton.getStyleClass().add("tab-button");
            tabButton.setOnAction(e -> Platform.runLater(() -> switchToCategory(category)));
            tabHeader.getChildren().add(tabButton);
        }

        switchToCategory(categories.getFirst());
    }


    // для сдвига индикатора
    private void moveTabIndicatorTo(Button targetButton) {
        double targetX = targetButton.localToScene(0, 0).getX() - tabHeader.localToScene(0, 0).getX();
        double targetWidth = targetButton.getWidth();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(tabIndicator.translateXProperty(), tabIndicator.getTranslateX()),
                        new KeyValue(tabIndicator.widthProperty(), tabIndicator.getWidth())
                ),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(tabIndicator.translateXProperty(), targetX, Interpolator.EASE_BOTH),
                        new KeyValue(tabIndicator.widthProperty(), targetWidth, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }



    private void switchToCategory(String category) {
        Node next = categoryContentMap.computeIfAbsent(category, this::createCategoryContent);

        // сдвигаем индикатор
        int newIndex = categories.indexOf(category);
        Button targetButton = (Button) tabHeader.getChildren().get(newIndex);
        Platform.runLater(() -> moveTabIndicatorTo(targetButton));


        // замещаем контейнер
        tabContentContainer.getChildren().setAll(next);

        // если контейнер пуст
        StackPane pane = (StackPane) next;
        ScrollPane scroll = (ScrollPane) pane.getChildren().getFirst();
        FlowPane container = (FlowPane) scroll.getContent();
        if (container.getChildren().isEmpty() && (ApiService.filterParams != null)) loadContent(category);
    }

    private Node createCategoryContent(String category) {
        StackPane stackPane = new StackPane();

        // основной скролл
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);

        // контейнер
        FlowPane container = new FlowPane();
        container.setPadding(new Insets(50, 0, 0, 0));
        container.getStyleClass().add("anime-flow-pane");

        scroll.setContent(container);

        // индикатор
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.getStyleClass().add("loading-indicator");
        indicator.setMaxSize(30, 30);
        indicator.setVisible(false);
        indicator.setMouseTransparent(true);
        StackPane.setAlignment(indicator, Pos.BOTTOM_CENTER);

        // кнопка наверх
        FontIcon arrowUpIcon = new FontIcon("fas-arrow-up");
        arrowUpIcon.setIconSize(20);
        arrowUpIcon.setIconColor(Color.WHITE);

        HBox scrollToTopButton = new HBox(arrowUpIcon);
        scrollToTopButton.setMaxSize(40, 40);
        scrollToTopButton.setAlignment(Pos.CENTER);
        scrollToTopButton.getStyleClass().add("scroll-to-top-btn");
        StackPane.setAlignment(scrollToTopButton, Pos.BOTTOM_RIGHT);
        scrollToTopButton.setVisible(false);

        stackPane.getChildren().addAll(scroll, indicator, scrollToTopButton);

        // кнопка наверх
        scrollToTopButton.setOnMouseClicked(e -> {
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(scroll.vvalueProperty(), 0, Interpolator.EASE_OUT);
            KeyFrame kf = new KeyFrame(Duration.millis(500), kv); // за 0.5 сек наверх
            timeline.getKeyFrames().add(kf);
            timeline.play();
        });

        scroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > MIN_V_SCROLL && !scrollToTopButton.isVisible()) {
                scrollToTopButton.setVisible(true);
                Animations.FadeInScale(scrollToTopButton, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            } else if (newVal.doubleValue() <= MIN_V_SCROLL && scrollToTopButton.isVisible()) {
                Animations.FadeOutScale(scrollToTopButton, 1.0, 0.5, Animations.DEFAULT_DURATION, Duration.ZERO)
                        .setOnFinished(e -> Platform.runLater(() -> scrollToTopButton.setVisible(false)));
            }

            boolean isEmpty = (container.getChildren().size() % PAGE_SIZE != 0);
            if (newVal.doubleValue() > MAX_V_LOAD_SCROLL && !isEmpty && !indicator.isVisible()) loadContent(category);
        });

        return stackPane;
    }

    private void loadContent(String category) {
        // извлекаем контейнер и индикатор
        StackPane pane = (StackPane) categoryContentMap.get(category);
        ScrollPane scroll = (ScrollPane) pane.getChildren().getFirst();
        ProgressIndicator indicator = (ProgressIndicator) pane.getChildren().get(1);
        FlowPane container = (FlowPane) scroll.getContent();

        // если больше нечего грузить
        boolean isEmpty = (container.getChildren().size() % PAGE_SIZE != 0);
        if (indicator.isVisible() || isEmpty) return;

        indicator.setVisible(true);
        int offset = container.getChildren().size();

        System.out.printf("Load content: tab %s offset %d count %d\n", category, offset, PAGE_SIZE);


        // создаем запрос
        FilterRequest request = new FilterRequest();
        request.setCount(PAGE_SIZE);
        request.setOffset(offset);

        switch (category) {
            case "Последнее" -> request.setOffset(offset);
            case "Онгоинги" -> request.setStatuses(List.of(getStatusIdByName("ongoing")));
            case "Фильмы" -> request.setTypes(List.of(getTypeIdByName("movie")));
            case "Анонсы" -> request.setStatuses(List.of(getStatusIdByName("anons")));
            case "Спешл" -> request.setTypes(List.of(getTypeIdByName("special")));
            case "OVA" -> request.setTypes(List.of(getTypeIdByName("ova")));
            case "ONA" -> request.setTypes(List.of(getTypeIdByName("ona")));
            default -> {
                return;
            }
        }


        apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
            // добавляем в контейнер
            for (int i = 0; i < animeList.size(); i++) {
                Node node = AnimeUtils.createAnimeTile(animeList.get(i));
                container.getChildren().add(node);
                Animations.FadeInSlideVertical(node, -100, 0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
            }
            indicator.setVisible(false);
        })).exceptionally(e -> {
            System.out.println("Failed to fetch latest");
            return null;
        });
    }
}
