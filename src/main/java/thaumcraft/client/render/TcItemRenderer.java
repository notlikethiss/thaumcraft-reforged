package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import javax.annotation.Nullable;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.client.render.model.HungryChestModel;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.item.FilledJarItem;
import thaumcraft.item.JarContents;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;

public class TcItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final EntityModelSet entityModelSet;
    private JarRendering.Models jarModels;
    private BellowsRendering.Models bellowsModels;
    private BoreRendering.Models boreModels;
    private CrystalRendering.Models crystalModels;
    private HungryChestModel chestModel;

    public TcItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet entityModelSet) {
        super(dispatcher, entityModelSet);
        this.entityModelSet = entityModelSet;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        jarModels = null;
        bellowsModels = null;
        boreModels = null;
        crystalModels = null;
        chestModel = null;
    }

    private JarRendering.Models jarModels() {
        if (jarModels == null) {
            jarModels = JarRendering.Models.bake(entityModelSet);
        }
        return jarModels;
    }

    private BellowsRendering.Models bellowsModels() {
        if (bellowsModels == null) {
            bellowsModels = BellowsRendering.Models.bake(entityModelSet);
        }
        return bellowsModels;
    }

    private BoreRendering.Models boreModels() {
        if (boreModels == null) {
            boreModels = BoreRendering.Models.bake(entityModelSet);
        }
        return boreModels;
    }

    private CrystalRendering.Models crystalModels() {
        if (crystalModels == null) {
            crystalModels = CrystalRendering.Models.bake(entityModelSet);
        }
        return crystalModels;
    }

    private HungryChestModel chestModel() {
        if (chestModel == null) {
            chestModel = new HungryChestModel(entityModelSet.bakeLayer(TcModelLayers.HUNGRY_CHEST));
        }
        return chestModel;
    }

    private static boolean is(Item item, Item other) {
        return item == other;
    }

    private static int clusterType(Item item) {
        Item[] clusters = {
            ModItems.AIR_CRYSTAL_CLUSTER.get(),
            ModItems.FIRE_CRYSTAL_CLUSTER.get(),
            ModItems.WATER_CRYSTAL_CLUSTER.get(),
            ModItems.EARTH_CRYSTAL_CLUSTER.get(),
            ModItems.VIS_CRYSTAL_CLUSTER.get(),
            ModItems.MIXED_CRYSTAL_CLUSTER.get(),
        };
        for (int index = 0; index < clusters.length; index++) {
            if (clusters[index] == item) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Item item = stack.getItem();
        if (is(item, ModItems.WARDED_JAR.get()) || is(item, ModItems.BRAIN_JAR.get()) || is(item, ModItems.FILLED_JAR.get())) {
            renderJar(stack, poseStack, buffers, light, is(item, ModItems.BRAIN_JAR.get()));
        } else if (is(item, ModItems.ARCANE_BELLOWS.get())) {
            Minecraft minecraft = Minecraft.getInstance();
            int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
            float inflation = Mth.sin(ticks / 8.0F) * 0.3F + 0.7F;
            BellowsRendering.render(bellowsModels(), poseStack, buffers, light, Direction.NORTH, inflation);
        } else if (is(item, ModItems.HUNGRY_CHEST.get())) {
            HungryChestRendering.render(chestModel(), poseStack, buffers, light, Direction.NORTH, 0.0F);
        } else if (is(item, ModItems.ARCANE_BORE_BASE.get())) {
            BoreRendering.renderBase(boreModels(), poseStack, buffers, light, Direction.EAST);
        } else if (is(item, ModItems.ARCANE_BORE.get())) {
            BoreRendering.renderBore(boreModels(), poseStack, buffers, light, new BoreRendering.BoreState(0.0F, 0.0F, false, false, 0.0F, CrystalRendering.ticks() % 45));
        } else if (is(item, ModItems.CRYSTAL_CORE.get())) {
            CrystalRendering.renderCore(crystalModels(), poseStack, buffers, 0L, 0.0F, 0.0F);
        } else if (is(item, ModItems.CRYSTAL_CAPACITOR.get())) {
            renderBlockModel(ModBlocks.CRYSTAL_CAPACITOR.get().defaultBlockState(), poseStack, buffers, light, overlay);
            CrystalRendering.renderCapacitor(crystalModels(), poseStack, buffers, 0, 0, 0, 50);
        } else {
            int type = clusterType(item);
            if (type >= 0) {
                CrystalRendering.renderCluster(crystalModels(), poseStack, buffers, type, Direction.UP, type);
            }
        }
    }

    private void renderJar(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, boolean brain) {
        @Nullable JarContents contents = FilledJarItem.getContents(stack);
        float fill = contents == null ? 0.0F : Math.min(contents.amount(), JarBlockEntity.MAX_AMOUNT) / (float) JarBlockEntity.MAX_AMOUNT * 0.625F;
        int color = contents == null ? 0xFFFFFF : contents.aspect().color;
        JarRendering.render(jarModels(), poseStack, buffers, light, 0.0F, 0.0F, fill, color, brain, 0.0F, 0.03F);
    }

    private static void renderBlockModel(BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                state,
                model,
                1.0F,
                1.0F,
                1.0F,
                light,
                overlay,
                ModelData.EMPTY,
                renderType
            );
        }
    }
}
