package thaumcraft.entity.golem;

public interface MultiColorGolem {
    int getSlotColor(int slot);

    void setSlotColor(int slot, int color);

    default boolean hasAnyColor() {
        for (int slot = 0; slot < 6; slot++) {
            if (getSlotColor(slot) >= 0) {
                return true;
            }
        }
        return false;
    }
}
