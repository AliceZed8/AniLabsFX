package manager;

import animation.Animations;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class TabSceneManager {
    private static final Map<String, Deque<SceneState>> tabHistories = new HashMap<>();
    private static Pane root;
    private static Node navPanel; // нижняя панель
    private static String currentTab;

    public static void initialize(Pane rootPane, Node panel) {
        navPanel = panel;
        root = rootPane;
    }

    public static void switchTab(String tabName) {
        currentTab = tabName;
        tabHistories.putIfAbsent(tabName, new ArrayDeque<>());
    }

    public static String getCurrentTabName() {
        return  currentTab;
    }

    public static SceneState create(String fxmlPath, Consumer<Object> initController) {
        try {
            FXMLLoader loader = new FXMLLoader(TabSceneManager.class.getResource(fxmlPath));
            Node node = loader.load();
            Object controller = loader.getController();
            if (initController != null) initController.accept(controller);

            SceneState state = new SceneState(node, controller);
            tabHistories.get(currentTab).push(state);
            setRoot(node);
            return state;
        } catch (IOException e) {
            System.out.println("Failed to create SceneState for tab " + currentTab);
        }
        return null;
    }

    public static void setRoot(Node node) {
        root.getChildren().setAll(node);
    }

    public static void showPanel() {
        navPanel.setVisible(true);
        navPanel.setManaged(true);
        Animations.FadeInSlideVertical(navPanel, 100, 0, Duration.millis(1000), Duration.ZERO);
    }

    public static void hidePanel() {
        navPanel.setManaged(false);
        navPanel.setVisible(false);
    }

    public static SceneState get() {
        Deque<SceneState> history = tabHistories.get(currentTab);
        if (history.isEmpty()) return null;

        return history.peek();
    }

    public static void show() {
        Deque<SceneState> history = tabHistories.get(currentTab);
        if (!history.isEmpty()) {
            SceneState state = history.peek();
            setRoot(state.getNode());
        }
    }

    public static void showCombined(Node first, Node second) {
        if (first == null || second == null) return;

        StackPane transitionLayer = new StackPane();
        transitionLayer.getChildren().setAll(first, second);
        setRoot(transitionLayer);
    }

    public static void createAndShowAnimated(String fxmlPath, Consumer<Object> initController) {
        // новое состояние
        SceneState previous = TabSceneManager.get();
        SceneState current = TabSceneManager.create(fxmlPath, initController);

        if (previous != null && current != null) {
            // анимируем
            TabSceneManager.showCombined(previous.getNode(), current.getNode());
            Animations.FadeInSlideHorizontal(current.getNode(), 500, 0, Duration.millis(300), Duration.ZERO)
                    .setOnFinished(ee -> TabSceneManager.show());
        }
    }

    public static void goBackAndShowAnimated() {
        // получаем состояния
        SceneState current = TabSceneManager.get();
        TabSceneManager.goBack();
        SceneState previous = TabSceneManager.get();

        if (current == null || previous == null) return;

        // анимируем
        TabSceneManager.showCombined(previous.getNode(), current.getNode());
        Animations.FadeOutSlideHorizontal(current.getNode(), 0, 500, Duration.millis(400), Duration.ZERO)
                .setOnFinished(e -> TabSceneManager.show());
    }


    public static void goBack() {
        Deque<SceneState> history = tabHistories.get(currentTab);
        if (history.size() > 1) history.pop();
    }


    public static void goToFirstAndShowAnimated() {
        Deque<SceneState> history = tabHistories.get(currentTab);
        if (history == null || history.size() < 2) return;

        SceneState first = history.getLast();
        SceneState last = history.peek();

        if (first == null || last == null || first == last) return;
        history.clear();
        history.push(first);
        history.push(last);

        goBackAndShowAnimated();
    }


}


