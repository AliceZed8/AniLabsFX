package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import com.anilabs.anilabsfx.manager.SceneState;
import com.anilabs.anilabsfx.manager.TabSceneManager;
import com.anilabs.anilabsfx.model.FilterRequest;
import com.anilabs.anilabsfx.service.ApiService;
import com.anilabs.anilabsfx.utils.AnimeUtils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anilabs.anilabsfx.utils.AnimeUtils.getStatusIdByName;

public class ScheduleController {
    private final ApiService apiService = ApiService.getInstance();
    @FXML private FlowPane mondayContainer;
    @FXML private FlowPane tuesdayContainer;
    @FXML private FlowPane wednesdayContainer;
    @FXML private FlowPane thursdayContainer;
    @FXML private FlowPane fridayContainer;
    @FXML private FlowPane saturdayContainer;
    @FXML private FlowPane sundayContainer;

    Map<DayOfWeek, FlowPane> containerMap;

    public void initialize() {
        containerMap = Map.of(
                DayOfWeek.MONDAY, mondayContainer,
                DayOfWeek.TUESDAY, tuesdayContainer,
                DayOfWeek.WEDNESDAY, wednesdayContainer,
                DayOfWeek.THURSDAY, thursdayContainer,
                DayOfWeek.FRIDAY, fridayContainer,
                DayOfWeek.SATURDAY, saturdayContainer,
                DayOfWeek.SUNDAY, sundayContainer
        );

        fetchOngoings();
    }

    private void fetchOngoings() {
        FilterRequest request = new FilterRequest();
        request.setCount(200);
        request.setOffset(0);
        request.setStatuses(List.of(getStatusIdByName("ongoing")));

        apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
            // Сортируем аниме по дням недели
            Map<DayOfWeek, List<Node>> dayToAnimeMap = new HashMap<>();
            for (int i = 0; i < animeList.size(); i++) {
                if (animeList.get(i).getNext_ep_at() == null) continue;

                ZonedDateTime zdt = ZonedDateTime.parse(animeList.get(i).getNext_ep_at());
                DayOfWeek day = zdt.getDayOfWeek();

                Node node = AnimeUtils.createAnimeTile(animeList.get(i));

                dayToAnimeMap.computeIfAbsent(day, k -> new ArrayList<>()).add(node);
            }


            for (DayOfWeek day : DayOfWeek.values()) {
                FlowPane container = containerMap.get(day);
                List<Node> nodes = dayToAnimeMap.get(day);

                if (container != null && nodes != null) {
                    for (int i = 0; i < nodes.size(); i++) {
                        Node node = nodes.get(i);
                        container.getChildren().add(node);
                        // Применяем анимацию
                        Animations.FadeInScale(node, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
                    }
                }
            }
        })).exceptionally(e -> {
            System.out.println("Failed to fetch ongoings");
            return null;
        });
    }

























    public void goBack(MouseEvent mouseEvent) {
        SceneState current = TabSceneManager.get();
        TabSceneManager.goBack();
        SceneState previous = TabSceneManager.get();

        if (current == null || previous == null) return;

        TabSceneManager.showCombined(previous.getNode(), current.getNode());
        Animations.FadeOutSlideHorizontal(current.getNode(), 0, 500, Duration.millis(400), Duration.ZERO)
                .setOnFinished(e -> TabSceneManager.show());
    }
}
