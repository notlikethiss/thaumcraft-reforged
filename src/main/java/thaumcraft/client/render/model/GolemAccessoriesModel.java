package thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import thaumcraft.entity.golem.GolemBase;

public class GolemAccessoriesModel extends EntityModel<GolemBase> {
    private final ModelPart root;
    private final ModelPart fez;
    private final ModelPart plate;
    private final ModelPart plateLeft;
    private final ModelPart plateRight;
    private final ModelPart hat;
    private final ModelPart glasses;
    private final ModelPart visor;
    private final ModelPart hatRim;
    private final ModelPart dartgun;
    private final ModelPart bowtie;

    public GolemAccessoriesModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        ModelPart golem = root.getChild("golem");
        fez = golem.getChild("fez");
        plate = golem.getChild("plate");
        plateLeft = golem.getChild("plate_left");
        plateRight = golem.getChild("plate_right");
        hat = golem.getChild("hat");
        glasses = golem.getChild("glasses");
        visor = golem.getChild("visor");
        hatRim = golem.getChild("hat_rim");
        dartgun = golem.getChild("dartgun");
        bowtie = golem.getChild("bowtie");
    }

    @Override
    public void setupAnim(GolemBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = ageInTicks - entity.tickCount;
        float yaw = netHeadYaw * ((float) Math.PI / 180.0F);
        float pitch = headPitch * ((float) Math.PI / 180.0F);
        for (ModelPart part : new ModelPart[] {fez, glasses, visor, hat, hatRim}) {
            part.yRot = yaw;
            part.xRot = pitch;
        }
        dartgun.xRot = GolemModel.armAngle(entity, true, dartgun.xRot, partialTick, limbSwing, limbSwingAmount);
        String decoration = entity.getDecoration();
        dartgun.visible = decoration.contains("R");
        fez.visible = decoration.contains("F");
        hat.visible = !fez.visible && decoration.contains("H");
        hatRim.visible = hat.visible;
        bowtie.visible = decoration.contains("B");
        plate.visible = !bowtie.visible && decoration.contains("P");
        plateLeft.visible = plate.visible;
        plateRight.visible = plate.visible;
        glasses.visible = decoration.contains("G");
        visor.visible = !glasses.visible && decoration.contains("V");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        poseStack.pushPose();
        poseStack.scale(0.4F, 0.4F, 0.4F);
        root.render(poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }
}
