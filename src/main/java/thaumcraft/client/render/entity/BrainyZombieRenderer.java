package thaumcraft.client.render.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;

public class BrainyZombieRenderer extends ZombieRenderer {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/model/bzombie.png");

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, TcModelLayers.BRAINY_ZOMBIE, TcModelLayers.BRAINY_ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, TcModelLayers.BRAINY_ZOMBIE_BABY_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
