package thaumcraft.client.render.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import thaumcraft.client.render.entity.FireBatRenderState;

public class FireBatModel extends EntityModel<FireBatRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;

    public FireBatModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        body = root.getChild("body");
        rightWing = body.getChild("right_wing");
        rightWingTip = rightWing.getChild("right_wing_tip");
        leftWing = body.getChild("left_wing");
        leftWingTip = leftWing.getChild("left_wing_tip");
    }

    @Override
    public void setupAnim(FireBatRenderState state) {
        super.setupAnim(state);
        float degToRad = (float) Math.PI / 180.0F;
        if (state.hanging) {
            head.xRot = state.xRot * degToRad;
            head.yRot = (float) Math.PI - state.yRot * degToRad;
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
            head.xRot = state.xRot * degToRad;
            head.yRot = state.yRot * degToRad;
            body.xRot = (float) (Math.PI / 4) + Mth.cos(state.ageInTicks * 0.1F) * 0.15F;
            rightWing.yRot = Mth.cos(state.ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
            leftWing.yRot = -rightWing.yRot;
            rightWingTip.yRot = rightWing.yRot * 0.5F;
            leftWingTip.yRot = -rightWing.yRot * 0.5F;
        }
    }
}
