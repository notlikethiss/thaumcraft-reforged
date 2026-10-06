package thaumcraft.client.render.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;

public class BrainyZombieRenderer extends ZombieRenderer {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/bzombie.png");

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, TcModelLayers.BRAINY_ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }
}
