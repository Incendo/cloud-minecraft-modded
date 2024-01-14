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
package cloud.commandframework.neoforge;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.permission.PredicatePermission;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A command manager for registering client-side commands.
 *
 * <p>All commands should be registered within mod initializers. Any registrations occurring after the first call to
 * {@link net.neoforged.neoforge.client.event.RegisterClientCommandsEvent} will be considered <em>unsafe</em>, and will only be permitted when the unsafe
 * registration manager option is enabled.</p>
 *
 * @param <C> the command sender type
 */
public final class NeoForgeClientCommandManager<C> extends NeoForgeCommandManager<C> {

    /**
     * Create a command manager using native source types.
     *
     * @param executionCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #NeoForgeClientCommandManager(ExecutionCoordinator, SenderMapper) for a more thorough explanation
     */
    public static NeoForgeClientCommandManager<CommandSourceStack> createNative(
        final ExecutionCoordinator<CommandSourceStack> executionCoordinator
    ) {
        return new NeoForgeClientCommandManager<>(executionCoordinator, SenderMapper.identity());
    }

    /**
     * Create a new command manager instance.
     *
     * @param executionCoordinator       Execution coordinator instance.
     * @param senderMapper               Mapper between Minecraft's {@link CommandSourceStack} and the command sender type {@code C}.
     */
    public NeoForgeClientCommandManager(
        final ExecutionCoordinator<C> executionCoordinator,
        final SenderMapper<CommandSourceStack, C> senderMapper
    ) {
        super(
            executionCoordinator,
            senderMapper,
            new NeoForgeCommandRegistrationHandler.Client<>(),
            () -> new ClientCommandSourceStack(
                CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                4,
                "",
                Component.empty(),
                null
            )
        );

        this.registerParsers();
    }

    private void registerParsers() {
    }

    /**
     * Check if a sender has a certain permission.
     *
     * <p>The implementation for client commands always returns true.</p>
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return whether the sender has the specified permission
     */
    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return true;
    }

    /**
     * Get a permission predicate which passes when the integrated server is running.
     *
     * @param <C> sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> integratedServerRunning() {
        return PredicatePermission.of(
            CloudKey.of("integrated-server-running"),
            sender -> Minecraft.getInstance().hasSingleplayerServer()
        );
    }

    /**
     * Get a permission predicate which passes when the integrated server is not running.
     *
     * @param <C> sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> integratedServerNotRunning() {
        return PredicatePermission.of(
            CloudKey.of("integrated-server-not-running"),
            sender -> !Minecraft.getInstance().hasSingleplayerServer()
        );
    }

    /**
     * Get a permission predicate which passes when cheats are enabled on the currently running integrated server.
     *
     * <p>This predicate will always pass if there is no integrated server running, i.e. when connected to a multiplayer server.</p>
     *
     * @param <C> sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> cheatsAllowed() {
        return cheatsAllowed(true);
    }

    /**
     * Get a permission predicate which passes when cheats are enabled on the currently running integrated server.
     *
     * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
     * fall back to the provided boolean argument.</p>
     *
     * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
     * @param <C>                sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> cheatsAllowed(final boolean allowOnMultiplayer) {
        return PredicatePermission.of(CloudKey.of("cheats-allowed"), sender -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) {
                return allowOnMultiplayer;
            }
            return Minecraft.getInstance().getSingleplayerServer().getPlayerList().isAllowCheatsForAllPlayers()
                || Minecraft.getInstance().getSingleplayerServer().getWorldData().getAllowCommands();
        });
    }

    /**
     * Get a permission predicate which passes when cheats are disabled on the currently running integrated server.
     *
     * <p>This predicate will always pass if there is no integrated server running, i.e. when connected to a multiplayer server.</p>
     *
     * @param <C> sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> cheatsDisallowed() {
        return cheatsDisallowed(true);
    }

    /**
     * Get a permission predicate which passes when cheats are disabled on the currently running integrated server.
     *
     * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
     * fall back to the provided boolean argument.</p>
     *
     * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
     * @param <C>                sender type
     * @return a predicate permission
     */
    public static <C> @NonNull PredicatePermission<C> cheatsDisallowed(final boolean allowOnMultiplayer) {
        return PredicatePermission.of(CloudKey.of("cheats-disallowed"), sender -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) {
                return allowOnMultiplayer;
            }
            return !Minecraft.getInstance().getSingleplayerServer().getPlayerList().isAllowCheatsForAllPlayers()
                && !Minecraft.getInstance().getSingleplayerServer().getWorldData().getAllowCommands();
        });
    }
}
