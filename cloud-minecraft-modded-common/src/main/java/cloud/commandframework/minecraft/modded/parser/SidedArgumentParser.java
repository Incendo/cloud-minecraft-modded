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
package cloud.commandframework.minecraft.modded.parser;

import cloud.commandframework.minecraft.modded.ModdedCommandContextKeys;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;

/**
 * An argument parser that is resolved in different ways on the logical server and logical client.
 *
 * @param <C>                command sender type
 * @param <IntermediateType> intermediate type to resolve
 * @param <T>                resolved type
 */
abstract class SidedArgumentParser<C, IntermediateType, T> implements ArgumentParser.FutureArgumentParser<C, T> {

    private final Predicate<SharedSuggestionProvider> isClient;

    protected SidedArgumentParser() {
        this.isClient = VanillaArgumentParsers::isClientSource;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> parseFuture(
        final @NonNull CommandContext<@NonNull C> commandContext,
        final @NonNull CommandInput commandInput
    ) {
        final SharedSuggestionProvider source = commandContext.get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER);
        return this.intermediateParser().flatMapSuccess((ctx, result) -> {
            if (this.isClient.test(source)) {
                return this.resolveClient(commandContext, result);
            } else {
                return this.resolveServer(commandContext, result);
            }
        }).parseFuture(commandContext, commandInput);
    }

    protected abstract @NonNull FutureArgumentParser<C, IntermediateType> intermediateParser();

    /**
     * Resolve the final value for this argument when running on the client.
     *
     * @param context Command context
     * @param value   parsed intermediate value
     * @return a resolved value
     */
    protected abstract @NonNull CompletableFuture<@NonNull ArgumentParseResult<@NonNull T>> resolveClient(
        @NonNull CommandContext<@NonNull C> context,
        @NonNull IntermediateType value
    );

    /**
     * Resolve the final value for this argument when running on the server.
     *
     * @param context Command context
     * @param value   Parsed intermediate value
     * @return a resolved value
     */
    protected abstract @NonNull CompletableFuture<@NonNull ArgumentParseResult<@NonNull T>> resolveServer(
        @NonNull CommandContext<@NonNull C> context,
        @NonNull IntermediateType value
    );
}
