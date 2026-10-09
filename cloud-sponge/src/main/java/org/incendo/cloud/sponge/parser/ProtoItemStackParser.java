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
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.ComponentUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.minecraft.modded.internal.ContextualArgumentTypeProvider;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.sponge.NodeSource;
import org.incendo.cloud.sponge.data.ProtoItemStack;
import org.incendo.cloud.sponge.exception.ComponentMessageRuntimeException;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.common.adventure.SpongeAdventure;

/**
 * An argument for parsing {@link ProtoItemStack ProtoItemStacks} from an {@link ItemType} identifier
 * and optional data components.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code apple}</li>
 *     <li>{@code minecraft:apple}</li>
 *     <li>{@code diamond_sword[enchantments={sharpness:5}]}</li>
 * </ul>
 *
 * @param <C> command sender type
 */
public final class ProtoItemStackParser<C> implements NodeSource,
    ArgumentParser.FutureArgumentParser<C, ProtoItemStack>, MappedArgumentParser<C, ItemInput, ProtoItemStack>, SuggestionProvider<C> {

    private ProtoItemStackParser() {
        this.nativeParser = new WrappedBrigadierParser<>(new ContextualArgumentTypeProvider<>(ItemArgument::item));
        this.mappedParser = this.nativeParser
            .flatMapSuccess((ctx, itemInput) -> ArgumentParseResult.successFuture(new ProtoItemStackImpl(itemInput)));
    }

    /**
     * Creates a new {@link ProtoItemStackParser}.
     *
     * @param <C> command sender type
     * @return new parser
     */
    public static <C> ParserDescriptor<C, ProtoItemStack> protoItemStackParser() {
        return ParserDescriptor.of(new ProtoItemStackParser<>(), ProtoItemStack.class);
    }

    private final WrappedBrigadierParser<C, ItemInput> nativeParser;
    private final ArgumentParser<C, ProtoItemStack> mappedParser;

    @Override
    public @NonNull ArgumentParser<C, ItemInput> baseParser() {
        return this.nativeParser;
    }

    @Override
    public @NonNull CompletableFuture<ArgumentParseResult<@NonNull ProtoItemStack>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput inputQueue
    ) {
        return this.mappedParser.parseFuture(commandContext, inputQueue);
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
        final @NonNull CommandContext<C> context,
        final @NonNull CommandInput input
    ) {
        return this.mappedParser.suggestionProvider().suggestionsFuture(context, input);
    }

    @Override
    public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node(final RegistryHolder holder) {
        return CommandTreeNodeTypes.ITEM_STACK.get(holder).createNode();
    }

    private static final class ProtoItemStackImpl implements ProtoItemStack {

        private final ItemInput itemInput;

        ProtoItemStackImpl(final @NonNull ItemInput itemInput) {
            this.itemInput = itemInput;
        }

        @Override
        public @NonNull ItemType itemType() {
            return (ItemType) this.itemInput.item().value();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public @NonNull ItemStack createItemStack(final int stackSize) throws ComponentMessageRuntimeException {
            try {
                return (ItemStack) (Object) this.itemInput.createItemStack(stackSize);
            } catch (final CommandSyntaxException ex) {
                throw new ComponentMessageRuntimeException(
                    SpongeAdventure.asAdventure(ComponentUtils.fromMessage(ex.getRawMessage())), ex
                );
            }
        }

        @Override
        public @NonNull ItemStackSnapshot createItemStackSnapshot(final int stackSize) throws ComponentMessageRuntimeException {
            return this.createItemStack(stackSize).asImmutable();
        }

    }

}
