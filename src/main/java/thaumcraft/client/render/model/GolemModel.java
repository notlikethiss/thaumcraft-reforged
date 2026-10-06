package thaumcraft.client.render.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import javax.annotation.Nullable;
import thaumcraft.client.render.entity.GolemRenderState;

public class GolemModel extends EntityModel<GolemRenderState> {
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
        super(root, RenderTypes::entityTranslucent);
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

    public static float armAngle(GolemRenderState state, boolean left, float current) {
        if (state.actionActive) {
            return -2.0F + 1.5F * triangle(state.actionTimer, 10.0F);
        }
        if (state.leftArmActive) {
            return left ? -2.0F + 1.5F * triangle(state.leftArm, 10.0F) : current;
        }
        if (state.carrying) {
            return -1.0F;
        }
        float swing = 1.5F * triangle(state.walkAnimationPos, 13.0F);
        return (left ? -0.2F - swing : -0.2F + swing) * state.walkAnimationSpeed;
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);
        float yaw = state.yRot * ((float) Math.PI / 180.0F);
        float pitch = state.xRot * ((float) Math.PI / 180.0F);
        for (ModelPart part : new ModelPart[] {head, headSmart, headObserver, headBrain, headJar}) {
            if (part != null) {
                part.yRot = yaw;
                part.xRot = pitch;
            }
        }
        if (headSmart != null && headObserver != null) {
            head.visible = state.core != 2 && state.core != 3;
            headSmart.visible = state.core == 2;
            headObserver.visible = state.core == 3;
        }
        float legSwing = 1.5F * triangle(state.walkAnimationPos, 13.0F) * state.walkAnimationSpeed;
        rightLeg.xRot = -legSwing;
        leftLeg.xRot = legSwing;
        if (rightLegFast != null && leftLegFast != null) {
            rightLegFast.xRot = rightLeg.xRot;
            leftLegFast.xRot = leftLeg.xRot;
            boolean fast = state.core == 1;
            rightLeg.visible = !fast;
            leftLeg.visible = !fast;
            rightLegFast.visible = fast;
            leftLegFast.visible = fast;
        }
        rightArm.xRot = armAngle(state, false, rightArm.xRot);
        leftArm.xRot = armAngle(state, true, leftArm.xRot);
        if (rightArmStrong != null && leftArmStrong != null) {
            rightArmStrong.xRot = rightArm.xRot;
            leftArmStrong.xRot = leftArm.xRot;
            boolean strong = state.core == 4;
            rightArm.visible = !strong;
            leftArm.visible = !strong;
            rightArmStrong.visible = strong;
            leftArmStrong.visible = strong;
        }
    }
}
