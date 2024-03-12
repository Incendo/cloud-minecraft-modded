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
package org.incendo.cloud.sponge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
final class SpongeDefaultExceptionHandlers {
    private static final Component NULL = text("null");
    private static final Component MESSAGE_INTERNAL_ERROR =
        text("An internal error occurred while attempting to perform this command.", RED);
    private static final Component MESSAGE_NO_PERMS =
        text("I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.", RED);
    private static final Component MESSAGE_UNKNOWN_COMMAND = text("Unknown command. Type \"/help\" for help.");

    private SpongeDefaultExceptionHandlers() {
    }

    static <C> void register(final SpongeCommandManager<C> mgr) {
        mgr.exceptionController().registerHandler(InvalidSyntaxException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text().append(
                text("Invalid Command Syntax. Correct command syntax is: ", RED),
                text("/" + ctx.exception().correctSyntax(), GRAY)
            ).build());
        });
        mgr.exceptionController().registerHandler(InvalidCommandSenderException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text(ctx.exception().getMessage(), RED));
        });
        mgr.exceptionController().registerHandler(NoPermissionException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_NO_PERMS);
        });
        mgr.exceptionController().registerHandler(NoSuchCommandException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_UNKNOWN_COMMAND);
        });
        mgr.exceptionController().registerHandler(ArgumentParseException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(text().append(
                text("Invalid Command Argument: ", RED),
                getMessage(ctx.exception().getCause()).colorIfAbsent(GRAY)
            ).build());
        });
        mgr.exceptionController().registerHandler(CommandExecutionException.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_INTERNAL_ERROR);
            mgr.owningPluginContainer().logger()
                .error("Exception executing command handler", ctx.exception().getCause());
        });
        mgr.exceptionController().registerHandler(Throwable.class, ctx -> {
            final Audience audience = ctx.context().get(SpongeCommandContextKeys.COMMAND_CAUSE).audience();
            audience.sendMessage(MESSAGE_INTERNAL_ERROR);
            mgr.owningPluginContainer().logger()
                .error("An unhandled exception was thrown during command execution", ctx.exception());
        });
    }

    private static Component getMessage(final Throwable throwable) {
        final @Nullable Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }
}
