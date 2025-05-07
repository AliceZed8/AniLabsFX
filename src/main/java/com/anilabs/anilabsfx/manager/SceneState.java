package com.anilabs.anilabsfx.manager;

import javafx.scene.Node;

public class SceneState {
    private final Node node;
    private final Object controller;

    public SceneState(Node node, Object controller) {
        this.node = node;
        this.controller = controller;

    }

    public Node getNode() {
        return node;
    }

    public Object getController() {
        return controller;
    }
}
