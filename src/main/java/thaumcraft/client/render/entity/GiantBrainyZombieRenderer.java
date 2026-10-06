package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.entity.monster.GiantBrainyZombie;

public class GiantBrainyZombieRenderer extends BrainyZombieRenderer {
    public GiantBrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new GiantState();
    }

    @Override
    public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity instanceof GiantBrainyZombie giant && state instanceof GiantState giantState) {
            giantState.anger = giant.getAnger();
        }
    }

    @Override
    protected void scale(ZombieRenderState state, PoseStack poseStack) {
        if (state instanceof GiantState giantState) {
            poseStack.scale(giantState.anger, giantState.anger, giantState.anger);
        }
    }

    private static class GiantState extends ZombieRenderState {
        private float anger = 1.0F;
    }
}
