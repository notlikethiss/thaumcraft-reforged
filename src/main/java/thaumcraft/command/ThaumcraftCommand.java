package thaumcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraManager;
import thaumcraft.aura.NodeType;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchNoteData;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchManager;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ThaumcraftCommand {
    private static final DynamicCommandExceptionType UNKNOWN_RESEARCH = new DynamicCommandExceptionType(
        key -> Component.literal("Unknown research: " + key)
    );

    private ThaumcraftCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal(Thaumcraft.MODID)
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("research")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.literal("all")
                                        .executes(context -> grantAll(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                                )
                                .then(
                                    Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ResearchList.RESEARCH.keySet(), builder))
                                        .executes(
                                            context -> grant(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                StringArgumentType.getString(context, "key")
                                            )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("note")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ResearchList.RESEARCH.keySet(), builder))
                                        .executes(
                                            context -> giveNote(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                StringArgumentType.getString(context, "key"),
                                                0.0F
                                            )
                                        )
                                        .then(
                                            Commands.argument("progress", FloatArgumentType.floatArg(0.0F, 1.0F))
                                                .executes(
                                                    context -> giveNote(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "key"),
                                                        FloatArgumentType.getFloat(context, "progress")
                                                    )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("node")
                        .then(
                            Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("level", IntegerArgumentType.integer(1, 1000))
                                        .executes(
                                            context -> createNode(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                IntegerArgumentType.getInteger(context, "level")
                                            )
                                        )
                                )
                        )
                )
        );
    }

    private static int createNode(CommandSourceStack source, BlockPos pos, int level) {
        int key = AuraManager.registerAuraNode(source.getLevel(), level, NodeType.NORMAL, pos);
        source.sendSuccess(() -> Component.literal("Created aura node " + key + " at " + pos.toShortString()), true);
        return key;
    }

    private static int giveNote(CommandSourceStack source, Collection<ServerPlayer> targets, String key, float progress) throws CommandSyntaxException {
        ResearchItem research = ResearchList.getResearch(key);
        if (research == null) {
            throw UNKNOWN_RESEARCH.create(key);
        }
        for (ServerPlayer player : targets) {
            ItemStack note = ResearchManager.createNote(new ItemStack(ModItems.RESEARCH_NOTES.get()), key);
            ResearchNoteData data = ResearchManager.getData(note);
            for (int i = 0; i < data.tags.length; i++) {
                data.progress[i] = Math.round(research.tags.getAmount(data.tags[i]) * progress);
            }
            ResearchManager.updateData(note, data);
            if (data.getTotalProgress() == 1.0F) {
                note = ResearchManager.toDiscovery(note);
            }
            if (!player.getInventory().add(note)) {
                player.drop(note, false);
            }
        }
        source.sendSuccess(() -> Component.literal("Gave research note " + key + " to " + targets.size() + " player(s)"), true);
        return targets.size();
    }

    private static int grant(CommandSourceStack source, Collection<ServerPlayer> targets, String key) throws CommandSyntaxException {
        if (ResearchList.getResearch(key) == null) {
            throw UNKNOWN_RESEARCH.create(key);
        }
        for (ServerPlayer player : targets) {
            ResearchManager.completeResearch(player, key);
        }
        source.sendSuccess(() -> Component.literal("Granted research " + key + " to " + targets.size() + " player(s)"), true);
        return targets.size();
    }

    private static int grantAll(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = ResearchManager.knowledge(player);
            for (ResearchItem research : ResearchList.RESEARCH.values()) {
                knowledge.complete(research.key);
            }
            ResearchManager.saveKnowledge(player, knowledge);
        }
        source.sendSuccess(() -> Component.literal("Granted all research to " + targets.size() + " player(s)"), true);
        return targets.size();
    }
}
