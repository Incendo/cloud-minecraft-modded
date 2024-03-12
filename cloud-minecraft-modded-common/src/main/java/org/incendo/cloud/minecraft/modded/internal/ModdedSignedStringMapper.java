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

import io.leangen.geantyref.TypeToken;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.util.Services;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.PlayerChatMessage;
import org.apiguardian.api.API;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.signed.SignedGreedyStringParser;
import org.incendo.cloud.minecraft.signed.SignedString;
import org.incendo.cloud.minecraft.signed.SignedStringMapper;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = API.Status.INTERNAL)
public final class ModdedSignedStringMapper implements SignedStringMapper {
    private final SignedStringFactory factory;

    /**
     * Creates a new mapper.
     */
    public ModdedSignedStringMapper() {
        this.factory = Services.service(SignedStringFactory.class)
            .orElseThrow(() -> new IllegalStateException("Could not locate " + SignedStringFactory.class));
    }

    @Override
    public void registerBrigadier(final CommandManager<?> manager) {
        registerBrigadier((BrigadierManagerHolder<?, ?>) manager);
    }

    private static <C> void registerBrigadier(final BrigadierManagerHolder<C, ?> manager) {
        manager.brigadierManager().registerMapping(
            new TypeToken<SignedGreedyStringParser<C>>() {},
            builder -> builder.toConstant(MessageArgument.message()).cloudSuggestions()
        );
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedString>> apply(
        final CommandContext<?> ctx,
        final String str
    ) {
        final CommandSourceStack stack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
        final Map<String, PlayerChatMessage> signedArgs;
        if (stack.getSigningContext() instanceof CommandSigningContext.SignedArguments) {
            signedArgs = ((CommandSigningContext.SignedArguments) stack.getSigningContext()).arguments();
        } else {
            return ArgumentParseResult.successFuture(SignedString.unsigned(str));
        }
        if (signedArgs.size() != 1) {
            throw new IllegalStateException();
        }

        return ArgumentParseResult.successFuture(
            this.factory.create(
                str,
                signedArgs.entrySet().iterator().next().getValue()
            )
        );
    }

    public interface SignedStringFactory {
        /**
         * Creates a signed string.
         *
         * @param str           raw string
         * @param signedMessage signed message
         * @return new signed string
         */
        SignedString create(String str, PlayerChatMessage signedMessage);
    }
}
