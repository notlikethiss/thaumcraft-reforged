package thaumcraft.aspect;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import thaumcraft.registry.ModEnchantments;

public final class AspectHelper {
    private static final Map<TagKey<Block>, Integer> HARVEST_LEVELS = Map.of(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0,
        BlockTags.INCORRECT_FOR_GOLD_TOOL, 0,
        BlockTags.INCORRECT_FOR_STONE_TOOL, 1,
        BlockTags.INCORRECT_FOR_IRON_TOOL, 2,
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 3,
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4
    );

    private AspectHelper() {
    }

    public static @Nullable AspectList getObjectTags(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        AspectList aspects = AspectRegistry.lookup(stack.getItem()).orElse(null);
        if (stack.getItem() instanceof AspectProvidingItem provider) {
            aspects = provider.getStackAspects(stack, aspects);
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            aspects = applyPotion(stack, aspects == null ? new AspectList() : aspects);
        }
        return aspects == null ? null : aspects.cull(5);
    }

    public static @Nullable AspectList getObjectTagsWithBonus(ItemStack stack) {
        return getBonusTags(stack, getObjectTags(stack));
    }

    private static AspectList applyPotion(ItemStack stack, AspectList aspects) {
        aspects.merge(Aspect.WATER, 1);
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return aspects;
        }
        if (!stack.is(Items.POTION)) {
            aspects.merge(Aspect.DESTRUCTION, 2);
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            int level = effect.getAmplifier() + 1;
            aspects.merge(Aspect.MAGIC, level * 2);
            Holder<MobEffect> type = effect.getEffect();
            if (type.is(MobEffects.BLINDNESS)) {
                aspects.merge(Aspect.DARK, level * 3);
            } else if (type.is(MobEffects.CONFUSION)) {
                aspects.merge(Aspect.ELDRITCH, level * 3);
            } else if (type.is(MobEffects.DAMAGE_BOOST)) {
                aspects.merge(Aspect.WEAPON, level * 3);
            } else if (type.is(MobEffects.DIG_SLOWDOWN)) {
                aspects.merge(Aspect.TRAP, level * 3);
            } else if (type.is(MobEffects.DIG_SPEED)) {
                aspects.merge(Aspect.TOOL, level * 3);
            } else if (type.is(MobEffects.FIRE_RESISTANCE)) {
                aspects.merge(Aspect.ARMOR, level);
                aspects.merge(Aspect.FIRE, level * 2);
            } else if (type.is(MobEffects.HARM)) {
                aspects.merge(Aspect.DEATH, level * 3);
            } else if (type.is(MobEffects.HEAL)) {
                aspects.merge(Aspect.HEAL, level * 3);
            } else if (type.is(MobEffects.HUNGER)) {
                aspects.merge(Aspect.DEATH, level * 3);
            } else if (type.is(MobEffects.INVISIBILITY)) {
                aspects.merge(Aspect.VISION, level * 3);
            } else if (type.is(MobEffects.JUMP)) {
                aspects.merge(Aspect.FLIGHT, level * 3);
            } else if (type.is(MobEffects.MOVEMENT_SLOWDOWN)) {
                aspects.merge(Aspect.TRAP, level * 3);
            } else if (type.is(MobEffects.MOVEMENT_SPEED)) {
                aspects.merge(Aspect.MOTION, level * 3);
            } else if (type.is(MobEffects.NIGHT_VISION)) {
                aspects.merge(Aspect.VISION, level * 3);
            } else if (type.is(MobEffects.POISON)) {
                aspects.merge(Aspect.POISON, level * 3);
            } else if (type.is(MobEffects.REGENERATION)) {
                aspects.merge(Aspect.HEAL, level * 3);
            } else if (type.is(MobEffects.DAMAGE_RESISTANCE)) {
                aspects.merge(Aspect.ARMOR, level * 3);
            } else if (type.is(MobEffects.WATER_BREATHING)) {
                aspects.merge(Aspect.WIND, level * 3);
            } else if (type.is(MobEffects.WEAKNESS)) {
                aspects.merge(Aspect.DEATH, level * 3);
            }
        }
        return aspects;
    }

    public static AspectList getBonusTags(ItemStack stack, @Nullable AspectList source) {
        AspectList result = source == null ? new AspectList() : source.copy();
        if (stack.isEmpty()) {
            return result;
        }
        double armor = attributeTotal(stack, Attributes.ARMOR, EquipmentSlotGroup.ARMOR);
        if (armor > 0) {
            result.merge(Aspect.ARMOR, (int) armor + 1);
        } else if (stack.is(ItemTags.SWORDS)) {
            double damage = attributeTotal(stack, Attributes.ATTACK_DAMAGE, EquipmentSlotGroup.MAINHAND) + 1;
            result.merge(Aspect.WEAPON, (int) damage * 2 - 4);
        } else if (stack.getItem() instanceof BowItem) {
            result.merge(Aspect.WEAPON, 6).merge(Aspect.FLIGHT, 2);
        } else if (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS)) {
            harvestLevel(stack).ifPresent(level -> result.merge(Aspect.TOOL, (level + 1) * 2));
        } else if (stack.getItem() instanceof ShearsItem || stack.is(ItemTags.HOES)) {
            int durability = stack.getMaxDamage();
            if (durability <= 59) {
                result.merge(Aspect.TOOL, 1);
            } else if (durability <= 131) {
                result.merge(Aspect.TOOL, 2);
            } else if (durability <= 250) {
                result.merge(Aspect.TOOL, 4);
            } else {
                result.merge(Aspect.TOOL, 8);
            }
        }
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        if (!enchantments.isEmpty()) {
            int total = 0;
            for (var entry : enchantments.entrySet()) {
                int level = entry.getIntValue();
                applyEnchantment(entry.getKey(), level, result);
                total += level;
            }
            result.merge(Aspect.MAGIC, total * 2);
        }
        return result.cull(5);
    }

    private static void applyEnchantment(Holder<Enchantment> enchantment, int level, AspectList result) {
        if (is(enchantment, Enchantments.AQUA_AFFINITY)) {
            result.merge(Aspect.WATER, level * 2);
        } else if (is(enchantment, Enchantments.BANE_OF_ARTHROPODS)) {
            result.merge(Aspect.INSECT, level * 2);
        } else if (is(enchantment, Enchantments.BLAST_PROTECTION)) {
            result.merge(Aspect.ARMOR, level);
            result.merge(Aspect.DESTRUCTION, level);
        } else if (is(enchantment, Enchantments.EFFICIENCY)) {
            result.merge(Aspect.TOOL, level * 2);
        } else if (is(enchantment, Enchantments.FEATHER_FALLING)) {
            result.merge(Aspect.FLIGHT, level * 2);
        } else if (is(enchantment, Enchantments.FIRE_ASPECT)) {
            result.merge(Aspect.FIRE, level * 2);
        } else if (is(enchantment, Enchantments.FIRE_PROTECTION)) {
            result.merge(Aspect.ARMOR, level);
            result.merge(Aspect.FIRE, level);
        } else if (is(enchantment, Enchantments.FLAME)) {
            result.merge(Aspect.FIRE, level * 2);
        } else if (is(enchantment, Enchantments.FORTUNE)) {
            result.merge(Aspect.VALUABLE, level * 2);
        } else if (is(enchantment, Enchantments.INFINITY)) {
            result.merge(Aspect.CRAFT, level * 2);
        } else if (is(enchantment, Enchantments.KNOCKBACK)) {
            result.merge(Aspect.WIND, level * 2);
        } else if (is(enchantment, Enchantments.LOOTING)) {
            result.merge(Aspect.VALUABLE, level * 2);
        } else if (is(enchantment, Enchantments.POWER)) {
            result.merge(Aspect.WEAPON, level * 2);
        } else if (is(enchantment, Enchantments.PROJECTILE_PROTECTION)) {
            result.merge(Aspect.ARMOR, level);
            result.merge(Aspect.FLIGHT, level);
        } else if (is(enchantment, Enchantments.PROTECTION)) {
            result.merge(Aspect.ARMOR, level * 2);
        } else if (is(enchantment, Enchantments.PUNCH)) {
            result.merge(Aspect.WIND, level * 2);
        } else if (is(enchantment, Enchantments.RESPIRATION)) {
            result.merge(Aspect.WIND, level * 2);
        } else if (is(enchantment, Enchantments.SHARPNESS)) {
            result.merge(Aspect.WEAPON, level * 2);
        } else if (is(enchantment, Enchantments.SILK_TOUCH)) {
            result.merge(Aspect.EXCHANGE, level * 2);
        } else if (is(enchantment, Enchantments.THORNS)) {
            result.merge(Aspect.WEAPON, level);
            result.merge(Aspect.PLANT, level);
        } else if (is(enchantment, Enchantments.SMITE)) {
            result.merge(Aspect.DESTRUCTION, level * 2);
        } else if (is(enchantment, Enchantments.UNBREAKING)) {
            result.merge(Aspect.ROCK, level * 2);
        } else if (is(enchantment, ModEnchantments.CHARGING)) {
            result.merge(Aspect.EXCHANGE, level * 2);
        } else if (is(enchantment, ModEnchantments.FRUGAL)) {
            result.merge(Aspect.CONTROL, level * 2);
        } else if (is(enchantment, ModEnchantments.HASTE)) {
            result.merge(Aspect.MOTION, level * 2);
        } else if (is(enchantment, ModEnchantments.POTENCY)) {
            result.merge(Aspect.POWER, level * 2);
        } else if (is(enchantment, ModEnchantments.REPAIR)) {
            result.merge(Aspect.CRAFT, level);
            result.merge(Aspect.TOOL, level);
        } else if (is(enchantment, ModEnchantments.TREASURE)) {
            result.merge(Aspect.VALUABLE, level * 2);
        }
    }

    private static boolean is(Holder<Enchantment> enchantment, ResourceKey<Enchantment> key) {
        return enchantment.is(key);
    }

    private static double attributeTotal(ItemStack stack, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, EquipmentSlotGroup slot) {
        double[] total = {0};
        stack.getAttributeModifiers().forEach(slot, (holder, modifier) -> {
            if (holder.is(attribute)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    private static Optional<Integer> harvestLevel(ItemStack stack) {
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) {
            return Optional.empty();
        }
        for (Tool.Rule rule : tool.rules()) {
            if (rule.correctForDrops().isPresent() && !rule.correctForDrops().get()) {
                Optional<TagKey<Block>> tag = rule.blocks().unwrapKey();
                if (tag.isPresent() && HARVEST_LEVELS.containsKey(tag.get())) {
                    return Optional.of(HARVEST_LEVELS.get(tag.get()));
                }
            }
        }
        return Optional.of(0);
    }
}
