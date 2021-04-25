package cf.litetech.litebotmod.connection;

public enum EventActions {
    AUTH("auth"),
    COMMAND("command"),
    SUGGESTER("suggester"),
    EVENT("event");

    public String val;

    EventActions(String val) {
        this.val = val;
    }

    public enum Events {
        ON_MESSAGE("on_message");

        public String val;

        Events(String val) {
            this.val = val;
        }
    }
}
