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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.permission.AndPermission;
import org.incendo.cloud.permission.OrPermission;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PredicatePermission;

import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@API(status = API.Status.INTERNAL)
@Mod("cloud")
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class CloudNeoForgeEntrypoint {
    private static boolean serverStartingCalled;

    /**
     * Creates a {@link CloudNeoForgeEntrypoint}.
     */
    public CloudNeoForgeEntrypoint() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (ServerStartingEvent event) -> serverStartingCalled = true);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CloudNeoForgeEntrypoint::registerPermissions);

        if (Boolean.getBoolean("cloud.test_commands")) {
            testServerManager();
            testClientManager();
        }
    }

    /**
     * Returns whether {@link ServerStartingEvent} was called already.
     *
     * @return whether the server started already
     */
    public static boolean hasServerAlreadyStarted() {
        return serverStartingCalled;
    }

    private static void registerPermissions(final PermissionGatherEvent.Nodes event) {
        event.addNodes(new PermissionNode<>(
            "cloud",
            "hover-stacktrace",
            PermissionTypes.BOOLEAN,
            CloudNeoForgeEntrypoint::defaultPermissionHandler
        ));
        for (final NeoForgeCommandManager<?> manager : NeoForgeServerCommandManager.INSTANCES) {
            registerPermissionsForManager(event, manager);
        }
    }

    private static void registerPermissionsForManager(final PermissionGatherEvent.Nodes event, final NeoForgeCommandManager<?> manager) {
        final Set<String> permissions = new HashSet<>();
        collectPermissions(permissions, manager.commandTree().rootNodes());
        permissions.stream()
            .filter(permissionString -> event.getNodes().stream().noneMatch(node -> node.getNodeName().equals(permissionString)))
            .map(permissionString -> {
                final int i = permissionString.indexOf(".");
                return new PermissionNode<>(
                    permissionString.substring(0, i),
                    permissionString.substring(i + 1),
                    PermissionTypes.BOOLEAN,
                    CloudNeoForgeEntrypoint::defaultPermissionHandler
                );
            })
            .forEach(event::addNodes);
    }

    private static <C> void collectPermissions(
        final Set<String> permissions,
        final Collection<CommandNode<C>> nodes
    ) {
        for (final CommandNode<C> node : nodes) {
            final @Nullable Command<C> owningCommand = node.command();
            if (owningCommand != null) {
                recurseCommandPermission(permissions, owningCommand.commandPermission());
            }
            collectPermissions(permissions, node.children());
        }
    }

    private static void recurseCommandPermission(final Set<String> permissions, final Permission permission) {
        if (permission instanceof PredicatePermission<?> || permission == Permission.empty()) {
            return;
        }
        if (permission instanceof OrPermission || permission instanceof AndPermission) {
            for (final Permission child : permission.permissions()) {
                recurseCommandPermission(permissions, child);
            }
        } else {
            permissions.add(permission.permissionString());
        }
    }

    private static Boolean defaultPermissionHandler(
        final @Nullable ServerPlayer player,
        final UUID uuid,
        final PermissionDynamicContext<?>... contexts
    ) {
        return player != null && player.hasPermissions(player.server.getOperatorUserPermissionLevel());
    }

    private static void testClientManager() {
        final NeoForgeClientCommandManager<CommandSourceStack> manager =
            NeoForgeClientCommandManager.createNative(ExecutionCoordinator.simpleCoordinator());
        manager.command(manager.commandBuilder("cloud_client")
            .literal("forge")
            .required("string", greedyStringParser())
            .handler(ctx -> ctx.sender().sendSystemMessage(Component.literal(ctx.get("string")))));
    }

    private static void testServerManager() {
        final NeoForgeServerCommandManager<CommandSourceStack> manager =
            NeoForgeServerCommandManager.createNative(ExecutionCoordinator.simpleCoordinator());
        manager.command(manager.commandBuilder("cloud")
            .literal("forge")
            .required("string", greedyStringParser())
            .permission("cloud.hello")
            .handler(ctx -> ctx.sender().sendSystemMessage(Component.literal(ctx.get("string")))));
    }
}
