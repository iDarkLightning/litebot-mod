package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.connection.ResponseData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.List;

public class CommandRegisters {
    private static List<ResponseData.CommandResponse> commandData;
    public static final HashMap<String, Command> REGISTERED_COMMANDS = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (commandData != null) {
            for (ResponseData.CommandResponse command : commandData) {
                Command INSTANCE = Command.register(dispatcher, command);
                REGISTERED_COMMANDS.put(command.name, INSTANCE);
            }
        }
    }

    public static void setCommandData(List<ResponseData.CommandResponse> commandData) {
        CommandRegisters.commandData = commandData;
    }

}
