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

import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * An argument parsing a {@link ResourceLocation}.
 *
 * @param <C> the sender type
 */
public final class ResourceLocationParser<C> extends WrappedBrigadierParser<C, ResourceLocation> {

    /**
     * Creates a new resource location parser.
     *
     * @param <C> command sender type
     * @return the created parser
     */
    public static <C> @NonNull ParserDescriptor<C, ResourceLocation> resourceLocationParser() {
        return ParserDescriptor.of(new ResourceLocationParser<>(), ResourceLocation.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #resourceLocationParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    public static <C> CommandComponent.@NonNull Builder<C, ResourceLocation> resourceLocationComponent() {
        return CommandComponent.<C, ResourceLocation>builder().parser(resourceLocationParser());
    }

    ResourceLocationParser() {
        super(net.minecraft.commands.arguments.ResourceLocationArgument.id());
    }

}
