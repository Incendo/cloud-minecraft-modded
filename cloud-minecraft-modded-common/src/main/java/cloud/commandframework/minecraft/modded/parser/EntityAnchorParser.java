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

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * An argument parsing an {@link Anchor}.
 *
 * @param <C> the sender type
 */
public final class EntityAnchorParser<C> extends WrappedBrigadierParser<C, Anchor> {

    /**
     * Creates a new entity anchor parser.
     *
     * @param <C> command sender type
     * @return the created parser
     */
    public static <C> @NonNull ParserDescriptor<C, Anchor> entityAnchorParser() {
        return ParserDescriptor.of(new EntityAnchorParser<>(), Anchor.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #entityAnchorParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    public static <C> CommandComponent.@NonNull Builder<C, Anchor> entityAnchorComponent() {
        return CommandComponent.<C, Anchor>builder().parser(entityAnchorParser());
    }

    EntityAnchorParser() {
        super(net.minecraft.commands.arguments.EntityAnchorArgument.anchor());
    }
}
