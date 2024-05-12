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

import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

/**
 * A command manager for registering client-side commands.
 *
 * <p>All commands should be registered within mod initializers. Any registrations occurring after the first call to
 * {@link net.neoforged.neoforge.client.event.RegisterClientCommandsEvent} will be considered <em>unsafe</em>, and will only be permitted when the unsafe
 * registration manager option is enabled.</p>
 *
 * @param <C> the command sender type
 */
public final class NeoForgeClientCommandManager<C> extends NeoForgeCommandManager<C> {

    /**
     * Create a command manager using native source types.
     *
     * @param executionCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #NeoForgeClientCommandManager(ExecutionCoordinator, SenderMapper) for a more thorough explanation
     */
    public static NeoForgeClientCommandManager<CommandSourceStack> createNative(
        final ExecutionCoordinator<CommandSourceStack> executionCoordinator
    ) {
        return new NeoForgeClientCommandManager<>(executionCoordinator, SenderMapper.identity());
    }

    /**
     * Create a new command manager instance.
     *
     * @param executionCoordinator Execution coordinator instance.
     * @param senderMapper         Mapper between Minecraft's {@link CommandSourceStack} and the command sender type {@code C}.
     */
    public NeoForgeClientCommandManager(
        final ExecutionCoordinator<C> executionCoordinator,
        final SenderMapper<CommandSourceStack, C> senderMapper
    ) {
        super(
            executionCoordinator,
            senderMapper,
            new NeoForgeCommandRegistrationHandler.Client<>()
        );

        this.registerParsers();
    }

    private void registerParsers() {
    }

    /**
     * Check if a sender has a certain permission.
     *
     * <p>The implementation for client commands always returns true.</p>
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return whether the sender has the specified permission
     */
    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return true;
    }
}
