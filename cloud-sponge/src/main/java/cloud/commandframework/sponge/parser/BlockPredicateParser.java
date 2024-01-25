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
package cloud.commandframework.sponge.parser;

import cloud.commandframework.sponge.NodeSource;
import cloud.commandframework.sponge.data.BlockPredicate;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.minecraft.modded.internal.ContextualArgumentTypeProvider;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.util.VecHelper;

/**
 * An argument for parsing {@link BlockPredicate BlockPredicates}.
 *
 * @param <C> command sender type
 */
public final class BlockPredicateParser<C> implements ArgumentParser.FutureArgumentParser<C, BlockPredicate>,
    NodeSource, SuggestionProvider<C> {

    /**
     * Creates a new {@link BlockPredicateParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, BlockPredicate> blockPredicateParser() {
        return ParserDescriptor.of(new BlockPredicateParser<>(), BlockPredicate.class);
    }

    private final ArgumentParser<C, BlockPredicate> mappedParser =
        new WrappedBrigadierParser<C, BlockPredicateArgument.Result>(
            new ContextualArgumentTypeProvider<>(net.minecraft.commands.arguments.blocks.BlockPredicateArgument::blockPredicate)
        ).flatMapSuccess((ctx, result) -> ArgumentParseResult.successFuture(new BlockPredicateImpl(result)));

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<BlockPredicate>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        return this.mappedParser.parseFuture(commandContext, commandInput);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
        final @NonNull CommandContext<C> context,
        final @NonNull CommandInput input
    ) {
        return this.mappedParser.suggestionProvider().suggestionsFuture(context, input);
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
        return CommandTreeNodeTypes.BLOCK_PREDICATE.get().createNode();
    }

    private record BlockPredicateImpl(Predicate<BlockInWorld> predicate) implements BlockPredicate {

        private BlockPredicateImpl(final @NonNull Predicate<BlockInWorld> predicate) {
            this.predicate = predicate;
        }

        private boolean testImpl(final @NonNull ServerLocation location, final boolean loadChunks) {
            return this.predicate.test(new BlockInWorld(
                (ServerLevel) location.world(),
                VecHelper.toBlockPos(location.position()),
                loadChunks
            ));
        }

        @Override
        public boolean test(final @NonNull ServerLocation location) {
            return this.testImpl(location, false);
        }

        @Override
        public @NonNull BlockPredicate loadChunks() {
            return new BlockPredicate() {
                @Override
                public @NonNull BlockPredicate loadChunks() {
                    return this;
                }

                @Override
                public boolean test(final @NonNull ServerLocation location) {
                    return BlockPredicateImpl.this.testImpl(location, true);
                }
            };
        }

    }

}
