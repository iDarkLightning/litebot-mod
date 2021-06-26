package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.commands.CommandRegisters;
import cf.litetech.litebotmod.connection.rpc.ServerCommandHandler;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResponseData {
    public String name;
    public JsonObject data;
    public MessageResponse messageData;
    public List<CommandResponse> commandData;
    public String afterInvoke;
    public HashMap<String, Object> args;
    public String id;
    public ArrayList<String> res;

    public static class MessageResponse {
        public String message;
        public String player;
        public boolean opOnly;
    }

    public static class CommandResponse {
        public String name;
        public int OPLevel;
        public ArrayList<CommandResponse> subs;
        public ArrayList<Argument> arguments;

        public static class Argument {
            public String name;
            public String type;
            public boolean optional;
        }

        public static ServerCommandHandler.ServerCommandHandlerDeserializer.Argument getArgumentFromName(String name, String commandPath) {
            String[] commandNames = commandPath.split("\\.");

            for (String commandName : commandNames) {
                ServerCommandHandler.ServerCommandHandlerDeserializer command = CommandRegisters.getCommandData(commandName);
                if (command == null) {
                    continue;
                }

                for (ServerCommandHandler.ServerCommandHandlerDeserializer.Argument arg : command.arguments) {
                    if (arg.name.equals(name)) {
                        return arg;
                    }
                }
            }

            return null;
        }

        public Argument getArgumentFromName(String name) {
            for (Argument arg : arguments) {
                if (arg.name.equals(name)) {
                    return arg;
                }
            }

            return null;
        }



    }
}
