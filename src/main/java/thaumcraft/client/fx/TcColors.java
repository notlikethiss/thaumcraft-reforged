package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;

public final class TcColors {
    private TcColors() {
    }

    public static float[] typeColor(int type, RandomSource random) {
        return switch (type) {
            case 0 -> new float[]{0.75F + random.nextFloat() * 0.25F, 0.25F + random.nextFloat() * 0.25F, 0.75F + random.nextFloat() * 0.25F};
            case 1 -> new float[]{0.5F + random.nextFloat() * 0.3F, 0.5F + random.nextFloat() * 0.3F, 0.2F};
            case 2 -> new float[]{0.2F, 0.2F, 0.7F + random.nextFloat() * 0.3F};
            case 3 -> new float[]{0.2F, 0.7F + random.nextFloat() * 0.3F, 0.2F};
            case 4 -> new float[]{0.7F + random.nextFloat() * 0.3F, 0.2F, 0.2F};
            case 5 -> new float[]{random.nextFloat() * 0.1F, random.nextFloat() * 0.1F, random.nextFloat() * 0.1F};
            case 6 -> new float[]{0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F};
            case 7 -> new float[]{0.2F, 0.5F + random.nextFloat() * 0.3F, 0.6F + random.nextFloat() * 0.3F};
            default -> new float[]{1.0F, 0.0F, 0.0F};
        };
    }
}
