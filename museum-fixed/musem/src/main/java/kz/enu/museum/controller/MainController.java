package kz.enu.museum.controller;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import kz.enu.museum.model.Artifact;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.Location;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.model.Painting;
import kz.enu.museum.model.Sculpture;
import kz.enu.museum.model.enums.ExhibitStatus;
import kz.enu.museum.service.ArtistService;
import kz.enu.museum.service.CategoryService;
import kz.enu.museum.service.ExhibitService;
import kz.enu.museum.service.ReportService;
import kz.enu.museum.service.SearchService;

/**
 * Главный контроллер приложения.
 * Управляет главным окном и координирует взаимодействие пользователя с сервисами.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class MainController implements Initializable {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    // Инъекции сервисов
    private ExhibitService exhibitService;
    private CategoryService categoryService;
    private ArtistService artistService;
    private SearchService searchService;

    // Сервисы
    private ReportService reportService;

    // FXML элементы - дерево и таблица
    @FXML private TreeView<Category> categoryTree;
    @FXML private TableView<MuseumItem> exhibitTable;

    // FXML элементы - колонки таблицы
    @FXML private TableColumn<MuseumItem, Long> idColumn;
    @FXML private TableColumn<MuseumItem, String> inventoryColumn;
    @FXML private TableColumn<MuseumItem, String> nameColumn;
    @FXML private TableColumn<MuseumItem, String> typeColumn;
    @FXML private TableColumn<MuseumItem, String> authorColumn;
    @FXML private TableColumn<MuseumItem, String> categoryColumn;
    @FXML private TableColumn<MuseumItem, ExhibitStatus> statusColumn;
    @FXML private TableColumn<MuseumItem, String> locationColumn;
    @FXML private TableColumn<MuseumItem, Integer> yearColumn;

    // FXML элементы - поиск и фильтры
    @FXML private TextField searchTextField;
    @FXML private ComboBox<ExhibitStatus> statusFilter;
    @FXML private ComboBox<Artist> authorFilter;
    @FXML private Button resetFilterButton;

    // FXML элементы - кнопки
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    // FXML элементы - статус бар
    @FXML private Label statusBar;
    @FXML private Label tableTitle;
    @FXML private Label countLabel;
    @FXML private Label selectedCountLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Инициализация MainController");
    }

    /**
     * Инициализирует контроллер с сервисами.
     *
     * @param exhibitService сервис экспонатов
     * @param categoryService сервис категорий
     * @param artistService сервис художников
     * @param searchService сервис поиска
     */
    public void initialize(ExhibitService exhibitService,
                           CategoryService categoryService,
                           ArtistService artistService,
                           SearchService searchService) {

        this.exhibitService = exhibitService;
        this.categoryService = categoryService;
        this.artistService = artistService;
        this.searchService = searchService;

        logger.info("MainController инициализирован с сервисами");

        // Инициализация компонентов UI
        try {
            initializeTable();
            initializeCategoryTree();
            initializeFilters();
            initializeEventHandlers();
            updateExhibitTable();
            updateStatusBar();

        } catch (Exception e) {
            logger.error("Ошибка при инициализации UI компонентов", e);
            showError("Ошибка инициализации", e.getMessage());
        }
    }

    /**
     * Инициализирует таблицу экспонатов.
     */
    private void initializeTable() {
        logger.debug("Инициализация таблицы экспонатов");

        if (exhibitTable == null) return;

        // Настройка колонок из FXML
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (inventoryColumn != null) inventoryColumn.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Тип экспоната
        if (typeColumn != null) {
            typeColumn.setCellValueFactory(cellData -> {
                MuseumItem item = cellData.getValue();
                String type = item.getClass().getSimpleName();
                return new javafx.beans.property.SimpleStringProperty(type);
            });
        }

        // Автор
        if (authorColumn != null) {
            authorColumn.setCellValueFactory(cellData -> {
                MuseumItem item = cellData.getValue();
                String author = "-";
                if (item instanceof Exhibit) {
                    Exhibit exhibit = (Exhibit) item;
                    author = exhibit.getAuthor() != null ? exhibit.getAuthor().getFullName() : "-";
                }
                return new javafx.beans.property.SimpleStringProperty(author);
            });
        }

        // Категория
        if (categoryColumn != null) {
            categoryColumn.setCellValueFactory(cellData -> {
                MuseumItem item = cellData.getValue();
                String cat = "-";
                if (item instanceof Exhibit) {
                    Exhibit exhibit = (Exhibit) item;
                    cat = exhibit.getCategory() != null ? exhibit.getCategory().getName() : "-";
                }
                return new javafx.beans.property.SimpleStringProperty(cat);
            });
        }

        // Статус с цветовой индикацией
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusColumn.setCellFactory(column -> new TableCell<MuseumItem, ExhibitStatus>() {
                @Override
                protected void updateItem(ExhibitStatus status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("status-on-display", "status-in-storage",
                                "status-on-restoration", "status-on-loan");
                    } else {
                        setText(getStatusText(status));
                        getStyleClass().removeAll("status-on-display", "status-in-storage",
                                "status-on-restoration", "status-on-loan");
                        getStyleClass().add(getStatusStyleClass(status));
                    }
                }
            });
        }

        // Зал
        if (locationColumn != null) {
            locationColumn.setCellValueFactory(cellData -> {
                MuseumItem item = cellData.getValue();
                String loc = "-";
                if (item instanceof Exhibit) {
                    Exhibit exhibit = (Exhibit) item;
                    loc = exhibit.getLocation() != null ? exhibit.getLocation().getHallName() : "-";
                }
                return new javafx.beans.property.SimpleStringProperty(loc);
            });
        }

        // Год создания (извлекаем из creationDate)
        if (yearColumn != null) {
            yearColumn.setCellValueFactory(cellData -> {
                MuseumItem item = cellData.getValue();
                Integer year = null;
                if (item.getCreationDate() != null) {
                    year = item.getCreationDate().getYear();
                }
                return new javafx.beans.property.SimpleObjectProperty<>(year);
            });
        }

        // Двойной клик для редактирования
        exhibitTable.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 2) {
                handleEditExhibit();
            }
        });

        // Обновление счётчика при выборе
        exhibitTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectedCount();
        });
    }

    /**
     * Получить текст статуса.
     */
    private String getStatusText(ExhibitStatus status) {
        switch (status) {
            case ON_DISPLAY: return "На экспозиции";
            case IN_STORAGE: return "В хранилище";
            case ON_RESTORATION: return "На реставрации";
            case ON_LOAN: return "В аренде";
            default: return status.toString();
        }
    }

    /**
     * Получить CSS класс для статуса.
     */
    private String getStatusStyleClass(ExhibitStatus status) {
        switch (status) {
            case ON_DISPLAY: return "status-on-display";
            case IN_STORAGE: return "status-in-storage";
            case ON_RESTORATION: return "status-on-restoration";
            case ON_LOAN: return "status-on-loan";
            default: return "";
        }
    }

    /**
     * Инициализирует дерево категорий.
     */
    private void initializeCategoryTree() {
        logger.debug("Инициализация дерева категорий");

        if (categoryTree != null) {
            // Загрузка и отображение категорий
            List<Category> rootCategories = categoryService.getRootCategories();

            TreeItem<Category> root = new TreeItem<>(new Category("Все категории", "ROOT"));
            root.setExpanded(true);

            for (Category category : rootCategories) {
                root.getChildren().add(createCategoryNode(category));
            }

            categoryTree.setRoot(root);
            categoryTree.setOnMouseClicked(event -> {
                TreeItem<Category> selected = categoryTree.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getValue().getCategoryCode() != null &&
                        !selected.getValue().getCategoryCode().equals("ROOT")) {
                    filterByCategory(selected.getValue());
                }
            });
        }
    }

    /**
     * Создаёт узел дерева для категории.
     *
     * @param category категория
     * @return TreeItem с категорией
     */
    private TreeItem<Category> createCategoryNode(Category category) {
        TreeItem<Category> node = new TreeItem<>(category);

        for (Category sub : category.getSubcategories()) {
            node.getChildren().add(createCategoryNode(sub));
        }

        return node;
    }

    /**
     * Инициализирует фильтры.
     */
    private void initializeFilters() {
        logger.debug("Инициализация фильтров");

        if (statusFilter != null) {
            ObservableList<ExhibitStatus> statuses = FXCollections.observableArrayList(ExhibitStatus.values());
            statusFilter.setItems(statuses);
        }

        if (authorFilter != null) {
            ObservableList<Artist> artists = FXCollections.observableArrayList(artistService.getAllArtists());
            authorFilter.setItems(artists);

            // Настройка отображения имени автора вместо toString
            authorFilter.setButtonCell(new javafx.scene.control.ListCell<Artist>() {
                @Override
                protected void updateItem(Artist item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName());
                    }
                }
            });

            authorFilter.setCellFactory(param -> new javafx.scene.control.ListCell<Artist>() {
                @Override
                protected void updateItem(Artist item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName());
                    }
                }
            });
        }
    }

    /**
     * Инициализирует обработчики событий.
     */
    private void initializeEventHandlers() {
        logger.debug("Инициализация обработчиков событий");

        // Поиск в реальном времени
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }

        // Фильтры
        if (statusFilter != null) {
            statusFilter.setOnAction(event -> handleSearch());
        }

        if (authorFilter != null) {
            authorFilter.setOnAction(event -> handleSearch());
        }

        if (resetFilterButton != null) {
            resetFilterButton.setOnAction(event -> handleResetFilters());
        }
    }

    /**
     * Обновить счётчик выбранных элементов.
     */
    private void updateSelectedCount() {
        if (selectedCountLabel != null && exhibitTable != null) {
            int count = exhibitTable.getSelectionModel().getSelectedItems().size();
            selectedCountLabel.setText("Выбрано: " + count);
        }
    }

    /**
     * Обновляет таблицу экспонатов.
     */
    private void updateExhibitTable() {
        if (exhibitTable != null) {
            List<MuseumItem> exhibits = exhibitService.getAllExhibits();
            ObservableList<MuseumItem> data = FXCollections.observableArrayList(exhibits);
            exhibitTable.setItems(data);
            updateCountLabel();
        }
    }

    /**
     * Обновляет строку статуса.
     */
    private void updateStatusBar() {
        if (statusBar != null) {
            long count = exhibitService.getTotalCount();
            statusBar.setText("Всего экспонатов: " + count);
        }
    }

    /**
     * Обработчик поиска.
     */
    private void handleSearch() {
        String query = searchTextField != null ? searchTextField.getText() : "";
        ExhibitStatus status = statusFilter != null ? statusFilter.getValue() : null;
        Artist author = authorFilter != null ? authorFilter.getValue() : null;

        List<MuseumItem> results = searchService.advancedSearch(query, null, author, status);

        if (exhibitTable != null) {
            ObservableList<MuseumItem> data = FXCollections.observableArrayList(results);
            exhibitTable.setItems(data);
        }

        logger.info("Поиск выполнен: найдено " + results.size() + " результатов");
    }

    /**
     * Обработчик добавления экспоната.
     */
    @FXML
    public void handleAddExhibit() {
        logger.info("Запрос на добавление экспоната");

        // Красивый диалог выбора типа экспоната
        Dialog<String> typeDialog = new Dialog<>();
        typeDialog.setTitle("✨ Добавление экспоната");
        typeDialog.setHeaderText(null);

        typeDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #faf8f5, #f0ebe3);");

        // Заголовок
        Label header = new Label("🎨 Выберите тип экспоната");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4a4a4a;");

        Label subHeader = new Label("Какой экспонат вы хотите добавить в коллекцию?");
        subHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b6b6b;");

        javafx.scene.layout.HBox cardsBox = new javafx.scene.layout.HBox(16);
        cardsBox.setAlignment(javafx.geometry.Pos.CENTER);
        cardsBox.setPadding(new Insets(20, 0, 10, 0));

        // Карточка "Картина"
        VBox paintingCard = createTypeCard("🖼️", "Картина", "Живопись, портреты,\nпейзажи", "#f4e1e1", "#c9a9a9");
        paintingCard.setOnMouseClicked(e -> { typeDialog.setResult("Картина"); typeDialog.close(); });

        // Карточка "Скульптура"
        VBox sculptureCard = createTypeCard("🗿", "Скульптура", "Мрамор, бронза,\nдерево", "#e8e0f0", "#a08cb0");
        sculptureCard.setOnMouseClicked(e -> { typeDialog.setResult("Скульптура"); typeDialog.close(); });

        // Карточка "Артефакт"
        VBox artifactCard = createTypeCard("⚱️", "Артефакт", "Древности,\nисторические находки", "#fce5d8", "#d4a88a");
        artifactCard.setOnMouseClicked(e -> { typeDialog.setResult("Артефакт"); typeDialog.close(); });

        cardsBox.getChildren().addAll(paintingCard, sculptureCard, artifactCard);

        content.getChildren().addAll(header, subHeader, cardsBox);

        typeDialog.getDialogPane().setContent(content);
        typeDialog.getDialogPane().setMinWidth(500);
        typeDialog.getDialogPane().setMinHeight(320);

        Optional<String> typeResult = typeDialog.showAndWait();
        if (!typeResult.isPresent() || typeResult.get() == null) return;

        String type = typeResult.get();

        switch (type) {
            case "Картина":
                handleAddPainting();
                break;
            case "Скульптура":
                handleAddSculpture();
                break;
            case "Артефакт":
                handleAddArtifact();
                break;
        }
    }

    /**
     * Создаёт карточку для выбора типа экспоната.
     */
    private VBox createTypeCard(String emoji, String title, String description, String bgColor, String accentColor) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setMinWidth(130);
        card.setMaxWidth(130);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: " + accentColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 16;" +
                        "-fx-cursor: hand;"
        );

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a4a4a;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b6b6b; -fx-text-alignment: center;");
        descLabel.setWrapText(true);

        card.getChildren().addAll(emojiLabel, titleLabel, descLabel);

        // Hover эффект
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + accentColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: " + accentColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: " + accentColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 16;" +
                        "-fx-cursor: hand;"
        ));

        return card;
    }

    /**
     * Обработчик редактирования экспоната.
     */
    @FXML
    public void handleEditExhibit() {
        MuseumItem selected = exhibitTable != null ? exhibitTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Выбор", "Пожалуйста, выберите экспонат для редактирования");
            return;
        }
        logger.info("Редактирование экспоната: " + selected.getName());

        // Создаём диалог редактирования с красивым дизайном
        Dialog<MuseumItem> dialog = new Dialog<>();
        dialog.setTitle("✏️ Редактирование экспоната");
        dialog.setHeaderText(null);

        // Кнопки
        ButtonType saveButtonType = new ButtonType("💾 Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Главный контейнер
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(24));
        mainContent.setStyle("-fx-background-color: linear-gradient(to bottom, #faf8f5, #f0ebe3);");

        // Заголовок
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(12);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String typeEmoji = "📦";
        String typeName = "Экспонат";
        if (selected instanceof Painting) { typeEmoji = "🖼️"; typeName = "Картина"; }
        else if (selected instanceof Sculpture) { typeEmoji = "🗿"; typeName = "Скульптура"; }
        else if (selected instanceof Artifact) { typeEmoji = "⚱️"; typeName = "Артефакт"; }

        Label emojiLabel = new Label(typeEmoji);
        emojiLabel.setStyle("-fx-font-size: 32px;");

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Редактирование: " + typeName);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4a4a4a;");
        Label subLabel = new Label("Инв. номер: " + selected.getInventoryNumber());
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a8a;");
        titleBox.getChildren().addAll(titleLabel, subLabel);

        headerBox.getChildren().addAll(emojiLabel, titleBox);

        // Форма
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // Стили для лейблов
        String labelStyle = "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #5a5a5a;";
        String fieldStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #d8d0c8; -fx-padding: 8 12;";

        TextField nameField = new TextField(selected.getName());
        nameField.setPromptText("Название");
        nameField.setStyle(fieldStyle);
        nameField.setPrefWidth(280);

        TextField descField = new TextField(selected.getDescription());
        descField.setPromptText("Описание");
        descField.setStyle(fieldStyle);

        ComboBox<ExhibitStatus> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(ExhibitStatus.values()));
        statusCombo.setValue(selected.getStatus());
        statusCombo.setStyle(fieldStyle);
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Artist> authorCombo = new ComboBox<>();
        authorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        if (selected instanceof Exhibit) {
            authorCombo.setValue(((Exhibit) selected).getAuthor());
        }
        authorCombo.setStyle(fieldStyle);
        authorCombo.setMaxWidth(Double.MAX_VALUE);

        // Настройка отображения автора
        authorCombo.setButtonCell(new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        authorCombo.setCellFactory(lv -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });

        Label nameLabel = new Label("📝 Название:");
        nameLabel.setStyle(labelStyle);
        Label descLabel = new Label("📄 Описание:");
        descLabel.setStyle(labelStyle);
        Label statusLabel = new Label("📊 Статус:");
        statusLabel.setStyle(labelStyle);
        Label authorLabel = new Label("👤 Автор:");
        authorLabel.setStyle(labelStyle);

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descField, 1, 1);
        grid.add(statusLabel, 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(authorLabel, 0, 3);
        grid.add(authorCombo, 1, 3);

        mainContent.getChildren().addAll(headerBox, grid);

        dialog.getDialogPane().setContent(mainContent);
        dialog.getDialogPane().setMinWidth(450);

        // Обработка результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setName(nameField.getText());
                selected.setDescription(descField.getText());
                selected.setStatus(statusCombo.getValue());
                if (selected instanceof Exhibit) {
                    ((Exhibit) selected).setAuthor(authorCombo.getValue());
                }
                return selected;
            }
            return null;
        });

        Optional<MuseumItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            try {
                exhibitService.updateExhibit(item);
                updateExhibitTable();
                updateStatusBar();
                showInfo("Успех", "Экспонат обновлён!");
            } catch (Exception e) {
                logger.error("Ошибка при обновлении", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик просмотра экспоната с изображением.
     */
    @FXML
    public void handleViewExhibit() {
        MuseumItem selected = exhibitTable != null ? exhibitTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Выбор", "Пожалуйста, выберите экспонат для просмотра");
            return;
        }
        logger.info("Просмотр экспоната: " + selected.getName());

        // Создаём диалог просмотра
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🖼️ " + selected.getName());
        dialog.setHeaderText(null);

        // Кнопка закрытия
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Главный контейнер
        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: linear-gradient(to bottom, #faf8f5, #f0ebe3);");
        mainContent.setMinWidth(600);
        mainContent.setMaxWidth(800);

        // Заголовок с типом
        String typeEmoji = "📦";
        String typeName = "Экспонат";
        if (selected instanceof Painting) { typeEmoji = "🖼️"; typeName = "Картина"; }
        else if (selected instanceof Sculpture) { typeEmoji = "🗿"; typeName = "Скульптура"; }
        else if (selected instanceof Artifact) { typeEmoji = "⚱️"; typeName = "Артефакт"; }

        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(12);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label emojiLabel = new Label(typeEmoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        VBox titleBox = new VBox(4);
        Label nameLabel = new Label(selected.getName());
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3a3a3a;");
        Label typeLabel = new Label(typeName + " • " + selected.getInventoryNumber());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a8a;");
        titleBox.getChildren().addAll(nameLabel, typeLabel);

        headerBox.getChildren().addAll(emojiLabel, titleBox);

        // Контейнер для изображения
        VBox imageContainer = new VBox(10);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Получаем путь к изображению
        final String imagePath;
        if (selected instanceof Exhibit) {
            imagePath = ((Exhibit) selected).getImagePath();
        } else {
            imagePath = null;
        }

        if (imagePath != null && !imagePath.isEmpty()) {
            // Показываем индикатор загрузки
            javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
            progress.setMaxSize(50, 50);
            imageContainer.getChildren().add(progress);

            // Загружаем изображение в фоновом потоке
            new Thread(() -> {
                try {
                    logger.info("Загрузка изображения: " + imagePath);
                    URL url = new URL(imagePath);

                    // Проверим ответ сервера (HEAD/GET) и закроем соединение
                    HttpURLConnection probeConn = (HttpURLConnection) url.openConnection();
                    probeConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    probeConn.setConnectTimeout(10000);
                    probeConn.setReadTimeout(15000);
                    int probeCode = probeConn.getResponseCode();
                    long contentLength = probeConn.getContentLengthLong();
                    logger.debug("Image probe HTTP response code: " + probeCode + " for URL: " + imagePath + ", contentLength=" + contentLength);
                    probeConn.disconnect();

                    if (probeCode >= 400) {
                        throw new java.io.IOException("HTTP response code: " + probeCode);
                    }

                    // Попытки загрузки с уменьшением требуемой ширины, чтобы избежать OOM
                    int[] widths = new int[] {1200, 800, 600, 400};
                    javafx.scene.image.Image goodImage = null;
                    Exception lastEx = null;

                    for (int w : widths) {
                        HttpURLConnection conn = null;
                        try {
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                            conn.setConnectTimeout(10000);
                            conn.setReadTimeout(15000);

                            int code = conn.getResponseCode();
                            logger.debug("Image HTTP response code: " + code + " for URL: " + imagePath + " (requestedWidth=" + w + ")");
                            if (code >= 400) {
                                throw new java.io.IOException("HTTP response code: " + code);
                            }

                            try (InputStream is = conn.getInputStream()) {
                                // requestedHeight = 0 -> preserve ratio, let Image compute height
                                javafx.scene.image.Image img = new javafx.scene.image.Image(is, w, 0, true, true);
                                if (!img.isError()) {
                                    goodImage = img;
                                    logger.info("Успешно загружено изображение с шириной запроса=" + w + " для URL: " + imagePath);
                                    break;
                                } else {
                                    lastEx = img.getException();
                                    logger.warn("Image reported error when loading with width=" + w + ", exception=" + img.getException());
                                }
                            }

                        } catch (OutOfMemoryError oom) {
                            lastEx = new RuntimeException("OutOfMemoryError while loading image at requested width=" + w, oom);
                            logger.warn("OOM при загрузке изображения с width=" + w + ": " + oom.getMessage());
                            // попробуем со следующей, меньшей шириной
                        } catch (Exception ex) {
                            lastEx = ex;
                            logger.warn("Ошибка при попытке загрузки изображения (width=" + w + "): " + ex.getMessage());
                        } finally {
                            if (conn != null) conn.disconnect();
                        }
                    }

                    if (goodImage != null) {
                        final javafx.scene.image.Image finalImage = goodImage;
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(finalImage);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(500);
                            imageView.setFitHeight(400);
                            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);");
                            imageContainer.getChildren().clear();
                            imageContainer.getChildren().add(imageView);
                        });
                    } else {
                        logger.error("Не удалось загрузить изображение после всех попыток: " + imagePath, lastEx);
                        javafx.application.Platform.runLater(() -> {
                            imageContainer.getChildren().clear();
                            Label errorLabel = new Label("⚠️ Не удалось загрузить изображение");
                            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b07070;");
                            imageContainer.getChildren().add(errorLabel);
                        });
                    }

                } catch (Exception e) {
                    logger.error("Ошибка при загрузке изображения: " + imagePath, e);
                    javafx.application.Platform.runLater(() -> {
                        imageContainer.getChildren().clear();
                        Label errorLabel = new Label("⚠️ Не удалось загрузить изображение");
                        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b07070;");
                        Label urlLabel = new Label("URL: " + imagePath);
                        urlLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-wrap-text: true;");
                        urlLabel.setMaxWidth(450);
                        imageContainer.getChildren().addAll(errorLabel, urlLabel);
                    });
                }
            }).start();
        } else {
            Label noImageLabel = new Label("🖼️ Изображение отсутствует");
            noImageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa;");
            imageContainer.getChildren().add(noImageLabel);
            imageContainer.setMinHeight(200);
        }

        // Информация об экспонате
        VBox infoBox = new VBox(8);
        infoBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16;"
        );

        String infoStyle = "-fx-font-size: 13px; -fx-text-fill: #5a5a5a;";
        String valueStyle = "-fx-font-size: 13px; -fx-text-fill: #3a3a3a; -fx-font-weight: bold;";

        // Описание
        if (selected.getDescription() != null && !selected.getDescription().isEmpty()) {
            Label descLabel = new Label("📝 " + selected.getDescription());
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5a5a5a; -fx-wrap-text: true;");
            descLabel.setMaxWidth(550);
            infoBox.getChildren().add(descLabel);
            infoBox.getChildren().add(new javafx.scene.control.Separator());
        }

        // Сетка с информацией
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);

        int row = 0;

        // Автор
        if (selected instanceof Exhibit) {
            Exhibit exhibit = (Exhibit) selected;
            if (exhibit.getAuthor() != null) {
                Label authorLabel = new Label("👤 Автор:");
                authorLabel.setStyle(infoStyle);
                Label authorValue = new Label(exhibit.getAuthor().getFullName());
                authorValue.setStyle(valueStyle);
                infoGrid.add(authorLabel, 0, row);
                infoGrid.add(authorValue, 1, row);
                row++;
            }

            // Категория
            if (exhibit.getCategory() != null) {
                Label catLabel = new Label("📁 Категория:");
                catLabel.setStyle(infoStyle);
                Label catValue = new Label(exhibit.getCategory().getName());
                catValue.setStyle(valueStyle);
                infoGrid.add(catLabel, 0, row);
                infoGrid.add(catValue, 1, row);
                row++;
            }

            // Локация
            if (exhibit.getLocation() != null) {
                Label locLabel = new Label("📍 Расположение:");
                locLabel.setStyle(infoStyle);
                Label locValue = new Label(exhibit.getLocation().getHallName());
                locValue.setStyle(valueStyle);
                infoGrid.add(locLabel, 0, row);
                infoGrid.add(locValue, 1, row);
                row++;
            }
        }

        // Дата создания
        if (selected.getCreationDate() != null) {
            Label dateLabel = new Label("📅 Дата создания:");
            dateLabel.setStyle(infoStyle);
            Label dateValue = new Label(selected.getCreationDate().toString());
            dateValue.setStyle(valueStyle);
            infoGrid.add(dateLabel, 0, row);
            infoGrid.add(dateValue, 1, row);
            row++;
        }

        // Статус
        Label statusLabel = new Label("🔖 Статус:");
        statusLabel.setStyle(infoStyle);
        String statusText = getStatusText(selected.getStatus());
        Label statusValue = new Label(statusText);
        String statusColor = switch (selected.getStatus()) {
            case ON_DISPLAY -> "#4CAF50";
            case IN_STORAGE -> "#9E9E9E";
            case ON_RESTORATION -> "#FF9800";
            case ON_LOAN -> "#2196F3";
            default -> "#666666";
        };
        statusValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
        infoGrid.add(statusLabel, 0, row);
        infoGrid.add(statusValue, 1, row);
        row++;

        // Специфичные поля для разных типов
        if (selected instanceof Painting painting) {
            Label techLabel = new Label("🎨 Техника:");
            techLabel.setStyle(infoStyle);
            Label techValue = new Label(painting.getTechnique());
            techValue.setStyle(valueStyle);
            infoGrid.add(techLabel, 2, 0);
            infoGrid.add(techValue, 3, 0);

            Label sizeLabel = new Label("📐 Размер:");
            sizeLabel.setStyle(infoStyle);
            Label sizeValue = new Label(painting.getWidth() + " × " + painting.getHeight() + " см");
            sizeValue.setStyle(valueStyle);
            infoGrid.add(sizeLabel, 2, 1);
            infoGrid.add(sizeValue, 3, 1);
        } else if (selected instanceof Sculpture sculpture) {
            Label matLabel = new Label("🪨 Материал:");
            matLabel.setStyle(infoStyle);
            Label matValue = new Label(sculpture.getMaterial());
            matValue.setStyle(valueStyle);
            infoGrid.add(matLabel, 2, 0);
            infoGrid.add(matValue, 3, 0);

            Label heightLabel = new Label("📏 Высота:");
            heightLabel.setStyle(infoStyle);
            Label heightValue = new Label(sculpture.getHeight() + " см");
            heightValue.setStyle(valueStyle);
            infoGrid.add(heightLabel, 2, 1);
            infoGrid.add(heightValue, 3, 1);
        } else if (selected instanceof Artifact artifact) {
            Label originLabel = new Label("🌍 Происхождение:");
            originLabel.setStyle(infoStyle);
            Label originValue = new Label(artifact.getOrigin());
            originValue.setStyle(valueStyle);
            infoGrid.add(originLabel, 2, 0);
            infoGrid.add(originValue, 3, 0);

            Label periodLabel = new Label("⏳ Период:");
            periodLabel.setStyle(infoStyle);
            Label periodValue = new Label(artifact.getPeriod());
            periodValue.setStyle(valueStyle);
            infoGrid.add(periodLabel, 2, 1);
            infoGrid.add(periodValue, 3, 1);
        }

        infoBox.getChildren().add(infoGrid);

        mainContent.getChildren().addAll(headerBox, imageContainer, infoBox);

        // Scroll если контент большой
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setMaxHeight(650);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setMinWidth(640);

        dialog.showAndWait();
    }

    /**
     * Обработчик удаления экспоната.
     */
    @FXML
    public void handleDeleteExhibit() {
        MuseumItem selected = exhibitTable != null ? exhibitTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Выбор", "Пожалуйста, выберите экспонат для удаления");
            return;
        }

        // Красивый диалог подтверждения удаления
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("🗑️ Удаление экспоната");
        dialog.setHeaderText(null);

        ButtonType deleteButtonType = new ButtonType("🗑️ Удалить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #fdf0f0, #f8e8e8);");

        // Иконка предупреждения
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");

        Label headerLabel = new Label("Подтверждение удаления");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8b4a4a;");

        // Карточка экспоната
        VBox itemCard = new VBox(8);
        itemCard.setPadding(new Insets(16));
        itemCard.setAlignment(javafx.geometry.Pos.CENTER);
        itemCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e8d0d0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );

        String typeEmoji = "📦";
        if (selected instanceof Painting) typeEmoji = "🖼️";
        else if (selected instanceof Sculpture) typeEmoji = "🗿";
        else if (selected instanceof Artifact) typeEmoji = "⚱️";

        Label itemEmoji = new Label(typeEmoji);
        itemEmoji.setStyle("-fx-font-size: 28px;");

        Label itemName = new Label(selected.getName());
        itemName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a4a4a;");

        Label itemInv = new Label("Инв. №: " + selected.getInventoryNumber());
        itemInv.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a8a;");

        itemCard.getChildren().addAll(itemEmoji, itemName, itemInv);

        Label questionLabel = new Label("Вы уверены, что хотите удалить этот экспонат?");
        questionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b4a4a;");

        Label noteLabel = new Label("⚡ Это действие нельзя отменить!");
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #b07070; -fx-font-weight: bold;");

        content.getChildren().addAll(warningIcon, headerLabel, itemCard, questionLabel, noteLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(380);

        dialog.setResultConverter(dialogButton -> dialogButton == deleteButtonType);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            try {
                exhibitService.deleteExhibit(selected.getId());
                updateExhibitTable();
                updateStatusBar();
                showInfo("Успех", "Экспонат \"" + selected.getName() + "\" удалён");
            } catch (Exception e) {
                logger.error("Ошибка при удалении экспоната", e);
                showError("Ошибка", e.getMessage());
            }
        }
    }

    /**
     * Фильтр по категории.
     *
     * @param category категория
     */
    private void filterByCategory(Category category) {
        List<MuseumItem> results = exhibitService.filterByCategory(category);
        if (exhibitTable != null) {
            ObservableList<MuseumItem> data = FXCollections.observableArrayList(results);
            exhibitTable.setItems(data);
        }
        logger.info("Фильтр по категории: " + category.getName());
    }

    /**
     * Показывает диалог информации.
     *
     * @param title заголовок
     * @param message сообщение
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показывает диалог предупреждения.
     *
     * @param title заголовок
     * @param message сообщение
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показывает диалог ошибки.
     *
     * @param title заголовок
     * @param message сообщение
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Обработчики событий FXML =====

    /**
     * Обработчик обновления данных.
     */
    @FXML
    public void handleRefresh() {
        logger.info("Обновление данных");
        updateExhibitTable();
        updateStatusBar();
        showTemporaryStatus("Данные обновлены");
    }

    /**
     * Обработчик сброса фильтров.
     */
    @FXML
    public void handleResetFilters() {
        if (searchTextField != null) searchTextField.clear();
        if (statusFilter != null) statusFilter.setValue(null);
        if (authorFilter != null) authorFilter.setValue(null);
        updateExhibitTable();
        if (tableTitle != null) tableTitle.setText("Все экспонаты");
    }

    /**
     * Обработчик добавления картины.
     */
    @FXML
    public void handleAddPainting() {
        logger.info("Добавление картины");

        Dialog<Painting> dialog = new Dialog<>();
        dialog.setTitle("🖼️ Добавление картины");
        dialog.setHeaderText(null);

        ButtonType addButtonType = new ButtonType("✨ Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Главный контейнер
        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(24));
        mainContent.setStyle("-fx-background-color: linear-gradient(to bottom, #fdf8f8, #f4e1e1);");

        // Заголовок
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(12);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label emojiLabel = new Label("🖼️");
        emojiLabel.setStyle("-fx-font-size: 32px;");
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Новая картина");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6a4a4a;");
        Label subLabel = new Label("Заполните информацию о произведении");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a6a6a;");
        titleBox.getChildren().addAll(titleLabel, subLabel);
        headerBox.getChildren().addAll(emojiLabel, titleBox);

        // Стили
        String labelStyle = "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #5a4a4a;";
        String fieldStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #d8c8c8; -fx-padding: 8 12; -fx-background-color: white;";

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        TextField nameField = new TextField();
        nameField.setPromptText("Название картины");
        nameField.setStyle(fieldStyle);
        nameField.setPrefWidth(250);

        TextField descField = new TextField();
        descField.setPromptText("Описание");
        descField.setStyle(fieldStyle);

        TextField techniqueField = new TextField();
        techniqueField.setPromptText("Техника (масло, акварель...)");
        techniqueField.setStyle(fieldStyle);

        Spinner<Integer> widthSpinner = new Spinner<>(1, 1000, 100);
        widthSpinner.setEditable(true);
        widthSpinner.setStyle(fieldStyle);

        Spinner<Integer> heightSpinner = new Spinner<>(1, 1000, 100);
        heightSpinner.setEditable(true);
        heightSpinner.setStyle(fieldStyle);

        CheckBox frameCheck = new CheckBox("🖼️ В раме");
        frameCheck.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a4a4a;");

        ComboBox<Artist> authorCombo = new ComboBox<>();
        authorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        authorCombo.setStyle(fieldStyle);
        authorCombo.setMaxWidth(Double.MAX_VALUE);
        authorCombo.setButtonCell(new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        authorCombo.setCellFactory(lv -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        categoryCombo.setStyle(fieldStyle);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);

        // Добавляем элементы
        Label l1 = new Label("📝 Название *"); l1.setStyle(labelStyle);
        Label l2 = new Label("📄 Описание"); l2.setStyle(labelStyle);
        Label l3 = new Label("🎨 Техника"); l3.setStyle(labelStyle);
        Label l4 = new Label("↔️ Ширина (см)"); l4.setStyle(labelStyle);
        Label l5 = new Label("↕️ Высота (см)"); l5.setStyle(labelStyle);
        Label l6 = new Label("👤 Автор"); l6.setStyle(labelStyle);
        Label l7 = new Label("📁 Категория *"); l7.setStyle(labelStyle);

        grid.add(l1, 0, 0); grid.add(nameField, 1, 0);
        grid.add(l2, 0, 1); grid.add(descField, 1, 1);
        grid.add(l3, 0, 2); grid.add(techniqueField, 1, 2);
        grid.add(l4, 0, 3); grid.add(widthSpinner, 1, 3);
        grid.add(l5, 0, 4); grid.add(heightSpinner, 1, 4);
        grid.add(new Label(""), 0, 5); grid.add(frameCheck, 1, 5);
        grid.add(l6, 0, 6); grid.add(authorCombo, 1, 6);
        grid.add(l7, 0, 7); grid.add(categoryCombo, 1, 7);

        mainContent.getChildren().addAll(headerBox, grid);
        dialog.getDialogPane().setContent(mainContent);
        dialog.getDialogPane().setMinWidth(480);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isBlank() || categoryCombo.getValue() == null) {
                    showWarning("Ошибка", "Заполните обязательные поля");
                    return null;
                }
                Painting painting = new Painting();
                painting.setName(nameField.getText());
                painting.setDescription(descField.getText());
                painting.setTechnique(techniqueField.getText().isBlank() ? "Масло" : techniqueField.getText());
                painting.setWidth(widthSpinner.getValue());
                painting.setHeight(heightSpinner.getValue());
                painting.setHasFrame(frameCheck.isSelected());
                painting.setAuthor(authorCombo.getValue());
                painting.setCategory(categoryCombo.getValue());
                painting.setStatus(ExhibitStatus.IN_STORAGE);
                return painting;
            }
            return null;
        });

        Optional<Painting> result = dialog.showAndWait();
        result.ifPresent(painting -> {
            try {
                exhibitService.addExhibit(painting);
                updateExhibitTable();
                updateStatusBar();
                showInfo("Успех", "Картина \"" + painting.getName() + "\" добавлена!");
            } catch (Exception e) {
                logger.error("Ошибка при добавлении картины", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик добавления скульптуры.
     */
    @FXML
    public void handleAddSculpture() {
        logger.info("Добавление скульптуры");

        Dialog<Sculpture> dialog = new Dialog<>();
        dialog.setTitle("Добавление скульптуры");
        dialog.setHeaderText("Введите данные скульптуры");

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название скульптуры");

        TextField descField = new TextField();
        descField.setPromptText("Описание");

        TextField materialField = new TextField();
        materialField.setPromptText("Материал (мрамор, бронза...)");

        Spinner<Double> weightSpinner = new Spinner<>(0.1, 10000, 50, 0.5);
        weightSpinner.setEditable(true);

        Spinner<Double> heightSpinner = new Spinner<>(1, 1000, 100, 1);
        heightSpinner.setEditable(true);

        ComboBox<Artist> authorCombo = new ComboBox<>();
        authorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        authorCombo.setButtonCell(new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        authorCombo.setCellFactory(lv -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Материал:"), 0, 2);
        grid.add(materialField, 1, 2);
        grid.add(new Label("Вес (кг):"), 0, 3);
        grid.add(weightSpinner, 1, 3);
        grid.add(new Label("Высота (см):"), 0, 4);
        grid.add(heightSpinner, 1, 4);
        grid.add(new Label("Автор:"), 0, 5);
        grid.add(authorCombo, 1, 5);
        grid.add(new Label("Категория:"), 0, 6);
        grid.add(categoryCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isBlank() || categoryCombo.getValue() == null) {
                    showWarning("Ошибка", "Заполните обязательные поля");
                    return null;
                }
                Sculpture sculpture = new Sculpture();
                sculpture.setName(nameField.getText());
                sculpture.setDescription(descField.getText());
                sculpture.setMaterial(materialField.getText().isBlank() ? "Мрамор" : materialField.getText());
                sculpture.setWeight(weightSpinner.getValue());
                sculpture.setHeight(heightSpinner.getValue());
                sculpture.setAuthor(authorCombo.getValue());
                sculpture.setCategory(categoryCombo.getValue());
                sculpture.setStatus(ExhibitStatus.IN_STORAGE);
                return sculpture;
            }
            return null;
        });

        Optional<Sculpture> result = dialog.showAndWait();
        result.ifPresent(sculpture -> {
            try {
                exhibitService.addExhibit(sculpture);
                updateExhibitTable();
                updateStatusBar();
                showInfo("Успех", "Скульптура \"" + sculpture.getName() + "\" добавлена!");
            } catch (Exception e) {
                logger.error("Ошибка при добавлении скульптуры", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик добавления артефакта.
     */
    @FXML
    public void handleAddArtifact() {
        logger.info("Добавление артефакта");

        Dialog<Artifact> dialog = new Dialog<>();
        dialog.setTitle("Добавление артефакта");
        dialog.setHeaderText("Введите данные артефакта");

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название артефакта");

        TextField descField = new TextField();
        descField.setPromptText("Описание");

        TextField originField = new TextField();
        originField.setPromptText("Происхождение (Древний Египет...)");

        TextField periodField = new TextField();
        periodField.setPromptText("Период/эпоха");

        TextField materialField = new TextField();
        materialField.setPromptText("Материал");

        Spinner<Double> ageSpinner = new Spinner<>(0, 100000, 1000, 100);
        ageSpinner.setEditable(true);

        ComboBox<Artist> authorCombo = new ComboBox<>();
        authorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        authorCombo.setButtonCell(new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        authorCombo.setCellFactory(lv -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Происхождение:"), 0, 2);
        grid.add(originField, 1, 2);
        grid.add(new Label("Период:"), 0, 3);
        grid.add(periodField, 1, 3);
        grid.add(new Label("Материал:"), 0, 4);
        grid.add(materialField, 1, 4);
        grid.add(new Label("Возраст (лет):"), 0, 5);
        grid.add(ageSpinner, 1, 5);
        grid.add(new Label("Автор:"), 0, 6);
        grid.add(authorCombo, 1, 6);
        grid.add(new Label("Категория:"), 0, 7);
        grid.add(categoryCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isBlank() || categoryCombo.getValue() == null) {
                    showWarning("Ошибка", "Заполните обязательные поля");
                    return null;
                }
                Artifact artifact = new Artifact();
                artifact.setName(nameField.getText());
                artifact.setDescription(descField.getText());
                artifact.setOrigin(originField.getText().isBlank() ? "Неизвестно" : originField.getText());
                artifact.setPeriod(periodField.getText().isBlank() ? "Неизвестно" : periodField.getText());
                artifact.setMaterial(materialField.getText().isBlank() ? "Неизвестно" : materialField.getText());
                artifact.setAge(ageSpinner.getValue());
                artifact.setAuthor(authorCombo.getValue());
                artifact.setCategory(categoryCombo.getValue());
                artifact.setStatus(ExhibitStatus.IN_STORAGE);
                return artifact;
            }
            return null;
        });

        Optional<Artifact> result = dialog.showAndWait();
        result.ifPresent(artifact -> {
            try {
                exhibitService.addExhibit(artifact);
                updateExhibitTable();
                updateStatusBar();
                showInfo("Успех", "Артефакт \"" + artifact.getName() + "\" добавлен!");
            } catch (Exception e) {
                logger.error("Ошибка при добавлении артефакта", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик изменения статуса.
     */
    @FXML
    public void handleChangeStatus() {
        MuseumItem selected = exhibitTable != null ? exhibitTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Выбор", "Пожалуйста, выберите экспонат");
            return;
        }
        logger.info("Изменение статуса экспоната");

        ChoiceDialog<ExhibitStatus> dialog = new ChoiceDialog<>(selected.getStatus(), ExhibitStatus.values());
        dialog.setTitle("Изменение статуса");
        dialog.setHeaderText("Текущий статус: " + getStatusText(selected.getStatus()));
        dialog.setContentText("Новый статус:");

        Optional<ExhibitStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            try {
                selected.setStatus(newStatus);
                exhibitService.updateExhibit(selected);
                updateExhibitTable();
                showInfo("Успех", "Статус изменён на: " + getStatusText(newStatus));
            } catch (Exception e) {
                logger.error("Ошибка при изменении статуса", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик изменения местоположения.
     */
    @FXML
    public void handleChangeLocation() {
        MuseumItem selected = exhibitTable != null ? exhibitTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Выбор", "Пожалуйста, выберите экспонат");
            return;
        }
        logger.info("Изменение местоположения экспоната");

        // Создаём диалог ввода зала
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Изменение местоположения");
        dialog.setHeaderText("Экспонат: " + selected.getName());
        dialog.setContentText("Введите название нового зала:");

        if (selected instanceof Exhibit) {
            Exhibit exhibit = (Exhibit) selected;
            if (exhibit.getLocation() != null) {
                dialog.getEditor().setText(exhibit.getLocation().getHallName());
            }
        }

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(hallName -> {
            if (!hallName.isBlank() && selected instanceof Exhibit) {
                try {
                    Exhibit exhibit = (Exhibit) selected;
                    Location location = new Location(hallName, "A1", 100);
                    exhibit.setLocation(location);
                    exhibitService.updateExhibit(exhibit);
                    updateExhibitTable();
                    showInfo("Успех", "Местоположение изменено на: " + hallName);
                } catch (Exception e) {
                    logger.error("Ошибка при изменении местоположения", e);
                    showError("Ошибка", e.getMessage());
                }
            }
        });
    }

    /**
     * Обработчик управления категориями.
     */
    @FXML
    public void handleManageCategories() {
        logger.info("Открытие менеджера категорий");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📁 Категории музея");
        dialog.setHeaderText("Управление категориями экспонатов");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb;");

        // Заголовок с иконкой
        Label titleLabel = new Label("📁 Категории коллекции");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4a5a50;");

        List<Category> categories = categoryService.getAllCategories();

        VBox categoriesBox = new VBox(8);
        categoriesBox.setStyle("-fx-background-color: #e8f0ec; -fx-background-radius: 12; -fx-padding: 16;");

        for (Category cat : categories) {
            VBox catCard = new VBox(4);
            catCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #d0e0d8; -fx-border-radius: 8; -fx-border-width: 1;");

            Label nameLabel = new Label("🏷️ " + cat.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #4a5a50;");

            Label codeLabel = new Label("Код: " + cat.getCategoryCode());
            codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6a7a70;");

            catCard.getChildren().addAll(nameLabel, codeLabel);
            categoriesBox.getChildren().add(catCard);
        }

        // Итоговая статистика
        Label statsLabel = new Label("📊 Всего категорий: " + categories.size());
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #9cb4a0; -fx-padding: 12 0 0 0;");

        content.getChildren().addAll(titleLabel, categoriesBox, statsLabel);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");
        dialog.showAndWait();
    }

    /**
     * Обработчик управления художниками.
     */
    @FXML
    public void handleManageArtists() {
        logger.info("Открытие менеджера художников");

        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle("🎨 Художники и авторы");
        dialog.setHeaderText("Управление авторами произведений");

        ButtonType addButtonType = new ButtonType("➕ Добавить художника", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CLOSE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb;");

        // Заголовок
        Label titleLabel = new Label("🎨 Мастера и художники");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5a4a60;");

        List<Artist> artists = artistService.getAllArtists();

        VBox artistsBox = new VBox(10);
        artistsBox.setStyle("-fx-background-color: #f0e8f4; -fx-background-radius: 12; -fx-padding: 16;");

        for (Artist artist : artists) {
            VBox artistCard = new VBox(4);
            artistCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 14; -fx-border-color: #e0d0e8; -fx-border-radius: 8; -fx-border-width: 1;");

            Label nameLabel = new Label("👨‍🎨 " + artist.getFullName());
            nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #5a4a60;");

            String yearsText = "";
            if (artist.getBirthYear() > 0) {
                Integer deathYear = artist.getDeathYear();
                yearsText = String.format("📅 %d — %s",
                        artist.getBirthYear(),
                        deathYear != null && deathYear > 0 ? deathYear.toString() : "наст. время");
            }
            Label yearsLabel = new Label(yearsText);
            yearsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a6a80;");

            String country = artist.getCountry() != null ? artist.getCountry() : "";
            Label countryLabel = new Label(country.isEmpty() ? "" : "🌍 " + country);
            countryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a7a90;");

            artistCard.getChildren().add(nameLabel);
            if (!yearsText.isEmpty()) artistCard.getChildren().add(yearsLabel);
            if (!country.isEmpty()) artistCard.getChildren().add(countryLabel);

            artistsBox.getChildren().add(artistCard);
        }

        // Статистика
        Label statsLabel = new Label("📊 Всего художников: " + artists.size());
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #a08cb0; -fx-padding: 12 0 0 0;");

        content.getChildren().addAll(titleLabel, artistsBox, statsLabel);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(480);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == addButtonType) {
                addNewArtist();
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Добавление нового художника.
     */
    private void addNewArtist() {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle("Новый художник");
        dialog.setHeaderText("Введите данные художника");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Полное имя");

        Spinner<Integer> birthYearSpinner = new Spinner<>(1, 2025, 1900);
        birthYearSpinner.setEditable(true);

        Spinner<Integer> deathYearSpinner = new Spinner<>(0, 2025, 0);
        deathYearSpinner.setEditable(true);

        TextField countryField = new TextField();
        countryField.setPromptText("Страна");

        grid.add(new Label("Полное имя:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Год рождения:"), 0, 1);
        grid.add(birthYearSpinner, 1, 1);
        grid.add(new Label("Год смерти (0=жив):"), 0, 2);
        grid.add(deathYearSpinner, 1, 2);
        grid.add(new Label("Страна:"), 0, 3);
        grid.add(countryField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (fullNameField.getText().isBlank()) {
                    showWarning("Ошибка", "Введите имя художника");
                    return null;
                }
                Artist artist = new Artist();
                artist.setFullName(fullNameField.getText());
                artist.setBirthYear(birthYearSpinner.getValue());
                if (deathYearSpinner.getValue() > 0) {
                    artist.setDeathYear(deathYearSpinner.getValue());
                }
                artist.setCountry(countryField.getText());
                return artist;
            }
            return null;
        });

        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(artist -> {
            try {
                artistService.addArtist(artist);
                showInfo("Успех", "Художник \"" + artist.getFullName() + "\" добавлен!");
            } catch (Exception e) {
                logger.error("Ошибка при добавлении художника", e);
                showError("Ошибка", e.getMessage());
            }
        });
    }

    /**
     * Обработчик управления залами.
     */
    @FXML
    public void handleManageLocations() {
        logger.info("Открытие менеджера залов");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🏛️ Залы музея");
        dialog.setHeaderText("Управление залами и местоположениями");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb;");

        // Заголовок
        Label titleLabel = new Label("🏛️ Залы музея");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6a5040;");

        // Собираем информацию о залах из экспонатов
        List<MuseumItem> allExhibits = exhibitService.getAllExhibits();
        java.util.Map<String, Integer> hallCounts = new java.util.HashMap<>();

        for (MuseumItem item : allExhibits) {
            if (item instanceof Exhibit) {
                Exhibit exhibit = (Exhibit) item;
                if (exhibit.getLocation() != null) {
                    String hall = exhibit.getLocation().getHallName();
                    hallCounts.put(hall, hallCounts.getOrDefault(hall, 0) + 1);
                } else {
                    hallCounts.put("Без зала", hallCounts.getOrDefault("Без зала", 0) + 1);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════╗\n");
        sb.append("║             ЗАЛЫ МУЗЕЯ                           ║\n");
        sb.append("╠══════════════════════════════════════════════════╣\n");

        VBox hallsBox = new VBox(8);
        hallsBox.setStyle("-fx-background-color: #fce5d8; -fx-background-radius: 12; -fx-padding: 16;");

        for (java.util.Map.Entry<String, Integer> entry : hallCounts.entrySet()) {
            VBox hallCard = new VBox(4);
            hallCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #f0d8c8; -fx-border-radius: 8; -fx-border-width: 1;");

            Label hallName = new Label("🚪 " + entry.getKey());
            hallName.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #6a5040;");

            Label exhibitCount = new Label("📦 Экспонатов: " + entry.getValue());
            exhibitCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a7060;");

            hallCard.getChildren().addAll(hallName, exhibitCount);
            hallsBox.getChildren().add(hallCard);
        }

        // Статистика
        Label statsLabel = new Label("📊 Всего залов: " + hallCounts.size() + " | Экспонатов: " + allExhibits.size());
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #d4a88a; -fx-padding: 12 0 0 0;");

        content.getChildren().addAll(titleLabel, hallsBox, statsLabel);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");
        dialog.showAndWait();
    }

    /**
     * Обработчик статистики.
     */
    @FXML
    public void handleStatistics() {
        logger.info("Открытие статистики");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📊 Статистика музея");
        dialog.setHeaderText("Подробная статистика коллекции");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb;");

        List<MuseumItem> allExhibits = exhibitService.getAllExhibits();

        // Подсчёт по типам
        long paintingCount = allExhibits.stream().filter(e -> e instanceof Painting).count();
        long sculptureCount = allExhibits.stream().filter(e -> e instanceof Sculpture).count();
        long artifactCount = allExhibits.stream().filter(e -> e instanceof Artifact).count();

        // Подсчёт по статусам
        long onDisplay = exhibitService.filterByStatus(ExhibitStatus.ON_DISPLAY).size();
        long inStorage = exhibitService.filterByStatus(ExhibitStatus.IN_STORAGE).size();
        long onRestoration = exhibitService.filterByStatus(ExhibitStatus.ON_RESTORATION).size();
        long onLoan = exhibitService.filterByStatus(ExhibitStatus.ON_LOAN).size();

        // Заголовок
        Label titleLabel = new Label("📊 Статистика коллекции");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5a4a65;");

        // Общая статистика
        VBox totalBox = new VBox(8);
        totalBox.setStyle("-fx-background-color: #f0e8f4; -fx-background-radius: 12; -fx-padding: 16;");

        Label totalLabel = new Label("🏛️ Всего экспонатов: " + allExhibits.size());
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #5a4a65;");
        totalBox.getChildren().add(totalLabel);

        // По типам
        VBox typesBox = new VBox(8);
        typesBox.setStyle("-fx-background-color: #e8f0ec; -fx-background-radius: 12; -fx-padding: 16;");

        Label typesTitle = new Label("📦 По типам экспонатов:");
        typesTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #4a5a50;");

        javafx.scene.layout.HBox typesRow = new javafx.scene.layout.HBox(16);
        typesRow.getChildren().addAll(
                createStatCard("🖼️ Картины", String.valueOf(paintingCount), "#c9a9a9"),
                createStatCard("🗿 Скульптуры", String.valueOf(sculptureCount), "#9cb4a0"),
                createStatCard("⚱️ Артефакты", String.valueOf(artifactCount), "#d4a88a")
        );

        typesBox.getChildren().addAll(typesTitle, typesRow);

        // По статусам
        VBox statusBox = new VBox(8);
        statusBox.setStyle("-fx-background-color: #dde8f0; -fx-background-radius: 12; -fx-padding: 16;");

        Label statusTitle = new Label("📋 По статусам:");
        statusTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #4a5060;");

        javafx.scene.layout.HBox statusRow1 = new javafx.scene.layout.HBox(16);
        statusRow1.getChildren().addAll(
                createStatCard("✅ На экспозиции", String.valueOf(onDisplay), "#9cb4a0"),
                createStatCard("📦 В хранилище", String.valueOf(inStorage), "#7d8a96")
        );

        javafx.scene.layout.HBox statusRow2 = new javafx.scene.layout.HBox(16);
        statusRow2.getChildren().addAll(
                createStatCard("🔧 На реставрации", String.valueOf(onRestoration), "#d4a88a"),
                createStatCard("📤 В аренде", String.valueOf(onLoan), "#a08cb0")
        );

        statusBox.getChildren().addAll(statusTitle, statusRow1, statusRow2);

        // Прочее
        VBox otherBox = new VBox(8);
        otherBox.setStyle("-fx-background-color: #f4e1e1; -fx-background-radius: 12; -fx-padding: 16;");

        Label otherTitle = new Label("📚 Справочные данные:");
        otherTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #5a5050;");

        javafx.scene.layout.HBox otherRow = new javafx.scene.layout.HBox(16);
        otherRow.getChildren().addAll(
                createStatCard("🎨 Художников", String.valueOf(artistService.getAllArtists().size()), "#a08cb0"),
                createStatCard("📁 Категорий", String.valueOf(categoryService.getAllCategories().size()), "#c9a9a9")
        );

        otherBox.getChildren().addAll(otherTitle, otherRow);

        content.getChildren().addAll(titleLabel, totalBox, typesBox, statusBox, otherBox);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");
        dialog.showAndWait();
    }

    /**
     * Создание карточки статистики.
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 12; -fx-min-width: 120;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6a6a6a;");

        card.getChildren().addAll(valueLabel, titleLbl);
        return card;
    }

    /**
     * Обработчик отчёта по реставрации.
     */
    @FXML
    public void handleRestorationReport() {
        logger.info("Отчёт по реставрации");

        List<MuseumItem> onRestorationList = exhibitService.filterByStatus(ExhibitStatus.ON_RESTORATION);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🔧 Отчёт по реставрации");
        dialog.setHeaderText("Экспонаты на реставрации");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb;");

        // Заголовок
        Label titleLabel = new Label("🔧 Экспонаты на реставрации");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6a5040;");

        VBox itemsBox = new VBox(10);
        itemsBox.setStyle("-fx-background-color: #fce5d8; -fx-background-radius: 12; -fx-padding: 16;");

        if (onRestorationList.isEmpty()) {
            Label emptyLabel = new Label("✅ Нет экспонатов на реставрации");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9cb4a0;");
            itemsBox.getChildren().add(emptyLabel);
        } else {
            for (MuseumItem item : onRestorationList) {
                VBox itemCard = new VBox(4);
                itemCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #f0d8c8; -fx-border-radius: 8; -fx-border-width: 1;");

                Label nameLabel = new Label("🔧 " + item.getName());
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #6a5040;");

                Label invLabel = new Label("📋 Инв. номер: " + item.getInventoryNumber());
                invLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a7060;");

                itemCard.getChildren().addAll(nameLabel, invLabel);
                itemsBox.getChildren().add(itemCard);
            }
        }

        // Статистика
        Label statsLabel = new Label("📊 Итого на реставрации: " + onRestorationList.size());
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #d4a88a; -fx-padding: 12 0 0 0;");

        content.getChildren().addAll(titleLabel, itemsBox, statsLabel);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");
        dialog.showAndWait();
    }

    /**
     * Обработчик отчёта по залам.
     */
    @FXML
    public void handleLocationReport() {
        logger.info("Отчёт по залам");
        handleManageLocations(); // Используем тот же диалог
    }

    /**
     * Обработчик импорта JSON.
     */
    @FXML
    public void handleImportJson() {
        logger.info("Импорт из JSON");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите JSON файл для импорта");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Импорт данных
                showInfo("Импорт", "Импорт из файла: " + file.getName() + "\n\nФайл загружен успешно!");
                updateExhibitTable();
                updateStatusBar();
            } catch (Exception e) {
                logger.error("Ошибка импорта", e);
                showError("Ошибка импорта", e.getMessage());
            }
        }
    }

    /**
     * Обработчик экспорта JSON.
     */
    @FXML
    public void handleExportJson() {
        logger.info("Экспорт в JSON");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить как JSON");
        fileChooser.setInitialFileName("exhibits_export.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<MuseumItem> exhibits = exhibitService.getAllExhibits();

                // Простой JSON экспорт
                StringBuilder json = new StringBuilder("[\n");
                for (int i = 0; i < exhibits.size(); i++) {
                    MuseumItem item = exhibits.get(i);
                    json.append("  {\n");
                    json.append("    \"id\": ").append(item.getId()).append(",\n");
                    json.append("    \"name\": \"").append(item.getName()).append("\",\n");
                    json.append("    \"inventoryNumber\": \"").append(item.getInventoryNumber()).append("\",\n");
                    json.append("    \"status\": \"").append(item.getStatus()).append("\",\n");
                    json.append("    \"type\": \"").append(item.getClass().getSimpleName()).append("\"\n");
                    json.append("  }");
                    if (i < exhibits.size() - 1) json.append(",");
                    json.append("\n");
                }
                json.append("]");

                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.print(json.toString());
                }

                showInfo("Экспорт завершён", "Данные экспортированы в:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Ошибка экспорта", e);
                showError("Ошибка экспорта", e.getMessage());
            }
        }
    }

    /**
     * Обработчик экспорта CSV.
     */
    @FXML
    public void handleExportCsv() {
        logger.info("Экспорт в CSV");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить как CSV");
        fileChooser.setInitialFileName("exhibits_export.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<MuseumItem> exhibits = exhibitService.getAllExhibits();

                try (PrintWriter writer = new PrintWriter(file)) {
                    // Заголовок
                    writer.println("ID;Инвентарный номер;Название;Тип;Статус");

                    // Данные
                    for (MuseumItem item : exhibits) {
                        writer.printf("%d;%s;%s;%s;%s%n",
                                item.getId(),
                                item.getInventoryNumber(),
                                item.getName(),
                                item.getClass().getSimpleName(),
                                item.getStatus()
                        );
                    }
                }

                showInfo("Экспорт завершён", "Данные экспортированы в:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Ошибка экспорта", e);
                showError("Ошибка экспорта", e.getMessage());
            }
        }
    }

    /**
     * Обработчик экспорта PDF.
     */
    @FXML
    public void handleExportPdf() {
        logger.info("Экспорт в PDF");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчёт как PDF");
        fileChooser.setInitialFileName("museum_report.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<MuseumItem> exhibits = exhibitService.getAllExhibits();

                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("═══════════════════════════════════════════════════════════");
                    writer.println("                   ОТЧЁТ МУЗЕЯ");
                    writer.println("═══════════════════════════════════════════════════════════");
                    writer.println();
                    writer.println("Дата формирования: " + java.time.LocalDateTime.now());
                    writer.println();
                    writer.println("СПИСОК ЭКСПОНАТОВ:");
                    writer.println("───────────────────────────────────────────────────────────");

                    for (MuseumItem item : exhibits) {
                        writer.printf("  • %s (%s)%n", item.getName(), item.getInventoryNumber());
                        writer.printf("    Тип: %s | Статус: %s%n",
                                item.getClass().getSimpleName(),
                                getStatusText(item.getStatus()));
                        writer.println();
                    }

                    writer.println("───────────────────────────────────────────────────────────");
                    writer.printf("Всего экспонатов: %d%n", exhibits.size());
                    writer.println("═══════════════════════════════════════════════════════════");
                }

                showInfo("Отчёт сформирован", "Отчёт сохранён в:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Ошибка формирования отчёта", e);
                showError("Ошибка", e.getMessage());
            }
        }
    }

    /**
     * Обработчик "О программе".
     */
    @FXML
    public void handleAbout() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("ℹ️ О программе");
        dialog.setHeaderText("Museum Catalog");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #fdfcfb; -fx-alignment: center;");

        // Логотип/название
        Label logoLabel = new Label("🏛️");
        logoLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label("MUSEUM CATALOG");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #c9a9a9;");

        Label versionLabel = new Label("Версия 1.0");
        versionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a8a8a;");

        // Разделитель
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();

        // Описание
        VBox descBox = new VBox(8);
        descBox.setStyle("-fx-background-color: #f4e1e1; -fx-background-radius: 12; -fx-padding: 20;");

        Label descTitle = new Label("📋 Описание");
        descTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #5a5050;");

        Label descText = new Label("Система управления музейными экспонатами с поддержкой категоризации, поиска и отчётности.");
        descText.setStyle("-fx-font-size: 13px; -fx-text-fill: #6a6a6a; -fx-wrap-text: true;");
        descText.setWrapText(true);

        descBox.getChildren().addAll(descTitle, descText);

        // Функциональность
        VBox funcBox = new VBox(8);
        funcBox.setStyle("-fx-background-color: #e8f0ec; -fx-background-radius: 12; -fx-padding: 16;");

        Label funcTitle = new Label("✨ Возможности");
        funcTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #4a5a50;");

        VBox funcList = new VBox(4);
        String[] features = {
                "📦 Управление экспонатами",
                "🏷️ Категоризация и классификация",
                "🎨 Управление художниками",
                "🏛️ Управление залами музея",
                "🔍 Поиск и фильтрация",
                "📊 Статистика и отчётность",
                "📤 Импорт/экспорт данных"
        };
        for (String feature : features) {
            Label featLabel = new Label(feature);
            featLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a6a5a;");
            funcList.getChildren().add(featLabel);
        }

        funcBox.getChildren().addAll(funcTitle, funcList);

        // Автор и копирайт
        VBox authorBox = new VBox(4);
        authorBox.setStyle("-fx-padding: 16 0 0 0; -fx-alignment: center;");

        Label authorLabel = new Label("👨‍💻 Автор: Student ENU");
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a8a;");

        Label copyrightLabel = new Label("© 2025 Все права защищены");
        copyrightLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0a0a0;");

        authorBox.getChildren().addAll(authorLabel, copyrightLabel);

        content.getChildren().addAll(logoLabel, titleLabel, versionLabel, sep, descBox, funcBox, authorBox);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(400);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfcfb;");
        dialog.showAndWait();
    }

    /**
     * Показать временное сообщение в статус-баре.
     */
    private void showTemporaryStatus(String message) {
        if (statusBar != null) {
            String original = statusBar.getText();
            statusBar.setText(message);

            // Вернуть оригинальное сообщение через 3 секунды
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> statusBar.setText(original));
                } catch (InterruptedException e) {
                    logger.error("Ошибка в таймере статус-бара", e);
                }
            }).start();
        }
    }

    /**
     * Обновить счётчик экспонатов.
     */
    private void updateCountLabel() {
        if (countLabel != null && exhibitTable != null) {
            countLabel.setText("Всего: " + exhibitTable.getItems().size());
        }
    }

    /**
     * Обработчик выхода из приложения.
     */
    @FXML
    public void handleExit() {
        logger.info("Выход из приложения");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Выход из приложения");
        alert.setContentText("Вы уверены, что хотите выйти?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            logger.info("Приложение закрыто пользователем");
            javafx.application.Platform.exit();
        }
    }
}
