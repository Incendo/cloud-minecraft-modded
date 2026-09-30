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
package org.incendo.cloud.fabric;

import net.minecraft.commands.SharedSuggestionProvider;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.modded.ModdedCommandManager;
import org.incendo.cloud.minecraft.modded.caption.ModdedDefaultCaptionsProvider;
import org.incendo.cloud.minecraft.modded.internal.ModdedParserMappings;
import org.incendo.cloud.minecraft.modded.internal.ModdedPreprocessor;
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
@DefaultQualifier(NonNull.class)
public abstract class FabricCommandManager<C, S extends SharedSuggestionProvider> extends ModdedCommandManager<C, S> {

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
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings("unchecked")
    FabricCommandManager(
        final ExecutionCoordinator<C> commandExecutionCoordinator,
        final SenderMapper<S, C> senderMapper,
        final FabricCommandRegistrationHandler<C, S> registrationHandler
    ) {
        super(commandExecutionCoordinator, registrationHandler);
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);

        // We're always brigadier
        this.brigadierManager = new CloudBrigadierManager<>(
            this,
            this.senderMapper
        );

        ModdedParserMappings.register(this, this.brigadierManager);
        this.captionRegistry().registerProvider(new ModdedDefaultCaptionsProvider<>());
        this.registerCommandPreProcessor(new ModdedPreprocessor<>(senderMapper));

        ((FabricCommandRegistrationHandler<C, S>) this.commandRegistrationHandler()).initialize(this);
    }

    @Override
    public final SenderMapper<S, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
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
    public final CloudBrigadierManager<C, S> brigadierManager() {
        return this.brigadierManager;
    }

    /* transition state to prevent further registration */
    final void registrationCalled() {
        this.lockRegistration();
    }
}
