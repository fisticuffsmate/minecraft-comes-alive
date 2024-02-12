package net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mca.Config;
import net.mca.MCA;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.entity.ai.GPT3;
import net.mca.network.s2c.OpenGuiRequest;
import net.mca.server.ServerInteractionManager;
import net.mca.server.world.data.PlayerSaveData;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Command {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(MCA.MOD_ID)
                .then(register("help", Command::displayHelp))
                .then(register("propose").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::propose)))
                .then(register("accept").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::accept)))
                .then(register("proposals", Command::displayProposal))
                .then(register("procreate", Command::procreate))
                .then(register("separate", Command::separate))
                .then(register("reject").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::reject)))
                .then(register("editor", Command::editor))
                .then(register("destiny", Command::destiny))
                .then(register("mail", Command::mail))
                .then(register("verify").then(CommandManager.argument("email", StringArgumentType.greedyString()).executes(Command::verify)))
                .then(register("chatAI")
                        .executes(Command::chatAIHelp)
                        .then(CommandManager.argument("model", StringArgumentType.greedyString())
                                .executes(c -> Command.chatAI(c.getArgument("model", String.class), (new Config()).villagerChatAIEndpoint, ""))
                                .then(CommandManager.argument("endpoint", StringArgumentType.greedyString())
                                        .executes(c -> Command.chatAI(c.getArgument("model", String.class), c.getArgument("endpoint", String.class), ""))
                                        .then(CommandManager.argument("token", StringArgumentType.greedyString())
                                                .executes(c -> Command.chatAI(c.getArgument("model", String.class), c.getArgument("endpoint", String.class), c.getArgument("token", String.class)))))))
        );
    }

    private static int chatAIHelp(CommandContext<ServerCommandSource> context) {
        MutableText styled = (Text.translatable("mca.ai_help")).styled(s -> s
                .withColor(Formatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Luke100000/minecraft-comes-alive/wiki/GPT3-based-conversations")));
        sendMessage(context, styled);
        return 0;
    }

    private static int chatAI(String model, String endpoint, String token) {
        Config.getInstance().villagerChatAIModel = model;
        Config.getInstance().villagerChatAIEndpoint = endpoint;
        Config.getInstance().villagerChatAIToken = token;
        Config.getInstance().save();
        return 0;
    }

    private static int ttsEnable(CommandContext<ServerCommandSource> ctx) {
        Config.getInstance().enableOnlineTTS = BoolArgumentType.getBool(ctx, "enabled");
        Config.getInstance().save();
        return 0;
    }

    private static int ttsLanguage(CommandContext<ServerCommandSource> ctx) {
        Set<String> languages = Set.of("en", "es", "fr", "de", "it", "pt", "pl", "tr", "ru", "nl", "cs", "ar", "zh-cn", "hu", "ko", "ja", "hi");
        String language = ctx.getArgument("language", String.class);
        if (languages.contains(language)) {
            Config.getInstance().onlineTTSLanguage = language;
            Config.getInstance().save();
            return 0;
        } else {
            sendMessage(ctx, "Choose one of: " + String.join(", ", languages));
            return 1;
        }
    }

    private static boolean couldBePersonalityRelated(String phrase) {
        for (Personality value : Personality.values()) {
            if (phrase.contains(value.name().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static int ttsScan(CommandContext<ServerCommandSource> ctx) {
        for (Map.Entry<String, String> text : MCA.translations.entrySet()) {
            String key = text.getKey();
            if ((key.contains("dialogue.") || key.contains("interaction.") || key.contains("villager.")) && !couldBePersonalityRelated(key)) {
                String hash = OnlineSpeechManager.INSTANCE.getHash(text.getValue());
                String language = Config.getInstance().onlineTTSLanguage;
                CompletableFuture.runAsync(() -> {
                    OnlineSpeechManager.INSTANCE.downloadAudio(language, "male_" + (SpeechManager.TOTAL_VOICES - 1), text.getValue(), hash);
                });
            }
        }

        return 0;
    }

    private static int editor(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            return 1;
        }
    private static int editor(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ctx.getSource().hasPermissionLevel(2) || Config.getInstance().allowFullPlayerEditor) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, ctx.getSource().getPlayer()), ctx.getSource().getPlayer());
            return 0;
        } else if (Config.getInstance().allowLimitedPlayerEditor) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.LIMITED_VILLAGER_EDITOR, ctx.getSource().getPlayer()), ctx.getSource().getPlayer());
            return 0;
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_permission").formatted(Formatting.RED), Util.NIL_UUID);
            return 1;
        }
    }

    private static int destiny(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ctx.getSource().hasPermissionLevel(2) || Config.getInstance().allowDestinyCommandOnce) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (!PlayerSaveData.get(player).isEntityDataSet() || Config.getInstance().allowDestinyCommandMoreThanOnce) {
                ServerInteractionManager.launchDestiny(player);
                return 0;
            } else {
                ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.only_one_destiny").formatted(Formatting.RED), Util.NIL_UUID);
                return 1;
            }
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_permission").formatted(Formatting.RED), Util.NIL_UUID);
            return 1;
        }
    }

    private static int mail(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        PlayerSaveData data = PlayerSaveData.get(player);
        if (data.hasMail()) {
            while (data.hasMail()) {
                player.getInventory().offerOrDrop(data.getMail());
            }
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_mail"), Util.NIL_UUID);
        }
        return 0;
    }

    private static int verify(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        CompletableFuture.runAsync(() -> {
            // build http request
            Map<String, String> params = new HashMap<>();
            params.put("email", StringArgumentType.getString(ctx, "email"));
            params.put("player", player.getName().asString());

            // encode and create url
            String encodedURL = params.keySet().stream()
                    .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&", Config.getInstance().villagerChatAIEndpoint.replace("v1/mca/chat", "v1/mca/verify") + "?", ""));

            GPT3.Answer request = GPT3.request(encodedURL);

            if (request.answer().equals("success")) {
                player.sendSystemMessage(new TranslatableText("command.verify.success").formatted(Formatting.GREEN), Util.NIL_UUID);
            } else if (request.answer().equals("failed")) {
                player.sendSystemMessage(new TranslatableText("command.verify.failed").formatted(Formatting.RED), Util.NIL_UUID);
            } else {
                player.sendSystemMessage(new TranslatableText("command.verify.crashed").formatted(Formatting.RED), Util.NIL_UUID);
            }
        });
        return 0;
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        sendMessage(ctx.getSource().getPlayer(), Formatting.DARK_RED + "--- " + Formatting.GOLD + "PLAYER COMMANDS" + Formatting.DARK_RED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca editor" + Formatting.GOLD + " - Choose your genetics and stuff.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca propose <PlayerName>" + Formatting.GOLD + " - Proposes marriage to the given player.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca proposals " + Formatting.GOLD + " - Shows all active proposals.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca accept <PlayerName>" + Formatting.GOLD + " - Accepts the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca reject <PlayerName>" + Formatting.GOLD + " - Rejects the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca procreate " + Formatting.GOLD + " - Starts procreation.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca separate " + Formatting.GOLD + " - Ends your marriage.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.DARK_RED + "--- " + Formatting.GOLD + "GLOBAL COMMANDS" + Formatting.DARK_RED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca help " + Formatting.GOLD + " - Shows this list of commands.");
        return 0;
    }

    private static int propose(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().sendProposal(ctx.getSource().getPlayer(), target);

        return 0;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().acceptProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }

    private static int displayProposal(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().listProposals(ctx.getSource().getPlayer());

        return 0;
    }

    private static int procreate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().procreate(ctx.getSource().getPlayer());
        return 0;
    }

    private static int separate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().endMarriage(ctx.getSource().getPlayer());
        return 0;
    }

    private static int reject(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().rejectProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }


    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, com.mojang.brigadier.Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0));
    }

    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(Formatting.GOLD + "[MCA] " + Formatting.RESET + message), Util.NIL_UUID);
    }
}
