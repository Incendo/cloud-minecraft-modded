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
package cloud.commandframework.minecraft.modded;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Provides the default captions for messages in cloud-minecraft-modded-common.
 *
 * @param <C> command sender type
 */
public final class ModdedDefaultCaptionsProvider<C> extends DelegatingCaptionProvider<C> {

    /**
     * Default caption for {@link ModdedCaptionKeys#ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY}
     */
    public static final String ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY =
        "Could not find value with key '<id>' in registry '<registry>'.";

    /**
     * Default caption for {@link ModdedCaptionKeys#ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN}
     */
    public static final String ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN = "Could not find any team named '<input>'!";

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
        .putCaption(
            ModdedCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
            ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY
        ).putCaption(
            ModdedCaptionKeys.ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN,
            ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN
        )
        .build();

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
