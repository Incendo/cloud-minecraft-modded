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

import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link net.minecraft.commands.arguments.coordinates.Coordinates} wrapper for easier use with cloud commands.
 */
public interface Coordinates {

    /**
     * Resolve a position from the parsed coordinates.
     *
     * @return position
     */
    @NonNull Vec3 position();

    /**
     * Resolve a block position from the parsed coordinates.
     *
     * @return block position
     */
    @NonNull BlockPos blockPos();

    /**
     * Get whether the x coordinate is relative.
     *
     * @return whether the x coordinate is relative
     */
    boolean isXRelative();

    /**
     * Get whether the y coordinate is relative.
     *
     * @return whether the y coordinate is relative
     */
    boolean isYRelative();

    /**
     * Get whether the z coordinate is relative.
     *
     * @return whether the z coordinate is relative
     */
    boolean isZRelative();

    /**
     * Get the coordinates wrapped by this {@link Coordinates}.
     *
     * @return the base coordinates
     */
    net.minecraft.commands.arguments.coordinates.@NonNull Coordinates wrappedCoordinates();

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link Vec2Argument},
     * which accepts two doubles for the x and z coordinate, always defaulting to 0 for the y coordinate.
     */
    interface CoordinatesXZ extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link BlockPosArgument}.
     */
    interface BlockCoordinates extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link ColumnPosArgument}.
     */
    interface ColumnCoordinates extends Coordinates {

    }
}
