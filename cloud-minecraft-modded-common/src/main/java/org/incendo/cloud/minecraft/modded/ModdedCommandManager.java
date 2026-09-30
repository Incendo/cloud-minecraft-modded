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
package org.incendo.cloud.minecraft.modded;

import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;

@DefaultQualifier(NonNull.class)
public abstract class ModdedCommandManager<C, S extends SharedSuggestionProvider> extends CommandManager<C> implements
    BrigadierManagerHolder<C, S>, SenderMapperHolder<S, C> {

    /**
     * Create a new command manager instance.
     *
     * @param executionCoordinator       Execution coordinator instance. When choosing the appropriate coordinator for your
     *                                   project, be sure to consider any limitations noted by the platform documentation.
     * @param commandRegistrationHandler Command registration handler. This will get called every time a new command is
     *                                   registered to the command manager. This may be used to forward command registration
     *                                   to the platform.
     */
    protected ModdedCommandManager(final ExecutionCoordinator<C> executionCoordinator, final CommandRegistrationHandler<C> commandRegistrationHandler) {
        super(executionCoordinator, commandRegistrationHandler);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will always return true for {@link ModdedCommandManager}s.</p>
     *
     * @return {@code true}
     * @since 2.0.0
     */
    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    /**
     * Returns whether this is a client command manager.
     *
     * @return whether this is a client command manager
     * @since 2.0.0
     */
    public abstract boolean isClientCommandManager();
}
