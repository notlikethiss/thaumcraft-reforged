package thaumcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModAttachments;
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
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
        );
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
            player.setData(ModAttachments.KNOWLEDGE, knowledge);
        }
        source.sendSuccess(() -> Component.literal("Granted all research to " + targets.size() + " player(s)"), true);
        return targets.size();
    }
}
