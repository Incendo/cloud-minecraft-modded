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
package org.incendo.cloud.sponge;

import io.leangen.geantyref.TypeToken;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.commands.CommandBuildContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.minecraft.modded.internal.ContextualArgumentTypeProvider;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.EitherParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

import static java.util.Objects.requireNonNull;

final class SpongeRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private SpongeCommandManager<C> commandManager;
    private final Set<org.incendo.cloud.Command<C>> registeredCommands = new HashSet<>();

    SpongeRegistrationHandler() {
    }

    private void handleRegistrationEvent(final RegisterCommandEvent<Command.Raw> event) {
        this.commandManager.registrationCalled();
        // Sponge's CommandsMixin exposes the native CommandBuildContext as the event's RegistryHolder.
        // Initialize before parsing/completions, and refresh cached types on every registration (including reloads).
        ContextualArgumentTypeProvider.withBuildContext(
            this.commandManager,
            (CommandBuildContext) event.registryHolder(),
            true,
            () -> {
                this.commandManager.registerParsers();
                this.initializeNativeArgumentTypes();
                for (final CommandNode<C> node : this.commandManager.commandTree().rootNodes()) {
                    this.registerCommand(event, requireNonNull(node.component()));
                }
            }
        );
    }

    private void registerCommand(final RegisterCommandEvent<Command.Raw> event, final CommandComponent<C> rootLiteral) {
        final String label = rootLiteral.name();
        event.register(
            this.commandManager.owningPluginContainer(),
            new CloudSpongeCommand<>(label, this.commandManager),
            label,
            rootLiteral.alternativeAliases().toArray(new String[0])
        );
    }

    private void initializeNativeArgumentTypes() {
        for (final org.incendo.cloud.Command<C> registeredCommand : this.registeredCommands) {
            for (final CommandComponent<C> component : registeredCommand.components()) {
                if (component.type() == CommandComponent.ComponentType.LITERAL) {
                    continue;
                }
                for (final ArgumentParser<?, ?> parser : unwrap(component.parser())) {
                    if (parser instanceof WrappedBrigadierParser<?, ?> wrappedBrigadierParser) {
                        wrappedBrigadierParser.nativeArgumentType();
                    }
                }
            }
        }
    }

    void initialize(final @NonNull SpongeCommandManager<C> commandManager) {
        this.commandManager = commandManager;
        Sponge.eventManager().registerListener(
            EventListenerRegistration.builder(new TypeToken<RegisterCommandEvent<Command.Raw>>() {})
                .plugin(this.commandManager.owningPluginContainer())
                .listener(this::handleRegistrationEvent)
                .order(Order.DEFAULT)
                .build()
        );
    }

    @Override
    public boolean registerCommand(final org.incendo.cloud.@NonNull Command<C> command) {
        this.registeredCommands.add(command);
        return true;
    }

    private static Set<ArgumentParser<?, ?>> unwrap(final ArgumentParser<?, ?> parser) {
        final HashSet<ArgumentParser<?, ?>> parsers = new HashSet<>();
        unwrap(parsers, parser);
        return parsers;
    }

    private static void unwrap(
        final Set<ArgumentParser<?, ?>> parsers,
        final ArgumentParser<?, ?> parser
    ) {
        if (parser instanceof CommandFlagParser<?> flagParser) {
            for (final CommandFlag<?> flag : flagParser.flags()) {
                final CommandComponent<?> component = flag.commandComponent();
                if (component != null) {
                    unwrap(parsers, component.parser());
                }
            }
            return;
        }
        if (parser instanceof MappedArgumentParser<?, ?, ?> mapped) {
            unwrap(parsers, mapped.baseParser());
            return;
        }
        if (parser instanceof EitherParser<?, ?, ?> eitherParser) {
            unwrap(parsers, eitherParser.primary().parser());
            unwrap(parsers, eitherParser.fallback().parser());
            return;
        }
        if (parser instanceof AggregateParser<?, ?> aggregateParser) {
            for (final CommandComponent<?> component : aggregateParser.components()) {
                unwrap(parsers, component.parser());
            }
            return;
        }
        parsers.add(parser);
    }
}
