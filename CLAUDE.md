# Thaumcraft 3 Reforged

Порт Thaumcraft 3.0.5e (MC 1.5.2, Azanor) на Minecraft 26.3 + NeoForge 26.3. Только для личного использования: логика и ассеты оригинала переносятся напрямую.

## Окружение
- Java 25: `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./gradlew ...`
- NeoForge `26.3.0.40-beta`, ModDevGradle `2.0.148`, JEI `31.8.0.53`
- Сборка: `./gradlew compileJava`, запуск: `./gradlew runClient`

## Справочники (в .gitignore)
- `reference/decompiled/` декомпилированный TC3 (Vineflower) с MCP-именами 1.5.2 (`tools/remap_srg.py`)
- `reference/mc/` исходники Minecraft 26.3 (patched), `reference/neo/` исходники NeoForge
- `Thaumcraft3.0.5e/` оригинальный мод, `research.xml` в корне русский перевод исследований

## Инструменты
- `tools/convert_assets.py` текстуры, звуки, lang (.lang → json, новые ключи), XML исследований. Доп. строки в `tools/lang/<lang>.json`
- `tools/convert_tags.py` генерирует `aspect/ConfigAspects.java` из `Config.initTags` оригинала
- `tools/remap_srg.py` SRG → MCP для декомпилята

## Архитектура
- `thaumcraft/aspect` аспекты: `Aspect` (EnumTag), `AspectList` (ObjectTags), `AspectRegistry` (теги + генерация из рецептов), `AspectHelper` (стек: зелья, бонусы, зачарования)
- `thaumcraft/aura` ноды ауры: `AuraManager` (бывшие статические мапы + флюкс-события), `AuraTicker` (тик на главном потоке вместо 3 потоков), ноды хранятся в attachment чанка `AuraChunkData`, id нод в `AuraIdData`
- `thaumcraft/network` payload-ы, `thaumcraft/registry` DeferredRegister-ы
- Ресурсы (blockstates, модели, лут, рецепты) генерируются python-скриптами в `tools/`, не datagen
- Тексты исследований: `assets/thaumcraft/research/<lang>.xml` (у русского перевода свой набор страниц)

## Отступления от плана
- Теги аспектов в Java-коде (`ConfigAspects`), а не JSON: важен порядок регистрации и генерация из рецептов

## Маппинг имён
Старые ключи/мета → новые id см. `tools/convert_assets.py` (`build_key_map`) и `tools/convert_tags.py`.

## Статус фаз
- [x] 0 инструменты, декомпиляция, конвертация ассетов
- [x] 1 каркас
- [ ] 2 контент и аспекты (аспекты готовы, регистрация блоков/предметов в работе)
- [ ] 3 генерация мира
- [ ] 4 аура (серверная часть готова; клиент, рендер нод, сущности для флюкс-событий в работе)
- [ ] 5 жезлы и крафт
- [ ] 6 исследования
- [ ] 7 устройства
- [ ] 8 существа и големы
- [ ] 9 снаряжение
- [ ] 10 клиент и JEI
- [ ] 11 конфиг и локализация
