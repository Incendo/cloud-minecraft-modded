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
package org.incendo.cloud.minecraft.modded.caption;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionVariable;

import static java.util.Objects.requireNonNull;

public final class MinecraftCaptionFormatter<C> implements CaptionFormatter<C, Component> {
    private final Pattern pattern;

    /**
     * Creates a new {@link MinecraftCaptionFormatter}.
     *
     * @param pattern pattern
     */
    public MinecraftCaptionFormatter(final @NonNull Pattern pattern) {
        this.pattern = requireNonNull(pattern, "pattern");
    }

    /**
     * Creates a new {@link MinecraftCaptionFormatter} with the default {@code <(\S+)>} pattern.
     */
    public MinecraftCaptionFormatter() {
        this(Pattern.compile("<(\\S+)>"));
    }

    @Override
    public @NonNull Component formatCaption(
        final @NonNull Caption captionKey,
        final @NonNull C recipient,
        final @NonNull String caption,
        final @NonNull Collection<@NonNull CaptionVariable> variables
    ) {
        final Map<String, CaptionVariable> byKey = variables.stream().collect(Collectors.toMap(CaptionVariable::key, Function.identity()));
        final MutableComponent component = Component.empty();

        final String[] split = this.pattern.split(caption);
        final Matcher matcher = this.pattern.matcher(caption);
        for (int i = 0; i < split.length; i++) {
            component.append(split[i]);
            if (i != split.length - 1) {
                if (!matcher.find()) {
                    throw new IllegalStateException();
                }
                final String name = matcher.group(1);
                final CaptionVariable variable = byKey.getOrDefault(name, CaptionVariable.of(name, matcher.group(0)));
                if (variable instanceof MinecraftVariable mc) {
                    component.append(mc.componentValue());
                } else {
                    component.append(variable.value());
                }
            }
        }

        return component;
    }
}
