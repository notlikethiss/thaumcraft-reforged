package thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import thaumcraft.entity.golem.GolemBase;

public class GolemModel extends EntityModel<GolemBase> {
    private final ModelPart root;
    private int healing;
    private final ModelPart head;
    private final @Nullable ModelPart headSmart;
    private final @Nullable ModelPart headObserver;
    private final @Nullable ModelPart headBrain;
    private final @Nullable ModelPart headJar;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final @Nullable ModelPart rightArmStrong;
    private final @Nullable ModelPart leftArmStrong;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final @Nullable ModelPart rightLegFast;
    private final @Nullable ModelPart leftLegFast;

    public GolemModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        ModelPart golem = root.getChild("golem");
        head = golem.getChild("head");
        headSmart = child(golem, "head_smart");
        headObserver = child(golem, "head_observer");
        headBrain = child(golem, "head_brain");
        headJar = child(golem, "head_jar");
        rightArm = golem.getChild("right_arm");
        leftArm = golem.getChild("left_arm");
        rightArmStrong = child(golem, "right_arm_strong");
        leftArmStrong = child(golem, "left_arm_strong");
        rightLeg = golem.getChild("right_leg");
        leftLeg = golem.getChild("left_leg");
        rightLegFast = child(golem, "right_leg_fast");
        leftLegFast = child(golem, "left_leg_fast");
    }

    private static @Nullable ModelPart child(ModelPart parent, String name) {
        return parent.hasChild(name) ? parent.getChild(name) : null;
    }

    public static float triangle(float value, float period) {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    public static boolean carrying(GolemBase entity) {
        return !entity.getDisplayCarried().isEmpty() && entity.deathTime == 0;
    }

    public static float armAngle(GolemBase entity, boolean left, float current, float partialTick, float limbSwing, float limbSwingAmount) {
        int action = entity.getActionTimer();
        if (action > 0) {
            return -2.0F + 1.5F * triangle(action - partialTick, 10.0F);
        }
        if (entity.leftArm > 0) {
            return left ? -2.0F + 1.5F * triangle(entity.leftArm - partialTick, 10.0F) : current;
        }
        if (carrying(entity)) {
            return -1.0F;
        }
        float swing = 1.5F * triangle(limbSwing, 13.0F);
        return (left ? -0.2F - swing : -0.2F + swing) * limbSwingAmount;
    }

    @Override
    public void setupAnim(GolemBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = ageInTicks - entity.tickCount;
        int core = entity.getCore();
        healing = entity.healing;
        float yaw = netHeadYaw * ((float) Math.PI / 180.0F);
        float pitch = headPitch * ((float) Math.PI / 180.0F);
        for (ModelPart part : new ModelPart[] {head, headSmart, headObserver, headBrain, headJar}) {
            if (part != null) {
                part.yRot = yaw;
                part.xRot = pitch;
            }
        }
        if (headSmart != null && headObserver != null) {
            head.visible = core != 2 && core != 3;
            headSmart.visible = core == 2;
            headObserver.visible = core == 3;
        }
        float legSwing = 1.5F * triangle(limbSwing, 13.0F) * limbSwingAmount;
        rightLeg.xRot = -legSwing;
        leftLeg.xRot = legSwing;
        if (rightLegFast != null && leftLegFast != null) {
            rightLegFast.xRot = rightLeg.xRot;
            leftLegFast.xRot = leftLeg.xRot;
            boolean fast = core == 1;
            rightLeg.visible = !fast;
            leftLeg.visible = !fast;
            rightLegFast.visible = fast;
            leftLegFast.visible = fast;
        }
        rightArm.xRot = armAngle(entity, false, rightArm.xRot, partialTick, limbSwing, limbSwingAmount);
        leftArm.xRot = armAngle(entity, true, leftArm.xRot, partialTick, limbSwing, limbSwingAmount);
        if (rightArmStrong != null && leftArmStrong != null) {
            rightArmStrong.xRot = rightArm.xRot;
            leftArmStrong.xRot = leftArm.xRot;
            boolean strong = core == 4;
            rightArm.visible = !strong;
            leftArm.visible = !strong;
            rightArmStrong.visible = strong;
            leftArmStrong.visible = strong;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        if (healing > 0) {
            float h1 = healing / 10.0F;
            float h2 = healing / 5.0F;
            color = FastColor.ARGB32.multiply(color, FastColor.ARGB32.colorFromFloat(1.0F, Math.min(1.0F, 0.5F + h1), Math.min(1.0F, 0.9F + h2), Math.min(1.0F, 0.5F + h1)));
        }
        poseStack.pushPose();
        poseStack.scale(0.4F, 0.4F, 0.4F);
        root.render(poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }
}
