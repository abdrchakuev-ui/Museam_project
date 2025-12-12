#!/bin/bash

# Цвета
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔══════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     КАТАЛОГ МУЗЕЯ - Museum Catalog       ║${NC}"
echo -e "${BLUE}║              Версия 1.0                  ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════╝${NC}"
echo ""

# Переход в папку проекта
cd "$(dirname "$0")/musem" || exit 1

# Проверка Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}[ОШИБКА] Java не найдена! Установите JDK 17+${NC}"
    echo "Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "Mac: brew install openjdk@17"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n 1)
echo -e "${GREEN}[OK] Java найдена: ${JAVA_VER}${NC}"

# Проверка Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[ОШИБКА] Maven не найден!${NC}"
    echo "Ubuntu/Debian: sudo apt install maven"
    echo "Mac: brew install maven"
    exit 1
fi

show_menu() {
    echo ""
    echo "═══════════════════════════════════════════"
    echo "  МЕНЮ"
    echo "═══════════════════════════════════════════"
    echo "  [1] Запустить приложение"
    echo "  [2] Собрать проект (compile)"
    echo "  [3] Запустить тесты"
    echo "  [4] Создать JAR файл"
    echo "  [5] Очистить и пересобрать"
    echo "  [6] Сгенерировать Javadoc"
    echo "  [0] Выход"
    echo "═══════════════════════════════════════════"
    echo ""
}

while true; do
    show_menu
    read -p "Выберите действие: " choice
    
    case $choice in
        1)
            echo -e "\n${YELLOW}[INFO] Запуск приложения...${NC}\n"
            mvn javafx:run
            ;;
        2)
            echo -e "\n${YELLOW}[INFO] Компиляция проекта...${NC}"
            mvn compile
            ;;
        3)
            echo -e "\n${YELLOW}[INFO] Запуск тестов...${NC}"
            mvn test
            ;;
        4)
            echo -e "\n${YELLOW}[INFO] Создание JAR файла...${NC}"
            mvn package -DskipTests
            echo -e "${GREEN}[OK] JAR файл создан в папке target/${NC}"
            ;;
        5)
            echo -e "\n${YELLOW}[INFO] Очистка и пересборка...${NC}"
            mvn clean compile
            ;;
        6)
            echo -e "\n${YELLOW}[INFO] Генерация Javadoc...${NC}"
            mvn javadoc:javadoc
            echo -e "${GREEN}[OK] Документация: target/site/apidocs/index.html${NC}"
            ;;
        0)
            echo "До свидания!"
            exit 0
            ;;
        *)
            echo -e "${RED}Неверный выбор${NC}"
            ;;
    esac
done
