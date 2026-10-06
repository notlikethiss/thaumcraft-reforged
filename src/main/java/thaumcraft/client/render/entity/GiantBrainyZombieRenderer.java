package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.entity.monster.GiantBrainyZombie;

public class GiantBrainyZombieRenderer extends BrainyZombieRenderer {
    public GiantBrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Zombie entity, PoseStack poseStack, float partialTick) {
        if (entity instanceof GiantBrainyZombie giant) {
            float anger = giant.getAnger();
            poseStack.scale(anger, anger, anger);
        }
    }
}
