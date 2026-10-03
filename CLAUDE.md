# Thaumcraft 3 Reforged

Порт Thaumcraft 3.0.5e (MC 1.5.2, Azanor) на Minecraft 26.3 + NeoForge 26.3. Только для личного использования: логика и ассеты оригинала переносятся напрямую.

## Окружение

- Java 25: `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./gradlew ...`
- NeoForge `26.3.0.40-beta`, ModDevGradle `2.0.148`, JEI `31.8.0.53`
- Сборка: `./gradlew compileJava`, запуск: `./gradlew runClient`
- CI: `.github/workflows/build.yml` на пуш в `main` и `mc/**` собирает jar (`thaumcraft-<mc>-<версия>.jar`) и пересоздаёт релиз `nightly-<mc>` (с `main` помечается Latest и виден в сайдбаре репо); версия Java и MC берутся из `gradle.properties` ветки (`java_version`, `minecraft_version`)
- Ветки: `main` = 26.3, `mc/<версия>` = бэкпорты (создаются в начале бэкпорта, ветка с кодом 26.3 на старой версии не соберётся); правки workflow на `main` переносить в `mc/*` cherry-pick-ом
  - `mc/1.21.1`: NeoForge 21.1.x, Java 21, ModDevGradle 2.x, JEI 19.x
  - `mc/1.19.2`: Forge 43.x через плагин `net.neoforged.moddev.legacyforge`, Java 17, `META-INF/mods.toml` вместо `neoforge.mods.toml`, JEI 11.x

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
- `thaumcraft/blockentity` BE: база `TcBlockEntity` (`sync()`), `CrucibleBlockEntity`, `AlembicBlockEntity` (`EssentiaContainer`), `MagicWorkbenchBlockEntity` (11 слотов, слушатели-меню) → `ArcaneWorktableBlockEntity`, `InfusionWorkbenchBlockEntity` (источники аспектов ±12/±5)
- `thaumcraft/menu` меню верстаков (`MagicWorkbenchMenu` + наследники), `thaumcraft/client/screen` экраны, `thaumcraft/client/render` BER
- `thaumcraft/research` исследования; клиентские вещи из общего кода через `ResearchClientHooks` (имя исследования, открытие книги)
- `thaumcraft/item/wand` жезлы и `WandManager`; блоки, реагирующие на жезл, реализуют `block/WandTarget`
- `thaumcraft/block/device` устройства, `thaumcraft/block/ward` защищённые блоки (`WardedBlock`, `WardedGlassBlock`, `WardHelper`: владелец, креатив-слом, снятие жезлом); `blockentity/OwnedBlockEntity` владелец + доступ (0 использование, 1 выдача), `safeToRemove`, восстановление через `ward/WardManager` (очередь на уровень, блок эндер-жемчуга)
- Ресурсы (blockstates, модели, лут, рецепты) генерируются python-скриптами в `tools/`, не datagen
- Тексты исследований: `assets/thaumcraft/research/<lang>.xml` (у русского перевода свой набор страниц)

## Отступления от плана

- Теги аспектов в Java-коде (`ConfigAspects`), а не JSON: важен порядок регистрации и генерация из рецептов

## Маппинг имён

Старые ключи/мета → новые id см. `tools/convert_assets.py` (`build_key_map`) и `tools/convert_tags.py`.

## Заметки

- `PotionSoulShatter` (раскол души) не переносится: в оригинале применяется только мёртвой камерой `ItemCamera`
- Продвинутый тигель/перегонный куб (meta 5-7) в оригинале недостижимы, не переносятся
- Флюкс-слизь (`BlockFluxGoo` + падающая сущность) не переносится: в оригинале ничем не создаётся и не рисуется в мире
- Тин/серебро/свинец: предметы есть, рецепты переплавки кластеров не делаются (нет конкретного слитка)
- Рецепты с предметами TC имеют условие `neoforge:registered`, включаются по мере регистрации предметов
- Проверка сервера: `./gradlew runServer` (папка `run/`, в `server.properties` `pause-when-empty-seconds=-1`, иначе мир без игроков встаёт через минуту), stdin проброшен: `tail -f cmds | ./gradlew runServer`, команды дописываются в файл `cmds`
- Админ-команды: `/thaumcraft research <игроки> <ключ|all>` выдаёт исследования, `/thaumcraft note <игроки> <ключ> [прогресс 0..1]` выдаёт заметку, `/thaumcraft node <pos> <уровень>` создаёт обычную ноду ауры
- `/setblock` прогоняет `updateShape`, многоблочные штуки (стол исследований) ставить с режимом `strict`
- Блочные модели из `ModelRenderer` оригинала генерируются хелпером `model_box` в `gen_resources.py` (текстура копируется в `textures/block/*_model.png`, иначе её нет в атласе блоков)
- Теги с id мода пишутся с `"required": false`
- Пулы деревень: AT `StructureTemplatePool.rawTemplates` (без final) и `templates`, `VillageTowers` на `ServerAboutToStartEvent` добавляет `StructurePoolElement.legacy` (вес 2) в оба поля; шаблон башни лежит в 5 биомах (plains, taiga, savanna, snowy, desert с песчаником)
- Мехи ускоряют ванильную печь через access transformer (`META-INF/accesstransformer.cfg`, `AbstractFurnaceBlockEntity.cookingTimer`/`cookingTotalTime`)
- Модели из `ModelRenderer` 1.5.2 переносятся без `.mirror()`, если в оригинале `mirror = true` ставится после `addBox` (на бокс не влияет); если до `addBox`, то с `.mirror()`
- Текстуры гуманоидов 1.5.2 в раскладке 64×32 (низ пустой): слой `HumanoidModel.createMesh` + `LayerDefinition.create(mesh, 64, 64)`, малыш через `HumanoidModel.BABY_TRANSFORMER` (ванильная модель малыша 26.3 другая)
- Баги оригинала: чиним только поломки (уничтожение предметов, NPE, рассинхрон), игровые особенности 1:1
