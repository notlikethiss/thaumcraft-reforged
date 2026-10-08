# Backport to Minecraft 1.21.1

## Targets

- Minecraft 1.21.1, NeoForge `21.1.256`, Java 21 (Gradle runs on Java 25, toolchain 21), ModDevGradle `2.0.148`
- Parchment `1.21.1` / `2024.11.17`
- JEI `19.57.0.451` (`jei-1.21.1-*` artifacts)
- Branch: `mc/1.21.1`

## Last synced main commit

`c3256df` (fix: validate structure palettes with profile block state keys)

## Sync procedure

1. `git log --reverse 7b5ce8e..main` in the worktree of this branch.
2. A commit touching only resources, lang, research XML or `tools/`: `git cherry-pick`, then regenerate with `python3 tools/gen_resources.py --mc 1.21.1` and `python3 tools/convert_tags.py --mc 1.21.1 --config <Config.java>`.
3. A commit with Java: `git show <sha> | python3 tools/backport/rewrite.py --target 1.21.1 | git apply -3`, resolve conflicts by hand, run `compileJava`.
4. Commit with the same message, then update the sha above.
5. Check ids: `python3 tools/validate_ids.py --mc 1.21.1 --out src/generated/resources --aspects src/main/java/thaumcraft/aspect/ConfigAspects.java`.

## Reference sources

In the main checkout (gitignored): `reference/1.21.1/mc` (net/minecraft, com/mojang) and `reference/1.21.1/neo` (net/neoforged). These sources already carry NeoForge access transformers and patches.

## rewrite.py rules (1.21.1)

- Imports: `Identifier` to `ResourceLocation`, `EntitySpawnReason` to `MobSpawnType`, `EntityTypes` to `EntityType`, `ItemUseAnimation` to `UseAnim`, `VegetationBlock` to `BushBlock`, `ARGB` to `FastColor` (`FastColor.ARGB32.*`), `org.jspecify.annotations.Nullable` to `javax.annotation.Nullable`, `ValueInput`/`ValueOutput` to `thaumcraft.compat.*`
- Package moves: golems (`animal.golem.*` to `animal`), `animal.sheep`, `monster.zombie`, `monster.spider`, `npc.villager`, `projectile.arrow`, `projectile.throwableitemprojectile`, `ParticleStatus`, `BlockAndTintGetter`, `PlayerModel`
- Constants: `EntitySpawnReason.SPAWN_ITEM_USE` to `SPAWN_EGG`, `MobEffects` (SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, RESISTANCE), `PushReaction.POPPED/IMMOVEABLE` to `DESTROY/BLOCK`
- Calls: `x.hurtServer(level, source, amount)` to `x.hurt(source, amount)` (and declarations), `poseStack.rotateDegrees(Axis.X, a)` to `poseStack.mulPose(Axis.X.rotationDegrees(a))`, `.identifier()` to `.location()`, `getMinY()` to `getMinBuildHeight()`, `getSelectedSlot()` to `.selected`, `getNonEquipmentItems()` to `.items`

## API differences (26.3 vs 1.21.1)

| Area | 26.3 | 1.21.1 |
|---|---|---|
| Ids | `Identifier` | `ResourceLocation` |
| Save data | `ValueInput`/`ValueOutput`, `loadAdditional(ValueInput)` | `CompoundTag` + `HolderLookup.Provider` (compat adapters) |
| Saved data | `SavedDataType` | `SavedData.Factory` |
| Item component stack template | `ItemStackTemplate` | `ItemStack.OPTIONAL_CODEC` |
| Item data components getter | `DataComponentGetter` | `DataComponentInput` in `applyImplicitComponents` |
| Registration | `registerBlock(name, fn)` | `register(name, () -> new ...)` with `Properties` |
| Materials | `ToolMaterial`, `ArmorMaterial` + `ArmorType` + `EquipmentAsset` | `Tier`, `ArmorMaterial` holder + `ArmorItem.Type` |
| Item use | `ItemUseAnimation`, `Consumable` | `UseAnim`, `FoodProperties` and `use` overrides |
| Interaction | `InteractionResult.SUCCESS_SERVER`, `useItemOn`/`useWithoutItem` return `InteractionResult` | `ItemInteractionResult`, `InteractionResultHolder` for `use` |
| Block updates | `neighborChanged(..., Orientation)`, `updateShape(..., ScheduledTickAccess, ...)`, `affectNeighborsAfterRemoval` | `neighborChanged(..., Block, BlockPos, boolean)`, `updateShape(state, dir, neighborState, LevelAccessor, pos, neighborPos)`, `onRemove` |
| Entities | `hurtServer(ServerLevel, ...)`, `InsideBlockEffectApplier` | `hurt(...)`, `entityInside(state, level, pos, entity)` |
| Level | `getMinY()/getMaxY()` | `getMinBuildHeight()/getMaxBuildHeight()` (max is exclusive) |
| Inventory | `getSelectedSlot()`, `getNonEquipmentItems()` | `selected`, `items` |
| Potions | `MobEffects.SPEED` etc. | `MOVEMENT_SPEED` etc. |
| Transfer | `ResourceHandler`, `Transaction`, `ItemResource`, `FluidResource` | `IItemHandler`, `IFluidHandler` capabilities |
| Network | client handlers registered separately, `ClientPacketDistributor` | handlers in `playToClient`, `PacketDistributor` |
| Attachments | `.sync()` | custom payload |
| Villagers | `TradeSet`, `villager_trade` data | `VillagerTradesEvent` |
| Recipes | `recipeAccess()`, `RecipeDisplay`, `RecipesReceivedEvent` | `getRecipeManager()`, client `RecipeManager` |
| Fuel | `context_int_provider` item component | `furnace_fuels` data map |
| Furnace AT | `cookingTimer` | `cookingProgress` |
| Render | extract/submit, `SubmitNodeCollector`, render states, `RenderPipeline`, `GuiGraphicsExtractor` | `render(...)` with `MultiBufferSource`, `RenderType`, `GuiGraphics` |
| Entity render | `EntityRenderState`, `AvatarRenderer` | `LivingEntityRenderer`, `PlayerRenderer` |
| Item models | `items/*.json` client item definitions, `SpecialModelRenderer`, tint sources | `models/item` overrides, `BlockEntityWithoutLevelRenderer`, `RegisterColorHandlersEvent` |
| Input events | `KeyEvent`, `MouseButtonEvent` | plain `int` parameters |
| Particles | `SingleQuadParticle.Layer` | `TextureSheetParticle`, `ParticleRenderType` |
| Events | `ExtractLevelRenderStateEvent`, `SubmitCustomGeometryEvent`, `AddClientReloadListenersEvent` | `RenderLevelStageEvent`, `RegisterClientReloadListenersEvent` |
| Annotations | `org.jspecify.annotations.Nullable` | `javax.annotation.Nullable` |

## Compat shims (`thaumcraft/compat`)

- `ValueInput`: `of(CompoundTag, Provider)`, `read(name, codec)`, `read(MapCodec)`, `child`, `childOrEmpty`, `childrenList`, `childrenListOrEmpty`, `list`, `listOrEmpty`, `get*Or`, `getInt`, `getLong`, `getString`, `getIntArray`, `keySet`, `lookup`, `tag`
- `ValueOutput`: `of(CompoundTag, Provider)`, `store(name, codec, value)`, `storeNullable`, `store(MapCodec, value)`, `store(CompoundTag)`, `put*`, `child`, `childrenList`, `list`, `discard`, `isEmpty`, `tag`
- Codec IO goes through `provider.createSerializationContext(NbtOps.INSTANCE)`
- `TcBlockEntity` bridges `loadAdditional(CompoundTag, Provider)` and `saveAdditional(CompoundTag, Provider)` to `loadAdditional(ValueInput)` and `saveAdditional(ValueOutput)`. Block entities extending vanilla classes directly need the same bridge: `ArcaneBoreBlockEntity`, `HungryChestBlockEntity` (both `BaseContainerBlockEntity`), `InfernalFurnaceBlockEntity` (`BlockEntity`)
