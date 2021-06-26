package cf.litetech.litebotmod.connection.rpc;

import cf.litetech.litebotmod.commands.CommandRegisters;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerCommandHandler extends
        RPCHandler<ServerCommandHandler, ServerCommandHandler.ServerCommandHandlerDeserializer[]>{

    public ServerCommandHandler(String name, Class<ServerCommandHandlerDeserializer[]> deserializerClass) {
        super(name, deserializerClass);
    }

    public static class ServerCommandHandlerDeserializer {
        public String name;
        public int OPLevel;
        public ArrayList<ServerCommandHandlerDeserializer> subs;
        public ArrayList<ServerCommandHandlerDeserializer.Argument> arguments;

        public static class Argument {
            public String name;
            public String type;
            public boolean optional;
        }

        public static Argument getArgumentFromName(String name, String commandPath) {
            String[] commandNames = commandPath.split("\\.");

            for (String commandName : commandNames) {
                ServerCommandHandlerDeserializer command = CommandRegisters.getCommandData(commandName);
                if (command == null) {
                    continue;
                }

                for (ServerCommandHandlerDeserializer.Argument arg : command.arguments) {
                    if (arg.name.equals(name)) {
                        return arg;
                    }
                }
            }

            return null;
        }

        public ServerCommandHandlerDeserializer.Argument getArgumentFromName(String name) {
            for (ServerCommandHandlerDeserializer.Argument arg : arguments) {
                if (arg.name.equals(name)) {
                    return arg;
                }
            }

            return null;
        }
    }

    @Override
    public void handle(ServerCommandHandlerDeserializer[] args) {
        CommandRegisters.setCommandData(Arrays.asList(args));
    }


}
