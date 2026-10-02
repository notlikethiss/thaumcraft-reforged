# Продолжение порта Thaumcraft 3 Reforged

Файл для передачи работы в новый чат. Сначала прочитай `CLAUDE.md` (структура, статус фаз), потом этот файл. План целиком: `~/.claude/plans/memoized-exploring-crystal.md`.

## Задача
Порт Thaumcraft 3.0.5e (MC 1.5.2) на Minecraft 26.3 + NeoForge 26.3. Механики 1:1 с оригиналом, техническая часть на современных API, плюс интеграция JEI. Только для личного использования: декомпилированная логика и оригинальные ассеты переносятся напрямую.

## Договорённости с пользователем
- Отвечать на русском, без длинного тире и без конструкций "это не ...".
- Java: стандартный стиль, 4 пробела, без комментариев в коде.
- Коммиты на английском `feat: ...` / `fix: ...`, без упоминания соавторства.
- Коммитить после каждого законченного куска.

## Окружение и команды
- `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./gradlew compileJava --console=plain -q 2>&1 | grep -E "error:" -A2`
- Сервер для проверки: `./gradlew runServer` в фоне (игровая папка `run/`), ждать строку `Done (` в логе. Остановка: `kill` по PID java-процесса DevLaunch.
- Ресурсы: `python3 tools/gen_resources.py` (пишет в `src/generated/resources`, полностью пересоздаёт папку), `python3 tools/convert_assets.py` (текстуры, звуки, lang в `src/main/resources`). Доп. строки перевода в `tools/lang/en_us.json`, `tools/lang/ru_ru.json`, после правки перезапустить `convert_assets.py`.
- Справочники (в .gitignore): `reference/decompiled/thaumcraft/...` (оригинал с MCP-именами), `reference/mc/` (исходники MC 26.3 + ванильные assets/data), `reference/neo/` (NeoForge).
- Оболочка zsh: в `grep --include=*.java` экранировать glob или не использовать.
- Экономь контекст: большие файлы читай через `sed -n` диапазонами, вывод bash режь `head`/`grep`.

## Важные особенности API 26.3 (уже выяснено)
- `ResourceLocation` теперь `Identifier`. `EntityType.WITCH` переехал в `EntityTypes`. `GuiGraphics` теперь `GuiGraphicsExtractor`.
- Конфиг: `ModConfig.Type.SYNCED` (типы LOCAL, CLIENT, SYNCED, STARTUP).
- BlockEntity: `loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`, обновления через `ClientboundBlockEntityDataPacket.create(this)` + `getUpdateTag`.
- BER: `BlockEntityRenderer<T, S extends BlockEntityRenderState>` с `createRenderState`, `extractRenderState`, `submit(state, poseStack, SubmitNodeCollector, camera)`. Произвольная геометрия: `submitNodeCollector.submitCustomGeometry(poseStack, RenderType, (pose, buffer) -> ...)`, см. `BeaconRenderer`. RenderType из `net.minecraft.client.renderer.rendertype.RenderTypes`. Спрайт блока: `Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(id)`.
- Частицы: `SingleQuadParticle` с `Layer(translucent, textureId, pipeline, oitSet)`. Наши слои и пайплайны: `client/fx/TcParticleLayers`, `ModRenderPipelines` (аддитивный = `BlendFunction.LIGHTNING`). Новые частицы наследуют `client/fx/TcParticle`.
- Feature теперь интерфейс с `place(level, generator, random, origin)` и `MapCodec`, регистрируется в `Registries.FEATURE_TYPE`, сами фичи data-driven (`worldgen/feature`, `placed_feature`).
- Топливо: компонент `cookingFuel(ResourceKey<ContextIntProvider>)`, JSON в `data/thaumcraft/context_int_provider/cooking/`.
- Лут-таблицы: условия через `"condition": {...}` (одно), предикаты по id (`minecraft:tool/can_shear`). Biome modifier: в `biomes` один тег или список id, поэтому по файлу на измерение.
- Рендер-слой блока (cutout/translucent) определяется автоматически по прозрачности текстуры.
- Цвета блоков: `RegisterColorHandlersEvent.BlockTintSources`, у предметов тинты в `items/*.json`.
- Броня: `Item.Properties.humanoidArmor(ArmorMaterial, ArmorType)`, ассеты в `assets/thaumcraft/equipment/*.json`.
- Payload-ы: `registrar.playToClient(type, codec)` без хендлера, клиентский хендлер в `RegisterClientPayloadHandlersEvent` (`client/ThaumcraftClient`).

## Архитектурные решения (не пересматривать)
- Аспекты: `aspect/ConfigAspects.java` генерируется `tools/convert_tags.py`, реестр строится на сервере при старте и `/reload` (`AspectSync`) и синхронизируется клиенту.
- Рецепты TC (тигель, магический и инфузионный верстак) в Java-реестре `crafting/ThaumcraftRecipes` в порядке регистрации, заполняются в `crafting/ConfigRecipes.java` (перенесено всё из оригинала). Ингредиенты и результаты строками id (`TcIngredient`, `TcResult`), разрешаются лениво, поэтому рецепты с ещё не зарегистрированными предметами просто неактивны. Ссылки исследование → рецепт в `ThaumcraftRecipes.researchRecipes()` (`RecipeReference`: Workbench, Crucible, Vanilla, Compound, FakeShaped).
- Обычные рецепты верстака и печи генерируются в JSON (`gen_resources.py`, функция `recipes()`) с условием `neoforge:registered`.
- Исследования: `research/ConfigResearch.java` генерируется `tools/convert_research.py`. Знания игрока в attachment `ModAttachments.KNOWLEDGE` (сохраняется, копируется при смерти, синхронизируется владельцу). Тексты книги из `assets/thaumcraft/research/<lang>.xml` (у русского свой набор страниц, грузить по языку клиента с откатом на en_us).
- Ноды ауры в attachment чанка (`AuraChunkData`), id из `AuraIdData`. На генерации пишутся «отложенные» ноды с key=-1, регистрируются при загрузке чанка.
- Метаданные расплющены в отдельные блоки и предметы. Имена новых id смотреть в `tools/convert_assets.py` (`build_key_map`), `tools/convert_research.py` (ICONS/SIMPLE) и `ConfigRecipes.java`. Ядро голема хранится в компоненте `GOLEM_CORE` предмета-голема. Варды-камни по цветам красителя: `{dye}_warded_stone`, свечи и маркеры по цветам шерсти.
- FX вызываются через `fx.Fx.get()` (интерфейс `FxProxy`, на сервере пустой, на клиенте `client/fx/ClientFx`).

## Срочно исправить первым делом
- `ResearchCompletePayload` отправляется сервером, но клиентского хендлера нет. Зарегистрировать в `ThaumcraftClient.registerPayloadHandlers` (класть ключ в очередь всплывающих уведомлений «You've learned something new!», звук `thaumcraft:learn`). Без этого клиент, возможно, отключается при завершении исследования.
- Клиент ни разу не запускался: проверить `./gradlew runClient` на ошибки моделей, текстур, пайплайнов частиц.

## Что осталось по порядку

### Фаза 5. Жезлы и крафт (в работе)
1. Базовый `blockentity/TcBlockEntity` с синхронизацией (getUpdateTag/packet, метод `sync()` = setChanged + sendBlockUpdated).
2. Тигель: `CrucibleBlock` (форма с вырезом, `entityInside` плавит предметы через `AspectHelper.getObjectTagsWithBonus` и бьёт мобов, заливка водяным ведром, `onRemove` сливает остаток), `CrucibleBlockEntity` (heat, liquid, AspectList, нагрев от лавы/огня/нитора под ним, мехи по бокам, переполнение > 500 даёт флюкс, `spillRemnants` в перегонные кубы), события блока 1 = искры, 2 = бурление (`triggerEvent`). Оригинал: `common/blocks/BlockCrucible.java`, `common/tiles/TileCrucible.java`. BER жидкости по `client/renderers/tile/TileCrucibleRenderer.java`. JSON-модель котла (внешние стороны crucible1-3, внутренние crucible5/6). FX уже есть (`crucibleBoil`, `crucibleFroth`, `crucibleBubble`).
3. Перегонный куб `alembic` (facing, BE с одним аспектом до 16, отдаёт через `AspectSource`), рендер `ModelAlembic` + уровень жидкости. Оригинал `TileAlembic`, `TileAlembicRenderer`.
4. Жезлы: `CastingWandItem(maxVis, interval, rarity)` (ученик 50/10, адепт 250/7, тауматург 1000/5), компонент `WAND_VIS`, подзарядка `AuraManager.decreaseClosestAura`, подсказка `tc.thaumcraft.wandcharge`. `WandManager` (spendCharge со скидкой `IVisDiscounter` до 50%, Таумономикон из книжной полки, тигель из котла, адская печь, магнит нод, инфузионный верстак, перегонный куб → тигель). Взаимодействие с блоками TC через интерфейс на блоке. Оригинал `common/items/wands/`.
5. Эссенции: `essentia_phial` и `essence` (компонент `ESSENCE_ASPECT`, реализует `AspectProvidingItem`), набор из куба и банки.
6. Стол, магический верстак (Menu + Screen, слот жезла, стоимость вис), инфузионный верстак (мультиблок 2x2 из arcane_stone, берёт аспекты из источников рядом). Оригинал `TileArcaneWorkbench`, `TileInfusionWorkbench`, `GuiArcaneWorkbench`, `GuiInfusionWorkbench`.
7. Крафт в тигле: `performCrucibleCrafting` и `isCrucibleCreationSuccessful` из `ThaumcraftCraftingManager` и `ResearchManager` оригинала.

### Фаза 6. Исследования
Таумономикон (`GuiResearchWindow`, `GuiResearchRecipe`, рендер всех типов страниц и `RecipeReference`), загрузка XML по языку, исследовательский стол и мини-игра (`TileResearchTable`, `ResearchNoteData`, шансы из `Config`), заметки/открытия, чернильница, таумометр и `ScanManager` (аспекты сущностей `generateEntityAspects`), экспериментальные открытия в тигле, шпаргалка-книга в креативе (`ALLOW_CHEAT_SHEET`).

### Фаза 4, остаток
Рендер нод в очках откровения и HUD (`client/lib/RenderEventHandler`, `GUITicker`), FX молнии (`FXLightningBolt*`) для `nodeBolt`, эффекты флюкса ждут сущностей (ищутся по id `thaumcraft:brainy_zombie`, `giant_brainy_zombie`, `fire_bat`, `wisp`). Флюкс-слизь `BlockFluxGoo` + падающая сущность.

### Фаза 7. Устройства
Банки (обычная, с мозгом, предмет заполненной банки), адская печь, мехи, магическое ухо, нажимная плита, бур и основание, левитатор, кристаллы/ядро/конденсатор, зеркала и ручное зеркало, голодный сундук, защищённые блоки (камень 16 цветов, стекло, дверь, ключи, владелец, восстановление из `WorldTicker`), портативная дыра, маркеры, свечи, arcane_stone, arcane_wood, стол. Вписывать блоки в `ModBlocks`/`ModItems`, ресурсы в `gen_resources.py`.

### Фаза 3, остаток
Башня волшебника (`ComponentWizardTower`, `VillageManager`) как кусок деревни + волшебник-житель.

### Фаза 8. Существа и големы
Злой зомби, гигантский злой зомби, огненная летучая мышь, висп (`setAspect` через `AuraManager.AspectTyped`), снаряды (алюментум с броском и раздатчиком, дротик, ледяной осколок), особые предметы-сущности. Големы: 9 типов на `PathfinderMob`, ядра, 7 украшений, 42 AI в `Goal`, меню, сбор урожая по тегу `thaumcraft:golem_harvestable`. Спавны через biome modifier. Предметы: golem_core_{basic,speed,intelligence,perception,strength}, `{wood,clay,stone,tallow,straw,advanced_clay,advanced_stone,iron_guardian,decanting}_golem`, golem_{top_hat,spectacles,bowtie,fez,dart_launcher,visor,iron_plating}.

### Фаза 9. Снаряжение
Особые свойства элементальных инструментов и жезлов огня/мороза/молнии/обмена/раскопок, адский жезл, очки, сапоги странника (скорость, шаг), робы, термостатическая упряжь (OBJ `assets/thaumcraft/models/obj/hoverharness.obj`), 6 зачарований как JSON (ключи в `registry/ModEnchantments`), эффект «раскол души», Vis-ремонт (`doRepair`).

### Фазы 10-11
JEI-плагин (категории тигля, магического и инфузионного верстаков), оставшиеся частицы и HUD, локализация захардкоженных строк и недостающих переводов, переводы опций конфига (`thaumcraft.configuration.*`), описания аспектов (`aspect.thaumcraft.<name>.meaning`).

## Проверка после каждого шага
1. Компиляция.
2. `runServer` без ERROR/Exception (кроме `server.properties`).
3. Коммит, отметка статуса в `CLAUDE.md`.
