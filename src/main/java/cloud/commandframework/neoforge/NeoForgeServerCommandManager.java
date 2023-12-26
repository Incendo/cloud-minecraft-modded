/*
 * MIT License
 *
 * Copyright (c) 2023 Cloud Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.commandframework.neoforge;

import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public final class NeoForgeServerCommandManager<C> extends NeoForgeCommandManager<C> {

    public static final CloudKey<Commands.CommandSelection> META_REGISTRATION_ENVIRONMENT = CloudKey.of(
        "cloud:registration-environment",
        Commands.CommandSelection.class
    );

    private final Cache<String, PermissionNode<Boolean>> permissionNodeCache = CacheBuilder.newBuilder().maximumSize(100).build();

    public static NeoForgeServerCommandManager<CommandSourceStack> createNative(
        final Function<CommandTree<CommandSourceStack>,
            CommandExecutionCoordinator<CommandSourceStack>> execCoordinator
    ) {
        return new NeoForgeServerCommandManager<>(execCoordinator, Function.identity(), Function.identity());
    }

    public NeoForgeServerCommandManager(
        final Function<CommandTree<C>,
            CommandExecutionCoordinator<C>> commandExecutionCoordinator,
        final Function<CommandSourceStack, C> commandSourceMapper,
        final Function<C, CommandSourceStack> backwardsCommandSourceMapper
    ) {
        super(
            commandExecutionCoordinator,
            commandSourceMapper,
            backwardsCommandSourceMapper,
            new NeoForgeCommandRegistrationHandler.Server<>(),
            () -> new CommandSourceStack(
                CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                null,
                4,
                "",
                Component.empty(),
                null,
                null
            )
        );

        if (CloudNeoForgeEntrypoint.hasServerAlreadyStarted()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " was created too late! Because command registration "
                + "occurs before the server instance is created, commands should be registered in mod initializers.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasPermission(final C sender, final String permission) {
        final CommandSourceStack source = this.backwardsCommandSourceMapper().apply(sender);
        if (source.isPlayer()) {
            final PermissionNode<Boolean> node;
            try {
                node = this.permissionNodeCache.get(permission, () -> (PermissionNode<Boolean>) PermissionAPI.getRegisteredNodes().stream()
                    .filter(n -> n.getNodeName().equals(permission) && n.getType() == PermissionTypes.BOOLEAN)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find registered node for permission " + permission)));
            } catch (final ExecutionException e) {
                throw new RuntimeException("Exception location permission node", e);
            }
            return PermissionAPI.getPermission(source.getPlayer(), node);
        }
        return source.hasPermission(source.getServer().getOperatorUserPermissionLevel());
    }
}
