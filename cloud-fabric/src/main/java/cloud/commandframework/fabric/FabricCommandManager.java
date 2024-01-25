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
package cloud.commandframework.fabric;

import cloud.commandframework.minecraft.modded.ModdedDefaultCaptionsProvider;
import cloud.commandframework.minecraft.modded.internal.ModdedParserMappings;
import cloud.commandframework.minecraft.modded.internal.ModdedPreprocessor;
import java.util.function.Supplier;
import net.minecraft.commands.SharedSuggestionProvider;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.suggestion.SuggestionFactory;

/**
 * A command manager for either the server or client on Fabric.
 *
 * <p>Commands registered with managers of this type will be registered into a Brigadier command tree.</p>
 *
 * <p>Where possible, Vanilla argument types are made available in a cloud-friendly format. In some cases, these argument
 * types may only be available for server commands. Mod-provided argument types can be exposed to Cloud as well, by using
 * {@link org.incendo.cloud.brigadier.parser.WrappedBrigadierParser}.</p>
 *
 * @param <C> the manager's sender type
 * @param <S> the platform sender type
 * @see FabricServerCommandManager for server commands
 * @since 1.5.0
 */
public abstract class FabricCommandManager<C, S extends SharedSuggestionProvider> extends CommandManager<C> implements
    BrigadierManagerHolder<C, S>, SenderMapperHolder<S, C> {

    private final SenderMapper<S, C> senderMapper;
    private final CloudBrigadierManager<C, S> brigadierManager;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;


    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link ExecutionCoordinator#asyncCoordinator()}
     * @param senderMapper                Function that maps {@link SharedSuggestionProvider} to the command sender type
     * @param registrationHandler         the handler accepting command registrations
     * @param dummyCommandSourceProvider  a provider of a dummy command source, for use with brigadier registration
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings("unchecked")
    FabricCommandManager(
        final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
        final @NonNull SenderMapper<S, C> senderMapper,
        final @NonNull FabricCommandRegistrationHandler<C, S> registrationHandler,
        final @NonNull Supplier<S> dummyCommandSourceProvider
    ) {
        super(commandExecutionCoordinator, registrationHandler);
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);

        // We're always brigadier
        this.brigadierManager = new CloudBrigadierManager<>(
            this,
            () -> new CommandContext<>(
                // This looks ugly, but it's what the server does when loading datapack functions in 1.16+
                // See net.minecraft.server.function.FunctionLoader.reload for reference
                this.senderMapper.map(dummyCommandSourceProvider.get()),
                this
            ),
            this.senderMapper
        );

        ModdedParserMappings.register(this, this.brigadierManager);
        this.captionRegistry().registerProvider(new ModdedDefaultCaptionsProvider<>());
        this.registerCommandPreProcessor(new ModdedPreprocessor<>(senderMapper));

        ((FabricCommandRegistrationHandler<C, S>) this.commandRegistrationHandler()).initialize(this);
    }

    @Override
    public final @NonNull SenderMapper<S, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will always return true for {@link FabricCommandManager}s.</p>
     *
     * @return {@code true}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link FabricCommandManager}s always use Brigadier for registration, so the aforementioned check is not needed.</p>
     *
     * @return {@inheritDoc}
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final @NonNull CloudBrigadierManager<C, S> brigadierManager() {
        return this.brigadierManager;
    }

    /* transition state to prevent further registration */
    final void registrationCalled() {
        this.lockRegistration();
    }
}
