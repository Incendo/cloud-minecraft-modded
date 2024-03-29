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
package org.incendo.cloud.minecraft.modded.data;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A selector string to query multiple entity-like values.
 *
 * @param <V> value type
 */
public interface Selector<V> {

    /**
     * Get the raw string associated with the selector.
     *
     * @return the input
     */
    @NonNull String inputString();

    /**
     * If this value came from a parsed {@link EntitySelector}, this will provide the details of that selector.
     *
     * @return the selector
     */
    @Nullable EntitySelector selector();

    /**
     * Get the value of this selector.
     *
     * <p>A successfully parsed selector must match one or more values.</p>
     *
     * @return all matched entities
     */
    @NonNull Collection<V> values();

    /**
     * A specialized {@link Selector} that can only return one value.
     *
     * @param <V> value type
     */
    interface Single<V> extends Selector<V> {

        @Override
        default @NonNull Collection<V> values() {
            return Collections.singletonList(this.single());
        }

        /**
         * Get the single value from this selector.
         *
         * @return the value
         */
        @NonNull V single();
    }
}
