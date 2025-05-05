package controller;

import animation.Animations;
import elements.RangeSlider;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import manager.TabSceneManager;
import model.*;
import service.ApiService;
import utils.AnimeUtils;

import java.util.*;

public class FilterController {
    private final ApiService apiService = ApiService.getInstance();


    @FXML private HBox showFilterButton;
    @FXML private HBox hideFilterButton;
    private boolean showFilter = false;
    @FXML private ScrollPane filterScroll;
    @FXML private VBox filterBox;

    @FXML private RangeSlider yearSlider;
    @FXML private Label fromYearLabel;
    @FXML private Label toYearLabel;

    @FXML private FlowPane filterTypesContainer;
    @FXML private FlowPane filterStatusesContainer;


    @FXML private StackPane genreDropdownContainer;
    @FXML private Label genrePlaceholder;
    @FXML private StackPane genreDropdownWrapper;
    @FXML private ScrollPane genreScrollPane;
    @FXML private VBox genreCheckboxContainer;
    @FXML private FlowPane selectedGenresContainer;



    @FXML private StackPane studioDropdownContainer;
    @FXML private Label studioPlaceholder;
    @FXML private StackPane studioDropdownWrapper;
    @FXML private VBox studioCheckboxContainer;
    @FXML private FlowPane selectedStudiosContainer;



    private Set<Integer> selectedTypes = new HashSet<>();
    private Set<Integer> selectedStatuses = new HashSet<>();
    private Set<Integer> selectedStudios = new HashSet<>();
    private Set<Integer> selectedGenres = new HashSet<>();
    private final Map<Integer, CheckBox> studioCheckboxMap = new HashMap<>();
    private final Map<Integer, CheckBox> genreCheckboxMap = new HashMap<>();


    private final PauseTransition filterDebounce = new PauseTransition(Duration.millis(700));

    @FXML private ScrollPane filterContentScroll;
    @FXML private FlowPane container;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button scrollToTopButton;

    private final double MIN_V_SCROLL = 0.1;
    private final double MAX_V_LOAD_SCROLL = 0.99;

    public void initialize() {
        // слайдер для года выхода
        yearSlider.setValues(1943, 2025);
        fromYearLabel.setText("1943");
        toYearLabel.setText("2025");

        yearSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
            filterDebounce.stop();
            fromYearLabel.setText(newVal.toString());
            filterDebounce.playFromStart();
        });

        yearSlider.highValueProperty().addListener((obs, oldVal, newVal) -> {
            filterDebounce.stop();
            toYearLabel.setText(newVal.toString());
            filterDebounce.playFromStart();
        });


        // кнопка наверх
        scrollToTopButton.setOnAction(e -> {
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(filterContentScroll.vvalueProperty(), 0, Interpolator.EASE_OUT);
            KeyFrame kf = new KeyFrame(Duration.millis(500), kv); // за 0.5 сек наверх
            timeline.getKeyFrames().add(kf);
            timeline.play();
        });

        // скролл контента
        filterContentScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > MIN_V_SCROLL && !scrollToTopButton.isVisible()) {
                scrollToTopButton.setVisible(true);
                Animations.FadeInScale(scrollToTopButton, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);

            } else if (newVal.doubleValue() <= MIN_V_SCROLL && scrollToTopButton.isVisible()) {
                Animations.FadeOutScale(scrollToTopButton, 1.0, 0.5, Animations.DEFAULT_DURATION, Duration.ZERO)
                        .setOnFinished(e -> Platform.runLater(() -> scrollToTopButton.setVisible(false)));
            }

            boolean isEmpty = (container.getChildren().size() % ApiService.PAGE_SIZE != 0);
            if (newVal.doubleValue() > MAX_V_LOAD_SCROLL && !isEmpty && !loadingIndicator.isVisible()) fetchFiltered();
        });



        Platform.runLater(() -> {
            // для выбора типов
            for (AnimeType type: ApiService.filterParams.getTypes()) {
                Label filterTypeItem = new Label(AnimeUtils.typeMap.get(type.getType_name()));
                filterTypeItem.getStyleClass().add("filter-type-item");

                filterTypeItem.setOnMouseClicked(event -> {
                    filterDebounce.stop();
                    if (filterTypeItem.getStyleClass().contains("active")) {
                        filterTypeItem.getStyleClass().remove("active");
                        selectedTypes.remove(type.getId());
                    } else {
                        filterTypeItem.getStyleClass().add("active");
                        selectedTypes.add(type.getId());
                    }
                    filterDebounce.playFromStart();
                });

                filterTypesContainer.getChildren().add(filterTypeItem);

            }


            // статусы
            for (AnimeStatus status: ApiService.filterParams.getStatuses()) {
                Label filterStatusItem = new Label(AnimeUtils.statusMap.get(status.getStatus_name()));
                filterStatusItem.getStyleClass().add("filter-status-item");

                filterStatusItem.setOnMouseClicked(event -> {
                    filterDebounce.stop();
                    if (filterStatusItem.getStyleClass().contains("active")) {
                        filterStatusItem.getStyleClass().remove("active");
                        selectedStatuses.remove(status.getId());
                    } else {
                        filterStatusItem.getStyleClass().add("active");
                        selectedStatuses.add(status.getId());
                    }
                    filterDebounce.playFromStart();
                });

                filterStatusesContainer.getChildren().add(filterStatusItem);
            }

            // жанры
            for (Genre genre: ApiService.filterParams.getGenres()) {
                CheckBox checkBox = new CheckBox(genre.getGenre_name());
                genreCheckboxMap.put(genre.getId(), checkBox);

                checkBox.setTextFill(Color.WHITE);
                checkBox.setOnAction(e -> {
                    filterDebounce.stop();
                    if (checkBox.isSelected()) {
                        selectedGenres.add(genre.getId());
                        addGenreLabel(genre);
                    } else {
                        selectedGenres.remove(genre.getId());
                        removeGenreLabel(genre);
                    }
                    updatePlaceholder();
                    filterDebounce.playFromStart();
                });
                genreCheckboxContainer.getChildren().add(checkBox);
            }

            genreDropdownContainer.setOnMouseClicked(e -> {
                genreDropdownWrapper.setVisible(!genreDropdownWrapper.isVisible());
                genreDropdownWrapper.setManaged(genreDropdownWrapper.isVisible());
            });

            // студии
            for (Studio studio: ApiService.filterParams.getStudios()) {
                CheckBox checkBox = new CheckBox(studio.getStudio_name());
                studioCheckboxMap.put(studio.getId(), checkBox);

                checkBox.setTextFill(Color.WHITE);
                checkBox.setOnAction(e -> {
                    filterDebounce.stop();
                    if (checkBox.isSelected()) {
                        selectedStudios.add(studio.getId());
                        addStudioLabel(studio);
                    } else {
                        selectedStudios.remove(studio.getId());
                        removeStudioLabel(studio);
                    }
                    updatePlaceholder();
                    filterDebounce.playFromStart();
                });
                studioCheckboxContainer.getChildren().add(checkBox);
            }

            studioDropdownContainer.setOnMouseClicked(e -> {
                studioDropdownWrapper.setVisible(!studioDropdownWrapper.isVisible());
                studioDropdownWrapper.setManaged(studioDropdownWrapper.isVisible());
            });


            filterDebounce.setOnFinished( e -> Platform.runLater(() -> {
                container.getChildren().clear();
                filterContentScroll.setVvalue(0);
                fetchFiltered();
            }));

        });


        int FILTER_SHOW_WIDTH = 1000;


        // листенер на изменение ширины
        Platform.runLater(() -> {
            filterScroll.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.widthProperty().addListener((obs_, oldWidth, newWidth) -> {
                        if (newWidth.intValue() < FILTER_SHOW_WIDTH && filterScroll.isVisible()) {
                            Animations.FadeOutSlideHorizontal(filterScroll, 0, -500, Animations.DEFAULT_DURATION, Duration.ZERO)
                                    .setOnFinished(e -> Platform.runLater(() ->  {
                                        filterScroll.setManaged(false);
                                        filterScroll.setVisible(false);
                                    }));
                            showFilterButton.setVisible(true);
                            Animations.FadeInScale(showFilterButton, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
                        }
                        else if (newWidth.intValue() > FILTER_SHOW_WIDTH && !filterScroll.isVisible()) {
                            filterScroll.setVisible(true);
                            filterScroll.setManaged(true);
                            Animations.FadeInSlideHorizontal(filterScroll, -500, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
                            Animations.FadeOutScale(showFilterButton, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                                    .setOnFinished(ee -> {
                                        showFilterButton.setVisible(false);
                                    });
                        }
                    });
                }
            });

        });


        hideFilterButton.setOnMouseClicked(e -> Platform.runLater(() -> {
            showFilter = false;
            Animations.FadeOutSlideHorizontal(filterScroll, 0, -500, Animations.DEFAULT_DURATION, Duration.ZERO)
                    .setOnFinished(ee -> {
                        filterScroll.setManaged(false);
                        filterScroll.setVisible(false);
                        showFilterButton.setVisible(true);
                        Animations.FadeInScale(showFilterButton, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            });
        }));

        showFilterButton.setOnMouseClicked(e -> Platform.runLater(() -> {
            showFilter = true;
            Animations.FadeOutScale(showFilterButton, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                            .setOnFinished(ee -> {
                                showFilterButton.setVisible(false);
                            });

            filterScroll.setVisible(true);
            filterScroll.setManaged(true);
            Animations.FadeInSlideHorizontal(filterScroll, -500, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
        }));

        fetchFiltered();
    }




    private HBox createFilterTag(String text, Runnable onClose) {
        Label label = new Label(text);
        Button closeButton = new Button("✖");
        closeButton.setOnAction(e -> onClose.run());

        HBox box = new HBox(label, closeButton);
        box.setSpacing(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("filter-tag");

        return box;
    }

    // добавление лейблов
    private void addGenreLabel(Genre genre) {
        HBox genreTag = createFilterTag(genre.getGenre_name(), () -> {
            filterDebounce.stop();

            selectedGenres.remove(genre.getId());
            removeGenreLabel(genre);
            updatePlaceholder();

            filterDebounce.playFromStart();
        });

        selectedGenresContainer.getChildren().add(genreTag);
    }

    private void removeGenreLabel(Genre genre) {
        // удаляем лейбл
        selectedGenresContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox && ((HBox) node).getChildren().getFirst() instanceof Label) {
                Label label = (Label) ((HBox) node).getChildren().getFirst();
                return label.getText().equals(genre.getGenre_name());
            }
            return false;
        });

        // отключаем чекбокс
        CheckBox checkBox = genreCheckboxMap.get(genre.getId());
        if (checkBox != null) {
            checkBox.setSelected(false);
        }
    }


    private void addStudioLabel(Studio studio) {
        HBox studioTag = createFilterTag(studio.getStudio_name(), () -> {
            filterDebounce.stop();

            selectedStudios.remove(studio.getId());
            removeStudioLabel(studio);
            updatePlaceholder();

            filterDebounce.playFromStart();
        });
        selectedStudiosContainer.getChildren().add(studioTag);
    }


    private void removeStudioLabel(Studio studio) {
        // удаляем лейбл
        selectedStudiosContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox && ((HBox) node).getChildren().getFirst() instanceof Label) {
                Label label = (Label) ((HBox) node).getChildren().getFirst();
                return label.getText().equals(studio.getStudio_name());
            }
            return false;
        });

        CheckBox checkBox = studioCheckboxMap.get(studio.getId());
        if (checkBox != null) {
            checkBox.setSelected(false);
        }
    }


    private void updatePlaceholder() {
        if (selectedGenres.isEmpty()) {
            genrePlaceholder.setVisible(true);
        } else {
            genrePlaceholder.setVisible(false);
        }

        if (selectedStudios.isEmpty()) {
            studioPlaceholder.setVisible(true);
        } else {
            studioPlaceholder.setVisible(false);
        }
    }


    private void fetchFiltered() {
        boolean isEmpty = (container.getChildren().size() % ApiService.PAGE_SIZE != 0);
        if (loadingIndicator.isVisible() || isEmpty) return;


        loadingIndicator.setVisible(true);
        int offset = container.getChildren().size();


        System.out.printf("Filter... offset %d count %d\n", offset, ApiService.PAGE_SIZE);
        FilterRequest request = new FilterRequest();
        Integer yearFrom = yearSlider.getLowValue();
        Integer yearTo = yearSlider.getHighValue();
        if (Objects.equals(yearFrom, yearTo)) {
            yearTo = yearSlider.getMaxValue();
            yearSlider.highValueProperty().set(yearTo);
        }

        request.setYearFrom(yearFrom);
        request.setYearTo(yearTo);
        request.setTypes(selectedTypes.stream().toList());
        request.setStatuses(selectedStatuses.stream().toList());
        request.setGenres(selectedGenres.stream().toList());
        request.setStudios(selectedStudios.stream().toList());
        request.setCount(ApiService.PAGE_SIZE);
        request.setOffset(offset);

        apiService.filterAsync(request).thenAccept(animeList -> Platform.runLater(() -> {
            for (int i = 0; i < animeList.size(); i++) {
                Node node = AnimeUtils.createAnimeTile(animeList.get(i));
                container.getChildren().add(node);
                Animations.FadeInSlideVertical(node, -100, 0, Animations.DEFAULT_DURATION, Duration.millis((i+1)*200));
            }
            loadingIndicator.setVisible(false);
        })).exceptionally(e -> {
            System.out.println("Failed to filter");
            Platform.runLater(() -> loadingIndicator.setVisible(false));
            return null;
        });
    }




    // для вызовов извне
    public void selectType(AnimeType type) {
        Platform.runLater(() -> {
            for (Node node: filterTypesContainer.getChildren()) {
                if ((node instanceof Label label) && label.getText().equals(AnimeUtils.typeMap.get(type.getType_name()))) {
                    if (!label.getStyleClass().contains("active")) {
                        label.getStyleClass().add("active");
                        selectedTypes.add(type.getId());
                        filterDebounce.playFromStart();
                        break;
                    }
                }
            }
        });
    }

    public void selectStatus(AnimeStatus status) {
        Platform.runLater(() -> {
            for (Node node: filterStatusesContainer.getChildren()) {
                if ((node instanceof Label label) && label.getText().equals(AnimeUtils.statusMap.get(status.getStatus_name()))) {
                    if (!label.getStyleClass().contains("active")) {
                        label.getStyleClass().add("active");
                        selectedStatuses.add(status.getId());
                        filterDebounce.playFromStart();
                        break;
                    }
                }
            }
        });

    }

    public void selectGenre(Genre genre) {
        Platform.runLater(() -> {
            selectedGenres.add(genre.getId());
            updatePlaceholder();
            addGenreLabel(genre);
            filterDebounce.playFromStart();
        });

    }

    public void selectStudio(Studio studio) {
        Platform.runLater(() -> {
            filterDebounce.stop();
            selectedStudios.add(studio.getId());
            updatePlaceholder();
            addStudioLabel(studio);
            filterDebounce.playFromStart();
        });
    }

















    public void goBack(MouseEvent mouseEvent) {
        TabSceneManager.goBackAndShowAnimated();
    }

}
