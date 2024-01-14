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
package cloud.commandframework.minecraft.modded.internal;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.standard.UUIDParser;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.parser.WrappedBrigadierParser;
import cloud.commandframework.minecraft.modded.ModdedParserParameters;
import cloud.commandframework.minecraft.modded.annotation.specifier.Center;
import cloud.commandframework.minecraft.modded.data.Coordinates;
import cloud.commandframework.minecraft.modded.data.Message;
import cloud.commandframework.minecraft.modded.data.MinecraftTime;
import cloud.commandframework.minecraft.modded.data.MultipleEntitySelector;
import cloud.commandframework.minecraft.modded.data.MultiplePlayerSelector;
import cloud.commandframework.minecraft.modded.data.SingleEntitySelector;
import cloud.commandframework.minecraft.modded.data.SinglePlayerSelector;
import cloud.commandframework.minecraft.modded.parser.VanillaArgumentParsers;
import com.mojang.brigadier.arguments.ArgumentType;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL)
public final class ModdedParserMappings {
    private ModdedParserMappings() {
    }

    /**
     * Register common mappings.
     *
     * @param manager   manager
     * @param brigadier brig manager
     * @param <C>       sender type
     * @param <S>       source type
     */
    public static <C, S> void register(final CommandManager<C> manager, final @NonNull CloudBrigadierManager<C, S> brigadier) {
        /* Cloud-native argument types */
        brigadier.registerMapping(new TypeToken<UUIDParser<C>>() {
        }, builder -> builder.toConstant(UuidArgument.uuid()));

        /* Wrapped/Constant Brigadier types, native value type */
        registerConstantNativeParserSupplier(manager, ChatFormatting.class, ColorArgument.color());
        registerConstantNativeParserSupplier(manager, CompoundTag.class, CompoundTagArgument.compoundTag());
        registerConstantNativeParserSupplier(manager, Tag.class, NbtTagArgument.nbtTag());
        registerConstantNativeParserSupplier(manager, NbtPathArgument.NbtPath.class, NbtPathArgument.nbtPath());
        registerConstantNativeParserSupplier(manager, ObjectiveCriteria.class, ObjectiveCriteriaArgument.criteria());
        registerConstantNativeParserSupplier(manager, OperationArgument.Operation.class, OperationArgument.operation());
        registerConstantNativeParserSupplier(manager, AngleArgument.SingleAngle.class, AngleArgument.angle());
        registerConstantNativeParserSupplier(manager, new TypeToken<>() {}, SwizzleArgument.swizzle());
        registerConstantNativeParserSupplier(manager, ResourceLocation.class, ResourceLocationArgument.id());
        registerConstantNativeParserSupplier(manager, EntityAnchorArgument.Anchor.class, EntityAnchorArgument.anchor());
        registerConstantNativeParserSupplier(manager, MinMaxBounds.Ints.class, RangeArgument.intRange());
        registerConstantNativeParserSupplier(manager, MinMaxBounds.Doubles.class, RangeArgument.floatRange());
        registerContextualNativeParserSupplier(manager, ParticleOptions.class, ParticleArgument::particle);
        registerContextualNativeParserSupplier(manager, ItemInput.class, ItemArgument::item);
        registerContextualNativeParserSupplier(manager, BlockPredicateArgument.Result.class, BlockPredicateArgument::blockPredicate);

        /* Wrapped/Constant Brigadier types, mapped value type */
        registerConstantNativeParserSupplier(manager, MessageArgument.Message.class, MessageArgument.message());
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(MinecraftTime.class),
            params -> VanillaArgumentParsers.<C>timeParser().parser()
        );
    }

    /**
     * Register server mappings.
     *
     * @param manager manager
     * @param <C>     sender type
     */
    public static <C> void registerServer(final CommandManager<C> manager) {
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(Message.class),
            params -> VanillaArgumentParsers.<C>messageParser().parser()
        );

        // Location arguments
        manager.parserRegistry().registerAnnotationMapper(
            Center.class,
            (annotation, type) -> ParserParameters.single(ModdedParserParameters.CENTER_INTEGERS, true)
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(Coordinates.class),
            params -> VanillaArgumentParsers.<C>vec3Parser(params.get(
                ModdedParserParameters.CENTER_INTEGERS,
                false
            )).parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(Coordinates.CoordinatesXZ.class),
            params -> VanillaArgumentParsers.<C>vec2Parser(params.get(
                ModdedParserParameters.CENTER_INTEGERS,
                false
            )).parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(Coordinates.BlockCoordinates.class),
            params -> VanillaArgumentParsers.<C>blockPosParser().parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(Coordinates.ColumnCoordinates.class),
            params -> VanillaArgumentParsers.<C>columnPosParser().parser()
        );

        // Entity selectors
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(SinglePlayerSelector.class),
            params -> VanillaArgumentParsers.<C>singlePlayerSelectorParser().parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(MultiplePlayerSelector.class),
            params -> VanillaArgumentParsers.<C>multiplePlayerSelectorParser().parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(SingleEntitySelector.class),
            params -> VanillaArgumentParsers.<C>singleEntitySelectorParser().parser()
        );
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(MultipleEntitySelector.class),
            params -> VanillaArgumentParsers.<C>multipleEntitySelectorParser().parser()
        );
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param manager  manager
     * @param type     the Java type to map
     * @param argument a function providing the Brigadier parser given a build context
     * @param <C>      sender type
     * @param <T>      value type
     */
    public static <C, T> void registerContextualNativeParserSupplier(
        final @NonNull CommandManager<C> manager,
        final @NonNull Class<T> type,
        final @NonNull Function<CommandBuildContext, @NonNull ArgumentType<T>> argument
    ) {
        manager.parserRegistry().registerParserSupplier(
            TypeToken.get(type),
            params -> VanillaArgumentParsers.<C, T>contextualParser(argument, type).parser()
        );
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param manager  manager
     * @param type     the Java type to map
     * @param argument the Brigadier parser
     * @param <C>      sender type
     * @param <T>      value type
     */
    public static <C, T> void registerConstantNativeParserSupplier(
        final @NonNull CommandManager<C> manager,
        final @NonNull Class<T> type,
        final @NonNull ArgumentType<T> argument
    ) {
        registerConstantNativeParserSupplier(manager, TypeToken.get(type), argument);
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param manager  manager
     * @param type     the Java type to map
     * @param argument the Brigadier parser
     * @param <C>      sender type
     * @param <T>      value type
     */
    public static <C, T> void registerConstantNativeParserSupplier(
        final @NonNull CommandManager<C> manager,
        final @NonNull TypeToken<T> type,
        final @NonNull ArgumentType<T> argument
    ) {
        manager.parserRegistry().registerParserSupplier(type, params -> new WrappedBrigadierParser<>(argument));
    }
}
