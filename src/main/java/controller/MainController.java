package controller;


import animation.Animations;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.TabSceneManager;
import model.Anime;

import java.util.List;


public class MainController {
    @FXML private HBox navPanel;
    @FXML private StackPane contentPane;

    @FXML private VBox homeNavItem;
    @FXML private VBox searchNavItem;
    @FXML private VBox catalogNavItem;

    @FXML private VBox bookmarkNavItem;

    @FXML private VBox profileNavItem;


    private final List<String> tabsList = List.of("home", "search");



    @FXML
    public void initialize() {
        System.out.println("Init MainController");

        System.out.println("Init TabSceneManager");
        TabSceneManager.initialize(contentPane, navPanel);


        if (!homeNavItem.getStyleClass().contains("active")) homeNavItem.getStyleClass().add("active");


        // добавляем сцену загрузки
        TabSceneManager.switchTab("loading");
        TabSceneManager.create("/fxml/loadingScene.fxml", null);
        TabSceneManager.hidePanel();
        TabSceneManager.show();






        homeNavItem.setOnMouseClicked(e -> {
            unactiveAll();
            homeNavItem.getStyleClass().add("active");

            TabSceneManager.switchTab("home");
            if (TabSceneManager.get() == null) {
                TabSceneManager.create("/fxml/homeScene.fxml", null);
            }
            TabSceneManager.show();
        });

        searchNavItem.setOnMouseClicked(e -> {
            unactiveAll();
            searchNavItem.getStyleClass().add("active");

            TabSceneManager.switchTab("search");
            if (TabSceneManager.get() == null) {
                TabSceneManager.create("/fxml/searchScene.fxml", null);
            }
            TabSceneManager.show();
        });

        catalogNavItem.setOnMouseClicked(e -> {
            unactiveAll();
            catalogNavItem.getStyleClass().add("active");

            TabSceneManager.switchTab("catalog");
            if (TabSceneManager.get() == null) {
                TabSceneManager.create("/fxml/catalogScene.fxml", null);
            }
            TabSceneManager.show();
        });
    }

    private void unactiveAll() {
        homeNavItem.getStyleClass().remove("active");
        searchNavItem.getStyleClass().remove("active");
        catalogNavItem.getStyleClass().remove("active");
    }

}