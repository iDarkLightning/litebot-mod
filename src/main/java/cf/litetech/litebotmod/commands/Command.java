package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.ResponseData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.ListIterator;

public class Command {
    private static final HashMap<String, ArgumentType<?>> ARGUMENT_TYPES = new HashMap<>();
    public ResponseData.CommandResponse data;

    private Command(ResponseData.CommandResponse data) {
        this.data = data;
    }

    static {
        ARGUMENT_TYPES.put("StringArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("SuggesterArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("StrictSuggesterArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("MessageArgument", MessageArgumentType.message());
        ARGUMENT_TYPES.put("IntegerArgument", IntegerArgumentType.integer());
        ARGUMENT_TYPES.put("BooleanArgument", BoolArgumentType.bool());
    }

    public static Command register(CommandDispatcher<ServerCommandSource> dispatcher, ResponseData.CommandResponse commandData) {
        Command COMMAND = new Command(commandData);
        dispatcher.register(COMMAND.buildCommand(commandData));

        return COMMAND;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(ResponseData.CommandResponse command) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.name);
        commandBuilder.requires(source -> (
                LiteBotMod.getConnection().isOpen() && command.OPLevel >= 0 && source.hasPermissionLevel(command.OPLevel)
        ));

        if (!command.arguments.isEmpty()) {
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

    private RequiredArgumentBuilder<ServerCommandSource, ?> buildArgs(
            ResponseData.CommandResponse command, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
            ListIterator<ResponseData.CommandResponse.Argument> argumentIterator) {
        ResponseData.CommandResponse.Argument arg = argumentIterator.next();
        RequiredArgumentBuilder<ServerCommandSource, ?> curBuilder = CommandManager.argument(arg.name,
                ARGUMENT_TYPES.get(arg.type));

        if (arg.optional) {
            argumentBuilder.executes(context -> executeCommand(command, context));
        }

        if (arg.type.contains("SuggesterArgument")) {
            curBuilder.suggests((context, builder) -> CommandSource.suggestMatching(
                    new ExecutingCommand(command, context).fetchSuggestions(arg), builder));
        }
        if (argumentIterator.hasNext()) {
            curBuilder.then(buildArgs(command, curBuilder, argumentIterator));
        } else {
            curBuilder.executes(context -> executeCommand(command, context));
        }

        return curBuilder;
    }

    private int executeCommand(ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        ExecutingCommand execCommand = new ExecutingCommand(command, context);
        return execCommand.resolve();
    }
}
