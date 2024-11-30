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
package org.incendo.cloud.minecraft.modded.internal;

import java.util.Objects;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.server.MinecraftServer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@API(status = API.Status.INTERNAL)
@DefaultQualifier(NonNull.class)
public final class AdventureSupport {
    private static final AdventureSupport INSTANCE;

    static {
        INSTANCE = new AdventureSupport();
        INSTANCE.setupConverter();
    }

    private @Nullable MinecraftAudiences client;
    private @Nullable MinecraftAudiences server;

    private AdventureSupport() {
    }

    @SuppressWarnings("EmptyCatch")
    private void setupConverter() {
        try {
            ComponentMessageThrowableConverter.setup(this);
        } catch (final LinkageError ignored) {
        }
    }

    /**
     * Set up the client audience.
     */
    public void setupClient() {
        this.client = MinecraftClientAudiences.of();
    }

    /**
     * Set up the server audience.
     *
     * @param server server
     */
    public void setupServer(final MinecraftServer server) {
        this.server = MinecraftServerAudiences.of(server);
    }

    /**
     * Shutdown the server audience.
     *
     * @param server server
     */
    @SuppressWarnings("unused")
    public void removeServer(final MinecraftServer server) {
        this.server = null;
    }

    /**
     * Get the MinecraftAudiences instance.
     *
     * @return audiences
     */
    public MinecraftAudiences audiences() {
        final @Nullable MinecraftAudiences server = this.server;
        if (server == null) {
            return Objects.requireNonNull(this.client, "No audiences present");
        } else {
            return this.server;
        }
    }

    /**
     * Returns the AdventureSupport instance.
     *
     * @return the instance
     */
    public static AdventureSupport get() {
        return INSTANCE;
    }
}
