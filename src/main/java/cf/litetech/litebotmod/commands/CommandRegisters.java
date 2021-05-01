package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.ResponseData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRegisters {
    private static List<ResponseData.CommandResponse> commandData;
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public static final HashMap<String, Command> REGISTERED_COMMANDS = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (commandData != null) {
            for (ResponseData.CommandResponse command : commandData) {
                Command INSTANCE = Command.register(dispatcher, command);
                REGISTERED_COMMANDS.put(command.name, INSTANCE);
            }
        }

        if (LiteBotMod.getServer() == null) return;

        for (ServerPlayerEntity player : LiteBotMod.getServer().getPlayerManager().getPlayerList()) {
            LiteBotMod.getServer().getPlayerManager().sendCommandTree(player);
        }
    }

    public static boolean containsCommand(ResponseData.CommandResponse command) {
        return commandData.stream().map(c -> c.name).collect(Collectors.toList()).contains(command.name);
    }

    public static void setDispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandRegisters.dispatcher = dispatcher;
    }

    public static void setCommandData(List<ResponseData.CommandResponse> commandData) {
        CommandRegisters.commandData = commandData;

        if (dispatcher != null) {
            register(dispatcher);
        }
    }
}
