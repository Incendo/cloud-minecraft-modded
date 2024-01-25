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

import cloud.commandframework.minecraft.modded.ModdedCaptionKeys;
import cloud.commandframework.minecraft.modded.ModdedCommandContextKeys;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.scores.PlayerTeam;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * An argument for parsing {@link PlayerTeam Teams}.
 *
 * @param <C> the sender type
 */
public final class TeamParser<C> extends SidedArgumentParser<C, String, PlayerTeam> implements
    BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @return the created parser
     */
    public static <C> @NonNull ParserDescriptor<C, PlayerTeam> teamParser() {
        return ParserDescriptor.of(new TeamParser<>(), PlayerTeam.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #teamParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    public static <C> CommandComponent.@NonNull Builder<C, PlayerTeam> teamComponent() {
        return CommandComponent.<C, PlayerTeam>builder().parser(teamParser());
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull CommandInput input
    ) {
        return new ArrayList<>(commandContext.get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER).getAllTeams());
    }

    @Override
    protected ArgumentParser.@NonNull FutureArgumentParser<C, String> intermediateParser() {
        return (ctx, commandInput) -> ArgumentParseResult.successFuture(commandInput.readString());
    }

    @Override
    protected @NonNull CompletableFuture<@NonNull ArgumentParseResult<PlayerTeam>> resolveClient(
        final @NonNull CommandContext<C> context,
        final @NonNull String value
    ) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            throw new IllegalStateException();
        }
        final PlayerTeam result = connection.getLevel().getScoreboard().getPlayerTeam(value);
        if (result == null) {
            return ArgumentParseResult.failureFuture(new UnknownTeamException(context, value));
        }
        return ArgumentParseResult.successFuture(result);
    }

    @Override
    protected @NonNull CompletableFuture<@NonNull ArgumentParseResult<PlayerTeam>> resolveServer(
        final @NonNull CommandContext<C> context,
        final @NonNull String value
    ) {
        final CommandSourceStack source = (CommandSourceStack) context.get(ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER);
        final PlayerTeam result = source.getLevel().getScoreboard().getPlayerTeam(value);
        if (result == null) {
            return ArgumentParseResult.failureFuture(new UnknownTeamException(context, value));
        }
        return ArgumentParseResult.successFuture(result);
    }

    /**
     * Exception for when a team cannot be found for supplied input.
     */
    public static final class UnknownTeamException extends ParserException {


        UnknownTeamException(
            final @NonNull CommandContext<?> context,
            final @NonNull String input
        ) {
            super(
                TeamParser.class,
                context,
                ModdedCaptionKeys.ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN,
                CaptionVariable.of("input", input)
            );
        }
    }
}
