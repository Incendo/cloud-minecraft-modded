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
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.util.Services;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.PlayerChatMessage;
import org.apiguardian.api.API;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.signed.SignedGreedyStringParser;
import org.incendo.cloud.minecraft.signed.SignedString;
import org.incendo.cloud.minecraft.signed.SignedStringMapper;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.jetbrains.annotations.NotNull;

@API(status = API.Status.INTERNAL)
public final class ModdedSignedStringMapper implements SignedStringMapper {
    private final SignedStringFactory factory;

    /**
     * Creates a new mapper.
     */
    public ModdedSignedStringMapper() {
        this.factory = serviceWithFallback(SignedStringFactory.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void registerBrigadier(final CommandManager<?> commandManager, final Object brigManager) {
        registerBrigadier((CloudBrigadierManager) brigManager);
    }

    private static <C> void registerBrigadier(final CloudBrigadierManager<C, ?> manager) {
        manager.registerMapping(
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
        if (signedArgs.isEmpty()) {
            return ArgumentParseResult.successFuture(SignedString.unsigned(str));
        }
        if (signedArgs.size() != 1) {
            throw new IllegalStateException("Found more signed arguments than expected (" + signedArgs.size() + ")");
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

    public static final class FallbackSignedStringFactory implements SignedStringFactory, Services.Fallback {

        @Override
        public SignedString create(final String str, final PlayerChatMessage signedMessage) {
            return SignedString.unsigned(str);
        }
    }

    private static <P> P serviceWithFallback(final @NotNull Class<P> type) {
        final ServiceLoader<P> loader = ServiceLoader.load(type, type.getClassLoader());
        final Iterator<P> it = loader.iterator();
        Throwable cause = null;
        P firstFallback = null;

        while (it.hasNext()) {
            final P instance;

            try {
                instance = it.next();
            } catch (final Throwable t) {
                if (cause == null) {
                    cause = t;
                } else {
                    cause.addSuppressed(t);
                }
                continue;
            }

            if (instance instanceof Services.Fallback) {
                if (firstFallback == null) {
                    firstFallback = instance;
                }
            } else {
                return instance;
            }
        }

        if (firstFallback != null) {
            return firstFallback;
        }
        throw new IllegalStateException("Could not locate " + type, cause);
    }
}
