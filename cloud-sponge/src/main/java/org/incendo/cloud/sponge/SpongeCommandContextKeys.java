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
package org.incendo.cloud.sponge;

import io.leangen.geantyref.TypeToken;
import org.incendo.cloud.key.CloudKey;
import org.spongepowered.api.command.CommandCause;

/**
 * Sponge related {@link org.incendo.cloud.context.CommandContext} keys.
 */
public final class SpongeCommandContextKeys {

    /**
     * The Sponge native {@link org.spongepowered.api.command.CommandCause} instance is stored in the {@link org.incendo.cloud.context.CommandContext}
     * by {@link SpongeCommandPreprocessor}
     */
    public static final CloudKey<CommandCause> COMMAND_CAUSE = CloudKey.of(
        "cloud:sponge_command_cause",
        TypeToken.get(CommandCause.class)
    );

    private SpongeCommandContextKeys() {
    }

}