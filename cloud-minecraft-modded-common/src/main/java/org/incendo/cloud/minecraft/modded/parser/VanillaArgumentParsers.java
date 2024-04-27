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
package org.incendo.cloud.minecraft.modded.parser;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.modded.ModdedCommandContextKeys;
import org.incendo.cloud.minecraft.modded.data.Coordinates;
import org.incendo.cloud.minecraft.modded.data.Message;
import org.incendo.cloud.minecraft.modded.data.MinecraftTime;
import org.incendo.cloud.minecraft.modded.data.MultipleEntitySelector;
import org.incendo.cloud.minecraft.modded.data.MultiplePlayerSelector;
import org.incendo.cloud.minecraft.modded.data.SingleEntitySelector;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;
import org.incendo.cloud.minecraft.modded.internal.ContextualArgumentTypeProvider;
import org.incendo.cloud.minecraft.modded.internal.EntitySelectorAccess;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * Parsers for Vanilla command argument types.
 */
public final class VanillaArgumentParsers {

    private VanillaArgumentParsers() {
    }

    /**
     * A parser that wraps Brigadier argument types which need a {@link CommandBuildContext}
     *
     * @param <C>       sender type
     * @param <V>       argument value type
     * @param factory   factory that creates these arguments
     * @param valueType value type of parsers output
     * @return the parser
     */
    public static <C, V> @NonNull ParserDescriptor<C, V> contextualParser(
        final @NonNull Function<CommandBuildContext, ArgumentType<V>> factory,
        final @NonNull Class<V> valueType
    ) {
        return ParserDescriptor.of(new WrappedBrigadierParser<>(new ContextualArgumentTypeProvider<>(factory)), valueType);
    }

    /**
     * A parser for {@link ItemInput}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, ItemInput> itemInput() {
        return contextualParser(ItemArgument::item, ItemInput.class);
    }

    /**
     * A parser for in-game time, in ticks.
     *
     * @param <C> sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, MinecraftTime> timeParser() {
        ArgumentParser<C, MinecraftTime> parser = new WrappedBrigadierParser<C, Integer>(TimeArgument.time())
            .flatMapSuccess((ctx, val) -> ArgumentParseResult.successFuture(MinecraftTime.of(val)));

        return ParserDescriptor.of(parser, MinecraftTime.class);
    }

    /**
     * A parser for block coordinates.
     *
     * @param <C> sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, Coordinates.BlockCoordinates> blockPosParser() {
        ArgumentParser<C, Coordinates.BlockCoordinates> parser = new WrappedBrigadierParser<C,
            net.minecraft.commands.arguments.coordinates.Coordinates>(BlockPosArgument.blockPos())
            .flatMapSuccess(VanillaArgumentParsers::mapToCoordinates);

        return ParserDescriptor.of(parser, Coordinates.BlockCoordinates.class);
    }

    /**
     * A parser for column coordinates.
     *
     * @param <C> sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, Coordinates.ColumnCoordinates> columnPosParser() {
        ArgumentParser<C, Coordinates.ColumnCoordinates> parser = new WrappedBrigadierParser<C,
            net.minecraft.commands.arguments.coordinates.Coordinates>(ColumnPosArgument.columnPos())
            .flatMapSuccess(VanillaArgumentParsers::mapToCoordinates);

        return ParserDescriptor.of(parser, Coordinates.ColumnCoordinates.class);
    }

    /**
     * A parser for coordinates, relative or absolute, from 2 doubles for x and z,
     * with y always defaulting to 0.
     *
     * @param centerIntegers whether to center integers at x.5
     * @param <C>            sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, Coordinates.CoordinatesXZ> vec2Parser(final boolean centerIntegers) {
        ArgumentParser<C, Coordinates.CoordinatesXZ> parser = new WrappedBrigadierParser<C,
            net.minecraft.commands.arguments.coordinates.Coordinates>(new Vec2Argument(centerIntegers))
            .flatMapSuccess(VanillaArgumentParsers::mapToCoordinates);

        return ParserDescriptor.of(parser, Coordinates.CoordinatesXZ.class);
    }

    /**
     * A parser for coordinates, relative or absolute, from 3 doubles.
     *
     * @param centerIntegers whether to center integers at x.5
     * @param <C>            sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, Coordinates> vec3Parser(final boolean centerIntegers) {
        ArgumentParser<C, Coordinates> parser = new WrappedBrigadierParser<C,
            net.minecraft.commands.arguments.coordinates.Coordinates>(Vec3Argument.vec3(centerIntegers))
            .flatMapSuccess(VanillaArgumentParsers::mapToCoordinates);

        return ParserDescriptor.of(parser, Coordinates.class);
    }

    @SuppressWarnings("unchecked")
    private static <C, O extends Coordinates> @NonNull CompletableFuture<@NonNull ArgumentParseResult<O>> mapToCoordinates(
        final @NonNull CommandContext<C> ctx,
        final net.minecraft.commands.arguments.coordinates.@NonNull Coordinates posArgument
    ) {
        return requireServer(
            ctx,
            serverCommandSource -> ArgumentParseResult.successFuture((O) new CoordinatesImpl(
                serverCommandSource,
                posArgument
            ))
        );
    }

    /**
     * A parser for {@link SinglePlayerSelector}.
     *
     * @param <C> sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, SinglePlayerSelector> singlePlayerSelectorParser() {
        ArgumentParser<C, SinglePlayerSelector> parser = new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.player())
            .flatMapSuccess((ctx, entitySelector) -> requireServer(
                ctx,
                serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                    () -> ArgumentParseResult.success(new SinglePlayerSelectorImpl(
                        ((EntitySelectorAccess) entitySelector).inputString(),
                        entitySelector,
                        entitySelector.findSinglePlayer(serverCommandSource)
                    ))
                )
            ));

        return ParserDescriptor.of(parser, SinglePlayerSelector.class);
    }

    /**
     * A parser for {@link MultiplePlayerSelector}.
     *
     * @param <C> sender type
     * @return a parser descriptor
     */
    public static <C> @NonNull ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser() {
        ArgumentParser<C, MultiplePlayerSelector> parser = new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.players())
            .flatMapSuccess((ctx, entitySelector) -> requireServer(
                ctx,
                serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                    () -> ArgumentParseResult.success(new MultiplePlayerSelectorImpl(
                        ((EntitySelectorAccess) entitySelector).inputString(),
                        entitySelector,
                        entitySelector.findPlayers(serverCommandSource)
                    ))
                )
            ));

        return ParserDescriptor.of(parser, MultiplePlayerSelector.class);
    }

    /**
     * A parser for {@link SingleEntitySelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ParserDescriptor<C, SingleEntitySelector> singleEntitySelectorParser() {
        ArgumentParser<C, SingleEntitySelector> parser = new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.entity())
            .flatMapSuccess((ctx, entitySelector) -> requireServer(
                ctx,
                serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                    () -> ArgumentParseResult.success(new SingleEntitySelectorImpl(
                        ((EntitySelectorAccess) entitySelector).inputString(),
                        entitySelector,
                        entitySelector.findSingleEntity(serverCommandSource)
                    ))
                )
            ));

        return ParserDescriptor.of(parser, SingleEntitySelector.class);
    }

    /**
     * A parser for {@link MultipleEntitySelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ParserDescriptor<C, MultipleEntitySelector> multipleEntitySelectorParser() {
        ArgumentParser<C, MultipleEntitySelector> parser = new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.entities())
            .flatMapSuccess((ctx, entitySelector) -> requireServer(
                ctx,
                serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                    () -> ArgumentParseResult.success(new MultipleEntitySelectorImpl(
                        ((EntitySelectorAccess) entitySelector).inputString(),
                        entitySelector,
                        Collections.unmodifiableCollection(entitySelector.findEntities(serverCommandSource))
                    ))
                )
            ));

        return ParserDescriptor.of(parser, MultipleEntitySelector.class);
    }

    /**
     * A parser for {@link Message}.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ParserDescriptor<C, Message> messageParser() {
        ArgumentParser<C, Message> parser = new WrappedBrigadierParser<C, MessageArgument.Message>(MessageArgument.message())
            .flatMapSuccess((ctx, format) -> requireServer(
                ctx,
                serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                    () -> ArgumentParseResult.success(MessageImpl.from(
                        serverCommandSource,
                        format,
                        true
                    ))
                )
            ));

        return ParserDescriptor.of(parser, Message.class);
    }

    @FunctionalInterface
    private interface CommandSyntaxExceptionThrowingParseResultSupplier<O> {

        @NonNull ArgumentParseResult<O> result() throws CommandSyntaxException;
    }

    private static <O> @NonNull CompletableFuture<ArgumentParseResult<O>> handleCommandSyntaxExceptionAsFailure(
        final @NonNull CommandSyntaxExceptionThrowingParseResultSupplier<O> resultSupplier
    ) {
        try {
            return CompletableFuture.completedFuture(resultSupplier.result());
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failureFuture(ex);
        }
    }

    private static @NonNull IllegalStateException serverOnly() {
        return new IllegalStateException("This command argument type is server-only.");
    }

    private static <C, O> @NonNull CompletableFuture<ArgumentParseResult<O>> requireServer(
        final @NonNull CommandContext<C> context,
        final @NonNull Function<CommandSourceStack, CompletableFuture<ArgumentParseResult<O>>> resultFunction
    ) {
        final SharedSuggestionProvider nativeSource = context.get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER);
        if (!(nativeSource instanceof CommandSourceStack) || isClientSource(nativeSource)) {
            return ArgumentParseResult.failureFuture(serverOnly());
        }
        return resultFunction.apply((CommandSourceStack) nativeSource);
    }

    /**
     * Checks whether a source is from client commands.
     *
     * @param sharedSuggestionProvider source
     * @return whether the source is from client commands
     */
    @API(status = API.Status.INTERNAL)
    public static boolean isClientSource(final SharedSuggestionProvider sharedSuggestionProvider) {
        // NeoForge extends CommandSourceStack with ClientCommandSourceStack; Fabric has its own SharedSuggestionProvider impl
        return !sharedSuggestionProvider.getClass().equals(CommandSourceStack.class);
    }

    private record MessageImpl(Collection<Entity> mentionedEntities, Component contents) implements Message {
        static MessageImpl from(
            final @NonNull CommandSourceStack source,
            final MessageArgument.@NonNull Message message,
            final boolean useSelectors
        ) throws CommandSyntaxException {
            final Component contents = message.toComponent(source, useSelectors);
            final MessageArgument.Part[] selectors = message.parts();
            final Collection<Entity> entities;
            if (!useSelectors || selectors.length == 0) {
                entities = Collections.emptySet();
            } else {
                entities = new HashSet<>();
                for (final MessageArgument.Part selector : selectors) {
                    entities.addAll(selector.selector()
                        .findEntities(source));
                }
            }

            return new MessageImpl(entities, contents);
        }
    }

    private record CoordinatesImpl(CommandSourceStack source, net.minecraft.commands.arguments.coordinates.Coordinates wrappedCoordinates)
        implements Coordinates,
        Coordinates.CoordinatesXZ,
        Coordinates.BlockCoordinates,
        Coordinates.ColumnCoordinates {

        @Override
        public @NonNull Vec3 position() {
            return this.wrappedCoordinates.getPosition(this.source);
        }

        @Override
        public @NonNull BlockPos blockPos() {
            return BlockPos.containing(this.position());
        }

        @Override
        public boolean isXRelative() {
            return this.wrappedCoordinates.isXRelative();
        }

        @Override
        public boolean isYRelative() {
            return this.wrappedCoordinates.isYRelative();
        }

        @Override
        public boolean isZRelative() {
            return this.wrappedCoordinates.isZRelative();
        }
    }

    private record SingleEntitySelectorImpl(
        String inputString, EntitySelector selector, Entity single
    ) implements SingleEntitySelector {}

    private record MultipleEntitySelectorImpl(
        String inputString, EntitySelector selector, Collection<Entity> values
    ) implements MultipleEntitySelector {}

    private record SinglePlayerSelectorImpl(
        String inputString, EntitySelector selector, ServerPlayer single
    ) implements SinglePlayerSelector {}

    private record MultiplePlayerSelectorImpl(
        String inputString, EntitySelector selector, Collection<ServerPlayer> values
    ) implements MultiplePlayerSelector {}

}
