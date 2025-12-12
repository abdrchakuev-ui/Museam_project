#!/bin/bash
echo ""
echo "══════════════════════════════════════"
echo "  Запуск Museum Catalog..."
echo "══════════════════════════════════════"
echo ""
cd "$(dirname "$0")/musem" || exit 1
mvn javafx:run
