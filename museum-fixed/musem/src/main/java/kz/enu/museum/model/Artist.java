package kz.enu.museum.model;

/**
 * Класс для представления художника/автора экспоната.
 * Содержит информацию о художнике, его годах жизни и биографии.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Artist {
    private Long id;
    private String fullName;
    private int birthYear;
    private Integer deathYear; // может быть null если художник жив
    private String country;
    private String biography;
    
    /**
     * Конструктор по умолчанию.
     */
    public Artist() {
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param fullName полное имя художника
     * @param birthYear год рождения
     * @param country страна происхождения
     */
    public Artist(String fullName, int birthYear, String country) {
        this.fullName = fullName;
        this.birthYear = birthYear;
        this.country = country;
    }
    
    /**
     * Конструктор с полными параметрами.
     *
     * @param id уникальный идентификатор
     * @param fullName полное имя художника
     * @param birthYear год рождения
     * @param deathYear год смерти (может быть null)
     * @param country страна происхождения
     * @param biography биография художника
     */
    public Artist(Long id, String fullName, int birthYear, Integer deathYear, String country, String biography) {
        this.id = id;
        this.fullName = fullName;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
        this.country = country;
        this.biography = biography;
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Имя художника не может быть пустым");
        }
        this.fullName = fullName;
    }
    
    public int getBirthYear() {
        return birthYear;
    }
    
    public void setBirthYear(int birthYear) {
        if (birthYear < 1000 || birthYear > 2025) {
            throw new IllegalArgumentException("Год рождения должен быть между 1000 и 2025");
        }
        this.birthYear = birthYear;
    }
    
    public Integer getDeathYear() {
        return deathYear;
    }
    
    public void setDeathYear(Integer deathYear) {
        if (deathYear != null && (deathYear < birthYear || deathYear > 2025)) {
            throw new IllegalArgumentException("Год смерти должен быть позже года рождения и не позже 2025");
        }
        this.deathYear = deathYear;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Страна не может быть пустой");
        }
        this.country = country;
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography != null ? biography : "";
    }
    
    @Override
    public String toString() {
        return fullName + " (" + country + ", " + birthYear + 
               (deathYear != null ? "-" + deathYear : "") + ")";
    }
}
