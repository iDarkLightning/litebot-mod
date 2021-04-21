package cf.litetech.litebotmod.connection;

import java.util.ArrayList;
import java.util.List;

public class ResponseData {
    public MessageResponse messageData;
    public List<CommandResponse> commandData;

    public class MessageResponse {
        public String message;
        public String player;
        public boolean opOnly;
    }

    public class CommandResponse {
        public String name;
        public int OPLevel;
        public ArrayList<CommandResponse> subs;
        public ArrayList<Argument> arguments;

        public class Argument {
            public String name;
            public String type;
            public String[] suggestions;
            public boolean optional;
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
