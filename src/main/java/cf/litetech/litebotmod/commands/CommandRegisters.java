package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.ResponseData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class CommandRegisters {
    private static List<ResponseData.CommandResponse> commandData;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (commandData != null) {
            for (ResponseData.CommandResponse command : commandData) {
                dispatcher.register(buildCommand(command));
            }
        }
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildCommand(ResponseData.CommandResponse command) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.name);
        commandBuilder.requires(source -> command.OPLevel >= 0 && source.hasPermissionLevel(command.OPLevel));

        if (command.arguments != null) {
            commandBuilder.then(buildArgs(command, commandBuilder, command.arguments.listIterator()));
        } else {
            commandBuilder.executes(context -> executeCommand(command, context));
        }

        if (command.subs != null) {
            for (ResponseData.CommandResponse sub : command.subs) {
                commandBuilder.then(buildCommand(sub));
            }
        }

        return commandBuilder;
    }

    private static RequiredArgumentBuilder<ServerCommandSource, ?> buildArgs(
            ResponseData.CommandResponse command, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
            ListIterator<ResponseData.CommandResponse.Argument> argumentIterator) {
        ResponseData.CommandResponse.Argument arg = argumentIterator.next();
        RequiredArgumentBuilder<ServerCommandSource, ?> curBuilder = CommandManager.argument(arg.name, getArgumentType(arg.type));

        if (arg.optional) {
            argumentBuilder.executes(context -> executeCommand(command, context));
        }

        if (arg.type.equals("StringArgument") && arg.suggestions != null) {
            curBuilder.suggests((context, builder) -> CommandSource.suggestMatching(arg.suggestions, builder));
        }
        if (argumentIterator.hasNext()) {
            curBuilder.then(buildArgs(command, curBuilder, argumentIterator));
        }

        curBuilder.executes(context -> executeCommand(command, context));
        return curBuilder;
    }

    private static int executeCommand(ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<CommandNode<ServerCommandSource>> literalNodes = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                .map(ParsedCommandNode::getNode).collect(Collectors.toList());

        List<String> arguments = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(ArgumentCommandNode.class))
                .map(ParsedCommandNode::getNode).
                        map(n -> context.getArgument(n.getName(),
                                (command.getArgumentFromName(n.getName()).type).getClass())).collect(Collectors.toList());

        String commandName = literalNodes.get(literalNodes.size() - 1).getName();
        LiteBotMod.getBridge().sendCommand(commandName, context.getSource().getPlayer().getUuidAsString(), arguments);

        return 0;
    }

    private static ArgumentType<?> getArgumentType(String typeName) {
        switch (typeName) {
            case "StringArgument":
                return StringArgumentType.string();
            case "MessageArgument":
                return MessageArgumentType.message();
        }

        return StringArgumentType.string();
    }

    public static void setCommandData(List<ResponseData.CommandResponse> commandData) {
        CommandRegisters.commandData = commandData;
    }
}
