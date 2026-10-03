package thaumcraft.item.golem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GolemDecorationItem extends Item {
    private final String letter;
    private final String key;

    public GolemDecorationItem(String letter, String key, Properties properties) {
        super(properties);
        this.letter = letter;
        this.key = key;
    }

    public String letter() {
        return letter;
    }

    public static Component decorationName(String key) {
        return Component.translatable("tc.thaumcraft.golem_decoration." + key);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("tc.thaumcraft.golem_decoration.named", Component.translatable("item.thaumcraft.golem_decoration"), decorationName(key));
    }
}
