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
package cloud.commandframework.neoforge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minecraft.modded.internal.ModdedParserMappings;
import cloud.commandframework.minecraft.modded.internal.ModdedPreprocessor;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public abstract class NeoForgeCommandManager<C> extends CommandManager<C>
    implements BrigadierManagerHolder<C, CommandSourceStack>, SenderMapperHolder<CommandSourceStack, C> {

    static final Set<NeoForgeCommandManager<?>> INSTANCES = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NEWLINE = Component.literal("\n");
    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
        "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final SenderMapper<CommandSourceStack, C> senderMapper;
    private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    protected NeoForgeCommandManager(
        final ExecutionCoordinator<C> executionCoordinator,
        final SenderMapper<CommandSourceStack, C> senderMapper,
        final NeoForgeCommandRegistrationHandler<C> registrationHandler,
        final Supplier<CommandSourceStack> dummyCommandSourceProvider
    ) {
        super(executionCoordinator, registrationHandler);
        INSTANCES.add(this);
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);
        this.brigadierManager = new CloudBrigadierManager<>(this, () -> new CommandContext<>(
            this.senderMapper.map(dummyCommandSourceProvider.get()),
            this
        ), senderMapper);
        this.registerCommandPreProcessor(new NeoForgeCommandPreprocessor<>(this));
        this.registerDefaultExceptionHandlers();
        registrationHandler.initialize(this);
        this.registerCommandPreProcessor(new ModdedPreprocessor<>(senderMapper));

        ModdedParserMappings.register(this, this.brigadierManager);
    }

    @Override
    public final SenderMapper<CommandSourceStack, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    @Override
    public final CloudBrigadierManager<C, CommandSourceStack> brigadierManager() {
        return this.brigadierManager;
    }

    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    final void registrationCalled() {
        this.lockRegistration();
    }

    private void registerDefaultExceptionHandlers() {
        final BiConsumer<CommandSourceStack, Component> sendError = CommandSourceStack::sendFailure;
        final Function<CommandSourceStack, String> getName = CommandSourceStack::getTextName;

        this.registerHandler(Throwable.class, (source, sender, throwable) -> {
            sendError.accept(source, this.decorateHoverStacktrace(
                Component.literal(MESSAGE_INTERNAL_ERROR),
                throwable,
                sender
            ));
            LOGGER.warn("Error occurred while executing command for user {}:", getName.apply(source), throwable);
        });
        this.registerHandler(CommandExecutionException.class, (source, sender, throwable) -> {
            sendError.accept(source, this.decorateHoverStacktrace(
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
        this.registerHandler(ArgumentParseException.class, (source, sender, throwable) -> {
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
        this.registerHandler(NoSuchCommandException.class, (source, sender, throwable) ->
            sendError.accept(source, Component.literal(MESSAGE_UNKNOWN_COMMAND)));
        this.registerHandler(NoPermissionException.class, (source, sender, throwable) ->
            sendError.accept(source, Component.literal(MESSAGE_NO_PERMS)));
        this.registerHandler(InvalidCommandSenderException.class, (source, sender, throwable) ->
            sendError.accept(source, Component.literal(throwable.getMessage())));
        this.registerHandler(InvalidSyntaxException.class, (source, sender, throwable) ->
            sendError.accept(source, Component.literal("Invalid Command Syntax. Correct command syntax is: ")
                .append(Component.literal(String.format("/%s", throwable.correctSyntax()))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)))));
    }

    private <T extends Throwable> void registerHandler(final Class<T> exceptionType, final NeoForgeExceptionHandler<C, T> handler) {
        this.exceptionController().registerHandler(exceptionType, handler);
    }

    private MutableComponent decorateHoverStacktrace(final MutableComponent input, final Throwable cause, final C sender) {
        if (!this.hasPermission(sender, "cloud.hover-stacktrace")) {
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

    @FunctionalInterface
    private interface NeoForgeExceptionHandler<C, T extends Throwable> extends ExceptionHandler<C, T> {

        @Override
        default void handle(final ExceptionContext<C, T> context) throws Throwable {
            final CommandSourceStack commandSourceStack = context.context().get(NeoForgeCommandContextKeys.NATIVE_COMMAND_SOURCE);
            this.handle(commandSourceStack, context.context().sender(), context.exception());
        }

        void handle(CommandSourceStack source, C sender, T throwable);
    }
}
