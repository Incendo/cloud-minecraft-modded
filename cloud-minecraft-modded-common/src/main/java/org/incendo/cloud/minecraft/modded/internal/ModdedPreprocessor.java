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

import net.minecraft.commands.SharedSuggestionProvider;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;
import org.incendo.cloud.minecraft.modded.ModdedCommandContextKeys;

@API(status = API.Status.INTERNAL)
public final class ModdedPreprocessor<C> implements CommandPreprocessor<C> {

    private final SenderMapper<? extends SharedSuggestionProvider, C> mapper;

    /**
     * Creates a new processor.
     *
     * @param mapper sender mapper
     */
    public ModdedPreprocessor(final SenderMapper<? extends SharedSuggestionProvider, C> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void accept(final @NonNull CommandPreprocessingContext<C> context) {
        context.commandContext().store(
            ModdedCommandContextKeys.SHARED_SUGGESTION_PROVIDER,
            this.mapper.reverse(context.commandContext().sender())
        );
    }
}
