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
package cloud.commandframework.fabric.testmod;

import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.fabric.testmod.mixin.GiveCommandAccess;
import cloud.commandframework.minecraft.modded.data.Coordinates;
import cloud.commandframework.minecraft.modded.data.Coordinates.ColumnCoordinates;
import cloud.commandframework.minecraft.modded.data.MultipleEntitySelector;
import cloud.commandframework.minecraft.modded.data.MultiplePlayerSelector;
import cloud.commandframework.minecraft.modded.parser.NamedColorParser;
import cloud.commandframework.minecraft.modded.parser.RegistryEntryParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import static cloud.commandframework.minecraft.modded.parser.VanillaArgumentParsers.columnPosParser;
import static cloud.commandframework.minecraft.modded.parser.VanillaArgumentParsers.itemInput;
import static cloud.commandframework.minecraft.modded.parser.VanillaArgumentParsers.multiplePlayerSelectorParser;
import static cloud.commandframework.minecraft.modded.parser.VanillaArgumentParsers.vec3Parser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public final class FabricExample implements ModInitializer {

    @Override
    public void onInitialize() {
        // Create a commands manager. We'll use native command source types for this.
        final FabricServerCommandManager<CommandSourceStack> manager =
            FabricServerCommandManager.createNative(ExecutionCoordinator.simpleCoordinator());

        final Command.Builder<CommandSourceStack> base = manager.commandBuilder("cloudtest");

        final CloudKey<String> name = CloudKey.of("name", String.class);
        final CloudKey<Integer> hugs = CloudKey.of("hugs", Integer.class);

        manager.command(base
            .literal("hugs")
            .required(name, stringParser())
            .optional(hugs, integerParser(), DefaultValue.constant(1))
            .handler(ctx -> {
                ctx.sender().sendSuccess(() -> Component.literal("Hello, ")
                    .append(ctx.get(name))
                    .append(", hope you're doing well!")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA22BB))), false);

                ctx.sender().sendSuccess(() -> Component.literal("Cloud would like to give you ")
                    .append(Component.literal(String.valueOf(ctx.get(hugs)))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFAB3DA))))
                    .append(" hug(s) <3")
                    .withStyle(style -> style.withBold(true)), false);
            }));

        final ParserDescriptor<CommandSourceStack, Biome> biomeArgument = RegistryEntryParser.registryEntryParser(
            Registries.BIOME,
            Biome.class
        );

        manager.command(base
            .literal("land")
            .required("biome", biomeArgument)
            .handler(ctx -> {
                ctx.sender().sendSuccess(() -> Component.literal("Yes, the biome ")
                    .append(Component.literal(
                            ctx.sender().registryAccess()
                                .registryOrThrow(Registries.BIOME)
                                .getKey(ctx.get("biome")).toString())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .append(Component.literal(" is pretty cool"))
                    .withStyle(style -> style.withColor(0x884433)), false);
            })
        );

        final CloudKey<MultiplePlayerSelector> playersKey = CloudKey.of("players", MultiplePlayerSelector.class);
        final CloudKey<ChatFormatting> textColorKey = CloudKey.of("color", ChatFormatting.class);

        manager.command(base.literal("wave")
            .required(playersKey, multiplePlayerSelectorParser())
            .required(textColorKey, NamedColorParser.namedColorParser())
            .handler(ctx -> {
                final MultiplePlayerSelector selector = ctx.get(playersKey);
                final Collection<ServerPlayer> selected = selector.values();
                selected.forEach(selectedPlayer ->
                    selectedPlayer.sendSystemMessage(
                        Component.literal("Wave from ")
                            .withStyle(style -> style.withColor(ctx.get(textColorKey)))
                            .append(ctx.sender().getDisplayName())
                    ));
                ctx.sender().sendSuccess(
                    () -> Component.literal(String.format("Waved at %d players (%s)", selected.size(),
                        selector.inputString()
                    )),
                    false
                );
            }));

        manager.command(base.literal("give")
            .permission("cloud.give")
            .required("targets", multiplePlayerSelectorParser())
            .required("item", itemInput())
            .optional("amount", integerParser(1), DefaultValue.constant(1))
            .handler(ctx -> {
                final ItemInput item = ctx.get("item");
                final MultiplePlayerSelector targets = ctx.get("targets");
                final int amount = ctx.get("amount");
                GiveCommandAccess.giveItem(
                    ctx.sender(),
                    item,
                    targets.values(),
                    amount
                );
            }));

        final Command.Builder<CommandSourceStack> mods = base.literal("mods").permission("cloud.mods");

        manager.command(mods.handler(ctx -> {
            final List<ModMetadata> modList = FabricLoader.getInstance().getAllMods().stream()
                .map(ModContainer::getMetadata)
                .sorted(Comparator.comparing(ModMetadata::getId))
                .toList();
            final MutableComponent text = Component.literal("");
            text.append(Component.literal("Loaded Mods")
                .withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.BOLD)));
            text.append(Component.literal(String.format(" (%s)\n", modList.size()))
                .withStyle(style -> style.withColor(ChatFormatting.GRAY).applyFormat(ChatFormatting.ITALIC)));
            for (final ModMetadata mod : modList) {
                text.append(
                    Component.literal("")
                        .withStyle(style -> style.withColor(ChatFormatting.WHITE)
                            .withClickEvent(new ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                String.format("/cloudtest mods %s", mod.getId())
                            ))
                            .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click for more info")
                            )))
                        .append(Component
                            .literal(mod.getName())
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                        .append(Component.literal(String.format(" (%s) ", mod.getId()))
                            .withStyle(style -> style
                                .withColor(ChatFormatting.GRAY)
                                .applyFormat(ChatFormatting.ITALIC)))
                        .append(Component.literal(String.format("v%s", mod.getVersion())))
                );
                if (modList.indexOf(mod) != modList.size() - 1) {
                    text.append(Component.literal(", ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
                }
            }
            ctx.sender().sendSuccess(() -> text, false);
        }));

        final TypedCommandComponent<CommandSourceStack, ModMetadata> modMetadata = manager.componentBuilder(
                ModMetadata.class,
                "mod"
            )
            .suggestionProvider(SuggestionProvider.blocking((ctx, input) -> FabricLoader
                .getInstance()
                .getAllMods()
                .stream()
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getId)
                .map(Suggestion::simple)
                .collect(Collectors.toList())))
            .parser((commandContext, commandInput) -> {
                final ModMetadata meta = FabricLoader.getInstance().getModContainer(commandInput.readString())
                    .map(ModContainer::getMetadata)
                    .orElse(null);
                if (meta != null) {
                    return ArgumentParseResult.success(meta);
                }
                return ArgumentParseResult.failure(new IllegalArgumentException(String.format(
                    "No mod with id '%s'",
                    commandInput.peek()
                )));
            })
            .build();

        manager.command(mods.argument(modMetadata)
            .handler(ctx -> {
                final ModMetadata meta = ctx.get(modMetadata);
                final MutableComponent text = Component.literal("")
                    .append(Component.literal(meta.getName())
                        .withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.BOLD)))
                    .append(Component.literal("\n modid: " + meta.getId()))
                    .append(Component.literal("\n version: " + meta.getVersion()))
                    .append(Component.literal("\n type: " + meta.getType()));

                if (!meta.getDescription().isEmpty()) {
                    text.append(Component.literal("\n description: " + meta.getDescription()));
                }
                if (!meta.getAuthors().isEmpty()) {
                    text.append(Component.literal("\n authors: " + meta.getAuthors().stream()
                        .map(Person::getName)
                        .collect(Collectors.joining(", "))));
                }
                if (!meta.getLicense().isEmpty()) {
                    text.append(Component.literal("\n license: " + String.join(", ", meta.getLicense())));
                }
                ctx.sender().sendSuccess(
                    () -> text,
                    false
                );
            }));

        manager.command(base.literal("teleport")
            .permission("cloud.teleport")
            .required("targets", multiplePlayerSelectorParser())
            .required("location", vec3Parser(false))
            .handler(ctx -> {
                final MultipleEntitySelector selector = ctx.get("targets");
                final Vec3 location = ctx.<Coordinates>get("location").position();
                selector.values().forEach(target ->
                    target.teleportToWithTicket(location.x(), location.y(), location.z()));
            }));

        manager.command(base.literal("gotochunk")
            .permission("cloud.gotochunk")
            .required("chunk_position", columnPosParser())
            .handler(ctx -> {
                final ServerPlayer player;
                try {
                    player = ctx.sender().getPlayerOrException();
                } catch (final CommandSyntaxException e) {
                    ctx.sender().sendSuccess(() -> ComponentUtils.fromMessage(e.getRawMessage()), false);
                    return;
                }
                final Vec3 vec = ctx.<ColumnCoordinates>get("chunk_position").position();
                final ChunkPos pos = new ChunkPos((int) vec.x(), (int) vec.z());
                player.teleportToWithTicket(pos.getMinBlockX(), 128, pos.getMinBlockZ());
            }));
    }
}
