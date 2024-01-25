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
package cloud.commandframework.sponge.suggestion;

import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.common.adventure.SpongeAdventure;

/**
 * {@link Suggestion} that has an optional {@link net.kyori.adventure.text.Component} tooltip.
 */
public interface SpongeSuggestion extends Suggestion {

    /**
     * Returns a new {@link SpongeSuggestion} with the given {@code suggestion} and {@code tooltip}.
     *
     * @param suggestion the suggestion
     * @param tooltip    the optional tooltip that is displayed when hovering over the suggestion
     * @return the suggestion instance
     */
    static @NonNull SpongeSuggestion suggestion(
        final @NonNull String suggestion,
        final @Nullable Component tooltip
    ) {
        return new SpongeSuggestionImpl(suggestion, tooltip);
    }

    /**
     * Returns a new {@link SpongeSuggestion} that uses the given {@code suggestion} and has a {@code null} tooltip.
     *
     * @param suggestion the suggestion
     * @return the suggestion instance
     */
    static @NonNull SpongeSuggestion spongeSuggestion(
        final @NonNull Suggestion suggestion
    ) {
        if (suggestion instanceof SpongeSuggestion) {
            return (SpongeSuggestion) suggestion;
        }
        if (suggestion instanceof TooltipSuggestion tooltipSuggestion) {
            final @Nullable Component tooltip = Optional.ofNullable(tooltipSuggestion.tooltip())
                .map(ComponentUtils::fromMessage)
                .map(SpongeAdventure::asAdventure)
                .orElse(null);
            return suggestion(tooltipSuggestion.suggestion(), tooltip);
        }
        return suggestion(suggestion.suggestion(), null /* tooltip */);
    }

    /**
     * Returns the tooltip.
     *
     * @return the tooltip, or {@code null}
     */
    @Nullable Component tooltip();

    @Override
    default @NonNull SpongeSuggestion withSuggestion(@NonNull String string) {
        return suggestion(string, this.tooltip());
    }

    /**
     * Returns a copy of this suggestion instance using the given {@code tooltip}
     *
     * @param tooltip the new tooltip
     * @return the new suggestion
     */
    default @NonNull SpongeSuggestion withTooltip(@NonNull Component tooltip) {
        return suggestion(this.suggestion(), tooltip);
    }

}
