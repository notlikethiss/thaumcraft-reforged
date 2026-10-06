package thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class HungryChestModel extends Model {
    private final ModelPart root;
    private final ModelPart lid;
    private final ModelPart knob;

    public HungryChestModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
        lid = root.getChild("lid");
        knob = root.getChild("knob");
    }

    public void setOpenness(float openness) {
        float eased = 1.0F - openness;
        eased = 1.0F - eased * eased * eased;
        lid.xRot = -(eased * (float) Math.PI / 2.0F);
        knob.xRot = lid.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poseStack, buffer, light, overlay, color);
    }
}
