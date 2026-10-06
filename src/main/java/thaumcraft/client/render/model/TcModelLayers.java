package thaumcraft.client.render.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
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
    public static final ModelLayerLocation CRYSTAL = layer("crystal");
    public static final ModelLayerLocation CUBE = layer("cube");
    public static final ModelLayerLocation BORE = layer("bore");
    public static final ModelLayerLocation BORE_BASE = layer("bore_base");
    public static final ModelLayerLocation BRAINY_ZOMBIE = layer("brainy_zombie");
    public static final ModelLayerLocation FIRE_BAT = layer("fire_bat");
    public static final ModelLayerLocation GOLEM = layer("golem");
    public static final ModelLayerLocation GOLEM_ADVANCED = layer("golem_advanced");
    public static final ModelLayerLocation GOLEM_MARKER = layer("golem_marker");
    public static final ModelLayerLocation GOLEM_ACCESSORIES = layer("golem_accessories");

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
        event.registerLayerDefinition(CRYSTAL, TcModelLayers::crystal);
        event.registerLayerDefinition(CUBE, TcModelLayers::cube);
        event.registerLayerDefinition(BORE, TcModelLayers::bore);
        event.registerLayerDefinition(BORE_BASE, TcModelLayers::boreBase);
        event.registerLayerDefinition(BRAINY_ZOMBIE, TcModelLayers::legacyHumanoid);
        event.registerLayerDefinition(FIRE_BAT, TcModelLayers::fireBat);
        event.registerLayerDefinition(GOLEM, TcModelLayers::golem);
        event.registerLayerDefinition(GOLEM_ADVANCED, TcModelLayers::golemAdvanced);
        event.registerLayerDefinition(GOLEM_MARKER, TcModelLayers::golemMarker);
        event.registerLayerDefinition(GOLEM_ACCESSORIES, TcModelLayers::golemAccessories);
    }

    private static LayerDefinition jar() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition jarBrine() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("brine", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition brain() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 12.0F, 10.0F, 16.0F), PartPose.offset(-6.0F, 8.0F, -8.0F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, 0.0F, 0.0F, 8.0F, 3.0F, 7.0F), PartPose.offset(-4.0F, 18.0F, 0.0F));
        root.addOrReplaceChild(
            "shape3",
            CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, 0.4089647F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition bellows() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("middle_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("top_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(48, 0).addBox(-10.0F, -12.03333F, -10.0F, 20.0F, 24.0F, 20.0F), PartPose.offset(0.0F, 0.5F, 0.0F));
        root.addOrReplaceChild("nozzle", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 16.0F, 6.0F));
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

    private static LayerDefinition crystal() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
            "crystal",
            CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(0.0F, 32.0F, 0.0F, 0.7071F, 0.0F, 0.7071F)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition cube() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(8.0F, 8.0F, 8.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void box(PartDefinition root, String name, int u, int v, float x, float y, float z, float width, float height, float depth, PartPose pose) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth), pose);
    }

    private static LayerDefinition bore() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        box(root, "base", 0, 32, -6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F, PartPose.ZERO);
        box(root, "side1", 0, 0, -2.0F, 2.0F, -5.5F, 4.0F, 8.0F, 1.0F, PartPose.ZERO);
        box(root, "side2", 0, 0, -2.0F, 2.0F, 4.5F, 4.0F, 8.0F, 1.0F, PartPose.ZERO);
        box(root, "crossbar", 0, 48, -1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 12.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        box(root, "nozzle_front", 30, 14, 4.0F, -2.5F, -2.5F, 4.0F, 5.0F, 5.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        box(root, "nozzle_mid", 0, 14, -2.0F, -4.0F, -4.0F, 6.0F, 8.0F, 8.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        box(root, "knob", 66, 0, -2.0F, 12.0F, -2.0F, 4.0F, 4.0F, 4.0F, PartPose.ZERO);
        box(root, "cross1", 56, 16, -2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        box(root, "cross3", 56, 16, -2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, PartPose.ZERO);
        box(root, "cross2", 56, 24, -3.0F, 4.0F, -3.0F, 6.0F, 1.0F, 6.0F, PartPose.ZERO);
        box(root, "rod", 56, 0, -1.0F, 1.0F, -1.0F, 2.0F, 11.0F, 2.0F, PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition boreBase() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        box(root, "base1", 64, 24, -8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F, PartPose.ZERO);
        box(root, "base2", 64, 24, -8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F, PartPose.offset(0.0F, 14.0F, 0.0F));
        box(root, "pillar_mid", 84, 42, -2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, PartPose.offset(0.0F, 2.0F, 0.0F));
        box(root, "pillar2", 64, 42, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, PartPose.offset(-5.0F, 2.0F, -5.0F));
        box(root, "pillar3", 64, 42, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, PartPose.offset(-5.0F, 2.0F, 5.0F));
        box(root, "pillar4", 64, 42, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, PartPose.offset(5.0F, 2.0F, 5.0F));
        box(root, "pillar1", 64, 42, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, PartPose.offset(5.0F, 2.0F, -5.0F));
        box(root, "nozzle1", 106, 42, 2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        box(root, "nozzle2", 106, 51, 7.0F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition legacyHumanoid() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);
    }

    private static LayerDefinition fireBat() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F).texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10.0F, 6.0F, 1.0F),
            PartPose.ZERO
        );
        PartDefinition rightWing = body.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(42, 0).addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F),
            PartPose.ZERO
        );
        rightWing.addOrReplaceChild(
            "right_wing_tip",
            CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F),
            PartPose.offset(-12.0F, 1.0F, 1.5F)
        );
        PartDefinition leftWing = body.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F),
            PartPose.ZERO
        );
        leftWing.addOrReplaceChild(
            "left_wing_tip",
            CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F),
            PartPose.offset(12.0F, 1.0F, 1.5F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void golemPart(PartDefinition parent, String name, CubeListBuilder cubes, float x, float y, float z) {
        parent.addOrReplaceChild(name, cubes, PartPose.offset(x, y + 30.0F, z));
    }

    private static CubeListBuilder cubes(int u, int v, float x, float y, float z, float width, float height, float depth) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth);
    }

    private static CubeListBuilder mirrored(int u, int v, float x, float y, float z, float width, float height, float depth) {
        return CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, width, height, depth);
    }

    private static void golemLimbs(PartDefinition golem, String suffix, int armV, int legU) {
        golemPart(golem, "right_arm" + suffix, cubes(60, armV, -12.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "left_arm" + suffix, mirrored(60, armV, 8.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "right_leg" + suffix, cubes(legU, 0, -3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), -4.0F, 18.0F, 0.0F);
        golemPart(golem, "left_leg" + suffix, mirrored(legU, 0, -3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), 5.0F, 18.0F, 0.0F);
    }

    private static CubeListBuilder golemBody() {
        return CubeListBuilder.create()
            .texOffs(0, 40)
            .addBox(-8.0F, -2.0F, -6.0F, 16.0F, 12.0F, 11.0F)
            .texOffs(0, 70)
            .addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F));
    }

    private static LayerDefinition golem() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition golem = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(golem, "head", cubes(0, 0, -4.0F, -11.0F, -5.5F, 8.0F, 9.0F, 8.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "head_smart", cubes(0, 96, -4.0F, -13.0F, -5.5F, 8.0F, 11.0F, 8.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "head_observer", cubes(0, 17, -4.0F, -11.0F, -5.5F, 8.0F, 9.0F, 8.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "body", golemBody(), 0.0F, 0.0F, 0.0F);
        golemLimbs(golem, "", 21, 37);
        golemLimbs(golem, "_strong", 58, 60);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static LayerDefinition golemAdvanced() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition golem = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(golem, "head", cubes(64, 113, -4.0F, -9.0F, -5.5F, 8.0F, 7.0F, 8.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "head_brain", cubes(96, 118, -3.5F, -12.0F, -5.0F, 7.0F, 3.0F, 7.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "head_jar", cubes(96, 104, -4.0F, -13.0F, -5.5F, 8.0F, 4.0F, 8.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "body", golemBody(), 0.0F, 0.0F, 0.0F);
        golemLimbs(golem, "", 21, 37);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static LayerDefinition golemMarker() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition golem = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(golem, "marker", cubes(0, 88, -2.0F, 3.0F, -7.0F, 4.0F, 6.0F, 1.0F), 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static LayerDefinition golemAccessories() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition golem = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(golem, "fez", cubes(0, 94, -4.5F, -15.0F, -6.0F, 9.0F, 7.0F, 9.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "plate", cubes(32, 40, -6.5F, -1.0F, -7.0F, 13.0F, 12.0F, 13.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "plate_left", cubes(0, 44, -8.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "plate_right", mirrored(0, 44, 5.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "hat", cubes(0, 110, -4.5F, -17.0F, -6.0F, 9.0F, 9.0F, 9.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "glasses", cubes(0, 80, -4.5F, -8.0F, -6.0F, 9.0F, 4.0F, 9.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "visor", cubes(0, 70, -5.0F, -8.0F, -6.0F, 10.0F, 5.0F, 5.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "hat_rim", cubes(36, 114, -6.5F, -9.0F, -8.0F, 13.0F, 1.0F, 13.0F), 0.0F, 0.0F, -2.0F);
        golemPart(golem, "dartgun", cubes(80, 80, 7.9F, 7.5F, -3.5F, 6.0F, 16.0F, 7.0F), 0.0F, 0.0F, 0.0F);
        golemPart(golem, "bowtie", cubes(0, 0, -8.5F, -2.0F, -6.5F, 17.0F, 4.0F, 12.0F), 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 128, 128);
    }
}
