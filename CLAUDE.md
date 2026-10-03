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

## Статус фаз
- [x] 0 инструменты, декомпиляция, конвертация ассетов
- [x] 1 каркас
- [x] 2 контент: мировые блоки, ресурсы, осколки, самородки, еда, инструменты и броня (базовые), нитор, аспекты с синхронизацией и оверлеем
- [x] 3 генерация мира (руды, деревья, цветы, курган, обелиски, ноды) через фичу `thaumcraft:world_generation`; башня волшебника в 8.1
- [x] 4 аура: сервер, клиентские данные нод, рендер нод в очках (`client/render/AuraNodeRenderer`), молнии (`client/fx/bolt`), HUD очков и жезла (`client/gui/TcHud`), аспекты тигля/куба в очках (`GogglesTagRenderer`); флюкс-события с сущностями работают с фазы 8
- [x] 5 крафт: `ThaumcraftRecipes` + `ConfigRecipes`, JSON-рецепты верстака, тигель (BE + BER, плавление, флюкс, крафт `CrucibleCrafting`), перегонный куб, жезлы (`CastingWandItem`, `WAND_VIS`, скидка `VIS_DISCOUNT`), `WandManager` (книга, тигель из котла, интерфейс `WandTarget` на блоках), фиалы и эссенции, стол → магический верстак, инфузионный верстак (2×2 `arcane_stone`); адская печь и магнит нод ждут фазы 7
- [x] 6 исследования: `ResearchList` + `ConfigResearch` (89 записей), знания игрока (attachment), `ResearchManager`, тост «узнали новое», загрузчик текстов `client/research/ResearchTexts`; заметки/открытия (компонент `RESEARCH_NOTE`, `ResearchNoteData`), чернильница, эксперименты в тигле, исследовательский стол (`research_table` из двух столов + чернильница, BE, меню, экран с диаграммой), Таумономикон (`client/research/ResearchBookScreen` карта, `ResearchPageScreen` страницы всех типов, ванильные рецепты на клиенте через `ClientRecipes`), шпаргалка `thaumonomicon_cheat`, таумометр и заметки в руках как карта (`client/render/HandheldItemRenderer`, `RenderHandEvent`); `ScanManager` не переносится (в оригинале мёртвый код)
- [x] 7 устройства (план `~/.claude/plans/claude-md-glowing-sprout.md`): arcane wood, варды (камень, стекло, восстановление, жемчуг), свечи, маркеры (без логики до фазы 8), банки, мехи, адская печь, магическое ухо, плита, дверь, ключи, левитатор, голодный сундук, кристаллы/ядро-магнит/конденсатор (`block/crystal`), зеркала и портативная дыра (`block/mirror`, туннель через `END_PORTAL`), чародейский бур (`block/bore`, меню `ArcaneBoreMenu`, `BoreDigPayload`); мировые эффекты с произвольной геометрией `client/fx/world` (`WorldFxRenderer`, `BeamFx`, `BoreBeamFx`, `RuneFx`)
- [x] 8 существа и големы (план `~/.claude/plans/swift-puzzling-key.md`): сущности в `thaumcraft/entity` (`monster`), регистр `registry/ModEntities` (атрибуты, правила спавна), рендеры `client/render/entity`; злые зомби (обычный и гигант), огненная летучая мышь, висп и эссенция виспа, снаряды (`entity/projectile`: алюментум, дротик, ледяной осколок), магический и следующий предмет (`entity/SpecialItem`, `FollowingItem`), големы (`entity/golem`: база, 9 типов, цели в `goal`, предметы `item/golem`, рендер `GolemRenderer`), маркеры как POI (`ModPoiTypes`) и логистика `GolemUtils` (transfer API), меню `GolemMenu` + `GolemScreen`, яйца призыва, спавны через biome modifier
- [x] 8.1 волшебник-житель и башня волшебника (остаток фазы 3): житель готов (`registry/ModVillagers`: POI и профессия `wizard` на `arcane_worktable`, сделки data-driven `villager_trade/wizard`, `trade_set/wizard`, текстуры профессии в `textures/entity/villager` и `zombie_villager`); башня волшебника в деревнях готова (`tools/gen_structures.py` пишет `data/thaumcraft/structure/village/<biome>/wizard_tower.nbt`, `world/gen/VillageTowers` добавляет её в пулы `village/<biome>/houses`, лут `chests/wizard_tower`); лут TC в ванильных сундуках через global loot modifiers `neoforge:add_table` (`data/thaumcraft/loot_modifiers/*.json`, таблицы `loot_table/inject/*`, генерация `chest_injections()` в `gen_resources.py`: пул с весами оригинала и `minecraft:empty` весом основного пула ванили, броски как у основного пула)
- [x] 9 снаряжение (особые свойства): готова инфраструктура B0, 6 зачарований (JSON в `gen_resources.py`, функция `enchantments()`, теги `enchantable/*`, `vis_repairable`), вис-ремонт `event/EquipmentEvents`, база жезлов `item/wand/ElementalWandItem` (Frugal, Charging, потенция), порты `lib/Utils` (`getPointedEntity`, `getTargetBlock`, `isBlockExposed`, `consumeInventoryItem`, `useBonemealAtLoc`), жезлы мороза (`FrostWandItem`, `FrostShard`) и молний (`LightningWandItem`, `LightningWandPayload`, `Fx.wandLightning`), жезл огня (`FireWandItem`, частица `ScorchParticle`, `Fx.wandFire` на клиенте без payload) и адский жезл (`HellrodItem`, заряды в `HELLROD_CHARGES`, HUD в `TcHud`), жезл раскопок (`ExcavationWandItem`, состояние копания в transient-attachment `EXCAVATION` = `ExcavationState`, луч `WandBeamFx` через `Fx.wandBeam`, трещины через `destroyBlockProgress`), жезл равноценного обмена (`EqualTradeWandItem`, блок в компоненте `TRADE_BLOCK` как `BlockState`, ЛКМ через `PlayerInteractEvent.LeftClickBlock`, очередь `SwapperManager`, HUD в `TcHud`), элементальные инструменты `item/tool/Elemental{Axe,Pickaxe,Shovel,Hoe,Sword}Item` (слом топора и кирки через `BreakBlockEvent` в `ElementalToolEvents`, лопата через `mineBlock`, меч через `onLeftClickEntity`/`onUseTick`; `BlockBoilPayload`, `BlockTagsPayload` со скан-подсветкой аспекта в `GogglesTagRenderer.showBlockTags`, частица `SmokeSpiralParticle`), сапоги странника и Haste (`EquipmentEvents` + `lib/MovementModifiers`: модификаторы атрибутов скорости и шага, добавка к скорости в воздухе, вода, падение, прыжок через `LivingJumpEvent`, Haste работает на любых ботинках), термостатическая упряжь (`item/armor/HoverHarnessItem` + `Hover` (топливо, серверный тик), `menu/HoverHarnessMenu` + `HoverHarnessScreen` со слотом банки Potentia, компоненты `HOVER`, `HOVER_CHARGE`, `HARNESS_JAR` (`ItemStackTemplate`), C2S `HoverTogglePayload`, клиентская логика двойного прыжка и молний `client/HoverHarnessClient`, рендер на игроке `client/render/HoverHarnessLayer` с собственным разбором OBJ `client/render/model/ObjModel`, HUD топлива в `TcHud`)
- [ ] 10 клиент и JEI
- [ ] 11 конфиг и локализация

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
