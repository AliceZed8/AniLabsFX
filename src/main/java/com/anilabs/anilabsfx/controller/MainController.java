package com.anilabs.anilabsfx.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import com.anilabs.anilabsfx.manager.SceneState;
import com.anilabs.anilabsfx.manager.TabSceneManager;

import java.util.List;


public class MainController {
    @FXML private HBox navPanel;
    @FXML private StackPane contentPane;

    @FXML private VBox homeNavItem;
    @FXML private VBox searchNavItem;
    @FXML private VBox catalogNavItem;

    // на апдейт
    @FXML private VBox favoriteNavItem;
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
            TabSceneManager.switchTab("home");
            SceneState state = TabSceneManager.get();
            if (state == null) TabSceneManager.create("/fxml/homeScene.fxml", null);
            else {
                // если итак активная
                if (homeNavItem.getStyleClass().contains("active")) TabSceneManager.goToFirstAndShowAnimated();
                else TabSceneManager.show();
            }

            // ресет вкладок
            unactiveAll();
            homeNavItem.getStyleClass().add("active");
        });

        searchNavItem.setOnMouseClicked(e -> {

            TabSceneManager.switchTab("search");
            SceneState state = TabSceneManager.get();
            if (state == null) TabSceneManager.create("/fxml/searchScene.fxml", null);
            else {
                // если итак активная
                if (searchNavItem.getStyleClass().contains("active")) TabSceneManager.goToFirstAndShowAnimated();
                else TabSceneManager.show();
            }

            // ресет вкладок
            unactiveAll();
            searchNavItem.getStyleClass().add("active");
        });

        catalogNavItem.setOnMouseClicked(e -> {
            TabSceneManager.switchTab("catalog");
            SceneState state = TabSceneManager.get();
            if (state == null) TabSceneManager.create("/fxml/catalogScene.fxml", null);
            else {
                // если итак активная
                if (catalogNavItem.getStyleClass().contains("active")) TabSceneManager.goToFirstAndShowAnimated();
                else TabSceneManager.show();
            }

            // ресет вкладок
            unactiveAll();
            catalogNavItem.getStyleClass().add("active");
        });
    }

    private void unactiveAll() {
        homeNavItem.getStyleClass().remove("active");
        searchNavItem.getStyleClass().remove("active");
        catalogNavItem.getStyleClass().remove("active");
    }

}