package thaumcraft.fx;

public final class Fx {
    private static FxProxy proxy = new FxProxy() {
    };

    private Fx() {
    }

    public static FxProxy get() {
        return proxy;
    }

    public static void set(FxProxy value) {
        proxy = value;
    }
}
