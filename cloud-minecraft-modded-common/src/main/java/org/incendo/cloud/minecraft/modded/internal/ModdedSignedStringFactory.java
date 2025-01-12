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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.minecraft.signed.SignedString;

@API(status = API.Status.INTERNAL)
public final class ModdedSignedStringFactory implements ModdedSignedStringMapper.SignedStringFactory {

    /**
     * Creates a new {@link ModdedSignedStringFactory}.
     */
    public ModdedSignedStringFactory() {
        // Trigger service load failure when this isn't present
        Objects.requireNonNull(MinecraftAudiences.class.getName());
    }

    @Override
    public SignedString create(final String str, final PlayerChatMessage signedMessage) {
        return new SignedStringImpl(str, signedMessage);
    }

    private record SignedStringImpl(String string, PlayerChatMessage rawSignedMessage) implements SignedString {

        @Override
        public @Nullable SignedMessage signedMessage() {
            return cast(this.rawSignedMessage);
        }

        @SuppressWarnings("DataFlowIssue")
        private static SignedMessage cast(final PlayerChatMessage message) {
            return (SignedMessage) ((Object) message);
        }

        @Override
        public void sendMessage(final Audience audience, final ChatType.Bound chatType, final Component unsigned) {
            final net.minecraft.network.chat.Component nativeComponent =
                AdventureSupport.get().audiences().asNative(unsigned);
            final PlayerChatMessage playerChatMessage = this.rawSignedMessage.withUnsignedContent(nativeComponent);
            audience.sendMessage(cast(playerChatMessage), chatType);
        }
    }
}
