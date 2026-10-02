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
- Сервер для проверки: `tail -f cmds | ./gradlew runServer` в фоне (stdin проброшен в `build.gradle`, игровая папка `run/`), ждать строку `Done (` в логе, команды дописывать в файл `cmds` (`forceload add 0 0`, `setblock ... destroy`, `data get block ...`). Остановка: `kill` по PID процесса с `serverRunProgramArgs`.
- Ресурсы: `python3 tools/gen_resources.py` (пишет в `src/generated/resources`, полностью пересоздаёт папку), `python3 tools/convert_assets.py` (текстуры, звуки, lang в `src/main/resources`). Доп. строки перевода в `tools/lang/en_us.json`, `tools/lang/ru_ru.json`, после правки перезапустить `convert_assets.py`.
- Справочники (в .gitignore): `reference/decompiled/thaumcraft/...` (оригинал с MCP-именами), `reference/mc/` (исходники MC 26.3 + ванильные assets/data), `reference/neo/` (NeoForge).
- Оболочка zsh: в `grep --include=*.java` экранировать glob или не использовать.
- Экономь контекст: большие файлы читай через `sed -n` диапазонами, вывод bash режь `head`/`grep`.

## Важные особенности API 26.3 (уже выяснено)
- `ResourceLocation` теперь `Identifier`. `EntityType.WITCH` переехал в `EntityTypes`. `GuiGraphics` теперь `GuiGraphicsExtractor`.
- Конфиг: `ModConfig.Type.SYNCED` (типы LOCAL, CLIENT, SYNCED, STARTUP).
- BlockEntity: `loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`, обновления через `ClientboundBlockEntityDataPacket.create(this)` + `getUpdateTag`.
- BER: `BlockEntityRenderer<T, S extends BlockEntityRenderState>` с `createRenderState`, `extractRenderState`, `submit(state, poseStack, SubmitNodeCollector, camera)`. Произвольная геометрия: `submitNodeCollector.submitCustomGeometry(poseStack, RenderType, (pose, buffer) -> ...)`, см. `BeaconRenderer`. RenderType из `net.minecraft.client.renderer.rendertype.RenderTypes`. Спрайт блока: `Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(id)` (ключ атласа `AtlasIds`, путь `TextureAtlas.LOCATION_*` только для `RenderType`).
- Частицы: `SingleQuadParticle` с `Layer(translucent, textureId, pipeline, oitSet)`. Наши слои и пайплайны: `client/fx/TcParticleLayers`, `ModRenderPipelines` (аддитивный = `BlendFunction.LIGHTNING`). Новые частицы наследуют `client/fx/TcParticle`.
- Feature теперь интерфейс с `place(level, generator, random, origin)` и `MapCodec`, регистрируется в `Registries.FEATURE_TYPE`, сами фичи data-driven (`worldgen/feature`, `placed_feature`).
- Топливо: компонент `cookingFuel(ResourceKey<ContextIntProvider>)`, JSON в `data/thaumcraft/context_int_provider/cooking/`.
- Лут-таблицы: условия через `"condition": {...}` (одно), предикаты по id (`minecraft:tool/can_shear`). Biome modifier: в `biomes` один тег или список id, поэтому по файлу на измерение.
- Рендер-слой блока (cutout/translucent) определяется автоматически по прозрачности текстуры.
- Цвета блоков: `RegisterColorHandlersEvent.BlockTintSources`, у предметов тинты в `items/*.json`.
- Броня: `Item.Properties.humanoidArmor(ArmorMaterial, ArmorType)`, ассеты в `assets/thaumcraft/equipment/*.json`.
- Payload-ы: `registrar.playToClient(type, codec)` без хендлера, клиентский хендлер в `RegisterClientPayloadHandlersEvent` (`client/ThaumcraftClient`).
- Меню: `IMenuTypeExtension.create(factory)`, открытие `player.openMenu(provider, pos)`, экраны в `RegisterMenuScreensEvent`. Экран: `extractBackground`, `extractLabels`, `extractTooltip`, тултип `graphics.setTooltipForNextFrame(font, list, Optional.empty(), x, y)`. Ванильный крафт на сервере: `serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level)`, остаток предмета `item.getCraftingRemainder()` (`ItemStackTemplate`).
- Тосты: `Minecraft.getInstance().gui.toastManager()`. Тинты предметов: свой `ItemTintSource` + `RegisterColorHandlersEvent.ItemTintSources`.
- BER с предметом: `ItemModelResolver.updateForTopItem` + `ItemStackRenderState.submit`; поворот `poseStack.rotateDegrees(Axis.XP, deg)`. `BlockAndTintGetter` лежит в `net.minecraft.client.renderer.block`.
- Слом блока с BE: `BlockEntity.preRemoveSideEffects` (команда `/setblock` без `destroy` его не вызывает). Предмет до `useItemOn` блока: `onItemUseFirst`. `Player.drop(stack, false, Prediction.PREDICTED)`.
- Компоненты регистрируются раньше предметов, значение по умолчанию можно задать в `Item.Properties.component(...)`.
- Экраны: открывать через `minecraft.gui.setScreen(...)`, фон как в игре `isInGameUi() -> true`, кнопки меню `minecraft.gameMode.handleInventoryButtonClick` + `clickMenuButton`, поверх слотов рисовать после `graphics.nextStratum()`. Галактический шрифт: `FontDescription.Resource("minecraft:alt")` (`client/gui/TcFonts`). Свой GUI-пайплайн: `ModRenderPipelines.GUI_ADDITIVE`.
- Ванильные рецепты на клиенте: `OnDatapackSyncEvent.sendRecipes(RecipeType.CRAFTING)` + `RecipesReceivedEvent` (`client/research/ClientRecipes`), отображение через `Recipe.display()` и `SlotDisplay.resolveForStacks(SlotDisplayContext.fromLevel(level))`.
- Рендер в руке от первого лица: `RenderHandEvent` (отменить и повторить `FirstPersonHandsAndItemsRenderer.renderTwoHandedMap`), текст в мире `SubmitNodeCollector.submitText`.
- `/setblock` прогоняет `updateShape`; многоблочные конструкции ставить с режимом `strict`.

## Архитектурные решения (не пересматривать)
- Аспекты: `aspect/ConfigAspects.java` генерируется `tools/convert_tags.py`, реестр строится на сервере при старте и `/reload` (`AspectSync`) и синхронизируется клиенту.
- Рецепты TC (тигель, магический и инфузионный верстак) в Java-реестре `crafting/ThaumcraftRecipes` в порядке регистрации, заполняются в `crafting/ConfigRecipes.java` (перенесено всё из оригинала). Ингредиенты и результаты строками id (`TcIngredient`, `TcResult`), разрешаются лениво, поэтому рецепты с ещё не зарегистрированными предметами просто неактивны. Ссылки исследование → рецепт в `ThaumcraftRecipes.researchRecipes()` (`RecipeReference`: Workbench, Crucible, Vanilla, Compound, FakeShaped).
- Обычные рецепты верстака и печи генерируются в JSON (`gen_resources.py`, функция `recipes()`) с условием `neoforge:registered`.
- Исследования: `research/ConfigResearch.java` генерируется `tools/convert_research.py`. Знания игрока в attachment `ModAttachments.KNOWLEDGE` (сохраняется, копируется при смерти, синхронизируется владельцу). Тексты книги из `assets/thaumcraft/research/<lang>.xml` (у русского свой набор страниц, грузить по языку клиента с откатом на en_us).
- Ноды ауры в attachment чанка (`AuraChunkData`), id из `AuraIdData`. На генерации пишутся «отложенные» ноды с key=-1, регистрируются при загрузке чанка.
- Метаданные расплющены в отдельные блоки и предметы. Имена новых id смотреть в `tools/convert_assets.py` (`build_key_map`), `tools/convert_research.py` (ICONS/SIMPLE) и `ConfigRecipes.java`. Ядро голема хранится в компоненте `GOLEM_CORE` предмета-голема. Варды-камни по цветам красителя: `{dye}_warded_stone`, свечи и маркеры по цветам шерсти.
- FX вызываются через `fx.Fx.get()` (интерфейс `FxProxy`, на сервере пустой, на клиенте `client/fx/ClientFx`).

## Сделано в прошлой сессии
- Фаза 6 целиком (план `~/.claude/plans/luminous-spinning-hare.md`): заметки и открытия (компонент `RESEARCH_NOTE`, `ResearchNoteData`, тинт `ResearchNoteTint`), чернильница `scribing_tools`, экспериментальные открытия в тигле, исследовательский стол (`research_table` main/side, BE с бонусами окружения, `ResearchTableMenu`, `ResearchTableScreen`), Таумономикон (`ResearchBookScreen`, `ResearchPageScreen`), шпаргалка `thaumonomicon_cheat`, таумометр и заметки в руках (`HandheldItemRenderer`).
- Команда `/thaumcraft note <игроки> <ключ> [прогресс]`.
- Исправлен поиск атласа в `TcRenderUtil.blockSprite` (падал бы при рендере тигля и куба).
- Сервер стартует без ошибок, логика стола проверена консолью (установка `strict`, откат половины, выброс содержимого). Визуально ничего из фаз 5-6 ещё не проверялось.

## Чек-лист визуальной проверки (фазы 5-6)
- Фаза 5: модели тигля, куба (направление носика к тиглю), стола (поворот по оси), верстака, `arcane_stone` по частям со свечением; жидкость в тигле и её окраска; уровень в кубе; жезл на верстаке и парящий над инфузионным; экраны верстаков (тексты вис, призрачный результат, ряд аспектов); тинт эссенций; тост исследования.
- Фаза 6: модель исследовательского стола на 2 блока по 4 сторонам (пергамент и перо, поворот), тинт заметок и открытий, экран стола (колонка аспектов с бонусами, кнопка, переключатель режима, диаграмма с линиями, всплывающий пергамент с галактическим шрифтом), книга (карта, перетаскивание, линии, рамки, тултипы, иконки), страницы всех типов (текст, тигель, верстак, магический, инфузия с циклом `C_*`, мистическая конструкция), клик по ингредиенту ведёт на исследование, шпаргалка, таумометр в руках (стрелка на ноду), заметки в руках.

## Отложено
- Адская печь и магнит нод в `WandManager` (фаза 7), снятие вардов, поворот бура жезлом, превращение голема в предмет жезлом.
- Жидкостная capability тигля (для продвинутого сального голема), мехи (`thaumcraft:crucible_bellows` уже учитывается тиглем).
- JEI: 47 «дублей» эссенций во вкладке, нужен subtype-интерпретатор по `ESSENCE_ASPECT` (фаза 10).
- Поток частиц инфузии сделан на `WispParticle`, точный `FXWispArcing` в фазе 10.
- Таумометр и заметки в руках рисуются плоскими квадами (в оригинале предмет с толщиной `renderItemIn2D`); неразблокированные иконки-предметы на карте книги затемняются прямоугольником, а не цветом предмета.

## Что осталось по порядку

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
