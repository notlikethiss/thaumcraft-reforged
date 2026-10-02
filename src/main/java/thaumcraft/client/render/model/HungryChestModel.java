package thaumcraft.client.render.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class HungryChestModel extends Model<Float> {
    private final ModelPart lid;
    private final ModelPart knob;

    public HungryChestModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        lid = root.getChild("lid");
        knob = root.getChild("knob");
    }

    @Override
    public void setupAnim(Float openness) {
        super.setupAnim(openness);
        float eased = 1.0F - openness;
        eased = 1.0F - eased * eased * eased;
        lid.xRot = -(eased * (float) Math.PI / 2.0F);
        knob.xRot = lid.xRot;
    }
}
