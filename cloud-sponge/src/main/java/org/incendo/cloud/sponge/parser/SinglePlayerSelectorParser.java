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
package org.incendo.cloud.sponge.parser;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.sponge.NodeSource;
import org.incendo.cloud.sponge.SpongeCommandContextKeys;
import org.incendo.cloud.sponge.data.SinglePlayerSelector;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

/**
 * Argument for selecting a single {@link Player} using a {@link Selector}.
 *
 * @param <C> command sender type
 */
public final class SinglePlayerSelectorParser<C> implements NodeSource,
    ArgumentParser.FutureArgumentParser<C, SinglePlayerSelector>, SuggestionProvider<C> {

    /**
     * Creates a new {@link SinglePlayerSelectorParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, SinglePlayerSelector> singlePlayerSelectorParser() {
        return ParserDescriptor.of(new SinglePlayerSelectorParser<>(), SinglePlayerSelector.class);
    }

    private final ArgumentParser<C, EntitySelector> nativeParser = new WrappedBrigadierParser<>(EntityArgument.player());

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull SinglePlayerSelector>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        final CommandInput originalInput = inputQueue.copy();
        return this.nativeParser.parseFuture(commandContext, inputQueue).thenApply(result -> {
            if (result.failure().isPresent()) {
                return ArgumentParseResult.failure(result.failure().get());
            }
            final String consumedInput = originalInput.difference(inputQueue);
            final EntitySelector parsed = result.parsedValue().get();
            final ServerPlayer player;
            try {
                // todo: a more proper fix then setting permission level 2
                player = (ServerPlayer) parsed.findSinglePlayer(
                    ((CommandSourceStack) commandContext.get(SpongeCommandContextKeys.COMMAND_CAUSE)).withPermission(2)
                );
            } catch (final CommandSyntaxException ex) {
                return ArgumentParseResult.failure(ex);
            }
            return ArgumentParseResult.success(new SinglePlayerSelectorImpl((Selector) parsed, consumedInput, player));
        });
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
        final @NonNull CommandContext<C> context,
        final @NonNull CommandInput input
    ) {
        return this.nativeParser.suggestionProvider().suggestionsFuture(context, input);
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.ENTITY.get().createNode().playersOnly().single();
    }


    private static final class SinglePlayerSelectorImpl implements SinglePlayerSelector {

        private final Selector selector;
        private final String inputString;
        private final ServerPlayer result;

        private SinglePlayerSelectorImpl(
            final Selector selector,
            final String inputString,
            final ServerPlayer result
        ) {
            this.selector = selector;
            this.inputString = inputString;
            this.result = result;
        }

        @Override
        public @NonNull Selector selector() {
            return this.selector;
        }

        @Override
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull ServerPlayer getSingle() {
            return this.result;
        }

    }

}