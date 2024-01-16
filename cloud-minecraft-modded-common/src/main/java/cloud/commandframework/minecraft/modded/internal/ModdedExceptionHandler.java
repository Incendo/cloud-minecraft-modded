//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.minecraft.modded.internal;

import cloud.commandframework.CommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import cloud.commandframework.minecraft.modded.ModdedCommandContextKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@API(status = API.Status.INTERNAL)
@FunctionalInterface
@DefaultQualifier(NonNull.class)
public interface ModdedExceptionHandler<C, S extends SharedSuggestionProvider, T extends Throwable> extends ExceptionHandler<C, T> {

    Logger LOGGER = LogUtils.getLogger();
    Component NEWLINE = Component.literal("\n");
    String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    String MESSAGE_NO_PERMS =
        "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    @SuppressWarnings("unchecked")
    @Override
    default void handle(final ExceptionContext<C, T> context) throws Throwable {
        final S source = (S) context.context().get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER);
        this.handle(source, context.context().sender(), context.exception());
    }

    /**
     * Handles the exception.
     *
     * @param source    source
     * @param sender    sender
     * @param throwable throwable
     * @throws Throwable new throwable
     */
    void handle(S source, C sender, T throwable) throws Throwable;

    /**
     * Registers the default handlers.
     *
     * @param commandManager the command manager
     * @param sendError      error message sender
     * @param getName        name getter
     * @param <C>            command sender type
     * @param <S>            command source type
     */
    static <C, S extends SharedSuggestionProvider> void registerDefaults(
        final CommandManager<C> commandManager,
        final BiConsumer<S, Component> sendError,
        final Function<S, String> getName
    ) {
        final RegisterContext<C, S> ctx = new RegisterContext<>(commandManager);
        ctx.registerHandler(Throwable.class, (source, sender, throwable) -> {
            sendError.accept(source, decorateHoverStacktrace(
                commandManager,
                Component.literal(MESSAGE_INTERNAL_ERROR),
                throwable,
                sender
            ));
            LOGGER.warn("Error occurred while executing command for user {}:", getName.apply(source), throwable);
        });
        ctx.registerHandler(CommandExecutionException.class, (source, sender, throwable) -> {
            sendError.accept(source, decorateHoverStacktrace(
                commandManager,
                Component.literal(MESSAGE_INTERNAL_ERROR),
                throwable.getCause(),
                sender
            ));
            LOGGER.warn(
                "Error occurred while executing command for user {}:",
                getName.apply(source),
                throwable.getCause()
            );
        });
        ctx.registerHandler(ArgumentParseException.class, (source, sender, throwable) -> {
            if (throwable.getCause() instanceof CommandSyntaxException) {
                sendError.accept(source, Component.literal("Invalid Command Argument: ")
                    .append(Component.literal("")
                        .append(ComponentUtils
                            .fromMessage(((CommandSyntaxException) throwable.getCause()).getRawMessage()))
                        .withStyle(ChatFormatting.GRAY)));
            } else {
                sendError.accept(source, Component.literal("Invalid Command Argument: ")
                    .append(Component.literal(throwable.getCause().getMessage())
                        .withStyle(ChatFormatting.GRAY)));
            }
        });
        ctx.registerHandler(NoSuchCommandException.class, (source, sender, throwable) -> {
            sendError.accept(source, Component.literal(MESSAGE_UNKNOWN_COMMAND));
        });
        ctx.registerHandler(NoPermissionException.class, (source, sender, throwable) -> {
            sendError.accept(source, Component.literal(MESSAGE_NO_PERMS));
        });
        ctx.registerHandler(InvalidCommandSenderException.class, (source, sender, throwable) -> {
            sendError.accept(source, Component.literal(throwable.getMessage()));
        });
        ctx.registerHandler(InvalidSyntaxException.class, (source, sender, throwable) -> {
            sendError.accept(source, Component.literal("Invalid Command Syntax. Correct command syntax is: ")
                .append(Component.literal(String.format("/%s", throwable.correctSyntax()))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY))));
        });
    }

    @API(status = API.Status.INTERNAL)
    record RegisterContext<C, S extends SharedSuggestionProvider>(CommandManager<C> manager) {
        private <T extends Throwable> void registerHandler(
            final Class<T> exceptionType,
            final ModdedExceptionHandler<C, S, T> handler
        ) {
            this.manager.exceptionController().registerHandler(exceptionType, handler);
        }
    }

    private static <C> MutableComponent decorateHoverStacktrace(
        final CommandManager<C> manager,
        final MutableComponent input,
        final Throwable cause,
        final C sender
    ) {
        if (!manager.hasPermission(sender, "cloud.hover-stacktrace")) {
            return input;
        }

        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final String stackTrace = writer.toString().replace("\t", "    ");
        return input.withStyle(style -> style
            .withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.literal(stackTrace)
                    .append(NEWLINE)
                    .append(Component.literal("    Click to copy")
                        .withStyle(s2 -> s2.withColor(ChatFormatting.GRAY).withItalic(true)))
            ))
            .withClickEvent(new ClickEvent(
                ClickEvent.Action.COPY_TO_CLIPBOARD,
                stackTrace
            )));
    }
}
