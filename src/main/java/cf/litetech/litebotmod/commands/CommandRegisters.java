package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.rpc.ServerCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRegisters {
    private static List<ServerCommandHandler.ServerCommandHandlerDeserializer> commandData;
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public static final HashMap<String, Command> REGISTERED_COMMANDS = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (commandData != null) {
            for (ServerCommandHandler.ServerCommandHandlerDeserializer command : commandData) {
                Command INSTANCE = Command.register(dispatcher, command);
                REGISTERED_COMMANDS.put(command.name, INSTANCE);
            }
        }
        if (LiteBotMod.getServer() == null) return;

        for (ServerPlayerEntity player : LiteBotMod.getServer().getPlayerManager().getPlayerList()) {
            LiteBotMod.getServer().getPlayerManager().sendCommandTree(player);
        } 
    }

    public static boolean containsCommand(ServerCommandHandler.ServerCommandHandlerDeserializer command) {
        return commandData.stream().map(c -> c.name).collect(Collectors.toList()).contains(command.name);
    }

    public static ServerCommandHandler.ServerCommandHandlerDeserializer getUpdatedData(ServerCommandHandler.ServerCommandHandlerDeserializer command) {
        return commandData.stream().filter(c -> c.name.equals(command.name)).collect(Collectors.toList()).get(0);
    }

    public static void setDispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandRegisters.dispatcher = dispatcher;
    }

    public static void setCommandData(List<ServerCommandHandler.ServerCommandHandlerDeserializer> commandData) {
        CommandRegisters.commandData = commandData;

        if (dispatcher != null) {
            register(dispatcher);
        }
    }

    public static ServerCommandHandler.ServerCommandHandlerDeserializer getCommandData(String name) {
        for (ServerCommandHandler.ServerCommandHandlerDeserializer command : commandData) {
            if (command.name.equals(name)) {
                return command;
            }
        }

        return null;
    }
}
