package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.commands.CommandRegisters;

import java.util.ArrayList;
import java.util.List;

public class ResponseData {
    public MessageResponse messageData;
    public List<CommandResponse> commandData;
    public String afterInvoke;

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

        public static Argument getArgumentFromName(String name, String commandPath) {
            String[] commandNames = commandPath.split("\\.");

            for (String commandName : commandNames) {
                CommandResponse command = CommandRegisters.getCommandData(commandName);
                if (command == null) {
                    continue;
                }

                for (Argument arg : command.arguments) {
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
