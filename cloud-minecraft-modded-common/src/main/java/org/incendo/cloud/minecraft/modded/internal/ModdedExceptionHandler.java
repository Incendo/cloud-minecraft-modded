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
package org.incendo.cloud.minecraft.modded.internal;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.minecraft.modded.ModdedCommandContextKeys;
import org.incendo.cloud.minecraft.modded.caption.MinecraftVariable;
import org.incendo.cloud.util.TypeUtils;
import org.slf4j.Logger;

@API(status = API.Status.INTERNAL)
@FunctionalInterface
@DefaultQualifier(NonNull.class)
public interface ModdedExceptionHandler<C, S extends SharedSuggestionProvider, T extends Throwable> extends ExceptionHandler<C, T> {

    Logger LOGGER = LogUtils.getLogger();
    Component NEWLINE = Component.literal("\n");

    @SuppressWarnings("unchecked")
    @Override
    default void handle(final ExceptionContext<C, T> context) throws Throwable {
        final S source = (S) context.context().get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER);
        this.handle(source, context);
    }

    /**
     * Handles the exception.
     *
     * @param source source
     * @param ctx    context
     * @throws Throwable new throwable
     */
    void handle(S source, ExceptionContext<C, T> ctx) throws Throwable;

    /**
     * Registers the default handlers.
     *
     * @param commandManager   the command manager
     * @param captionFormatter caption formatter
     * @param <M>            command manager type
     * @param <C>            command sender type
     */
    static <C, M extends CommandManager<C> & BrigadierManagerHolder<C, CommandSourceStack>> void registerDefaults(
        final M commandManager,
        final CaptionFormatter<C, Component> captionFormatter
    ) {
        registerDefaults(commandManager, CommandSourceStack::sendFailure, CommandSourceStack::getTextName, captionFormatter);
    }

    /**
     * Registers the default handlers.
     *
     * @param commandManager   the command manager
     * @param sendError        error message sender
     * @param getName          name getter
     * @param captionFormatter caption formatter
     * @param <M>              command manager type
     * @param <C>              command sender type
     * @param <S>              command source type
     */
    static <C, S extends SharedSuggestionProvider,
            M extends CommandManager<C> & BrigadierManagerHolder<C, S>> void registerDefaults(
            final M commandManager,
        final BiConsumer<S, Component> sendError,
        final Function<S, String> getName,
        final CaptionFormatter<C, Component> captionFormatter
    ) {
        final RegisterContext<C, S> ctx = new RegisterContext<>(commandManager);
        ctx.registerHandler(Throwable.class, (source, exceptionContext) -> {
            sendError.accept(source, decorateHoverStacktrace(
                commandManager,
                exceptionContext.context().formatCaption(captionFormatter, StandardCaptionKeys.EXCEPTION_UNEXPECTED),
                exceptionContext.exception(),
                exceptionContext.context().sender()
            ));
            LOGGER.warn("Error occurred while executing command for user {}", getName.apply(source), exceptionContext.exception());
        });
        ctx.registerHandler(CommandExecutionException.class, (source, exceptionContext) -> {
            sendError.accept(source, decorateHoverStacktrace(
                commandManager,
                exceptionContext.context().formatCaption(captionFormatter, StandardCaptionKeys.EXCEPTION_UNEXPECTED),
                exceptionContext.exception().getCause(),
                exceptionContext.context().sender()
            ));
            LOGGER.warn(
                "Error occurred while executing command for user {}",
                getName.apply(source),
                exceptionContext.exception().getCause()
            );
        });
        ctx.registerHandler(ArgumentParseException.class, (source, exceptionContext) -> {
            final Component msg;
            if (exceptionContext.exception().getCause() instanceof CommandSyntaxException cse) {
                msg = ComponentUtils.fromMessage(cse.getRawMessage());
            } else if (exceptionContext.exception().getCause() instanceof ParserException parserException) {
                msg = parserException.formatCaption(captionFormatter);
            } else {
                msg = Component.literal(exceptionContext.exception().getCause().getMessage());
            }
            sendError.accept(source, exceptionContext.context().formatCaption(
                captionFormatter,
                StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                MinecraftVariable.of("cause", Component.literal("")
                    .append(ComponentUtils.fromMessage(msg))
                    .withStyle(ChatFormatting.GRAY))
            ));
        });
        ctx.registerHandler(NoSuchCommandException.class, (source, exceptionContext) -> {
            sendError.accept(source, exceptionContext.context().formatCaption(
                captionFormatter,
                StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND
            ));
        });
        ctx.registerHandler(NoPermissionException.class, (source, exceptionContext) -> {
            sendError.accept(source, exceptionContext.context().formatCaption(
                captionFormatter,
                StandardCaptionKeys.EXCEPTION_NO_PERMISSION
            ));
        });
        ctx.registerHandler(InvalidCommandSenderException.class, (source, exceptionContext) -> {
            final boolean multiple = exceptionContext.exception().requiredSenderTypes().size() > 1;
            final String expected = multiple
                ? exceptionContext.exception().requiredSenderTypes().stream().map(TypeUtils::simpleName).collect(Collectors.joining(", "))
                : TypeUtils.simpleName(exceptionContext.exception().requiredSenderTypes().iterator().next());
            sendError.accept(source, exceptionContext.context().formatCaption(
                captionFormatter,
                multiple ? StandardCaptionKeys.EXCEPTION_INVALID_SENDER_LIST : StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                CaptionVariable.of("actual", exceptionContext.context().sender().getClass().getSimpleName()),
                CaptionVariable.of("expected", expected)
            ));
        });
        ctx.registerHandler(InvalidSyntaxException.class, (source, exceptionContext) -> {
            sendError.accept(source, exceptionContext.context().formatCaption(
                captionFormatter,
                StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                MinecraftVariable.of("syntax", Component.literal(String.format("/%s", exceptionContext.exception().correctSyntax()))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
            ));
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

    private static <C> Component decorateHoverStacktrace(
        final CommandManager<C> manager,
        final Component input,
        final Throwable cause,
        final C sender
    ) {
        if (!manager.hasPermission(sender, "cloud.hover-stacktrace")) {
            return input;
        }

        final MutableComponent result = input.copy();
        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final String stackTrace = writer.toString().replace("\t", "    ");
        return result.withStyle(style -> style
            .withHoverEvent(new HoverEvent.ShowText(
                Component.literal(stackTrace)
                    .append(NEWLINE)
                    .append(Component.literal("    Click to copy")
                        .withStyle(s2 -> s2.withColor(ChatFormatting.GRAY).withItalic(true)))
            ))
            .withClickEvent(new ClickEvent.CopyToClipboard(
                stackTrace
            )));
    }
}
