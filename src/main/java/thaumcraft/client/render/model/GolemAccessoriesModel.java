package thaumcraft.client.render.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import thaumcraft.client.render.entity.GolemRenderState;

public class GolemAccessoriesModel extends EntityModel<GolemRenderState> {
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
        super(root, RenderTypes::entityTranslucent);
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
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);
        float yaw = state.yRot * ((float) Math.PI / 180.0F);
        float pitch = state.xRot * ((float) Math.PI / 180.0F);
        for (ModelPart part : new ModelPart[] {fez, glasses, visor, hat, hatRim}) {
            part.yRot = yaw;
            part.xRot = pitch;
        }
        dartgun.xRot = GolemModel.armAngle(state, true, dartgun.xRot);
        String decoration = state.decoration;
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
}
