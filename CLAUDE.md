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

## Статус фаз
- [x] 0 инструменты, декомпиляция, конвертация ассетов
- [x] 1 каркас
- [~] 2 контент: мировые блоки, ресурсы, осколки, самородки, еда, инструменты и броня (базовые), нитор, аспекты с синхронизацией и оверлеем
- [x] 3 генерация мира (руды, деревья, цветы, курган, обелиски, ноды) через фичу `thaumcraft:world_generation`; башня волшебника не сделана
- [x] 4 аура: сервер, клиентские данные нод, рендер нод в очках (`client/render/AuraNodeRenderer`), молнии (`client/fx/bolt`), HUD очков и жезла (`client/gui/TcHud`), аспекты тигля/куба в очках (`GogglesTagRenderer`); флюкс-события с сущностями ждут фазы 8
- [x] 5 крафт: `ThaumcraftRecipes` + `ConfigRecipes`, JSON-рецепты верстака, тигель (BE + BER, плавление, флюкс, крафт `CrucibleCrafting`), перегонный куб, жезлы (`CastingWandItem`, `WAND_VIS`, скидка `VIS_DISCOUNT`), `WandManager` (книга, тигель из котла, интерфейс `WandTarget` на блоках), фиалы и эссенции, стол → магический верстак, инфузионный верстак (2×2 `arcane_stone`); адская печь и магнит нод ждут фазы 7
- [x] 6 исследования: `ResearchList` + `ConfigResearch` (89 записей), знания игрока (attachment), `ResearchManager`, тост «узнали новое», загрузчик текстов `client/research/ResearchTexts`; заметки/открытия (компонент `RESEARCH_NOTE`, `ResearchNoteData`), чернильница, эксперименты в тигле, исследовательский стол (`research_table` из двух столов + чернильница, BE, меню, экран с диаграммой), Таумономикон (`client/research/ResearchBookScreen` карта, `ResearchPageScreen` страницы всех типов, ванильные рецепты на клиенте через `ClientRecipes`), шпаргалка `thaumonomicon_cheat`, таумометр и заметки в руках как карта (`client/render/HandheldItemRenderer`, `RenderHandEvent`); `ScanManager` не переносится (в оригинале мёртвый код)
- [~] 7 устройства (план `~/.claude/plans/claude-md-glowing-sprout.md`): arcane wood, `{dye}_warded_stone`, `warded_glass` с рунами по краям, восстановление вардов, свечи, маркеры (без логики до фазы 8), банки `warded_jar`/`brain_jar`/`filled_jar` (`JarRenderer`, `JarSpecialRenderer`), мехи (`BellowsRenderer`), адская печь (сборка жезлом); осталось ухо, плита, дверь, ключи, левитатор, голодный сундук, кристаллы/ядро/конденсатор, зеркала, портативная дыра, бур
- [ ] 8 существа и големы
- [ ] 9 снаряжение (особые свойства)
- [ ] 10 клиент и JEI
- [ ] 11 конфиг и локализация

## Заметки
- Продвинутый тигель/перегонный куб (meta 5-7) в оригинале недостижимы, не переносятся
- Флюкс-слизь (`BlockFluxGoo` + падающая сущность) не переносится: в оригинале ничем не создаётся и не рисуется в мире
- Тин/серебро/свинец: предметы есть, рецепты переплавки кластеров не делаются (нет конкретного слитка)
- Рецепты с предметами TC имеют условие `neoforge:registered`, включаются по мере регистрации предметов
- Проверка сервера: `./gradlew runServer` (папка `run/`), stdin проброшен: `tail -f cmds | ./gradlew runServer`, команды дописываются в файл `cmds`
- Админ-команды: `/thaumcraft research <игроки> <ключ|all>` выдаёт исследования, `/thaumcraft note <игроки> <ключ> [прогресс 0..1]` выдаёт заметку
- `/setblock` прогоняет `updateShape`, многоблочные штуки (стол исследований) ставить с режимом `strict`
- Блочные модели из `ModelRenderer` оригинала генерируются хелпером `model_box` в `gen_resources.py` (текстура копируется в `textures/block/*_model.png`, иначе её нет в атласе блоков)
- Теги с id мода пишутся с `"required": false`
- Мехи ускоряют ванильную печь через access transformer (`META-INF/accesstransformer.cfg`, `AbstractFurnaceBlockEntity.cookingTimer`/`cookingTotalTime`)
- Баги оригинала: чиним только поломки (уничтожение предметов, NPE, рассинхрон), игровые особенности 1:1
