package kz.enu.museum;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kz.enu.museum.controller.MainController;
import kz.enu.museum.exception.DataLoadException;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.repository.ArtistRepository;
import kz.enu.museum.repository.CategoryRepository;
import kz.enu.museum.repository.ExhibitRepository;
import kz.enu.museum.service.ArtistService;
import kz.enu.museum.service.CategoryService;
import kz.enu.museum.service.ExhibitService;
import kz.enu.museum.service.ReportService;
import kz.enu.museum.service.SearchService;
import kz.enu.museum.util.JsonDataManager;


public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);

    // Слой репозиториев
    private ExhibitRepository exhibitRepository;
    private CategoryRepository categoryRepository;
    private ArtistRepository artistRepository;

    // Слой сервисов
    private ExhibitService exhibitService;
    private CategoryService categoryService;
    private ArtistService artistService;
    private SearchService searchService;
    private ReportService reportService;

    // Менеджеры данных: предпочитаем H2, но поддерживаем JSON-файлы как fallback
    private kz.enu.museum.util.H2DataManager h2DataManager;
    private JsonDataManager jsonDataManager;

    @Override
    public void init() throws Exception {
        logger.info("=== Инициализация приложения ===");
        super.init();

        try {
            // Попытка инициализировать H2; при ошибке - откат на JSON-менеджер
            try {
                h2DataManager = new kz.enu.museum.util.H2DataManager();
                logger.info("H2DataManager инициализирован");
            } catch (DataLoadException e) {
                logger.warn("H2 init failed, falling back to JSON data manager: " + e.getMessage());
                jsonDataManager = new JsonDataManager();
                logger.info("JsonDataManager инициализирован (fallback)");
            }

            // Инициализация репозиториев
            exhibitRepository = new ExhibitRepository();
            categoryRepository = new CategoryRepository();
            artistRepository = new ArtistRepository();
            logger.info("Репозитории инициализированы");

            // Инициализация сервисов
            categoryService = new CategoryService(categoryRepository);
            artistService = new ArtistService(artistRepository);
            exhibitService = new ExhibitService(exhibitRepository);
            logger.info("Сервисы инициализированы");

            // Загрузка данных
            loadData();

            // Инициализация сервисов поиска и отчётов
            searchService = new SearchService(exhibitRepository.findAll());
            reportService = new ReportService(exhibitRepository.findAll(), categoryService, artistService);
            logger.info("Сервисы поиска и отчётов инициализированы");

            logger.info("=== Инициализация завершена успешно ===");

        } catch (Exception e) {
            logger.error("Ошибка при инициализации приложения", e);
            throw e;
        }
    }

    private void loadData() {
        try {
            logger.info("Загрузка данных...");

            // Загрузка категорий
            List<Category> categories;
            if (h2DataManager != null) categories = h2DataManager.loadCategories(); else categories = jsonDataManager.loadCategories();
            for (Category category : categories) {
                categoryRepository.save(category);
            }
            logger.info("Загружено " + categories.size() + " категорий");

            // Загрузка художников
            List<Artist> artists;
            if (h2DataManager != null) artists = h2DataManager.loadArtists(); else artists = jsonDataManager.loadArtists();
            for (Artist artist : artists) {
                artistRepository.save(artist);
            }
            logger.info("Загружено " + artists.size() + " художников");

            // Загрузка экспонатов
            List<MuseumItem> exhibits;
            if (h2DataManager != null) exhibits = h2DataManager.loadExhibits(); else exhibits = jsonDataManager.loadExhibits();
            for (MuseumItem exhibit : exhibits) {
                if (exhibit.getId() != null) {
                    exhibitRepository.setNextId(exhibit.getId() + 1);
                }
                exhibitRepository.save(exhibit);
            }
            logger.info("Загружено " + exhibits.size() + " экспонатов");

        } catch (DataLoadException e) {
            logger.warn("Ошибка при загрузке данных: " + e.getMessage());
            logger.info("Приложение запущено с пустыми данными");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Запуск GUI...");

            // Загрузка FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            // Инициализация контроллера
            MainController controller = loader.getController();
            controller.initialize(
                    exhibitService,
                    categoryService,
                    artistService,
                    searchService
            );

            // Создание сцены
            Scene scene = new Scene(root, 1200, 800);

            // Загрузка CSS стиля
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                logger.warn("CSS файл не найден, используется стиль по умолчанию");
            }

            // Настройка главного окна
            primaryStage.setTitle("Каталог музея - Учёт экспонатов");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setOnCloseRequest(event -> handleApplicationExit());

            primaryStage.show();
            logger.info("GUI запущен успешно");

        } catch (IOException e) {
            logger.error("Ошибка при загрузке GUI", e);
            System.exit(1);
        }
    }

    /**
     * Обработчик закрытия приложения.
     * Сохраняет данные перед выходом.
     */
    private void handleApplicationExit() {
        try {
            logger.info("=== Выход из приложения ===");

            // Сохранение данных (в H2 если доступна, иначе в JSON)
            if (h2DataManager != null) {
                h2DataManager.saveExhibits(exhibitRepository.findAll());
                h2DataManager.saveCategories(categoryRepository.findAll());
                h2DataManager.saveArtists(artistRepository.findAll());
            } else if (jsonDataManager != null) {
                jsonDataManager.saveExhibits(exhibitRepository.findAll());
                jsonDataManager.saveCategories(categoryRepository.findAll());
                jsonDataManager.saveArtists(artistRepository.findAll());
            }

            logger.info("Данные сохранены успешно");
            logger.info("=== Приложение завершено ===");

        } catch (Exception e) {
            logger.error("Ошибка при сохранении данных при выходе", e);
        }
    }

    /**
     * Точка входа приложения.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения 'Каталог музея'");
        launch(args);
    }
}
