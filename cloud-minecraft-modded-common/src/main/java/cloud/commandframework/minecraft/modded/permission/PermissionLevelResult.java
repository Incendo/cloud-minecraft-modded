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
package cloud.commandframework.minecraft.modded.permission;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.internal.ImmutableImpl;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;

/**
 * A {@link PermissionResult} that also contains the permission level that was required for the permission check to pass.
 */
@ImmutableImpl
@Value.Immutable
@SuppressWarnings("immutables:subtype")
public interface PermissionLevelResult extends PermissionResult {

    /**
     * Creates a new PermissionLevelResult.
     *
     * @param result                  {@code true} if the command may be executed, {@code false} otherwise
     * @param permission              permission that this result came from
     * @param requiredPermissionLevel minecraft permission level that was required for the permission check to pass
     * @return the created result
     */
    static @NonNull PermissionLevelResult of(
        final boolean result,
        final @NonNull Permission permission,
        final int requiredPermissionLevel
    ) {
        return PermissionLevelResultImpl.of(result, permission, requiredPermissionLevel);
    }

    @Override
    boolean allowed();

    @Override
    @NonNull Permission permission();

    /**
     * Returns the minecraft permission level that was required for the permission lookup to return true.
     *
     * @return the required permission level
     */
    @SuppressWarnings("unused")
    int requiredPermissionLevel();
}
