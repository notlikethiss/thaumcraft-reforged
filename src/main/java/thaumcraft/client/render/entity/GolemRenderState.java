package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;

public class GolemRenderState extends LivingEntityRenderState {
    public final ItemStackRenderState carried = new ItemStackRenderState();
    public ResourceLocation texture;
    public int core;
    public int color;
    public String decoration = "";
    public float healthPercent = 1.0F;
    public boolean actionActive;
    public float actionTimer;
    public boolean leftArmActive;
    public float leftArm;
    public int healing;
    public boolean carrying;
    public boolean carriedBlock;
    public boolean carriedJar;
}
