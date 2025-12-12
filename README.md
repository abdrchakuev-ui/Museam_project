# Museum Catalog - Каталог музейных экспонатов

Десктопное JavaFX приложение для управления музейной коллекцией. Позволяет каталогизировать картины, скульптуры и исторические артефакты.

![Java](https://img.shields.io/badge/Java-17+-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)
![Maven](https://img.shields.io/badge/Maven-3.8+-red)
![License](https://img.shields.io/badge/License-MIT-green)

## Содержание

- [Возможности](#-возможности)
- [Требования](#-требования)
- [Установка и запуск](#-установка-и-запуск)
- [Структура проекта](#-структура-проекта)
- [Архитектура](#-архитектура)
- [Модели данных](#-модели-данных)
- [Использование](#-использование)
- [Конфигурация](#-конфигурация)
- [Автор](#-автор)

---

## Возможности

### Управление экспонатами
- **Добавление** картин, скульптур и артефактов
- **Редактирование** информации об экспонатах
- **Удаление** экспонатов из коллекции
- **Поиск** по названию, описанию, автору
- **Фильтрация** по статусу и категории

### Типы экспонатов
| Тип | Описание | Специфичные поля |
|-----|----------|------------------|
| Картина | Живопись, портреты | Техника, размеры, наличие рамы |
| Скульптура | Статуи, рельефы | Материал, вес, высота |
| Артефакт | Древности | Происхождение, период, возраст |

### Статусы экспонатов
- **На экспозиции** - экспонат выставлен в зале
- **В хранилище** - экспонат находится в хранилище
- **На реставрации** - экспонат на восстановлении
- **В аренде** - экспонат на выставке в другом музее

### Справочники
- Управление художниками/авторами
- Категории экспонатов
- Залы и локации

### Отчёты
- Общая статистика коллекции
- Экспонаты на реставрации
- Распределение по залам
- Экспорт в PDF

---

## Требования

### Обязательные
- **Java JDK 17** или выше
- **Apache Maven 3.8+**

### Проверка версий
```bash
# Проверить версию Java
java -version

# Проверить версию Maven
mvn -version
```

### Установка Java (если не установлена)
**Windows:**
1. Скачайте [Eclipse Temurin JDK 17](https://adoptium.net/)
2. Установите и добавьте в PATH

**macOS:**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Установка Maven (если не установлен)
**Windows:**
1. Скачайте [Apache Maven](https://maven.apache.org/download.cgi)
2. Распакуйте в `C:\Program Files\Apache\maven`
3. Добавьте `MAVEN_HOME` и `%MAVEN_HOME%\bin` в PATH

**macOS:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

---

## Установка и запуск

### Способ 1: Через Maven (рекомендуется)

```bash
# 1. Клонирование/скачивание проекта
cd путь/к/проекту/musem

# 2. Сборка проекта
mvn clean compile

# 3. Запуск приложения
mvn javafx:run
```

### Способ 2: Через BAT-файлы (Windows)

```cmd
# Из корневой папки museum-fixed
start.bat
```

или

```cmd
run.bat
```

### Способ 3: Через Shell-скрипты (Linux/macOS)

```bash
# Сделать исполняемым
chmod +x start.sh run.sh

# Запустить
./start.sh
```

---

## 📁 Структура проекта

```
musem/
├── pom.xml                     # Maven конфигурация
├── README.md                   # Документация
├── src/
│   ├── main/
│   │   ├── java/kz/enu/museum/
│   │   │   ├── Main.java                 # Точка входа
│   │   │   ├── controller/
│   │   │   │   └── MainController.java   # Главный контроллер UI
│   │   │   ├── model/                    # Доменные модели
│   │   │   │   ├── MuseumItem.java       # Базовый класс экспоната
│   │   │   │   ├── Exhibit.java          # Экспонат с автором
│   │   │   │   ├── Painting.java         # Картина
│   │   │   │   ├── Sculpture.java        # Скульптура
│   │   │   │   ├── Artifact.java         # Артефакт
│   │   │   │   ├── Artist.java           # Художник/автор
│   │   │   │   ├── Category.java         # Категория
│   │   │   │   ├── Location.java         # Локация/зал
│   │   │   │   └── enums/
│   │   │   │       └── ExhibitStatus.java
│   │   │   ├── repository/               # Работа с данными
│   │   │   │   ├── Repository.java       # Интерфейс репозитория
│   │   │   │   ├── ExhibitRepository.java
│   │   │   │   ├── ArtistRepository.java
│   │   │   │   └── CategoryRepository.java
│   │   │   ├── service/                  # Бизнес-логика
│   │   │   │   ├── ExhibitService.java
│   │   │   │   ├── ArtistService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── SearchService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── util/                     # Утилиты
│   │   │   │   ├── JsonDataManager.java  # Работа с JSON
│   │   │   │   ├── ValidationUtil.java
│   │   │   │   └── InventoryNumberGenerator.java
│   │   │   ├── interfaces/               # Интерфейсы
│   │   │   │   ├── Searchable.java
│   │   │   │   └── Exportable.java
│   │   │   └── exception/                # Исключения
│   │   │       ├── MuseumException.java
│   │   │       ├── ExhibitNotFoundException.java
│   │   │       └── ...
│   │   └── resources/
│   │       ├── css/styles.css            # Стили UI
│   │       ├── fxml/                     # FXML разметка
│   │       │   ├── main.fxml
│   │       │   └── ...
│   │       ├── data/                     # JSON данные
│   │       │   ├── exhibits.json         # Экспонаты
│   │       │   ├── artists.json          # Художники
│   │       │   └── categories.json       # Категории
│   │       └── log4j2.xml                # Конфигурация логирования
│   └── test/                             # Unit-тесты
└── target/                               # Скомпилированные файлы
```

---

## Архитектура

Приложение построено по **многослойной архитектуре**:

```
┌─────────────────────────────────────────────┐
│              UI Layer (JavaFX)              │
│     MainController.java + FXML + CSS        │
├─────────────────────────────────────────────┤
│            Service Layer                     │
│  ExhibitService, ArtistService, SearchService│
├─────────────────────────────────────────────┤
│           Repository Layer                   │
│     ExhibitRepository, ArtistRepository     │
├─────────────────────────────────────────────┤
│            Data Layer (JSON)                │
│   exhibits.json, artists.json, categories.json│
└─────────────────────────────────────────────┘
```

### Принципы:
- **Разделение ответственности** - каждый слой выполняет свою функцию
- **Dependency Injection** - сервисы инжектируются в контроллер
- **Repository Pattern** - абстракция доступа к данным
- **Domain Model** - богатые объекты предметной области

---

## 📊 Модели данных

### Иерархия классов

```
MuseumItem (abstract)
    └── Exhibit (abstract)
            ├── Painting      # Картина
            ├── Sculpture     # Скульптура  
            └── Artifact      # Артефакт
```

### Пример данных (exhibits.json)

```json
{
  "type": "Painting",
  "id": 1,
  "name": "Звёздная ночь",
  "description": "Одна из самых известных картин Ван Гога",
  "inventoryNumber": "МУЗ-ПЛ-2020-001",
  "status": "ON_DISPLAY",
  "author": {
    "id": 4,
    "fullName": "Винсент ван Гог",
    "country": "Нидерланды"
  },
  "technique": "масло",
  "width": 74,
  "height": 92,
  "hasFrame": true
}
```

---

## 📖 Использование

### Главное окно

При запуске отображается таблица всех экспонатов с возможностью:
- Поиска по тексту
- Фильтрации по статусу и автору
- Навигации по дереву категорий

### Добавление экспоната

1. Нажмите кнопку **"➕ Добавить"**
2. Выберите тип экспоната (Картина/Скульптура/Артефакт)
3. Заполните форму
4. Нажмите **"Добавить"**

### Редактирование

1. Выберите экспонат в таблице
2. Нажмите **"Редактировать"** или дважды кликните
3. Измените данные
4. Нажмите **"Сохранить"**

### Удаление

1. Выберите экспонат
2. Нажмите **"Удалить"**
3. Подтвердите удаление

### Меню

| Меню | Функции |
|------|---------|
| **Файл** | Новый, Открыть, Сохранить, Экспорт, Выход |
| **Справочники** | Категории, Художники, Залы |
| **Отчёты** | Статистика, На реставрации, По залам, PDF |
| **Справка** | О программе |

---

## ⚙️ Конфигурация

### Зависимости (pom.xml)

```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- JSON -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    
    <!-- Logging -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.21.1</version>
    </dependency>
</dependencies>
```

### Логирование

Настройки в `src/main/resources/log4j2.xml`:
- Логи сохраняются в папку `logs/`
- Уровень: DEBUG для разработки, INFO для продакшена

---

## 🛠️ Разработка

### Сборка JAR

```bash
mvn clean package
```

JAR-файл будет в `target/museum-catalog-1.0.jar`

### Запуск тестов

```bash
mvn test
```

### Генерация документации

```bash
mvn javadoc:javadoc
```

---

## Возможные проблемы

### "JavaFX runtime components are missing"

Убедитесь, что используете Maven для запуска:
```bash
mvn javafx:run
```

### "Error: Could not find or load main class"

Проверьте, что вы находитесь в папке `musem/`:
```bash
cd musem
mvn clean compile
mvn javafx:run
```

### Проблемы с кодировкой (кракозябры)

Убедитесь, что файлы сохранены в UTF-8. В IntelliJ IDEA: 
`File → Settings → Editor → File Encodings → UTF-8`

---

## Автор

**Чакуев Абдурахим**

Евразийский Национальный Университет им. Л.Н. Гумилёва  
Факультет информационных технологий  
2025 год

---

## Лицензия

Проект распространяется под лицензией MIT. См. файл `LICENSE` для подробностей.


