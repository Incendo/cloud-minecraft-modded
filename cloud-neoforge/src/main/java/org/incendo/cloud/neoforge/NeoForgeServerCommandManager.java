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
package org.incendo.cloud.neoforge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.modded.internal.ModdedParserMappings;

public final class NeoForgeServerCommandManager<C> extends NeoForgeCommandManager<C> {

    private final Cache<String, PermissionNode<Boolean>> permissionNodeCache = CacheBuilder.newBuilder().maximumSize(100).build();

    /**
     * Create a command manager using native source types.
     *
     * @param executionCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #NeoForgeServerCommandManager(ExecutionCoordinator, SenderMapper) for a more thorough explanation
     */
    public static NeoForgeServerCommandManager<CommandSourceStack> createNative(
        final ExecutionCoordinator<CommandSourceStack> executionCoordinator
    ) {
        return new NeoForgeServerCommandManager<>(executionCoordinator, SenderMapper.identity());
    }

    /**
     * Create a new command manager instance.
     *
     * @param executionCoordinator       Execution coordinator instance.
     * @param senderMapper               Mapper between Minecraft's {@link CommandSourceStack} and the command sender type {@code C}.
     */
    public NeoForgeServerCommandManager(
        final ExecutionCoordinator<C> executionCoordinator,
        final SenderMapper<CommandSourceStack, C> senderMapper
    ) {
        super(
            executionCoordinator,
            senderMapper,
            new NeoForgeCommandRegistrationHandler.Server<>()
        );

        if (CloudNeoForgeEntrypoint.hasServerAlreadyStarted()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " was created too late! Because command registration "
                + "occurs before the server instance is created, commands should be registered in mod initializers.");
        }

        ModdedParserMappings.registerServer(this);
    }

    /**
     * Check if the command sender has the required permission. If the permission node is
     * empty, this should return {@code true}
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return {@code true} if the sender has the permission, else {@code false}
     * @throws PermissionNotRegisteredException if the permission is not registered to NeoForge
     */
    @SuppressWarnings({"unchecked", "ReferenceEquality"})
    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        final CommandSourceStack source = this.senderMapper().reverse(sender);
        if (source.isPlayer()) {
            final PermissionNode<Boolean> node;
            try {
                node = this.permissionNodeCache.get(permission, () -> (PermissionNode<Boolean>) PermissionAPI.getRegisteredNodes().stream()
                    .filter(n -> n.getNodeName().equals(permission) && n.getType() == PermissionTypes.BOOLEAN)
                    .findFirst()
                    .orElseThrow(() -> new PermissionNotRegisteredException(permission)));
            } catch (final UncheckedExecutionException e) {
                if (e.getCause() instanceof PermissionNotRegisteredException notRegisteredException) {
                    // PermissionNotRegisteredException is unchecked, so Cache#get will throw UncheckedExecutionException
                    throw notRegisteredException;
                }
                throw new RuntimeException("Exception location permission node " + permission, e);
            } catch (final ExecutionException e) {
                throw new RuntimeException("Exception location permission node " + permission, e);
            }
            return PermissionAPI.getPermission(source.getPlayer(), node);
        }
        return source.hasPermission(source.getServer().getOperatorUserPermissionLevel());
    }

    @Override
    public boolean isClientCommandManager() {
        return false;
    }
}
