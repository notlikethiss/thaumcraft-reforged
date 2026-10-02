package thaumcraft.crafting;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;

public record ShapedWorkbenchRecipe(Kind kind, String key, int width, int height, TcIngredient[] items, TcResult output, int cost, AspectList aspects)
    implements WorkbenchRecipe {
    @Override
    public boolean shaped() {
        return true;
    }

    @Override
    public List<TcIngredient> ingredients() {
        return Arrays.asList(items);
    }

    @Override
    public boolean matchesGrid(WorkbenchGrid grid) {
        for (int x = 0; x <= 3 - width; x++) {
            for (int y = 0; y <= 3 - height; y++) {
                if (checkMatch(grid, x, y, true) || checkMatch(grid, x, y, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMatch(WorkbenchGrid grid, int offsetX, int offsetY, boolean mirrored) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int rx = x - offsetX;
                int ry = y - offsetY;
                TcIngredient expected = null;
                if (rx >= 0 && ry >= 0 && rx < width && ry < height) {
                    expected = mirrored ? items[width - rx - 1 + ry * width] : items[rx + ry * width];
                }
                ItemStack actual = grid.get(x, y);
                if (actual.isEmpty() && expected == null) {
                    continue;
                }
                if (actual.isEmpty() || expected == null || !expected.test(actual)) {
                    return false;
                }
            }
        }
        return true;
    }
}
