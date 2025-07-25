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
package org.incendo.cloud.minecraft.modded;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.PermissionSource;
import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.modded.permission.PermissionLevelResult;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.permission.PredicatePermission;

public final class ModdedPredicatePermissions {
    private ModdedPredicatePermissions() {
    }

    /**
     * Get a permission predicate which passes when the sender has the specified permission level.
     *
     * @param <C>             command sender type
     * @param mapperHolder    sender mapper holder, usually a command manager
     * @param permissionLevel permission level to require
     * @return a permission predicate that will provide {@link PermissionLevelResult}s
     */
    public static <C> @NonNull PredicatePermission<C> permissionLevel(
        final SenderMapperHolder<? extends SharedSuggestionProvider, C> mapperHolder,
        final int permissionLevel
    ) {
        return new PredicatePermission<>() {
            @Override
            public @NonNull PermissionResult testPermission(final @NonNull C sender) {
                final SharedSuggestionProvider suggestionProvider = mapperHolder.senderMapper().reverse(sender);
                return PermissionLevelResult.of(
                    suggestionProvider instanceof PermissionSource permissionSource && permissionSource.hasPermission(permissionLevel),
                    this,
                    permissionLevel
                );
            }
        };
    }

    public static final class Client {
        private Client() {
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
         * Get a permission predicate which passes when commands are enabled on the currently running integrated server.
         *
         * <p>This predicate will always pass if there is no integrated server running, i.e. when connected to a multiplayer server.</p>
         *
         * @param <C> sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsAllowed() {
            return commandsAllowed(true);
        }

        /**
         * Get a permission predicate which passes when commands are enabled on the currently running integrated server.
         *
         * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
         * fall back to the provided boolean argument.</p>
         *
         * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
         * @param <C>                sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsAllowed(final boolean allowOnMultiplayer) {
            return commandsAllowed(PredicatePermission.of(sender -> allowOnMultiplayer));
        }

        /**
         * Get a permission predicate which passes when commands are enabled on the currently running integrated server.
         *
         * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
         * fall back to the provided predicate argument.</p>
         *
         * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
         * @param <C>                sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsAllowed(final @NonNull PredicatePermission<C> allowOnMultiplayer) {
            return new PredicatePermission<>() {
                private static final CloudKey<Void> KEY = CloudKey.of("commands-allowed");

                @Override
                public @NonNull CloudKey<Void> key() {
                    return KEY;
                }

                @Override
                public @NonNull PermissionResult testPermission(final @NonNull C sender) {
                    if (!Minecraft.getInstance().hasSingleplayerServer()) {
                        return allowOnMultiplayer.testPermission(sender);
                    }
                    return PermissionResult.of(
                        Minecraft.getInstance().getSingleplayerServer().getPlayerList().isAllowCommandsForAllPlayers()
                            || Minecraft.getInstance().getSingleplayerServer().getWorldData().isAllowCommands(),
                        this
                    );
                }
            };
        }

        /**
         * Get a permission predicate which passes when commands are disabled on the currently running integrated server.
         *
         * <p>This predicate will always pass if there is no integrated server running, i.e. when connected to a multiplayer server.</p>
         *
         * @param <C> sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsDisallowed() {
            return commandsDisallowed(true);
        }

        /**
         * Get a permission predicate which passes when commands are disabled on the currently running integrated server.
         *
         * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
         * fall back to the provided boolean argument.</p>
         *
         * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
         * @param <C>                sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsDisallowed(final boolean allowOnMultiplayer) {
            return commandsDisallowed(PredicatePermission.of(sender -> allowOnMultiplayer));
        }

        /**
         * Get a permission predicate which passes when commands are disabled on the currently running integrated server.
         *
         * <p>When there is no integrated server running, i.e. when connected to a multiplayer server, the predicate will
         * fall back to the provided predicate argument.</p>
         *
         * @param allowOnMultiplayer whether the predicate should pass on multiplayer servers
         * @param <C>                sender type
         * @return a predicate permission
         */
        public static <C> @NonNull PredicatePermission<C> commandsDisallowed(final @NonNull PredicatePermission<C> allowOnMultiplayer) {
            return new PredicatePermission<>() {
                private static final CloudKey<Void> KEY = CloudKey.of("commands-disallowed");

                @Override
                public @NonNull CloudKey<Void> key() {
                    return KEY;
                }

                @Override
                public @NonNull PermissionResult testPermission(final @NonNull C sender) {
                    if (!Minecraft.getInstance().hasSingleplayerServer()) {
                        return allowOnMultiplayer.testPermission(sender);
                    }
                    return PermissionResult.of(
                        !Minecraft.getInstance().getSingleplayerServer().getPlayerList().isAllowCommandsForAllPlayers()
                            && !Minecraft.getInstance().getSingleplayerServer().getWorldData().isAllowCommands(),
                        this
                    );
                }
            };
        }
    }
}
