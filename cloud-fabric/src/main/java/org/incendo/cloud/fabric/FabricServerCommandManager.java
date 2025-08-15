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
package org.incendo.cloud.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.internal.LateRegistrationCatcher;
import org.incendo.cloud.minecraft.modded.caption.MinecraftCaptionFormatter;
import org.incendo.cloud.minecraft.modded.internal.ModdedExceptionHandler;
import org.incendo.cloud.minecraft.modded.internal.ModdedParserMappings;

/**
 * A command manager for registering server-side commands.
 *
 * <p>All commands should be registered within mod initializers. Any registrations occurring after the first call to
 * {@link CommandRegistrationCallback} will be considered <em>unsafe</em>, and will only be permitted when the unsafe
 * registration manager option is enabled.</p>
 *
 * @param <C> the command sender type
 * @since 1.5.0
 */
public final class FabricServerCommandManager<C> extends FabricCommandManager<C, CommandSourceStack> {

    private static final boolean HAS_PERMISSIONS_API = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    /**
     * Create a command manager using native source types.
     *
     * @param execCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #FabricServerCommandManager(ExecutionCoordinator, SenderMapper) for a more thorough explanation
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull FabricServerCommandManager<@NonNull CommandSourceStack> createNative(
        final @NonNull ExecutionCoordinator<CommandSourceStack> execCoordinator
    ) {
        return new FabricServerCommandManager<>(execCoordinator, SenderMapper.identity());
    }

    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link ExecutionCoordinator#asyncCoordinator()}
     * @param senderMapper                Function that maps {@link CommandSourceStack} to the command sender type
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public FabricServerCommandManager(
        final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
        final @NonNull SenderMapper<CommandSourceStack, C> senderMapper
    ) {
        super(
            commandExecutionCoordinator,
            senderMapper,
            new FabricCommandRegistrationHandler.Server<>()
        );

        if (LateRegistrationCatcher.hasServerAlreadyStarted()) {
            throw new IllegalStateException("FabricServerCommandManager was created too late! Because command registration "
                + "occurs before the server instance is created, commands should be registered in mod initializers.");
        }

        ModdedParserMappings.registerServer(this);
        ModdedExceptionHandler.registerDefaults(this, new MinecraftCaptionFormatter<>());
    }

    /**
     * Check if a sender has a certain permission.
     *
     * <p>The current implementation checks permissions using {@code fabric-permissions-api-v0},
     * falling back to op level checks.</p>
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return whether the sender has the specified permission
     * @since 1.5.0
     */
    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        final CommandSourceStack source = this.senderMapper().reverse(sender);
        if (HAS_PERMISSIONS_API) {
            return Permissions.check(source, permission, source.getServer().getOperatorUserPermissionLevel());
        }
        return source.hasPermission(source.getServer().getOperatorUserPermissionLevel());
    }

    @Override
    public boolean isClientCommandManager() {
        return false;
    }
}
