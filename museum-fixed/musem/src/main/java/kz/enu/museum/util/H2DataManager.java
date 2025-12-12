package kz.enu.museum.util;

import kz.enu.museum.exception.DataLoadException;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.MuseumItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Небольшой менеджер данных для H2 (встроенная база).
 * Поддерживает базовые операции загрузки/сохранения для артистов, категорий и экспонатов.
 * Это минимальная реализация, достаточная для перехода с JSON на H2.
 */
public class H2DataManager {

    private static final Logger logger = LogManager.getLogger(H2DataManager.class);
    private static final String JDBC_URL = "jdbc:h2:./data/museum-db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public H2DataManager() throws DataLoadException {
        try {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException cnf) {
                throw new DataLoadException("H2 JDBC driver not found on classpath. Make sure H2 dependency is added and project dependencies are resolved (run 'mvn clean package' or refresh IDE Maven).", cnf);
            }
            initDatabase();
            logger.info("H2DataManager initialized, URL=" + JDBC_URL);
        } catch (SQLException e) {
            throw new DataLoadException("Не удалось инициализировать H2: " + e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    private void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // artists
            st.execute("CREATE TABLE IF NOT EXISTS artists (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "full_name VARCHAR(255) NOT NULL, " +
                    "birth_year INT NOT NULL, " +
                    "death_year INT, " +
                    "country VARCHAR(100), " +
                    "biography CLOB)");

            // categories
            st.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "category_code VARCHAR(100), " +
                    "parent_id BIGINT)");

            // exhibits - simplified schema: store common fields; subclass-specific fields can be stored in extra columns or ignored
            st.execute("CREATE TABLE IF NOT EXISTS exhibits (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "type VARCHAR(100), " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description CLOB, " +
                    "creation_date DATE, " +
                    "acquisition_date DATE, " +
                    "inventory_number VARCHAR(200), " +
                    "status VARCHAR(50), " +
                    "author_id BIGINT, " +
                    "category_id BIGINT, " +
                    "location VARCHAR(255), " +
                    "estimated_value DECIMAL(19,2), " +
                    "image_path VARCHAR(1000))");
        }
    }

    // --------- Loaders ---------
    public List<Artist> loadArtists() throws DataLoadException {
        List<Artist> result = new ArrayList<>();
        String sql = "SELECT id, full_name, birth_year, death_year, country, biography FROM artists";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artist a = new Artist();
                a.setId(rs.getLong("id"));
                a.setFullName(rs.getString("full_name"));
                a.setBirthYear(rs.getInt("birth_year"));
                int dy = rs.getInt("death_year");
                if (!rs.wasNull()) a.setDeathYear(dy);
                a.setCountry(rs.getString("country"));
                a.setBiography(rs.getString("biography"));
                result.add(a);
            }
            logger.info("Загружено " + result.size() + " художников (H2)");
            return result;
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при загрузке художников из H2: " + e.getMessage(), e);
        }
    }

    public List<Category> loadCategories() throws DataLoadException {
        List<Category> result = new ArrayList<>();
        String sql = "SELECT id, name, category_code, parent_id FROM categories";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            // First pass: create Category objects and remember parent IDs to resolve later
            List<Category> temp = new ArrayList<>();
            List<Long> parentIds = new ArrayList<>();
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getLong("id"));
                c.setName(rs.getString("name"));
                c.setCategoryCode(rs.getString("category_code"));
                long parentId = rs.getLong("parent_id");
                if (rs.wasNull()) parentIds.add(null); else parentIds.add(parentId);
                temp.add(c);
            }

            // Build lookup by id and resolve parent references
            java.util.Map<Long, Category> byId = new java.util.HashMap<>();
            for (Category c : temp) byId.put(c.getId(), c);
            for (int i = 0; i < temp.size(); i++) {
                Category c = temp.get(i);
                Long pid = parentIds.get(i);
                if (pid != null) {
                    Category parent = byId.get(pid);
                    if (parent != null) {
                        c.setParentCategory(parent);
                        parent.getSubcategories().add(c);
                    }
                }
                result.add(c);
            }

            logger.info("Загружено " + result.size() + " категорий (H2)");
            return result;
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при загрузке категорий из H2: " + e.getMessage(), e);
        }
    }

    public List<MuseumItem> loadExhibits() throws DataLoadException {
        List<MuseumItem> result = new ArrayList<>();
        String sql = "SELECT e.id, e.type, e.name, e.description, e.creation_date, e.acquisition_date, e.inventory_number, e.status, e.author_id, e.category_id, e.estimated_value, e.image_path " +
                "FROM exhibits e";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Exhibit ex = new Exhibit();
                ex.setId(rs.getLong("id"));
                ex.setName(rs.getString("name"));
                ex.setDescription(rs.getString("description"));
                Date cd = rs.getDate("creation_date");
                if (cd != null) ex.setCreationDate(cd.toLocalDate());
                Date ad = rs.getDate("acquisition_date");
                if (ad != null) ex.setAcquisitionDate(ad.toLocalDate());
                ex.setInventoryNumber(rs.getString("inventory_number"));
                String status = rs.getString("status");
                if (status != null && !status.isBlank()) {
                    try {
                        ex.setStatus(kz.enu.museum.model.enums.ExhibitStatus.valueOf(status));
                    } catch (Exception ignore) {
                    }
                }
                BigDecimal val = rs.getBigDecimal("estimated_value");
                if (val != null) ex.setEstimatedValue(val);
                ex.setImagePath(rs.getString("image_path"));
                // Note: author and category will be set by repository/service after loading, based on IDs if needed.
                result.add(ex);
            }
            logger.info("Загружено " + result.size() + " экспонатов (H2)");
            return result;
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при загрузке экспонатов из H2: " + e.getMessage(), e);
        }
    }

    // --------- Savers ---------
    public void saveArtists(List<Artist> artists) throws DataLoadException {
        String deleteSql = "DELETE FROM artists";
        String insertSql = "INSERT INTO artists(id, full_name, birth_year, death_year, country, biography) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute(deleteSql);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Artist a : artists) {
                    if (a.getId() != null) ps.setLong(1, a.getId()); else ps.setNull(1, Types.BIGINT);
                    ps.setString(2, a.getFullName());
                    ps.setInt(3, a.getBirthYear());
                    if (a.getDeathYear() != null) ps.setInt(4, a.getDeathYear()); else ps.setNull(4, Types.INTEGER);
                    ps.setString(5, a.getCountry());
                    ps.setString(6, a.getBiography());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            logger.info("Сохранено " + artists.size() + " художников (H2)");
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при сохранении художников в H2: " + e.getMessage(), e);
        }
    }

    public void saveCategories(List<Category> categories) throws DataLoadException {
        String deleteSql = "DELETE FROM categories";
        String insertSql = "INSERT INTO categories(id, name, category_code, parent_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute(deleteSql);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Category c : categories) {
                    if (c.getId() != null) ps.setLong(1, c.getId()); else ps.setNull(1, Types.BIGINT);
                    ps.setString(2, c.getName());
                    ps.setString(3, c.getCategoryCode());
                    // Use parentCategory's id if present
                    if (c.getParentCategory() != null && c.getParentCategory().getId() != null) {
                        ps.setLong(4, c.getParentCategory().getId());
                    } else {
                        ps.setNull(4, Types.BIGINT);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            logger.info("Сохранено " + categories.size() + " категорий (H2)");
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при сохранении категорий в H2: " + e.getMessage(), e);
        }
    }

    public void saveExhibits(List<MuseumItem> exhibits) throws DataLoadException {
        String deleteSql = "DELETE FROM exhibits";
        String insertSql = "INSERT INTO exhibits(id, type, name, description, creation_date, acquisition_date, inventory_number, status, author_id, category_id, location, estimated_value, image_path) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute(deleteSql);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (MuseumItem m : exhibits) {
                    if (m.getId() != null) ps.setLong(1, m.getId()); else ps.setNull(1, Types.BIGINT);
                    String type = m.getClass().getSimpleName();
                    ps.setString(2, type);
                    ps.setString(3, m.getName());
                    ps.setString(4, m.getDescription());
                    if (m.getCreationDate() != null) ps.setDate(5, Date.valueOf(m.getCreationDate())); else ps.setNull(5, Types.DATE);
                    if (m.getAcquisitionDate() != null) ps.setDate(6, Date.valueOf(m.getAcquisitionDate())); else ps.setNull(6, Types.DATE);
                    ps.setString(7, m.getInventoryNumber());
                    ps.setString(8, m.getStatus() != null ? m.getStatus().name() : null);

                    Long authorId = null;
                    Long categoryId = null;
                    String location = null;
                    BigDecimal estimatedValue = null;
                    String imagePath = null;
                    if (m instanceof Exhibit) {
                        Exhibit e = (Exhibit) m;
                        if (e.getAuthor() != null) authorId = e.getAuthor().getId();
                        if (e.getCategory() != null) categoryId = e.getCategory().getId();
                        if (e.getLocation() != null) location = e.getLocation().getHallName();
                        estimatedValue = e.getEstimatedValue();
                        imagePath = e.getImagePath();
                    }
                    if (authorId != null) ps.setLong(9, authorId); else ps.setNull(9, Types.BIGINT);
                    if (categoryId != null) ps.setLong(10, categoryId); else ps.setNull(10, Types.BIGINT);
                    if (location != null) ps.setString(11, location); else ps.setNull(11, Types.VARCHAR);
                    if (estimatedValue != null) ps.setBigDecimal(12, estimatedValue); else ps.setNull(12, Types.DECIMAL);
                    if (imagePath != null) ps.setString(13, imagePath); else ps.setNull(13, Types.VARCHAR);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            logger.info("Сохранено " + exhibits.size() + " экспонатов (H2)");
        } catch (SQLException e) {
            throw new DataLoadException("Ошибка при сохранении экспонатов в H2: " + e.getMessage(), e);
        }
    }
}
