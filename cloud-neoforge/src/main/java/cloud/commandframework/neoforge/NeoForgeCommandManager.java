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
package cloud.commandframework.neoforge;

import cloud.commandframework.minecraft.modded.ModdedDefaultCaptionsProvider;
import cloud.commandframework.minecraft.modded.internal.ModdedExceptionHandler;
import cloud.commandframework.minecraft.modded.internal.ModdedParserMappings;
import cloud.commandframework.minecraft.modded.internal.ModdedPreprocessor;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.meta.SimpleCommandMeta;
import org.incendo.cloud.suggestion.SuggestionFactory;

@DefaultQualifier(NonNull.class)
public abstract class NeoForgeCommandManager<C> extends CommandManager<C>
    implements BrigadierManagerHolder<C, CommandSourceStack>, SenderMapperHolder<CommandSourceStack, C> {

    static final Set<NeoForgeCommandManager<?>> INSTANCES = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private final SenderMapper<CommandSourceStack, C> senderMapper;
    private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    protected NeoForgeCommandManager(
        final ExecutionCoordinator<C> executionCoordinator,
        final SenderMapper<CommandSourceStack, C> senderMapper,
        final NeoForgeCommandRegistrationHandler<C> registrationHandler,
        final Supplier<CommandSourceStack> dummyCommandSourceProvider
    ) {
        super(executionCoordinator, registrationHandler);
        INSTANCES.add(this);
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);
        this.brigadierManager = new CloudBrigadierManager<>(this, () -> new CommandContext<>(
            this.senderMapper.map(dummyCommandSourceProvider.get()),
            this
        ), senderMapper);
        ModdedExceptionHandler.registerDefaults(this, CommandSourceStack::sendFailure, CommandSourceStack::getTextName);
        registrationHandler.initialize(this);
        this.captionRegistry().registerProvider(new ModdedDefaultCaptionsProvider<>());
        this.registerCommandPreProcessor(new ModdedPreprocessor<>(senderMapper));

        ModdedParserMappings.register(this, this.brigadierManager);
    }

    @Override
    public final SenderMapper<CommandSourceStack, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    @Override
    public final CloudBrigadierManager<C, CommandSourceStack> brigadierManager() {
        return this.brigadierManager;
    }

    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    final void registrationCalled() {
        this.lockRegistration();
    }

}
