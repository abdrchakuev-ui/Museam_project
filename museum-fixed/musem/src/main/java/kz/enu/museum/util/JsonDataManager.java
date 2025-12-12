package kz.enu.museum.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import kz.enu.museum.exception.DataLoadException;
import kz.enu.museum.model.Artifact;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.Location;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.model.Painting;
import kz.enu.museum.model.Sculpture;
import kz.enu.museum.model.enums.ExhibitStatus;

/**
 * Менеджер для работы с данными в JSON формате.
 * Обеспечивает загрузку и сохранение экспонатов, категорий и художников.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class JsonDataManager {

    private static final Logger logger = LogManager.getLogger(JsonDataManager.class);
    private static final String DATA_DIR = "src/main/resources/data";
    private static final String EXHIBITS_FILE = DATA_DIR + "/exhibits.json";
    private static final String CATEGORIES_FILE = DATA_DIR + "/categories.json";
    private static final String ARTISTS_FILE = DATA_DIR + "/artists.json";
    private static final String RESOURCE_DIR = "data"; // classpath resources dir (src/main/resources/data -> data/)
    private static final String RESOURCE_EXHIBITS = RESOURCE_DIR + "/exhibits.json";
    private static final String RESOURCE_CATEGORIES = RESOURCE_DIR + "/categories.json";
    private static final String RESOURCE_ARTISTS = RESOURCE_DIR + "/artists.json";

    private final Gson gson;

    /**
     * Конструктор с инициализацией Gson.
     */
    public JsonDataManager() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        return LocalDate.parse(json.getAsString());
                    }
                });

        this.gson = gsonBuilder.create();
    }

    /**
     * Загружает список экспонатов из JSON файла.
     *
     * @return список экспонатов
     * @throws DataLoadException если возникает ошибка при загрузке
     */
    public List<MuseumItem> loadExhibits() throws DataLoadException {
        try {
            String content = null;

            // Сначала пытаемся загрузить из classpath (resources)
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(RESOURCE_EXHIBITS)) {
                if (is != null) {
                    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            // Если в classpath не найдено, пробуем файловую систему (исходный путь)
            if (content == null) {
                Path path = Paths.get(EXHIBITS_FILE);
                if (!Files.exists(path)) {
                    logger.warn("Файл экспонатов не найден: " + EXHIBITS_FILE);
                    return new ArrayList<>();
                }
                content = Files.readString(path, StandardCharsets.UTF_8);
            }
            JsonArray array = JsonParser.parseString(content).getAsJsonArray();
            List<MuseumItem> exhibits = new ArrayList<>();

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                MuseumItem exhibit = deserializeExhibit(obj);
                if (exhibit != null) {
                    exhibits.add(exhibit);
                }
            }

            logger.info("Загружено " + exhibits.size() + " экспонатов");
            return exhibits;

        } catch (IOException e) {
            logger.error("Ошибка при загрузке экспонатов", e);
            throw new DataLoadException("Не удалось загрузить экспонаты: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет список экспонатов в JSON файл.
     *
     * @param exhibits список экспонатов для сохранения
     * @throws DataLoadException если возникает ошибка при сохранении
     */
    public void saveExhibits(List<MuseumItem> exhibits) throws DataLoadException {
        try {
            Path path = Paths.get(EXHIBITS_FILE);
            Files.createDirectories(path.getParent());

            JsonArray array = new JsonArray();
            for (MuseumItem exhibit : exhibits) {
                array.add(JsonParser.parseString(serializeExhibit(exhibit)));
            }

            Files.writeString(path, gson.toJson(array), StandardCharsets.UTF_8);
            logger.info("Сохранено " + exhibits.size() + " экспонатов");

        } catch (IOException e) {
            logger.error("Ошибка при сохранении экспонатов", e);
            throw new DataLoadException("Не удалось сохранить экспонаты: " + e.getMessage(), e);
        }
    }

    /**
     * Загружает список категорий из JSON файла.
     *
     * @return список категорий
     * @throws DataLoadException если возникает ошибка при загрузке
     */
    public List<Category> loadCategories() throws DataLoadException {
        try {
            String content = null;

            try (InputStream is = getClass().getClassLoader().getResourceAsStream(RESOURCE_CATEGORIES)) {
                if (is != null) {
                    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            if (content == null) {
                Path path = Paths.get(CATEGORIES_FILE);
                if (!Files.exists(path)) {
                    logger.warn("Файл категорий не найден: " + CATEGORIES_FILE);
                    return new ArrayList<>();
                }
                content = Files.readString(path, StandardCharsets.UTF_8);
            }
            JsonArray array = JsonParser.parseString(content).getAsJsonArray();
            List<Category> categories = new ArrayList<>();

            for (JsonElement element : array) {
                Category category = gson.fromJson(element, Category.class);
                categories.add(category);
            }

            logger.info("Загружено " + categories.size() + " категорий");
            return categories;

        } catch (IOException e) {
            logger.error("Ошибка при загрузке категорий", e);
            throw new DataLoadException("Не удалось загрузить категории: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет список категорий в JSON файл.
     *
     * @param categories список категорий для сохранения
     * @throws DataLoadException если возникает ошибка при сохранении
     */
    public void saveCategories(List<Category> categories) throws DataLoadException {
        try {
            Path path = Paths.get(CATEGORIES_FILE);
            Files.createDirectories(path.getParent());

            String json = gson.toJson(categories);
            Files.writeString(path, json, StandardCharsets.UTF_8);
            logger.info("Сохранено " + categories.size() + " категорий");

        } catch (IOException e) {
            logger.error("Ошибка при сохранении категорий", e);
            throw new DataLoadException("Не удалось сохранить категории: " + e.getMessage(), e);
        }
    }

    /**
     * Загружает список художников из JSON файла.
     *
     * @return список художников
     * @throws DataLoadException если возникает ошибка при загрузке
     */
    public List<Artist> loadArtists() throws DataLoadException {
        try {
            String content = null;

            try (InputStream is = getClass().getClassLoader().getResourceAsStream(RESOURCE_ARTISTS)) {
                if (is != null) {
                    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            if (content == null) {
                Path path = Paths.get(ARTISTS_FILE);
                if (!Files.exists(path)) {
                    logger.warn("Файл художников не найден: " + ARTISTS_FILE);
                    return new ArrayList<>();
                }
                content = Files.readString(path, StandardCharsets.UTF_8);
            }
            JsonArray array = JsonParser.parseString(content).getAsJsonArray();
            List<Artist> artists = new ArrayList<>();

            for (JsonElement element : array) {
                Artist artist = gson.fromJson(element, Artist.class);
                artists.add(artist);
            }

            logger.info("Загружено " + artists.size() + " художников");
            return artists;

        } catch (IOException e) {
            logger.error("Ошибка при загрузке художников", e);
            throw new DataLoadException("Не удалось загрузить художников: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет список художников в JSON файл.
     *
     * @param artists список художников для сохранения
     * @throws DataLoadException если возникает ошибка при сохранении
     */
    public void saveArtists(List<Artist> artists) throws DataLoadException {
        try {
            Path path = Paths.get(ARTISTS_FILE);
            Files.createDirectories(path.getParent());

            String json = gson.toJson(artists);
            Files.writeString(path, json, StandardCharsets.UTF_8);
            logger.info("Сохранено " + artists.size() + " художников");

        } catch (IOException e) {
            logger.error("Ошибка при сохранении художников", e);
            throw new DataLoadException("Не удалось сохранить художников: " + e.getMessage(), e);
        }
    }

    /**
     * Десериализует объект Exhibit из JSON.
     *
     * @param json JSON объект
     * @return объект Exhibit (или его подкласс)
     */
    private String serializeExhibit(MuseumItem exhibit) {
        JsonObject json = new JsonObject();
        json.addProperty("type", exhibit.getClass().getSimpleName());
        json.addProperty("id", exhibit.getId());
        json.addProperty("name", exhibit.getName());
        json.addProperty("description", exhibit.getDescription());

        if (exhibit.getCreationDate() != null) {
            json.addProperty("creationDate", exhibit.getCreationDate().toString());
        }
        if (exhibit.getAcquisitionDate() != null) {
            json.addProperty("acquisitionDate", exhibit.getAcquisitionDate().toString());
        }

        json.addProperty("inventoryNumber", exhibit.getInventoryNumber());
        json.addProperty("status", exhibit.getStatus().name());

        if (exhibit instanceof Exhibit) {
            Exhibit ex = (Exhibit) exhibit;
            if (ex.getAuthor() != null) {
                json.add("author", JsonParser.parseString(gson.toJson(ex.getAuthor())));
            }
            if (ex.getCategory() != null) {
                json.add("category", JsonParser.parseString(gson.toJson(ex.getCategory())));
            }
            if (ex.getLocation() != null) {
                json.add("location", JsonParser.parseString(gson.toJson(ex.getLocation())));
            }
            if (ex.getEstimatedValue() != null) {
                json.addProperty("estimatedValue", ex.getEstimatedValue());
            }
            json.addProperty("imagePath", ex.getImagePath());

            if (exhibit instanceof Painting) {
                Painting p = (Painting) exhibit;
                json.addProperty("technique", p.getTechnique());
                json.addProperty("width", p.getWidth());
                json.addProperty("height", p.getHeight());
                json.addProperty("hasFrame", p.isHasFrame());

            } else if (exhibit instanceof Sculpture) {
                Sculpture s = (Sculpture) exhibit;
                json.addProperty("material", s.getMaterial());
                json.addProperty("weight", s.getWeight());
                json.addProperty("height", s.getHeight());

            } else if (exhibit instanceof Artifact) {
                Artifact a = (Artifact) exhibit;
                json.addProperty("origin", a.getOrigin());
                json.addProperty("period", a.getPeriod());
                json.addProperty("material", a.getMaterial());
                json.addProperty("age", a.getAge());
            }
        }

        return gson.toJson(json);
    }

    /**
     * Десериализует Exhibit из JSON объекта.
     *
     * @param json JSON объект
     * @return объект Exhibit (или его подкласс)
     */
    private MuseumItem deserializeExhibit(JsonObject json) {
        try {
            String type = json.get("type").getAsString();

            Exhibit exhibit;

            if ("Painting".equals(type)) {
                exhibit = new Painting();
                if (json.has("technique")) ((Painting) exhibit).setTechnique(json.get("technique").getAsString());
                if (json.has("width")) ((Painting) exhibit).setWidth(json.get("width").getAsInt());
                if (json.has("height")) ((Painting) exhibit).setHeight(json.get("height").getAsInt());
                if (json.has("hasFrame")) ((Painting) exhibit).setHasFrame(json.get("hasFrame").getAsBoolean());

            } else if ("Sculpture".equals(type)) {
                exhibit = new Sculpture();
                if (json.has("material")) ((Sculpture) exhibit).setMaterial(json.get("material").getAsString());
                if (json.has("weight")) ((Sculpture) exhibit).setWeight(json.get("weight").getAsDouble());
                if (json.has("height")) ((Sculpture) exhibit).setHeight(json.get("height").getAsDouble());

            } else if ("Artifact".equals(type)) {
                exhibit = new Artifact();
                if (json.has("origin")) ((Artifact) exhibit).setOrigin(json.get("origin").getAsString());
                if (json.has("period")) ((Artifact) exhibit).setPeriod(json.get("period").getAsString());
                if (json.has("material")) ((Artifact) exhibit).setMaterial(json.get("material").getAsString());
                if (json.has("age")) ((Artifact) exhibit).setAge(json.get("age").getAsDouble());

            } else {
                exhibit = new Exhibit();
            }

            // Общие атрибуты
            if (json.has("id")) exhibit.setId(json.get("id").getAsLong());
            if (json.has("name")) exhibit.setName(json.get("name").getAsString());
            if (json.has("description")) exhibit.setDescription(json.get("description").getAsString());
            if (json.has("creationDate")) {
                exhibit.setCreationDate(LocalDate.parse(json.get("creationDate").getAsString()));
            }
            if (json.has("acquisitionDate")) {
                exhibit.setAcquisitionDate(LocalDate.parse(json.get("acquisitionDate").getAsString()));
            }
            if (json.has("inventoryNumber")) exhibit.setInventoryNumber(json.get("inventoryNumber").getAsString());
            if (json.has("status")) {
                exhibit.setStatus(ExhibitStatus.valueOf(json.get("status").getAsString()));
            }

            // Специфичные для Exhibit
            if (json.has("author")) {
                Artist author = gson.fromJson(json.get("author"), Artist.class);
                exhibit.setAuthor(author);
            }
            if (json.has("category")) {
                Category category = gson.fromJson(json.get("category"), Category.class);
                exhibit.setCategory(category);
            }
            if (json.has("location")) {
                Location location = gson.fromJson(json.get("location"), Location.class);
                exhibit.setLocation(location);
            }
            if (json.has("estimatedValue")) {
                exhibit.setEstimatedValue(new BigDecimal(json.get("estimatedValue").getAsString()));
            }
            if (json.has("imagePath") && !json.get("imagePath").isJsonNull()) {
                exhibit.setImagePath(json.get("imagePath").getAsString());
            }

            return exhibit;

        } catch (Exception e) {
            logger.error("Ошибка при десериализации экспоната", e);
            return null;
        }
    }
}
