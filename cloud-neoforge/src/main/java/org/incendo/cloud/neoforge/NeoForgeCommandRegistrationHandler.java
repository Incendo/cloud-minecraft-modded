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
package org.incendo.cloud.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.minecraft.modded.ModdedCommandMetaKeys;
import org.incendo.cloud.minecraft.modded.internal.ContextualArgumentTypeProvider;

import static org.incendo.cloud.brigadier.util.BrigadierUtil.buildRedirect;

/**
 * A registration handler for NeoForge.
 *
 * <p>Subtypes exist for client and server commands.</p>
 *
 * @param <C> command sender type
 */
abstract class NeoForgeCommandRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private @MonotonicNonNull NeoForgeCommandManager<C> commandManager;

    void initialize(final NeoForgeCommandManager<C> manager) {
        this.commandManager = manager;
    }

    NeoForgeCommandManager<C> commandManager() {
        return this.commandManager;
    }

    @SuppressWarnings("unchecked")
    protected final void registerCommand(final Command<C> command, final CommandDispatcher<CommandSourceStack> dispatcher) {
        final RootCommandNode<CommandSourceStack> rootNode = dispatcher.getRoot();
        final CommandComponent<C> first = command.rootComponent();
        final CommandNode<CommandSourceStack> baseNode = this.commandManager()
            .brigadierManager()
            .literalBrigadierNodeFactory()
            .createNode(
                first.name(),
                command,
                new CloudBrigadierCommand<>(this.commandManager(), this.commandManager().brigadierManager())
            );

        rootNode.addChild(baseNode);

        for (final String alias : first.alternativeAliases()) {
            rootNode.addChild(buildRedirect(alias, baseNode));
        }
    }

    static class Client<C> extends NeoForgeCommandRegistrationHandler<C> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();
        private volatile boolean registerEventFired = false;

        @Override
        void initialize(final NeoForgeCommandManager<C> manager) {
            super.initialize(manager);
            NeoForge.EVENT_BUS.addListener(this::registerCommands);
            NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> this.registerEventFired = false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(final @NonNull Command<C> command) {
            this.registeredCommands.add(command);
            if (this.registerEventFired) {
                final ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection == null) {
                    throw new IllegalStateException("Expected connection to be present but it wasn't!");
                }
                final CommandDispatcher<CommandSourceStack> dispatcher = ClientCommandHandler.getDispatcher();
                if (dispatcher == null) {
                    throw new IllegalStateException("Expected an active dispatcher!");
                }
                ContextualArgumentTypeProvider.withBuildContext(
                    this.commandManager(),
                    CommandBuildContext.simple(connection.registryAccess(), connection.enabledFeatures()),
                    false,
                    () -> this.registerCommand(command, dispatcher)
                );
            }
            return true;
        }

        public void registerCommands(final RegisterClientCommandsEvent event) {
            this.registerEventFired = true;
            ContextualArgumentTypeProvider.withBuildContext(
                this.commandManager(),
                event.getBuildContext(),
                true,
                () -> {
                    for (final Command<C> command : this.registeredCommands) {
                        this.registerCommand(command, event.getDispatcher());
                    }
                }
            );
        }
    }

    static class Server<C> extends NeoForgeCommandRegistrationHandler<C> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();

        @Override
        void initialize(final NeoForgeCommandManager<C> manager) {
            super.initialize(manager);
            NeoForge.EVENT_BUS.addListener(this::registerAllCommands);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(final @NonNull Command<C> command) {
            return this.registeredCommands.add(command);
        }

        private void registerAllCommands(final RegisterCommandsEvent event) {
            this.commandManager().registrationCalled();
            ContextualArgumentTypeProvider.withBuildContext(
                this.commandManager(),
                event.getBuildContext(),
                true,
                () -> {
                    for (final Command<C> command : this.registeredCommands) {
                        /* Only register commands in the declared environment */
                        final Commands.CommandSelection env = command.commandMeta().getOrDefault(
                            ModdedCommandMetaKeys.REGISTRATION_ENVIRONMENT,
                            Commands.CommandSelection.ALL
                        );

                        if ((env == Commands.CommandSelection.INTEGRATED && !event.getCommandSelection().includeIntegrated)
                            || (env == Commands.CommandSelection.DEDICATED && !event.getCommandSelection().includeDedicated)) {
                            continue;
                        }
                        this.registerCommand(command, event.getDispatcher());
                    }
                }
            );
        }
    }
}
