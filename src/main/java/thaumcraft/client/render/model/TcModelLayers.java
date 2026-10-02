package thaumcraft.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;

public final class TcModelLayers {
    public static final ModelLayerLocation JAR = layer("jar");
    public static final ModelLayerLocation JAR_BRINE = layer("jar_brine");
    public static final ModelLayerLocation BRAIN = layer("brain");
    public static final ModelLayerLocation BELLOWS = layer("bellows");
    public static final ModelLayerLocation HUNGRY_CHEST = layer("hungry_chest");

    private TcModelLayers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Thaumcraft.id(name), "main");
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(JAR, TcModelLayers::jar);
        event.registerLayerDefinition(JAR_BRINE, TcModelLayers::jarBrine);
        event.registerLayerDefinition(BRAIN, TcModelLayers::brain);
        event.registerLayerDefinition(BELLOWS, TcModelLayers::bellows);
        event.registerLayerDefinition(HUNGRY_CHEST, TcModelLayers::hungryChest);
    }

    private static LayerDefinition jar() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition jarBrine() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("brine", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition brain() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 12.0F, 10.0F, 16.0F), PartPose.offset(-6.0F, 8.0F, -8.0F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 8.0F, 3.0F, 7.0F), PartPose.offset(-4.0F, 18.0F, 0.0F));
        root.addOrReplaceChild(
            "shape3",
            CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, 0.4089647F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition bellows() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom_plank", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("middle_plank", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("top_plank", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-10.0F, -12.03333F, -10.0F, 20.0F, 24.0F, 20.0F), PartPose.offset(0.0F, 0.5F, 0.0F));
        root.addOrReplaceChild("nozzle", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 16.0F, 6.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition hungryChest() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(1.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(8.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(1.0F, 6.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
