package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.ResponseData;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutingCommand {
    private static final SimpleCommandExceptionType INVALID_ARGUMENT = new SimpleCommandExceptionType(
            new LiteralText("This argument is not valid! You must choose an argument that has been suggested"));
    private final CommandContext<ServerCommandSource> context;
    private final ResponseData.CommandResponse command;
    private final List<String> argumentNames;
    private HashMap<String, Object> arguments;
    private HashMap<String, String> serializedArguments;
    public final String commandName;

    ExecutingCommand(ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        this.command = command;
        this.context = context;
        this.commandName = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                .map(n -> n.getNode().getName()).collect(Collectors.joining("."));

        this.argumentNames = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(ArgumentCommandNode.class))
                .map(ParsedCommandNode::getNode).
                        map(CommandNode::getName).collect(Collectors.toList());

        this.processArguments();
    }

    public int resolve() throws CommandSyntaxException {
        List<String> validatedArguments = this.validateArguments();

        LiteBotMod.getBridge().sendCommand(this.commandName,
                context.getSource().getPlayer().getUuidAsString(), validatedArguments);

        return 0;
    }

    public List<String> validateArguments() throws IllegalArgumentException, CommandSyntaxException {
        List<String> validatedArguments = new ArrayList<>();
        for (String argName : this.serializedArguments.keySet()) {
            ResponseData.CommandResponse.Argument arg = command.getArgumentFromName(argName);
            String serializedArgument = this.serializedArguments.get(arg.name);

            if (!arg.type.equals("StrictSuggesterArgument")) {
                validatedArguments.add(serializedArgument);
                continue;
            }

            List<String> suggestions = this.fetchSuggestions(arg);
            if (!suggestions.contains(serializedArgument)) {
                throw INVALID_ARGUMENT.create();
            }

            validatedArguments.add(serializedArgument);
        }

        return validatedArguments;
    }

    public List<String> fetchSuggestions(ResponseData.CommandResponse.Argument arg) throws CommandSyntaxException {
        return LiteBotMod.getBridge().fetchSuggestions(this.commandName, this.context.getSource().getPlayer().getUuidAsString(),
                arg.name, this.serializedArguments.values());
    }

    private void processArguments() throws CommandSyntaxException {
        HashMap<String, Object> rawArguments = new HashMap<>();
        HashMap<String, String> serializedArguments = new HashMap<>();
        for (String argName : this.argumentNames) {
            ResponseData.CommandResponse.Argument arg = this.command.getArgumentFromName(argName);

            switch (arg.type) {
                case "StringArgument":
                case "SuggesterArgument":
                case "StrictSuggesterArgument":
                    rawArguments.put(arg.name, StringArgumentType.getString(this.context, arg.name));
                    serializedArguments.put(arg.name, StringArgumentType.getString(this.context, arg.name));
                    break;
                case "MessageArgument":
                    rawArguments.put(arg.name, MessageArgumentType.getMessage(this.context, arg.name));
                    serializedArguments.put(arg.name, MessageArgumentType.getMessage(this.context, arg.name).getString());
                    break;
                case "IntegerArgument":
                    rawArguments.put(arg.name, IntegerArgumentType.getInteger(this.context, arg.name));
                    rawArguments.put(arg.name, Integer.toString(IntegerArgumentType.getInteger(this.context, arg.name)));
                    break;
                case "BooleanArgument":
                    rawArguments.put(arg.name, BoolArgumentType.getBool(this.context, arg.name));
                    rawArguments.put(arg.name, Boolean.toString(BoolArgumentType.getBool(this.context, arg.name)));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + arg.type);
            }
        }

        this.arguments = rawArguments;
        this.serializedArguments = serializedArguments;
    }
}
