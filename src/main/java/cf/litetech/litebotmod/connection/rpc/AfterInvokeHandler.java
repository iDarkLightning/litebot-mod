package cf.litetech.litebotmod.connection.rpc;

import cf.litetech.litebotmod.commands.ExecutingCommand;

import java.util.HashMap;

public class AfterInvokeHandler extends RPCHandler<AfterInvokeHandler, AfterInvokeHandler.AfterInvokeHandlerDeserializer> {
    public AfterInvokeHandler(String name, Class<AfterInvokeHandlerDeserializer> deserializerClass) {
        super(name, deserializerClass);
    }

    public static class AfterInvokeHandlerDeserializer {
        public String name;
        public HashMap<String, Object> args;
    }


    @Override
    public void handle(AfterInvokeHandlerDeserializer args) {
        ExecutingCommand.callAfterInvoke(args.name, args.args);
    }
}
