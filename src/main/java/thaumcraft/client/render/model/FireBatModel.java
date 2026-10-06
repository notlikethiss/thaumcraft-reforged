package thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import thaumcraft.entity.monster.FireBat;

public class FireBatModel extends EntityModel<FireBat> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;

    public FireBatModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rightWing = body.getChild("right_wing");
        rightWingTip = rightWing.getChild("right_wing_tip");
        leftWing = body.getChild("left_wing");
        leftWingTip = leftWing.getChild("left_wing_tip");
    }

    @Override
    public void setupAnim(FireBat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float degToRad = (float) Math.PI / 180.0F;
        if (entity.isHanging()) {
            head.xRot = headPitch * degToRad;
            head.yRot = (float) Math.PI - netHeadYaw * degToRad;
            head.zRot = (float) Math.PI;
            head.setPos(0.0F, -2.0F, 0.0F);
            rightWing.setPos(-3.0F, 0.0F, 3.0F);
            leftWing.setPos(3.0F, 0.0F, 3.0F);
            body.xRot = (float) Math.PI;
            rightWing.xRot = (float) (-Math.PI / 20);
            rightWing.yRot = (float) (-Math.PI * 2.0 / 5.0);
            rightWingTip.yRot = -1.7278761F;
            leftWing.xRot = rightWing.xRot;
            leftWing.yRot = -rightWing.yRot;
            leftWingTip.yRot = -rightWingTip.yRot;
        } else {
            head.xRot = headPitch * degToRad;
            head.yRot = netHeadYaw * degToRad;
            body.xRot = (float) (Math.PI / 4) + Mth.cos(ageInTicks * 0.1F) * 0.15F;
            rightWing.yRot = Mth.cos(ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
            leftWing.yRot = -rightWing.yRot;
            rightWingTip.yRot = rightWing.yRot * 0.5F;
            leftWingTip.yRot = -rightWing.yRot * 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poseStack, buffer, light, overlay, color);
    }
}
